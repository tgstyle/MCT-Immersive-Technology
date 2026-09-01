package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.common.fluids.helper.ArrayFluidHandler;
import com.immersiveconvergence.api.util.MarkableFluidTank;
import com.immersiveconvergence.api.multiblock.IDisplayContext;
import com.immersiveconvergence.api.multiblock.IFluidOutputPump;
import com.immersiveconvergence.api.util.MultiBlockInventoryUtils;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.util.ConstrainedItemHandler;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.StyleNOXRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.StyleNOXShape;
import mctmods.immersivetechnology.core.ServerConfig;
import com.immersiveconvergence.api.client.MachineSound;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.util.CachedRecipe;
import mctmods.immersivetechnology.core.util.Utils;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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
import net.neoforged.neoforge.items.IItemHandler;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class StyleNOXLogic implements IMultiblockLogic<StyleNOXLogic.State>, IServerTickableComponent<StyleNOXLogic.State>, IClientTickableComponent<StyleNOXLogic.State>, IFluidOutputPump<StyleNOXLogic.State> {
    public static final int SLOT_FUEL = 0;
    public static final int SLOT_SPENT = 1;
    public static final int MAX_ROD_LEVEL = 4;
    public static final int CHANNELS_PER_LEVEL = 2;
    public static final int FULL_CHANNELS = MAX_ROD_LEVEL * CHANNELS_PER_LEVEL;
    public static final float ROD_TRAVEL = 2.0f;

    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(StyleNOXShape.DATA.pointsOfInterest);

    public static final BlockPos REDSTONE_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "redstone0").getFirst();
    public static final List<BlockPos> WATER_INPUT_POS_LIST = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_input0");
    private static final List<CapabilityPosition> WATER_INPUT_POIS = MultiblockPOIHelper.getCapabilityPositions(RAW_POIS, "fluid_input0");
    private static final List<CapabilityPosition> STEAM_OUTPUT_POIS = MultiblockPOIHelper.getCapabilityPositions(RAW_POIS, "fluid_output0");
    private static final List<CapabilityPosition> CO2_INPUT_POIS = MultiblockPOIHelper.getCapabilityPositions(RAW_POIS, "fluid_input1");
    private static final List<BlockPos> ENERGY_INPUT_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "energy_input0");
    private static final RelativeBlockFace ENERGY_INPUT_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "energy_input0");
    private static final List<CapabilityPosition> ITEM_IO_POIS = MultiblockPOIHelper.getCapabilityPositions(RAW_POIS, "item_input0");
    public static final MultiblockFace ITEM_OUTPUT_POI = new MultiblockFace(
            MultiblockPOIHelper.getFacing(RAW_POIS, "item_output0"),
            MultiblockPOIHelper.getPosList(RAW_POIS, "item_output0").getFirst());
    private static final List<BlockPos> STEAM_OUTPUT_POS_LIST = STEAM_OUTPUT_POIS.stream().map(CapabilityPosition::posInMultiblock).collect(ImmutableList.toImmutableList());
    private static final List<RelativeBlockFace> STEAM_OUTPUT_FACINGS = STEAM_OUTPUT_POIS.stream().map(CapabilityPosition::side).collect(ImmutableList.toImmutableList());
    private static final BlockPos SOUND_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "sound0").getFirst();

    public static int waterTankCapacity() { return ServerConfig.stylenoxWaterTankCapacity; }
    public static int steamTankCapacity() { return ServerConfig.stylenoxSteamTankCapacity; }
    public static int co2TankCapacity() { return ServerConfig.stylenoxCo2TankCapacity; }
    public static int energyCapacity() { return ServerConfig.stylenoxEnergyCapacity; }
    public static int startupTime() { return ServerConfig.stylenoxStartupTime; }

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
        ItemStack spent = state.inventory.getStackInSlot(SLOT_SPENT);
        if (!spent.isEmpty()) {
            ItemStack remaining = Utils.insertStackIntoInventory(state.outputRef, spent, false);
            if (remaining.getCount() != spent.getCount()) { state.inventory.setStackInSlot(SLOT_SPENT, remaining); }
        }
        int newComparatorValue = state.tanks.steam().getCapacity() > 0 ? (15 * state.tanks.steam().getFluidAmount()) / state.tanks.steam().getCapacity() : 0;
        boolean comparatorChanged = newComparatorValue != state.lastComparatorValue;
        if (comparatorChanged) {
            ctx.setComparatorOutputFor(REDSTONE_POI, newComparatorValue);
            state.lastComparatorValue = newComparatorValue;
        }
        boolean changed = wasActive != state.active
                || wasReactorState != state.reactorState
                || prevEnergy != state.energy.getEnergyStored()
                || (!prevTanksDirty && state.tanksDirty)
                || (!prevInventoryDirty && state.inventoryDirty)
                || comparatorChanged;
        if (changed) {
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
        }
    }

    private void tickStarting(State state, Level level, boolean rsEnabled) {
        if (!rsEnabled || state.rodLevel == 0) {
            state.reactorState = ReactorState.IDLE;
            state.startupProgress = 0;
            return;
        }
        RecipeHolder<StyleNOXRecipe> recipeHolder = state.recipeForLoad(level);
        if (recipeHolder == null) {
            state.reactorState = ReactorState.IDLE;
            state.startupProgress = 0;
            return;
        }
        int perTick = Math.max(1, recipeHolder.value().getTotalProcessEnergy() / startupTime());
        if (state.energy.extractEnergy(perTick, true) == perTick) {
            state.energy.extractEnergy(perTick, false);
            state.startupProgress++;
            state.active = true;
            if (state.startupProgress >= startupTime()) {
                state.reactorState = ReactorState.CRITICAL;
                state.startupProgress = 0;
            }
        }
    }

    private void tickCritical(State state, Level level, boolean rsEnabled) {
        if (!rsEnabled || state.rodLevel == 0 || !state.hasLoad()) {
            state.reactorState = ReactorState.IDLE;
            return;
        }
        RecipeHolder<StyleNOXRecipe> recipeHolder = state.recipeForLoad(level);
        if (recipeHolder == null) {
            state.reactorState = ReactorState.IDLE;
            return;
        }
        StyleNOXRecipe recipe = recipeHolder.value();
        int burnTotal = recipe.getTotalProcessTime() * FULL_CHANNELS;
        if (state.burnProgress >= burnTotal) {
            ItemStack spentOut = recipe.output.get().copy();
            ItemStack current = state.inventory.getStackInSlot(SLOT_SPENT);
            boolean fits = current.isEmpty() || (ItemStack.isSameItemSameComponents(current, spentOut) && current.getCount() + spentOut.getCount() <= current.getMaxStackSize());
            if (!fits) { return; }
            if (current.isEmpty()) { state.inventory.setStackInSlot(SLOT_SPENT, spentOut); }
            else {
                current.grow(spentOut.getCount());
                state.inventory.setStackInSlot(SLOT_SPENT, current);
            }
            state.loadedFuel = ItemStack.EMPTY;
            state.burnProgress = 0;
            if (!tryLoad(state, level) || state.recipeForLoad(level) == null) {
                state.reactorState = ReactorState.IDLE;
                return;
            }
            recipeHolder = state.recipeForLoad(level);
            recipe = recipeHolder.value();
        }
        int channels = state.rodLevel * CHANNELS_PER_LEVEL;
        int waterAcc = state.waterAcc + recipe.waterAmount() * channels;
        int co2Acc = state.co2Acc + recipe.co2Amount() * channels;
        int steamAcc = state.steamAcc + recipe.steamOutput().getAmount() * channels;
        int water = waterAcc / FULL_CHANNELS;
        int co2 = co2Acc / FULL_CHANNELS;
        int steam = steamAcc / FULL_CHANNELS;
        boolean waterOk = water == 0 || state.tanks.water().drain(water, FluidAction.SIMULATE).getAmount() == water;
        boolean co2Ok = co2 == 0 || state.tanks.co2().drain(co2, FluidAction.SIMULATE).getAmount() == co2;
        if (!waterOk || !co2Ok) {
            state.reactorState = ReactorState.IDLE;
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
        ItemStack fuel = state.inventory.getStackInSlot(SLOT_FUEL);
        if (fuel.isEmpty()) { return false; }
        RecipeHolder<StyleNOXRecipe> recipeHolder = state.recipeGetter.apply(level, fuel);
        if (recipeHolder == null) { return false; }
        int needed = recipeHolder.value().input.getCount();
        if (fuel.getCount() < needed) { return false; }
        state.loadedFuel = fuel.copyWithCount(needed);
        fuel.shrink(needed);
        state.inventory.setStackInSlot(SLOT_FUEL, fuel);
        state.burnProgress = 0;
        return true;
    }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        state.animation_rodPrev = state.animation_rodPosition;
        float target = state.reactorState == ReactorState.CRITICAL ? state.rodLevel / (float) MAX_ROD_LEVEL : 0f;
        if (state.animation_rodPosition < target) { state.animation_rodPosition = Math.min(target, state.animation_rodPosition + 0.00625f); }
        else if (state.animation_rodPosition > target) { state.animation_rodPosition = Math.max(target, state.animation_rodPosition - 0.0625f); }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) { return; }
        Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(SOUND_POI.getX() + 0.5, SOUND_POI.getY() + 0.5, SOUND_POI.getZ() + 0.5));
        float attenuation = (float) Math.max(player.distanceToSqr(soundPos) / 32, 1);
        float volume = 1 / attenuation;
        if (state.reactorState == ReactorState.CRITICAL && volume > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
            state.isSoundPlaying = MachineSound.startSound(
                    () -> state.reactorState == ReactorState.CRITICAL, ctx.isValid(), soundPos, Sounds.stylenox,
                    () -> {
                        LocalPlayer p = Minecraft.getInstance().player;
                        if (p == null) { return 0f; }
                        return 1 / (float) Math.max(p.distanceToSqr(soundPos) / 32, 1);
                    },
                    () -> 1f
            );
        }
    }

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.register(Capabilities.EnergyStorage.BLOCK, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (ENERGY_INPUT_POIS.contains(localPos) && (side == null || side == ENERGY_INPUT_FACING)) { return state.energy; }
            return null;
        });
        register.register(Capabilities.FluidHandler.BLOCK, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (matchesAny(WATER_INPUT_POIS, localPos, side)) { return state.waterInputCap; }
            if (matchesAny(CO2_INPUT_POIS, localPos, side)) { return state.co2InputCap; }
            if (matchesAny(STEAM_OUTPUT_POIS, localPos, side)) { return state.steamOutputCap; }
            return null;
        });
        register.register(Capabilities.ItemHandler.BLOCK, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (matchesAny(ITEM_IO_POIS, localPos, side)) { return state.inventory; }
            return null;
        });
    }

    private static boolean matchesAny(List<CapabilityPosition> pois, BlockPos pos, RelativeBlockFace side) {
        for (CapabilityPosition poi : pois) {
            if (poi.posInMultiblock().equals(pos) && (side == null || side == poi.side())) { return true; }
        }
        return false;
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) {
        MultiBlockInventoryUtils.dropItems(state.inventory, drop);
        if (!state.loadedFuel.isEmpty()) { drop.accept(state.loadedFuel); }
    }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return StyleNOXShape.GETTER; }

    public static class State implements IMultiblockState, IDisplayContext {
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final StyleNOXTank tanks;
        public final ConstrainedItemHandler inventory;
        public final IFluidHandler waterInputCap;
        public final IFluidHandler steamOutputCap;
        public final IFluidHandler co2InputCap;
        public final AveragingEnergyStorage energy;
        public final Supplier<IItemHandler> outputRef;
        private final BiFunction<Level, ItemStack, RecipeHolder<StyleNOXRecipe>> recipeGetter;
        public ReactorState reactorState = ReactorState.IDLE;
        public int rodLevel = 0;
        public int startupProgress = 0;
        public int burnProgress = 0;
        public int burnTotal = 0;
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
            this.tanks = new StyleNOXTank(v -> onChanged.run(), waterTankCapacity(), steamTankCapacity(), co2TankCapacity());
            this.inventory = new ConstrainedItemHandler(List.of(ConstrainedItemHandler.IOConstraint.INPUT, ConstrainedItemHandler.IOConstraint.OUTPUT), onChanged);
            this.waterInputCap = new ArrayFluidHandler(tanks.water(), false, true, onChanged);
            this.steamOutputCap = new ArrayFluidHandler(tanks.steam(), true, false, onChanged);
            this.co2InputCap = new ArrayFluidHandler(tanks.co2(), false, true, onChanged);
            this.energy = new SyncEnergyStorage(energyCapacity(), onChanged);
            this.outputRef = ctx.getCapabilityAt(Capabilities.ItemHandler.BLOCK, ITEM_OUTPUT_POI);
            this.recipeGetter = CachedRecipe.cached(StyleNOXRecipe::findRecipe);
        }

        private RecipeHolder<StyleNOXRecipe> recipeForLoad(Level level) {
            if (loadedFuel.isEmpty()) { return null; }
            if (lastRecipeCache != null && lastRecipeCache.value().matches(loadedFuel)) {
                burnTotal = lastRecipeCache.value().getTotalProcessTime() * FULL_CHANNELS;
                return lastRecipeCache;
            }
            lastRecipeCache = recipeGetter.apply(level, loadedFuel);
            if (lastRecipeCache != null) { burnTotal = lastRecipeCache.value().getTotalProcessTime() * FULL_CHANNELS; }
            return lastRecipeCache;
        }

        public boolean hasLoad() { return !loadedFuel.isEmpty(); }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putInt("reactorState", reactorState.ordinal());
            nbt.putInt("rodLevel", rodLevel);
            nbt.putInt("startupProgress", startupProgress);
            nbt.putInt("burnProgress", burnProgress);
            nbt.putInt("waterAcc", waterAcc);
            nbt.putInt("co2Acc", co2Acc);
            nbt.putInt("steamAcc", steamAcc);
            nbt.putBoolean("active", active);
            if (!loadedFuel.isEmpty()) { nbt.put("loadedFuel", loadedFuel.save(provider)); }
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.put("inventory", inventory.serializeNBT(provider));
            nbt.putInt("energy", energy.getEnergyStored());
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            reactorState = ReactorState.values()[nbt.getInt("reactorState")];
            rodLevel = nbt.getInt("rodLevel");
            startupProgress = nbt.getInt("startupProgress");
            burnProgress = nbt.getInt("burnProgress");
            waterAcc = nbt.getInt("waterAcc");
            co2Acc = nbt.getInt("co2Acc");
            steamAcc = nbt.getInt("steamAcc");
            active = nbt.getBoolean("active");
            loadedFuel = nbt.contains("loadedFuel", Tag.TAG_COMPOUND) ? ItemStack.parseOptional(provider, nbt.getCompound("loadedFuel")) : ItemStack.EMPTY;
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
            energy.setStoredEnergy(nbt.getInt("energy"));
            tanksDirty = false;
            inventoryDirty = false;
        }

        @Override public void writeSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            CompoundTag display = new CompoundTag();
            writeDisplaySyncNBT(display, provider);
            nbt.put("display", display);
        }

        @Override public void readSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display"), provider); }
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
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            energy.setStoredEnergy(nbt.getInt("energy"));
            tanksDirty = false;
        }
    }

    public record StyleNOXTank(MarkableFluidTank water, MarkableFluidTank steam, MarkableFluidTank co2) {
        public StyleNOXTank(Consumer<Void> markDirty, int waterCapacity, int steamCapacity, int co2Capacity) {
            this(new MarkableFluidTank(waterCapacity, markDirty), new MarkableFluidTank(steamCapacity, markDirty), new MarkableFluidTank(co2Capacity, markDirty));
        }

        public static StyleNOXTank makeClient() { return new StyleNOXTank(v -> {}, waterTankCapacity(), steamTankCapacity(), co2TankCapacity()); }

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

    private static class SyncEnergyStorage extends AveragingEnergyStorage {
        private final Runnable onChanged;

        public SyncEnergyStorage(int capacity, Runnable onChanged) {
            super(capacity);
            this.onChanged = onChanged;
        }

        @Override public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (received > 0 && !simulate) { onChanged.run(); }
            return received;
        }

        @Override public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (extracted > 0 && !simulate) { onChanged.run(); }
            return extracted;
        }

        @Override public void setStoredEnergy(int stored) {
            super.setStoredEnergy(stored);
            onChanged.run();
        }
    }
}
