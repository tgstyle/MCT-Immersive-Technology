package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import com.immersiveconvergence.api.integration.DisplayLines;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import mctmods.immersivetechnology.common.multiblocks.metal.process.MeltingCrucibleProcess;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.MeltingRecipe;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import com.immersiveconvergence.api.util.MultiTankFluidHandler;
import com.immersiveconvergence.api.util.MarkableFluidTank;
import mctmods.immersivetechnology.core.ServerConfig;
import com.immersiveconvergence.api.client.MachineSound;
import mctmods.immersivetechnology.core.registration.Sounds;
import com.immersiveconvergence.api.util.RecipeCache;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.BiFunction;
import com.immersiveconvergence.api.util.ConstrainedItemHandler;
import com.immersiveconvergence.api.multiblock.IFluidOutputPump;
import com.immersiveconvergence.api.multiblock.IProcessContext;
import com.immersiveconvergence.api.util.MultiBlockInventoryUtils;
import com.immersiveconvergence.api.multiblock.IDisplayContext;
import com.immersiveconvergence.api.multiblock.ShapeData;

public class MeltingCrucibleLogic implements IMultiblockLogic<MeltingCrucibleLogic.State>, IServerTickableComponent<MeltingCrucibleLogic.State>, IClientTickableComponent<MeltingCrucibleLogic.State>, IFluidOutputPump<MeltingCrucibleLogic.State> {
    private static final ShapeData SHAPE = ITShapes.get("melting_crucible");
    public static final int SLOT_INPUT_FILLED = 0;
    public static final int SLOT_INPUT_EMPTY = 1;
    public static final int SLOT_OUTPUT_EMPTY = 2;
    public static final int SLOT_OUTPUT_FILLED = 3;

    public static int inputTankCapacity() { return ServerConfig.meltingCrucibleInputTankCapacity; }
    public static int outputTankCapacity() { return ServerConfig.meltingCrucibleOutputTankCapacity; }
    public static int energyCapacity() { return ServerConfig.meltingCrucibleEnergyCapacity; }

    public static double workingHeatLevel() { return ServerConfig.meltingCrucibleHeatWorkingLevel; }
    private static double heatLossMultiplier() { return ServerConfig.meltingCrucibleHeatLossMultiplier; }
    private static double heatGainBase() { return ServerConfig.meltingCrucibleHeatGainBase; }
    private static int energyPerTickToHeat() { return ServerConfig.meltingCrucibleEnergyPerTickToHeat; }
    private static int energyPerTickToMaintain() { return ServerConfig.meltingCrucibleEnergyPerTickToMaintain; }

    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(ITShapes.data("melting_crucible").pointsOfInterest);

    public static final BlockPos REDSTONE_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "redstone0").get(0);
    public static final List<BlockPos> INPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_input0");
    public static final List<BlockPos> OUTPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output0");
    public static final CapabilityPosition ENERGY_INPUT_POI = MultiblockPOIHelper.getCapabilityPosition(RAW_POIS, "energy_input0");
    private static final List<BlockPos> FLUID_OUTPUT_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output0");
    private static final RelativeBlockFace OUTPUT_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output0");

    @Override public List<BlockPos> getOutputPositions() { return FLUID_OUTPUT_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FACING); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output()); }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        List<BlockPos> soundPosList = MultiblockPOIHelper.getPosList(RAW_POIS, "sound0");
        if (soundPosList.isEmpty()) { return; }
        BlockPos soundBlockPos = soundPosList.get(0);
        Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(soundBlockPos.getX() + 0.5, soundBlockPos.getY() + 0.5, soundBlockPos.getZ() + 0.5));
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) { return; }
        float distSq = (float) player.distanceToSqr(soundPos);
        float attenuation = Math.max(distSq / 32f, 1f);
        float vol = 1f / attenuation;
        if (state.active && vol > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
            state.isSoundPlaying = MachineSound.startSound(() -> state.active, ctx.isValid(), soundPos, Sounds.meltingCrucible, () -> {
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
        if (state.activeRecipe == null && state.activeRecipeId != null) { state.activeRecipe = MeltingRecipe.RECIPES.getById(ctx.getLevel().getRawLevel(), state.activeRecipeId); state.activeRecipeId = null; }
        if (state.activeRecipe == null || !state.activeRecipe.input.testIgnoringAmount(fs)) { state.activeRecipe = recipe; }

        boolean shouldRun = state.rsState.isEnabled(ctx);
        int energyThisTick = state.heatLevel >= workingHeatLevel() ? energyPerTickToMaintain() : energyPerTickToHeat();
        boolean heating = shouldRun && state.energy.extractEnergy(energyThisTick, true) >= energyThisTick;
        if (heating) { state.energy.extractEnergy(energyThisTick, false); }
        heatLogic(ctx, heating, state);

        boolean canProcess = shouldRun && state.activeRecipe != null && state.heatLevel >= state.activeRecipe.requiredTemp;
        state.active = state.processor.tickServer(state, ctx.getLevel(), canProcess);

        if (state.activeRecipe != null) {
            tryEnqueueProcess(state, ctx.getLevel().getRawLevel(), state.activeRecipe);
        }

        tryEmptyContainer(state.tanks.input(), state.inventory);
        FluidUtils.fillFluidContainer(state.tanks.output(), SLOT_OUTPUT_EMPTY, SLOT_OUTPUT_FILLED, state.inventory);
        pumpOutputs(ctx);

        boolean activeChanged = wasActive != state.active;
        int currentEnergy = state.energy.getEnergyStored();
        boolean energyChanged = prevEnergy != currentEnergy;
        boolean tanksChanged = prevTanksDirty != state.tanksDirty;
        int newQueueSize = state.processor.getQueueSize();
        boolean queueSizeChanged = newQueueSize != state.queueSize;
        if (queueSizeChanged) { state.queueSize = newQueueSize; }
        int newComparatorValue = (int) Math.min(15, (15 * state.heatLevel) / workingHeatLevel());
        boolean comparatorChanged = newComparatorValue != state.lastComparatorValue;
        if (comparatorChanged) { ctx.setComparatorOutputFor(REDSTONE_POI, newComparatorValue); state.lastComparatorValue = newComparatorValue; }
        boolean update = activeChanged || energyChanged || tanksChanged || queueSizeChanged || comparatorChanged;

        if (update) { ctx.markMasterDirty(); ctx.requestMasterBESync(); }
    }

    private void heatLogic(IMultiblockContext<State> ctx, boolean heating, State state) {
        double prev = state.heatLevel;
        state.heatLevel -= getCooldownAmount(ctx);
        state.heatLevel = Math.max(state.heatLevel, 0);
        if (heating) { state.heatLevel += heatGainBase(); }
        double maxHeat = state.activeRecipe != null ? state.activeRecipe.requiredTemp : workingHeatLevel();
        state.heatLevel = Math.min(state.heatLevel, maxHeat);
        if (prev != state.heatLevel) { ctx.markMasterDirty(); }
    }

    private double getCooldownAmount(IMultiblockContext<State> ctx) {
        Level level = ctx.getLevel().getRawLevel();
        BlockPos pos = ctx.getLevel().toAbsolute(BlockPos.ZERO);
        float biomeTemp = level.getBiome(pos).value().getBaseTemperature();
        double heatLost = biomeTemp > 0 ? biomeTemp : 0.1;
        return (1 / heatLost) * heatLossMultiplier();
    }

    private void tryEmptyContainer(IFluidHandler tank, IItemHandlerModifiable inv) {
        ItemStack filledContainer = inv.getStackInSlot(SLOT_INPUT_FILLED);
        if (filledContainer.isEmpty()) { return; }
        FluidActionResult result = FluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.SIMULATE);
        if (!result.isSuccess()) { return; }
        ItemStack emptyContainer = result.getResult();
        ItemStack outputStack = inv.getStackInSlot(SLOT_INPUT_EMPTY);
        if (!outputStack.isEmpty() && !ItemHandlerHelper.canItemStacksStack(outputStack, emptyContainer)) { return; }
        if (outputStack.getCount() + emptyContainer.getCount() > emptyContainer.getMaxStackSize()) { return; }
        result = FluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.EXECUTE);
        filledContainer.shrink(1);
        inv.setStackInSlot(SLOT_INPUT_FILLED, filledContainer);
        if (outputStack.isEmpty()) { inv.setStackInSlot(SLOT_INPUT_EMPTY, result.getResult()); }
        else { outputStack.grow(result.getResult().getCount()); }
    }

    private void tryEnqueueProcess(State state, Level level, MeltingRecipe recipe) {
        if (state.processor.getQueueSize() >= state.processor.getMaxQueueSize()) { return; }
        if (recipe == null) { return; }
        if (state.heatLevel < recipe.requiredTemp) { return; }
        FluidStack inputFluid = state.tanks.input().getFluid();
        if (inputFluid.getAmount() < recipe.input.getAmount()) { return; }
        FluidStack outputFluid = recipe.fluidOutput;
        if (outputFluid != null && !outputFluid.isEmpty() && state.tanks.output().getFluidAmount() + outputFluid.getAmount() > state.tanks.output().getCapacity()) { return; }
        MeltingCrucibleProcess process = new MeltingCrucibleProcess(recipe);
        process.setInputTanks(0);
        state.processor.addProcessToQueue(process, level, false);
    }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        State state = ctx.getState();
        if (cap == ForgeCapabilities.ENERGY) {
            if (position.posInMultiblock().equals(ENERGY_INPUT_POI.posInMultiblock()) && (position.side() == null || position.side() == ENERGY_INPUT_POI.side())) { return state.energyCap.cast(ctx); }
        } else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (INPUT_FLUID_POIS.contains(position.posInMultiblock())) { return state.inputCap.cast(ctx); }
            if (OUTPUT_FLUID_POIS.contains(position.posInMultiblock())) { return state.outputCap.cast(ctx); }
        } else if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return state.invCap.cast(ctx);
        }
        return LazyOptional.empty();
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { MultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return SHAPE.getter; }

    public static class State implements IMultiblockState, IProcessContext.ProcessContextInMachine<MeltingRecipe>, IDisplayContext {
        public final BiFunction<Level, FluidStack, MeltingRecipe> recipeGetter = RecipeCache.cached(MeltingRecipe::findRecipe);
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final MeltingCrucibleTank tanks;
        public final StoredCapability<IEnergyStorage> energyCap;
        public final StoredCapability<IFluidHandler> inputCap;
        public final StoredCapability<IFluidHandler> outputCap;
        public final StoredCapability<IItemHandler> invCap;
        public final ConstrainedItemHandler inventory;
        private final IFluidTank[] tankArray;
        public final MultiblockProcessor.InMachineProcessor<MeltingRecipe> processor;
        public AveragingEnergyStorage energy;
        public boolean active;
        public BooleanSupplier isSoundPlaying = () -> false;
        public double heatLevel = 0.0;
        public MeltingRecipe activeRecipe = null;
        private ResourceLocation activeRecipeId;
        public boolean tanksDirty = false;
        public int queueSize = 0;
        public int lastComparatorValue = -1;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); this.tanksDirty = true; };
            this.tanks = new MeltingCrucibleTank(v -> onChanged.run());
            this.tankArray = new IFluidTank[]{tanks.input(), tanks.output()};
            inventory = new ConstrainedItemHandler(
                    List.of(
                            ConstrainedItemHandler.IOConstraint.FLUID_INPUT,
                            ConstrainedItemHandler.IOConstraint.OUTPUT,
                            ConstrainedItemHandler.IOConstraint.FLUID_INPUT,
                            ConstrainedItemHandler.IOConstraint.OUTPUT
                    ),
                    onChanged
            );
            this.inputCap = new StoredCapability<>(new MultiTankFluidHandler(tanks.input(), false, true, onChanged));
            this.outputCap = new StoredCapability<>(new MultiTankFluidHandler(tanks.output(), true, false, onChanged));
            this.invCap = new StoredCapability<>(inventory);
            this.energy = new SyncEnergyStorage(energyCapacity(), onChanged);
            this.energyCap = new StoredCapability<>(this.energy);
            this.processor = new MultiblockProcessor.InMachineProcessor<>(1, 0f, 1, markDirty, MeltingRecipe.RECIPES::getById);
        }

        public ConstrainedItemHandler getInventory() { return inventory; }
        public MeltingCrucibleTank getTanks() { return tanks; }

        @Override public void writeSaveNBT(CompoundTag nbt) {
            nbt.put("energy", energy.serializeNBT());
            nbt.put("tanks", this.tanks.toNBT());
            nbt.put("processor", processor.toNBT());
            nbt.put("inventory", inventory.serializeNBT());
            nbt.putBoolean("active", active);
            nbt.putDouble("heatLevel", heatLevel);
            if (activeRecipe != null) { nbt.putString("activeRecipe", activeRecipe.getId().toString()); }
        }

        @Override public void readSaveNBT(CompoundTag nbt) {
            energy.deserializeNBT(nbt.get("energy"));
            this.tanks.readNBT(nbt.getCompound("tanks"));
            this.processor.fromNBT(nbt.getList("processor", Tag.TAG_COMPOUND), MeltingCrucibleProcess::new);
            this.inventory.deserializeNBT(nbt.getCompound("inventory"));
            active = nbt.getBoolean("active");
            heatLevel = nbt.getDouble("heatLevel");
            if (nbt.contains("activeRecipe")) { activeRecipeId = ResourceLocation.tryParse(nbt.getString("activeRecipe")); }
            tanksDirty = false;
        }

        @Override public void writeSyncNBT(CompoundTag nbt) {
            CompoundTag display = new CompoundTag();
            writeDisplaySyncNBT(display);
            nbt.put("display", display);
        }

        @Override public void readSyncNBT(CompoundTag nbt) {
            if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display")); }
        }

        @Override public AveragingEnergyStorage getEnergy() { return energy; }
        @Override public IFluidTank[] getInternalTanks() { return tankArray; }
        @Override public int[] getOutputTanks() { return new int[]{1}; }
        @Override public int[] getOutputSlots() { return new int[0]; }
        @Override public boolean isActive() { return active; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT());
            nbt.put("energy", energy.serializeNBT());
            nbt.put("inventory", inventory.serializeNBT());
            nbt.putDouble("heatLevel", heatLevel);
            if (activeRecipe != null) { nbt.putString("activeRecipe", activeRecipe.getId().toString()); }
            nbt.putInt("queueSize", queueSize);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt) {
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"));
            if (energy == null) { energy = new SyncEnergyStorage(energyCapacity(), () -> {}); }
            energy.deserializeNBT(nbt.get("energy"));
            inventory.deserializeNBT(nbt.getCompound("inventory"));
            heatLevel = nbt.getDouble("heatLevel");
            if (nbt.contains("activeRecipe")) { activeRecipeId = ResourceLocation.tryParse(nbt.getString("activeRecipe")); }
            queueSize = nbt.getInt("queueSize");
            tanksDirty = false;
        }
    

        @Override public void addDisplayLines(Level level, DisplayLines lines) {
            FluidStack input = tanks.input().getFluid();
            MeltingRecipe recipe = input.isEmpty() ? null : MeltingRecipe.findRecipe(level, input);
            lines.temperature(heatLevel, recipe != null ? recipe.requiredTemp : workingHeatLevel());
            if (queueSize > 0) { lines.text("Processing (" + queueSize + " queued)"); }
            if (input.isEmpty()) { lines.fuelEmpty(); }
        }
}

    public record MeltingCrucibleTank(MarkableFluidTank input, MarkableFluidTank output) {
        public MeltingCrucibleTank(Consumer<Void> markDirty) {
            this(new MarkableFluidTank(inputTankCapacity(), markDirty), new MarkableFluidTank(outputTankCapacity(), markDirty));
        }

        public static MeltingCrucibleTank makeClient() { return new MeltingCrucibleTank(v -> {}); }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("input", this.input.writeToNBT(new CompoundTag()));
            tag.put("output", this.output.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) {
            this.input.readFromNBT(tag.getCompound("input"));
            this.output.readFromNBT(tag.getCompound("output"));
        }

        @SuppressWarnings("unused")
        public int getCapacity() { return Math.max(inputTankCapacity(), outputTankCapacity()); }

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
