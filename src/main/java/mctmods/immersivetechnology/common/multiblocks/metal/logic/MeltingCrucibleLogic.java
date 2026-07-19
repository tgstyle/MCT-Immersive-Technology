package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.common.multiblocks.helper.*;
import mctmods.immersivetechnology.common.multiblocks.metal.process.MeltingCrucibleProcess;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.MeltingRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.MeltingCrucibleShape;
import mctmods.immersivetechnology.common.fluids.helper.ArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.MarkableFluidTank;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.core.lib.ModSound;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.util.CachedRecipe;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.BiFunction;

public class MeltingCrucibleLogic implements IMultiblockLogic<MeltingCrucibleLogic.State>, IServerTickableComponent<MeltingCrucibleLogic.State>, IClientTickableComponent<MeltingCrucibleLogic.State>, IPressurizedFluidOutput<MeltingCrucibleLogic.State> {
    public static final int SLOT_INPUT_FILLED = 0;
    public static final int SLOT_INPUT_EMPTY = 1;
    public static final int SLOT_OUTPUT_EMPTY = 2;
    public static final int SLOT_OUTPUT_FILLED = 3;

    public static final int INPUT_TANK_CAPACITY = ServerConfig.meltingCrucibleInputTankCapacity;
    public static final int OUTPUT_TANK_CAPACITY = ServerConfig.meltingCrucibleOutputTankCapacity;
    public static final int ENERGY_CAPACITY = ServerConfig.meltingCrucibleEnergyCapacity;

    public static final double WORKING_HEAT_LEVEL = ServerConfig.meltingCrucibleHeatWorkingLevel;
    private static final double HEAT_LOSS_MULTIPLIER = ServerConfig.meltingCrucibleHeatLossMultiplier;
    private static final double HEAT_GAIN_BASE = ServerConfig.meltingCrucibleHeatGainBase;
    private static final int ENERGY_PER_TICK_TO_HEAT = ServerConfig.meltingCrucibleEnergyPerTickToHeat;
    private static final int ENERGY_PER_TICK_TO_MAINTAIN = ServerConfig.meltingCrucibleEnergyPerTickToMaintain;

    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(MeltingCrucibleShape.DATA.pointsOfInterest);

    public static final BlockPos REDSTONE_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "redstone0").getFirst();
    public static final List<BlockPos> INPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_input0");
    public static final List<BlockPos> OUTPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output0");
    public static final CapabilityPosition ENERGY_INPUT_POI = MultiblockPOIHelper.getCapabilityPosition(RAW_POIS, "energy_input0");
    private static final List<BlockPos> FLUID_OUTPUT_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output0");
    private static final RelativeBlockFace OUTPUT_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output0");
    private static final RelativeBlockFace INPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_input0");
    private static final RelativeBlockFace OUTPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output0");

    @Override public List<BlockPos> getOutputPositions() { return FLUID_OUTPUT_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FACING); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output()); }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        List<BlockPos> soundPosList = MultiblockPOIHelper.getPosList(RAW_POIS, "sound0");
        if (soundPosList.isEmpty()) { return; }
        BlockPos soundBlockPos = soundPosList.getFirst();
        Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(soundBlockPos.getX() + 0.5, soundBlockPos.getY() + 0.5, soundBlockPos.getZ() + 0.5));
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) { return; }
        float distSq = (float) player.distanceToSqr(soundPos);
        float attenuation = Math.max(distSq / 32f, 1f);
        float vol = 1f / attenuation;
        if (state.active && vol > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
            state.isSoundPlaying = ModSound.startSound(() -> state.active, ctx.isValid(), soundPos, Sounds.meltingCrucible, () -> {
                LocalPlayer p = Minecraft.getInstance().player;
                if (p == null) { return 0f; }
                float a = (float) Math.max(p.distanceToSqr(soundPos) / 32f, 1f);
                return 1f / a;
            }, () -> 1f);
        }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        state.energy.updateAverage();
        int prevEnergy = state.energy.getEnergyStored();
        boolean prevTanksDirty = state.tanksDirty;
        boolean wasActive = state.active;

        FluidStack fs = state.tanks.input().getFluid();
        MeltingRecipe recipe = fs.getAmount() > 0 ? state.recipeGetter.apply(ctx.getLevel().getRawLevel(), fs) : null;

        if (state.activeRecipe == null && state.activeRecipeId != null) {
            state.activeRecipe = MeltingRecipe.RECIPES.getById(ctx.getLevel().getRawLevel(), state.activeRecipeId);
            state.activeRecipeId = null;
        }
        if (state.activeRecipe == null || !state.activeRecipe.matches(fs)) {
            state.activeRecipe = recipe;
        }

        boolean shouldRun = state.rsState.isEnabled(ctx);
        int energyThisTick = state.heatLevel >= WORKING_HEAT_LEVEL ? ENERGY_PER_TICK_TO_MAINTAIN : ENERGY_PER_TICK_TO_HEAT;
        boolean heating = shouldRun && state.energy.extractEnergy(energyThisTick, true) >= energyThisTick;
        if (heating) { state.energy.extractEnergy(energyThisTick, false); }
        heatLogic(ctx, heating, state);

        boolean canProcess = shouldRun && state.activeRecipe != null && state.heatLevel >= state.activeRecipe.requiredTemp;
        state.active = state.processor.tickServer(state, ctx.getLevel(), canProcess);

        if (state.activeRecipe != null && recipe != null) {
            tryEnqueueProcess(state, ctx.getLevel().getRawLevel(), fs);
        }

        tryEmptyContainer(state.tanks.input(), state.inventory);
        FluidUtils.fillFluidContainer(state.tanks.output(), SLOT_OUTPUT_EMPTY, SLOT_OUTPUT_FILLED, state.inventory);
        pumpOutputs(ctx);

        boolean activeChanged = wasActive != state.active;
        int currentEnergy = state.energy.getEnergyStored();
        boolean energyChanged = prevEnergy != currentEnergy;
        boolean tanksChanged = prevTanksDirty != state.tanksDirty;
        boolean update = activeChanged || energyChanged || tanksChanged;

        if (update) { ctx.markMasterDirty(); ctx.requestMasterBESync(); }
    }

    private void heatLogic(IMultiblockContext<State> ctx, boolean heating, State state) {
        double prev = state.heatLevel;
        state.heatLevel -= getCooldownAmount(ctx);
        state.heatLevel = Math.max(state.heatLevel, 0);
        if (heating) { state.heatLevel += HEAT_GAIN_BASE; }
        double maxHeat = state.activeRecipe != null ? state.activeRecipe.requiredTemp : WORKING_HEAT_LEVEL;
        state.heatLevel = Math.min(state.heatLevel, maxHeat);
        if (prev != state.heatLevel) { ctx.markMasterDirty(); }
    }

    private double getCooldownAmount(IMultiblockContext<State> ctx) {
        Level level = ctx.getLevel().getRawLevel();
        BlockPos pos = ctx.getLevel().toAbsolute(BlockPos.ZERO);
        float biomeTemp = level.getBiome(pos).value().getBaseTemperature();
        double heatLost = biomeTemp > 0 ? biomeTemp : 0.1;
        return (1 / heatLost) * HEAT_LOSS_MULTIPLIER;
    }

    private void tryEmptyContainer(IFluidHandler tank, IItemHandlerModifiable inv) {
        ItemStack filledContainer = inv.getStackInSlot(SLOT_INPUT_FILLED);
        if (filledContainer.isEmpty()) { return; }
        FluidActionResult result = FluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.SIMULATE);
        if (!result.isSuccess()) { return; }
        ItemStack emptyContainer = result.getResult();
        ItemStack outputStack = inv.getStackInSlot(SLOT_INPUT_EMPTY);
        if (!outputStack.isEmpty() && !ItemStack.isSameItemSameComponents(outputStack, emptyContainer)) { return; }
        if (outputStack.getCount() + emptyContainer.getCount() > emptyContainer.getMaxStackSize()) { return; }
        result = FluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.EXECUTE);
        filledContainer.shrink(1);
        inv.setStackInSlot(SLOT_INPUT_FILLED, filledContainer);
        if (outputStack.isEmpty()) { inv.setStackInSlot(SLOT_INPUT_EMPTY, result.getResult()); }
        else { outputStack.grow(result.getResult().getCount()); }
    }

    private void tryEnqueueProcess(State state, Level level, FluidStack currentInput) {
        if (state.processor.getQueueSize() >= state.processor.getMaxQueueSize()) { return; }
        RecipeHolder<MeltingRecipe> recipeHolder = state.recipeHolderGetter.apply(level, currentInput);
        if (recipeHolder == null) { return; }
        MeltingRecipe recipe = recipeHolder.value();
        if (state.heatLevel < recipe.requiredTemp) { return; }
        FluidStack inputFluid = state.tanks.input().getFluid();
        if (inputFluid.getAmount() < recipe.getInputAmount()) { return; }
        FluidStack outputFluid = recipe.fluidOutput();
        if (outputFluid != null && !outputFluid.isEmpty() && state.tanks.output().getFluidAmount() + outputFluid.getAmount() > state.tanks.output().getCapacity()) { return; }
        MeltingCrucibleProcess process = new MeltingCrucibleProcess(recipeHolder);
        state.processor.addProcessToQueue(process, level, false);
    }

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.register(Capabilities.EnergyStorage.BLOCK, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (localPos.equals(ENERGY_INPUT_POI.posInMultiblock()) && (side == null || side == ENERGY_INPUT_POI.side())) {
                return state.energy;
            }
            return null;
        });
        register.register(Capabilities.FluidHandler.BLOCK, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (INPUT_FLUID_POIS.contains(localPos) && (side == null || side == INPUT_FLUID_FACING)) {
                return state.inputCap;
            }
            if (OUTPUT_FLUID_POIS.contains(localPos) && (side == null || side == OUTPUT_FLUID_FACING)) {
                return state.outputCap;
            }
            return null;
        });
        register.register(Capabilities.ItemHandler.BLOCK, (state, position) -> state.invCap);
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { MultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return MeltingCrucibleShape.GETTER; }

    public static class State implements IMultiblockState, IProcessContext.IProcessContextInMachine<MeltingRecipe>, IDisplayContext {
        public final BiFunction<Level, FluidStack, MeltingRecipe> recipeGetter = CachedRecipe.cached(MeltingRecipe::findRecipe);
        public final BiFunction<Level, FluidStack, RecipeHolder<MeltingRecipe>> recipeHolderGetter = CachedRecipe.cached(MeltingRecipe::findRecipeHolder);
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final MeltingCrucibleTank tanks;
        public IFluidHandler inputCap;
        public IFluidHandler outputCap;
        public IItemHandler invCap;
        public final SlotwiseItemHandler inventory;
        private final IFluidTank[] tankArray;
        public final MultiblockProcessor.InMachineProcessor<MeltingRecipe> processor;
        public AveragingEnergyStorage energy;
        public boolean active;
        public BooleanSupplier isSoundPlaying = () -> false;
        public double heatLevel = 0.0;
        public MeltingRecipe activeRecipe = null;
        private ResourceLocation activeRecipeId;
        public boolean tanksDirty = false;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); this.tanksDirty = true; };
            this.tanks = new MeltingCrucibleTank(v -> onChanged.run());
            this.tankArray = new IFluidTank[]{tanks.input(), tanks.output()};
            inventory = new SlotwiseItemHandler(
                    List.of(
                            SlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            SlotwiseItemHandler.IOConstraint.OUTPUT,
                            SlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            SlotwiseItemHandler.IOConstraint.OUTPUT
                    ),
                    onChanged
            );
            this.inputCap = new ArrayFluidHandler(tanks.input(), false, true, onChanged);
            this.outputCap = new ArrayFluidHandler(tanks.output(), true, false, onChanged);
            this.invCap = inventory;
            this.energy = new SyncEnergyStorage(ENERGY_CAPACITY, onChanged);
            this.processor = new MultiblockProcessor.InMachineProcessor<>(1, 0f, 1, markDirty, MeltingRecipe.RECIPES::getById);
        }

        public SlotwiseItemHandler getInventory() { return inventory; }
        public MeltingCrucibleTank getTanks() { return tanks; }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.put("energy", energy.serializeNBT(provider));
            nbt.put("tanks", this.tanks.toNBT(provider));
            nbt.put("processor", processor.toNBT(provider));
            nbt.put("inventory", inventory.serializeNBT(provider));
            nbt.putBoolean("active", active);
            nbt.putDouble("heatLevel", heatLevel);
            if (activeRecipe != null) { nbt.putString("activeRecipe", activeRecipe.getId().toString()); }
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            CompoundTag energyTag = nbt.getCompound("energy");
            if (!energyTag.isEmpty()) {
                energy.deserializeNBT(provider, energyTag);
            }
            this.tanks.readNBT(nbt.getCompound("tanks"), provider);
            this.processor.fromNBT(nbt.getList("processor", Tag.TAG_COMPOUND), MeltingCrucibleProcess::new, provider);
            this.inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
            active = nbt.getBoolean("active");
            heatLevel = nbt.getDouble("heatLevel");
            if (nbt.contains("activeRecipe")) { activeRecipeId = ResourceLocation.tryParse(nbt.getString("activeRecipe")); }
            tanksDirty = false;
        }

        @Override public void writeSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            CompoundTag display = new CompoundTag();
            writeDisplaySyncNBT(display, provider);
            nbt.put("display", display);
        }

        @Override public void readSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            if (nbt.contains("display", Tag.TAG_COMPOUND)) {
                readDisplaySyncNBT(nbt.getCompound("display"), provider);
            }
        }

        @Override public AveragingEnergyStorage getEnergy() { return energy; }
        @Override public IFluidTank[] getInternalTanks() { return tankArray; }
        @Override public int[] getOutputTanks() { return new int[]{1}; }
        @Override public int[] getOutputSlots() { return new int[0]; }
        @Override public boolean isActive() { return active; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.put("energy", energy.serializeNBT(provider));
            nbt.put("inventory", inventory.serializeNBT(provider));
            nbt.putDouble("heatLevel", heatLevel);
            if (activeRecipe != null) { nbt.putString("activeRecipe", activeRecipe.getId().toString()); }
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            if (energy == null) { energy = new SyncEnergyStorage(ENERGY_CAPACITY, () -> {}); }
            CompoundTag energyTag = nbt.getCompound("energy");
            if (!energyTag.isEmpty()) {
                energy.deserializeNBT(provider, energyTag);
            }
            inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
            heatLevel = nbt.getDouble("heatLevel");
            if (nbt.contains("activeRecipe")) { activeRecipeId = ResourceLocation.tryParse(nbt.getString("activeRecipe")); }
            tanksDirty = false;
        }
    }

    public record MeltingCrucibleTank(MarkableFluidTank input, MarkableFluidTank output) {
        public MeltingCrucibleTank(Consumer<Void> markDirty) {
            this(new MarkableFluidTank(INPUT_TANK_CAPACITY, markDirty), new MarkableFluidTank(OUTPUT_TANK_CAPACITY, markDirty));
        }

        public static MeltingCrucibleTank makeClient() { return new MeltingCrucibleTank(v -> {}); }

        public CompoundTag toNBT(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.put("input", this.input.writeToNBT(provider, new CompoundTag()));
            tag.put("output", this.output.writeToNBT(provider, new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag, HolderLookup.Provider provider) {
            this.input.readFromNBT(provider, tag.getCompound("input"));
            this.output.readFromNBT(provider, tag.getCompound("output"));
        }

        @SuppressWarnings("unused")
        public int getCapacity() { return Math.max(INPUT_TANK_CAPACITY, OUTPUT_TANK_CAPACITY); }

        public MarkableFluidTank input() { return input; }
        public MarkableFluidTank output() { return output; }
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
    }
}
