package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.client.util.ClientUtils;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerTankRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.StyleNOXRecipe;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.util.IDisplaySyncState;
import mctmods.immersivetechnology.core.util.ItemOutputs;
import mctmods.immersivetechnology.core.util.SyncEnergyStorage;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.client.MachineSound;
import com.immersiveconvergence.api.integration.DisplayLines;
import com.immersiveconvergence.api.multiblock.IDataReloadAware;
import com.immersiveconvergence.api.multiblock.IFluidOutputPump;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.util.ConstrainedItemHandler;
import com.immersiveconvergence.api.util.MarkableFluidTank;
import com.immersiveconvergence.api.util.MultiBlockInventoryUtils;
import com.immersiveconvergence.api.util.MultiTankFluidHandler;
import com.immersiveconvergence.api.util.RecipeCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class StyleNOXLogic implements IMultiblockLogic<StyleNOXLogic.State>, IServerTickableComponent<StyleNOXLogic.State>, IClientTickableComponent<StyleNOXLogic.State>, IFluidOutputPump<StyleNOXLogic.State> {
    public static final int FUEL_SLOTS = 8;
    public static final int SLOT_SPENT = FUEL_SLOTS;
    public static final int MAX_ROD_LEVEL = 4;
    public static final int CHANNELS_PER_LEVEL = 2;
    public static final int FULL_CHANNELS = MAX_ROD_LEVEL * CHANNELS_PER_LEVEL;
    public static final float CONTROL_ROD_LIFT = 0.5f;
    public static final float CONTROL_ROD_TRAVEL = 0.875f;
    public static final float FUEL_ROD_TRAVEL = 0.4f;
    public static final int[][] FUEL_SLOT_CELLS = {{1, 1}, {0, 1}, {-1, 1}, {1, 0}, {-1, 0}, {1, -1}, {0, -1}, {-1, -1}};
    public static BlockPos REDSTONE_POI;
    public static List<BlockPos> WATER_INPUT_POS_LIST;
    private static List<CapabilityPosition> WATER_INPUT_POIS;
    private static List<CapabilityPosition> STEAM_OUTPUT_POIS;
    private static List<CapabilityPosition> CO2_INPUT_POIS;
    private static List<CapabilityPosition> ENERGY_INPUT_POIS;
    private static List<BlockPos> STEAM_OUTPUT_POS_LIST;
    private static List<RelativeBlockFace> STEAM_OUTPUT_FACINGS;
    private static BlockPos SOUND_POI;

    static { ITShapes.readPois("stylenox", StyleNOXLogic::loadPois); }

    public static int energyCapacity() { return ServerConfig.stylenoxEnergyCapacity; }

    public enum ReactorState { IDLE, STARTING, CRITICAL }

    @Override public List<BlockPos> getOutputPositions() { return STEAM_OUTPUT_POS_LIST; }

    @Override public List<RelativeBlockFace> getOutputFacings() { return STEAM_OUTPUT_FACINGS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(STEAM_OUTPUT_FACINGS.getFirst()); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.steam()); }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        state.energy.updateAverage();
        int prevEnergy = state.energy.getEnergyStored();
        boolean prevTanksDirty = state.tanksDirty;
        boolean prevInventoryDirty = state.inventoryDirty;
        boolean wasActive = state.active;
        ReactorState wasReactorState = state.reactorState;
        Level level = ctx.getLevel().getRawLevel();
        boolean rsEnabled = state.rsState.isEnabled(ctx);
        state.active = false;
        switch (state.reactorState) {
            case IDLE -> tickIdle(state, level, rsEnabled);
            case STARTING -> tickStarting(state, level, rsEnabled);
            case CRITICAL -> tickCritical(state, level, rsEnabled);
        }
        pumpOutputs(ctx);
        int newComparatorValue = state.tanks.steam().getCapacity() > 0 ? (15 * state.tanks.steam().getFluidAmount()) / state.tanks.steam().getCapacity() : 0;
        boolean comparatorChanged = newComparatorValue != state.lastComparatorValue;
        int newRodMask = state.computeRodMask();
        boolean newFuelDown = state.hasLoad();
        boolean rodsChanged = newRodMask != state.rodMask || newFuelDown != state.fuelDown;
        state.rodMask = newRodMask;
        state.fuelDown = newFuelDown;
        if (comparatorChanged) {
            ctx.setComparatorOutputFor(REDSTONE_POI, newComparatorValue);
            state.lastComparatorValue = newComparatorValue;
        }
        if (wasActive != state.active || wasReactorState != state.reactorState || prevEnergy != state.energy.getEnergyStored() || (!prevTanksDirty && state.tanksDirty) || (!prevInventoryDirty && state.inventoryDirty) || comparatorChanged || rodsChanged) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    private void tickIdle(State state, Level level, boolean rsEnabled) {
        if (!rsEnabled || state.rodLevel == 0) { return; }
        if (!state.hasLoad()) { tryLoad(state, level); }
        if (state.hasLoad() && state.recipeForLoad(level) != null) {
            state.reactorState = ReactorState.STARTING;
            state.startupProgress = 0;
            state.controlRodDown = false;
        }
    }

    private void tickStarting(State state, Level level, boolean rsEnabled) {
        if (!rsEnabled || state.rodLevel == 0) {
            state.shutdown();
            return;
        }
        RecipeHolder<StyleNOXRecipe> recipeHolder = state.recipeForLoad(level);
        if (recipeHolder == null) {
            state.shutdown();
            return;
        }
        int perTick = Math.max(1, recipeHolder.value().getTotalProcessEnergy() / ServerConfig.stylenoxStartupTime);
        if (state.energy.extractEnergy(perTick, true) == perTick) {
            state.energy.extractEnergy(perTick, false);
            state.startupProgress++;
            state.active = true;
            if (state.startupProgress >= ServerConfig.stylenoxStartupTime) {
                state.reactorState = ReactorState.CRITICAL;
                state.startupProgress = 0;
            }
        }
    }

    private void tickCritical(State state, Level level, boolean rsEnabled) {
        if (!rsEnabled || state.rodLevel == 0 || !state.hasLoad()) {
            state.shutdown();
            return;
        }
        RecipeHolder<StyleNOXRecipe> recipeHolder = state.recipeForLoad(level);
        if (recipeHolder == null) {
            state.shutdown();
            return;
        }
        StyleNOXRecipe recipe = recipeHolder.value();
        int burnTotal = recipe.getTotalProcessTime() * FULL_CHANNELS;
        if (state.burnProgress >= burnTotal) {
            if (!ItemOutputs.merge(state.inventory, SLOT_SPENT, recipe.output.get().copy())) { return; }
            state.loadedFuel = ItemStack.EMPTY;
            state.burnProgress = 0;
            if (!tryLoad(state, level)) {
                state.shutdown();
                return;
            }
            recipeHolder = state.recipeForLoad(level);
            if (recipeHolder == null) {
                state.shutdown();
                return;
            }
            recipe = recipeHolder.value();
        }
        int steamPerTick = recipe.steamOutput().getAmount();
        if (recipe.waterAmount() > 0) {
            BoilerTankRecipe waterRecipe = state.boilerRecipeGetter.apply(level, state.tanks.water().getFluid());
            if (waterRecipe == null) {
                state.shutdown();
                return;
            }
            steamPerTick = recipe.waterAmount() * waterRecipe.output().getAmount() / waterRecipe.getInputAmount();
        }
        int channels = state.rodLevel * CHANNELS_PER_LEVEL;
        int waterAcc = state.waterAcc + recipe.waterAmount() * channels;
        int co2Acc = state.co2Acc + recipe.co2Amount() * channels;
        int steamAcc = state.steamAcc + steamPerTick * channels;
        int water = waterAcc / FULL_CHANNELS;
        int co2 = co2Acc / FULL_CHANNELS;
        int steam = steamAcc / FULL_CHANNELS;
        boolean waterOk = water == 0 || state.tanks.water().drain(water, FluidAction.SIMULATE).getAmount() == water;
        boolean co2Ok = co2 == 0 || state.tanks.co2().drain(co2, FluidAction.SIMULATE).getAmount() == co2;
        if (!waterOk || !co2Ok) {
            state.shutdown();
            return;
        }
        if (steam > 0) {
            FluidStack steamStack = new FluidStack(recipe.steamOutput().getFluid(), steam);
            if (state.tanks.steam().fill(steamStack, FluidAction.SIMULATE) != steam) { return; }
            state.tanks.steam().fill(steamStack, FluidAction.EXECUTE);
        }
        if (water > 0) { state.tanks.water().drain(water, FluidAction.EXECUTE); }
        if (co2 > 0) { state.tanks.co2().drain(co2, FluidAction.EXECUTE); }
        state.waterAcc = waterAcc % FULL_CHANNELS;
        state.co2Acc = co2Acc % FULL_CHANNELS;
        state.steamAcc = steamAcc % FULL_CHANNELS;
        state.burnProgress += channels;
        state.active = true;
    }

    private boolean tryLoad(State state, Level level) {
        RecipeHolder<StyleNOXRecipe> recipeHolder = null;
        int[] loaded = new int[FUEL_SLOTS];
        int count = 0;
        for (int slot = 0; slot < FUEL_SLOTS; slot++) {
            ItemStack fuel = state.inventory.getStackInSlot(slot);
            if (fuel.isEmpty()) { continue; }
            if (recipeHolder == null) { recipeHolder = state.recipeGetter.apply(level, fuel); }
            if (recipeHolder == null || !recipeHolder.value().matches(fuel)) { continue; }
            loaded[count++] = slot;
            if (count == recipeHolder.value().input.getCount()) { break; }
        }
        if (recipeHolder == null || count < recipeHolder.value().input.getCount()) { return false; }
        state.loadedFuel = state.inventory.getStackInSlot(loaded[0]).copyWithCount(count);
        for (int i = 0; i < count; i++) {
            ItemStack fuel = state.inventory.getStackInSlot(loaded[i]);
            fuel.shrink(1);
            state.inventory.setStackInSlot(loaded[i], fuel);
        }
        state.burnProgress = 0;
        return true;
    }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        state.animation_rodPrev = state.animation_rodPosition;
        state.animation_rodPosition = approach(state.animation_rodPosition, state.controlRodDown ? 0f : 1f, 0.0625f);
        state.animation_fuelPrev = state.animation_fuelPosition;
        state.animation_fuelPosition = approach(state.animation_fuelPosition, state.fuelDown ? 1f : 0f, 0.00625f);
        Vec3 soundPos = ctx.getLevel().toAbsolute(Vec3.atCenterOf(SOUND_POI));
        if (state.reactorState == ReactorState.CRITICAL && ClientUtils.attenuated(soundPos, 32, 1) > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
            state.isSoundPlaying = MachineSound.startSound(() -> state.reactorState == ReactorState.CRITICAL, ctx.isValid(), soundPos, Sounds.stylenox, () -> ClientUtils.attenuated(soundPos, 32, 1), () -> 1f);
        }
    }

    private static float approach(float current, float target, float down) { return current < target ? Math.min(target, current + 0.00625f) : Math.max(target, current - down); }

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.register(Capabilities.EnergyStorage.BLOCK, (state, position) -> matchesAny(ENERGY_INPUT_POIS, position) ? state.energy : null);
        register.register(Capabilities.FluidHandler.BLOCK, (state, position) -> {
            if (matchesAny(WATER_INPUT_POIS, position)) { return state.waterInputCap; }
            if (matchesAny(CO2_INPUT_POIS, position)) { return state.co2InputCap; }
            return matchesAny(STEAM_OUTPUT_POIS, position) ? state.steamOutputCap : null;
        });
    }

    private static boolean matchesAny(List<CapabilityPosition> pois, CapabilityPosition position) { return pois.stream().anyMatch(poi -> poi.posInMultiblock().equals(position.posInMultiblock()) && (position.side() == null || position.side() == poi.side())); }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) {
        MultiBlockInventoryUtils.dropItems(state.inventory, drop);
        if (!state.loadedFuel.isEmpty()) { drop.accept(state.loadedFuel); }
    }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get("stylenox").getter; }

    public static class State implements IDisplaySyncState, IDataReloadAware {
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final StyleNOXTank tanks;
        public final ConstrainedItemHandler inventory;
        public final IFluidHandler waterInputCap;
        public final IFluidHandler steamOutputCap;
        public final IFluidHandler co2InputCap;
        public final AveragingEnergyStorage energy;
        private final BiFunction<Level, ItemStack, RecipeHolder<StyleNOXRecipe>> recipeGetter = RecipeCache.cached(StyleNOXRecipe::findRecipe);
        private final BiFunction<Level, FluidStack, BoilerTankRecipe> boilerRecipeGetter = RecipeCache.cached(BoilerTankRecipe::findRecipe);
        public ReactorState reactorState = ReactorState.IDLE;
        public int rodLevel = 0;
        public int startupProgress = 0;
        public int burnProgress = 0;
        public int burnTotal = 0;
        public int rodMask = 0;
        public boolean fuelDown = false;
        public boolean controlRodDown = false;
        public ItemStack loadedFuel = ItemStack.EMPTY;
        private int waterAcc = 0;
        private int co2Acc = 0;
        private int steamAcc = 0;
        public boolean active = false;
        public boolean tanksDirty = false;
        public boolean inventoryDirty = false;
        public int lastComparatorValue = -1;
        private RecipeHolder<StyleNOXRecipe> lastRecipeCache = null;
        public float animation_rodPosition = 0f;
        public float animation_rodPrev = 0f;
        public float animation_fuelPosition = 0f;
        public float animation_fuelPrev = 0f;
        private BooleanSupplier isSoundPlaying = () -> false;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> {
                markDirty.run();
                sync.run();
                this.tanksDirty = true;
                this.inventoryDirty = true;
            };
            this.tanks = new StyleNOXTank(v -> onChanged.run());
            this.inventory = new FuelChannelHandler(onChanged);
            this.waterInputCap = new MultiTankFluidHandler(tanks.water(), false, true, onChanged);
            this.steamOutputCap = new MultiTankFluidHandler(tanks.steam(), true, false, onChanged);
            this.co2InputCap = new MultiTankFluidHandler(tanks.co2(), false, true, onChanged);
            this.energy = new SyncEnergyStorage(energyCapacity(), onChanged);
        }

        @Override public void onDataReload(IInitialMultiblockContext<?> context) { lastComparatorValue = -1; }

        private RecipeHolder<StyleNOXRecipe> recipeForLoad(Level level) {
            if (loadedFuel.isEmpty()) { return null; }
            if (lastRecipeCache == null || !lastRecipeCache.value().matches(loadedFuel)) { lastRecipeCache = recipeGetter.apply(level, loadedFuel); }
            if (lastRecipeCache != null) { burnTotal = lastRecipeCache.value().getTotalProcessTime() * FULL_CHANNELS; }
            return lastRecipeCache;
        }

        public boolean hasLoad() { return !loadedFuel.isEmpty(); }

        public void shutdown() {
            reactorState = ReactorState.IDLE;
            startupProgress = 0;
            controlRodDown = true;
        }

        public int computeRodMask() { return hasLoad() ? (1 << FUEL_SLOTS) - 1 : fuelMask(); }

        private int fuelMask() {
            int mask = 0;
            for (int slot = 0; slot < FUEL_SLOTS; slot++) { if (!inventory.getStackInSlot(slot).isEmpty()) { mask |= 1 << slot; } }
            return mask;
        }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            rsState.writeSaveNBT(nbt, provider);
            nbt.putInt("reactorState", reactorState.ordinal());
            nbt.putInt("rodLevel", rodLevel);
            nbt.putInt("startupProgress", startupProgress);
            nbt.putInt("burnProgress", burnProgress);
            nbt.putInt("waterAcc", waterAcc);
            nbt.putInt("co2Acc", co2Acc);
            nbt.putInt("steamAcc", steamAcc);
            nbt.putBoolean("active", active);
            nbt.putBoolean("controlRodDown", controlRodDown);
            if (!loadedFuel.isEmpty()) { nbt.put("loadedFuel", loadedFuel.save(provider)); }
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.put("inventory", inventory.serializeNBT(provider));
            nbt.putInt("energy", energy.getEnergyStored());
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            rsState.readSaveNBT(nbt, provider);
            reactorState = ReactorState.values()[nbt.getInt("reactorState")];
            rodLevel = nbt.getInt("rodLevel");
            startupProgress = nbt.getInt("startupProgress");
            burnProgress = nbt.getInt("burnProgress");
            waterAcc = nbt.getInt("waterAcc");
            co2Acc = nbt.getInt("co2Acc");
            steamAcc = nbt.getInt("steamAcc");
            active = nbt.getBoolean("active");
            controlRodDown = nbt.getBoolean("controlRodDown");
            loadedFuel = nbt.contains("loadedFuel", Tag.TAG_COMPOUND) ? ItemStack.parseOptional(provider, nbt.getCompound("loadedFuel")) : ItemStack.EMPTY;
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            readInventoryNBT(nbt.getCompound("inventory"), provider);
            energy.setStoredEnergy(nbt.getInt("energy"));
            tanksDirty = false;
            inventoryDirty = false;
        }

        private void readInventoryNBT(CompoundTag inventoryTag, HolderLookup.Provider provider) {
            if (inventoryTag.getInt("Size") == 2) {
                for (Tag item : inventoryTag.getList("Items", Tag.TAG_COMPOUND)) {
                    CompoundTag itemTag = (CompoundTag) item;
                    if (itemTag.getInt("Slot") == 1) { itemTag.putInt("Slot", SLOT_SPENT); }
                }
            }
            inventoryTag.putInt("Size", FUEL_SLOTS + 1);
            inventory.deserializeNBT(provider, inventoryTag);
            for (int slot = 0; slot < FUEL_SLOTS; slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                for (int target = 0; stack.getCount() > 1 && target < FUEL_SLOTS; target++) {
                    if (inventory.getStackInSlot(target).isEmpty()) { inventory.setStackInSlot(target, stack.split(1)); }
                }
                inventory.getRawHandler().setStackInSlot(slot, stack);
            }
        }

        @Override public boolean isActive() { return active; }

        @Override public AveragingEnergyStorage getEnergy() { return energy; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.water(), tanks.steam(), tanks.co2()}; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("active", active);
            nbt.putInt("reactorState", reactorState.ordinal());
            nbt.putInt("rodLevel", rodLevel);
            nbt.putInt("startupProgress", startupProgress);
            nbt.putInt("burnProgress", burnProgress);
            nbt.putInt("burnTotal", burnTotal);
            nbt.putInt("rodMask", rodMask);
            nbt.putBoolean("fuelDown", fuelDown);
            nbt.putBoolean("controlRodDown", controlRodDown);
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.putInt("energy", energy.getEnergyStored());
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            active = nbt.getBoolean("active");
            reactorState = ReactorState.values()[nbt.getInt("reactorState")];
            rodLevel = nbt.getInt("rodLevel");
            startupProgress = nbt.getInt("startupProgress");
            burnProgress = nbt.getInt("burnProgress");
            burnTotal = nbt.getInt("burnTotal");
            rodMask = nbt.getInt("rodMask");
            fuelDown = nbt.getBoolean("fuelDown");
            controlRodDown = nbt.getBoolean("controlRodDown");
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            energy.setStoredEnergy(nbt.getInt("energy"));
            tanksDirty = false;
        }

        @Override public void addDisplayLines(Level level, DisplayLines lines) {
            lines.text(reactorState.name() + " (" + (rodLevel * 25) + "%)");
            if (burnTotal > 0 && hasLoad()) { lines.percent((int) (100L * burnProgress / burnTotal)); }
            if (!hasLoad() && fuelMask() == 0) { lines.fuelEmpty(); }
        }
    }

    public static class FuelChannelHandler extends ConstrainedItemHandler {
        public FuelChannelHandler(Runnable onChanged) { super(Stream.concat(Collections.nCopies(FUEL_SLOTS, ConstrainedItemHandler.IOConstraint.INPUT).stream(), Stream.of(ConstrainedItemHandler.IOConstraint.OUTPUT)).toList(), onChanged); }

        @Override public int getSlotLimit(int slot) { return slot < FUEL_SLOTS ? 1 : super.getSlotLimit(slot); }

        @Override @Nonnull public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (slot >= FUEL_SLOTS || stack.isEmpty()) { return super.insertItem(slot, stack, simulate); }
            if (!getStackInSlot(slot).isEmpty() || !isItemValid(slot, stack)) { return stack; }
            if (!super.insertItem(slot, stack.copyWithCount(1), simulate).isEmpty()) { return stack; }
            return stack.copyWithCount(stack.getCount() - 1);
        }
    }

    public record StyleNOXTank(MarkableFluidTank water, MarkableFluidTank steam, MarkableFluidTank co2) {
        public StyleNOXTank(Consumer<Void> markDirty) { this(new MarkableFluidTank(ServerConfig.stylenoxWaterTankCapacity, markDirty), new MarkableFluidTank(ServerConfig.stylenoxSteamTankCapacity, markDirty), new MarkableFluidTank(ServerConfig.stylenoxCo2TankCapacity, markDirty)); }

        public static StyleNOXTank makeClient() { return new StyleNOXTank(v -> {}); }

        public CompoundTag toNBT(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.put("water", this.water.writeToNBT(provider, new CompoundTag()));
            tag.put("steam", this.steam.writeToNBT(provider, new CompoundTag()));
            tag.put("co2", this.co2.writeToNBT(provider, new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag, HolderLookup.Provider provider) {
            this.water.readFromNBT(provider, tag.getCompound("water"));
            this.steam.readFromNBT(provider, tag.getCompound("steam"));
            this.co2.readFromNBT(provider, tag.getCompound("co2"));
        }
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        REDSTONE_POI = MultiblockPOIHelper.getPosList(pois, "redstone0").getFirst();
        WATER_INPUT_POS_LIST = MultiblockPOIHelper.getPosList(pois, "fluid_input0");
        WATER_INPUT_POIS = MultiblockPOIHelper.getCapabilityPositions(pois, "fluid_input0");
        STEAM_OUTPUT_POIS = MultiblockPOIHelper.getCapabilityPositions(pois, "fluid_output0");
        CO2_INPUT_POIS = MultiblockPOIHelper.getCapabilityPositions(pois, "fluid_input1");
        ENERGY_INPUT_POIS = MultiblockPOIHelper.getCapabilityPositions(pois, "energy_input0");
        STEAM_OUTPUT_POS_LIST = STEAM_OUTPUT_POIS.stream().map(CapabilityPosition::posInMultiblock).collect(ImmutableList.toImmutableList());
        STEAM_OUTPUT_FACINGS = STEAM_OUTPUT_POIS.stream().map(CapabilityPosition::side).collect(ImmutableList.toImmutableList());
        SOUND_POI = MultiblockPOIHelper.getPosList(pois, "sound0").getFirst();
    }
}
