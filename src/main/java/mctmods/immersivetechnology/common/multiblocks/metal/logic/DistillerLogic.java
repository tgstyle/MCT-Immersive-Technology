package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.client.util.ClientUtils;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.process.DistillerProcess;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.DistillerRecipe;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.util.FluidContainers;
import mctmods.immersivetechnology.core.util.IDisplaySyncState;
import mctmods.immersivetechnology.core.util.ItemOutputs;
import mctmods.immersivetechnology.core.util.SyncEnergyStorage;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.client.MachineSound;
import com.immersiveconvergence.api.integration.DisplayLines;
import com.immersiveconvergence.api.multiblock.IDataReloadAware;
import com.immersiveconvergence.api.multiblock.IFluidOutputPump;
import com.immersiveconvergence.api.multiblock.IProcessContext;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.util.ConstrainedItemHandler;
import com.immersiveconvergence.api.util.MarkableFluidTank;
import com.immersiveconvergence.api.util.MultiBlockInventoryUtils;
import com.immersiveconvergence.api.util.MultiTankFluidHandler;
import com.immersiveconvergence.api.util.RecipeCache;
import com.immersiveconvergence.api.util.SlotRangeItemHandler;
import com.immersiveconvergence.api.util.TankPair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class DistillerLogic implements IMultiblockLogic<DistillerLogic.State>, IServerTickableComponent<DistillerLogic.State>, IClientTickableComponent<DistillerLogic.State>, IFluidOutputPump<DistillerLogic.State> {
    public static final int SLOT_INPUT_FILLED = 0;
    public static final int SLOT_INPUT_EMPTY = 1;
    public static final int SLOT_OUTPUT_EMPTY = 2;
    public static final int SLOT_OUTPUT_FILLED = 3;
    public static final int OUTPUT_SLOT = 4;
    private static List<PoIJSONSchema> RAW_POIS;
    public static BlockPos REDSTONE_POI;
    public static List<BlockPos> INPUT_FLUID_POIS;
    public static List<BlockPos> OUTPUT_FLUID_POIS;
    private static List<BlockPos> ENERGY_INPUT_POIS;
    private static RelativeBlockFace ENERGY_INPUT_FACING;
    public static MultiblockFace ITEM_OUTPUT_POI;
    private static RelativeBlockFace INPUT_FLUID_FACING;
    private static RelativeBlockFace OUTPUT_FLUID_FACING;

    static { ITShapes.readPois("distiller", DistillerLogic::loadPois); }

    public static int inputTankCapacity() { return ServerConfig.distillerInputTankCapacity; }

    public static int outputTankCapacity() { return ServerConfig.distillerOutputTankCapacity; }

    public static int energyCapacity() { return ServerConfig.distillerEnergyCapacity; }

    public static int energyMaxIo() { return ServerConfig.distillerEnergyMaxIO; }

    @Override public List<BlockPos> getOutputPositions() { return OUTPUT_FLUID_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FLUID_FACING); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output()); }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        Vec3 soundPos = ctx.getLevel().toAbsolute(Vec3.atCenterOf(MultiblockPOIHelper.getPosList(RAW_POIS, "sound0").get(0)));
        if (Minecraft.getInstance().player == null) { return; }
        if (state.active && ClientUtils.attenuated(soundPos, 32, 1) > 0.01f && !state.isSoundPlaying.getAsBoolean()) { state.isSoundPlaying = MachineSound.startSound(() -> state.active, ctx.isValid(), soundPos, Sounds.distiller, () -> ClientUtils.attenuated(soundPos, 32, 1), () -> 1f); }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        state.energy.updateAverage();
        int prevEnergy = state.energy.getEnergyStored();
        boolean prevTanksDirty = state.tanksDirty;
        boolean prevInventoryDirty = state.inventoryDirty;
        boolean wasActive = state.active;
        DistillerRecipe recipe = state.lastRecipeCache;
        if (recipe == null || !recipe.input.testIgnoringAmount(state.tanks.input().getFluid())) {
            recipe = state.recipeGetter.apply(ctx.getLevel().getRawLevel(), state.tanks.input().getFluid());
            state.lastRecipeCache = recipe;
        }
        state.active = state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx));
        tryEnqueueProcess(state, ctx.getLevel().getRawLevel(), recipe);
        FluidContainers.emptyBucket(state.tanks.input(), state.inventory, SLOT_INPUT_FILLED, SLOT_INPUT_EMPTY);
        FluidUtils.fillFluidContainer(state.tanks.output(), SLOT_OUTPUT_EMPTY, SLOT_OUTPUT_FILLED, state.inventory);
        pumpOutputs(ctx);
        ItemOutputs.eject(state.inventory, state.outputRef, SLOT_INPUT_EMPTY, SLOT_OUTPUT_FILLED, OUTPUT_SLOT);
        int newQueueSize = state.processor.getQueueSize();
        boolean queueSizeChanged = newQueueSize != state.queueSize;
        if (queueSizeChanged) { state.queueSize = newQueueSize; }
        int maxEnergy = state.energy.getMaxEnergyStored();
        int newComparatorValue = maxEnergy > 0 ? (15 * state.energy.getEnergyStored()) / maxEnergy : 0;
        boolean comparatorChanged = newComparatorValue != state.lastComparatorValue;
        if (comparatorChanged) {
            ctx.setComparatorOutputFor(REDSTONE_POI, newComparatorValue);
            state.lastComparatorValue = newComparatorValue;
        }
        if (wasActive != state.active || prevEnergy != state.energy.getEnergyStored() || prevTanksDirty != state.tanksDirty || prevInventoryDirty != state.inventoryDirty || queueSizeChanged || comparatorChanged) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    private static void tryEnqueueProcess(State state, Level level, DistillerRecipe recipe) {
        if (state.processor.getQueueSize() >= state.processor.getMaxQueueSize() || recipe == null || state.tanks.input().getFluid().getAmount() < recipe.input.getAmount()) { return; }
        FluidStack outputFluid = recipe.fluidOutput;
        if (outputFluid != null && !outputFluid.isEmpty() && state.tanks.output().getFluidAmount() + outputFluid.getAmount() > state.tanks.output().getCapacity()) { return; }
        if (!recipe.itemOutput.isEmpty() && ItemOutputs.overflows(state.inventory.getStackInSlot(OUTPUT_SLOT), recipe.itemOutput)) { return; }
        state.processor.addProcessToQueue(new DistillerProcess(recipe), level, false);
    }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        State state = ctx.getState();
        BlockPos localPos = position.posInMultiblock();
        RelativeBlockFace side = position.side();
        if (cap == ForgeCapabilities.ENERGY) {
            if (ENERGY_INPUT_POIS.contains(localPos) && (side == null || side == ENERGY_INPUT_FACING)) { return state.energyCap.cast(ctx); }
        }
        else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (INPUT_FLUID_POIS.contains(localPos) && (side == null || side == INPUT_FLUID_FACING)) { return state.inputCap.cast(ctx); }
            if (OUTPUT_FLUID_POIS.contains(localPos) && (side == null || side == OUTPUT_FLUID_FACING)) { return state.outputCapSteam.cast(ctx); }
        }
        else if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (localPos.equals(ITEM_OUTPUT_POI.posInMultiblock()) && (side == null || side == ITEM_OUTPUT_POI.face())) { return state.itemOutputCap.cast(ctx); }
            return state.invCap.cast(ctx);
        }
        return LazyOptional.empty();
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { MultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get("distiller").getter; }

    public static class State implements IDisplaySyncState, IProcessContext.ProcessContextInMachine<DistillerRecipe>, IDataReloadAware {
        public final BiFunction<Level, FluidStack, DistillerRecipe> recipeGetter = RecipeCache.cached(DistillerRecipe::findRecipe);
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final TankPair tanks;
        public final StoredCapability<IEnergyStorage> energyCap;
        public final StoredCapability<IFluidHandler> inputCap;
        public final StoredCapability<IFluidHandler> outputCapSteam;
        public final StoredCapability<IItemHandler> invCap;
        public final StoredCapability<IItemHandler> itemOutputCap;
        public CapabilityReference<IItemHandler> outputRef;
        public final ConstrainedItemHandler inventory;
        private final IFluidTank[] tankArray;
        public final MultiblockProcessor.InMachineProcessor<DistillerRecipe> processor;
        public final AveragingEnergyStorage energy;
        public boolean active;
        public BooleanSupplier isSoundPlaying = () -> false;
        public boolean tanksDirty = false;
        public boolean inventoryDirty = false;
        public int queueSize = 0;
        public int lastComparatorValue = -1;
        public DistillerRecipe lastRecipeCache;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); this.tanksDirty = true; this.inventoryDirty = true; };
            this.tanks = new TankPair(v -> onChanged.run(), inputTankCapacity(), outputTankCapacity());
            this.tankArray = new IFluidTank[]{tanks.input(), tanks.output()};
            inventory = new ConstrainedItemHandler(List.of(ConstrainedItemHandler.IOConstraint.FLUID_INPUT, ConstrainedItemHandler.IOConstraint.OUTPUT, ConstrainedItemHandler.IOConstraint.FLUID_INPUT, ConstrainedItemHandler.IOConstraint.OUTPUT, ConstrainedItemHandler.IOConstraint.OUTPUT), onChanged);
            this.inputCap = new StoredCapability<>(new MultiTankFluidHandler(tanks.input(), false, true, onChanged));
            this.outputCapSteam = new StoredCapability<>(new MultiTankFluidHandler(tanks.output(), true, false, onChanged));
            this.invCap = new StoredCapability<>(inventory);
            this.energy = new SyncEnergyStorage(energyCapacity(), energyMaxIo(), onChanged);
            this.energyCap = new StoredCapability<>(this.energy);
            this.processor = new MultiblockProcessor.InMachineProcessor<>(1, 0f, 1, markDirty, DistillerRecipe.RECIPES::getById);
            this.itemOutputCap = new StoredCapability<>(new SlotRangeItemHandler(inventory, false, true, List.of(new SlotRangeItemHandler.IntRange(SLOT_INPUT_EMPTY, SLOT_INPUT_EMPTY + 1), new SlotRangeItemHandler.IntRange(SLOT_OUTPUT_FILLED, SLOT_OUTPUT_FILLED + 1), new SlotRangeItemHandler.IntRange(OUTPUT_SLOT, OUTPUT_SLOT + 1))));
            bindOutputs(ctx);
        }

        @Override public void onDataReload(IInitialMultiblockContext<?> context) {
            lastComparatorValue = -1;
            bindOutputs(context);
        }

        private void bindOutputs(IInitialMultiblockContext<?> context) { this.outputRef = context.getCapabilityAt(ForgeCapabilities.ITEM_HANDLER, ITEM_OUTPUT_POI); }

        public ConstrainedItemHandler getInventory() { return inventory; }

        public TankPair getTanks() { return tanks; }

        @Override public void writeSaveNBT(CompoundTag nbt) {
            rsState.writeSaveNBT(nbt);
            nbt.put("energy", energy.serializeNBT());
            nbt.put("tanks", this.tanks.toNBT());
            nbt.put("processor", processor.toNBT());
            nbt.put("inventory", inventory.serializeNBT());
            nbt.putBoolean("active", active);
        }

        @Override public void readSaveNBT(CompoundTag nbt) {
            rsState.readSaveNBT(nbt);
            energy.deserializeNBT(nbt.get("energy"));
            this.tanks.readNBT(nbt.getCompound("tanks"));
            this.processor.fromNBT(nbt.getList("processor", Tag.TAG_COMPOUND), DistillerProcess::new);
            this.inventory.deserializeNBT(nbt.getCompound("inventory"));
            active = nbt.getBoolean("active");
            tanksDirty = false;
            inventoryDirty = false;
        }

        @Override public AveragingEnergyStorage getEnergy() { return energy; }

        @Override public IFluidTank[] getInternalTanks() { return tankArray; }

        @Override public int[] getOutputTanks() { return new int[]{1}; }

        @Override public int[] getOutputSlots() { return new int[]{OUTPUT_SLOT}; }

        @Override public boolean isActive() { return active; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT());
            nbt.put("energy", energy.serializeNBT());
            nbt.put("inventory", inventory.serializeNBT());
            nbt.putInt("queueSize", queueSize);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt) {
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"));
            energy.deserializeNBT(nbt.get("energy"));
            inventory.deserializeNBT(nbt.getCompound("inventory"));
            queueSize = nbt.getInt("queueSize");
            tanksDirty = false;
            inventoryDirty = false;
        }

        @Override public void addDisplayLines(Level level, DisplayLines lines) { if (queueSize > 0) { lines.text("Processing (" + queueSize + " queued)"); } }
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        RAW_POIS = pois;
        REDSTONE_POI = MultiblockPOIHelper.getPosList(pois, "redstone0").get(0);
        INPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_input0");
        OUTPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_output0");
        ENERGY_INPUT_POIS = MultiblockPOIHelper.getPosList(pois, "energy_input0");
        ENERGY_INPUT_FACING = MultiblockPOIHelper.getFacing(pois, "energy_input0");
        ITEM_OUTPUT_POI = new MultiblockFace(MultiblockPOIHelper.getFacing(pois, "item_output0"), MultiblockPOIHelper.getPosList(pois, "item_output0").get(0));
        INPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_input0");
        OUTPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_output0");
    }
}
