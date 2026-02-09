package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.common.multiblocks.helper.ITDisplayContext;
import mctmods.immersivetechnology.common.multiblocks.helper.ITPressurizedFluidOutput;
import mctmods.immersivetechnology.common.multiblocks.metal.process.HeatExchangerProcess;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.HeatExchangerRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.HeatExchangerShape;
import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.core.lib.ITSound;
import mctmods.immersivetechnology.core.registration.ITSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class HeatExchangerLogic implements IMultiblockLogic<HeatExchangerLogic.State>, IServerTickableComponent<HeatExchangerLogic.State>, IClientTickableComponent<HeatExchangerLogic.State>, ITPressurizedFluidOutput<HeatExchangerLogic.State> {
    public static final List<BlockPos> FLUID_INPUT_POIS;
    public static final BlockPos REDSTONE_POI = getPosList("redstone0").get(0);

    private static final List<BlockPos> FLUID_INPUT_0_POI = getPosList("fluid_input0");
    private static final List<BlockPos> FLUID_INPUT_1_POI = getPosList("fluid_input1");
    private static final List<BlockPos> FLUID_OUTPUT_0_POI = getPosList("fluid_output0");
    private static final List<BlockPos> FLUID_OUTPUT_1_POI = getPosList("fluid_output1");
    private static final List<BlockPos> ENERGY_INPUT_POI = getPosList("energy_input0");
    private static final List<BlockPos> SOUND_POI = getPosList("sound");

    private static final RelativeBlockFace FLUID_INPUT_0_FACING = getFacing("fluid_input0");
    private static final RelativeBlockFace FLUID_INPUT_1_FACING = getFacing("fluid_input1");
    private static final RelativeBlockFace FLUID_OUTPUT_0_FACING = getFacing("fluid_output0");
    private static final RelativeBlockFace FLUID_OUTPUT_1_FACING = getFacing("fluid_output1");
    private static final RelativeBlockFace ENERGY_INPUT_FACING = getFacing("energy_input0");

    static {
        ImmutableList.Builder<BlockPos> builder = ImmutableList.builder();
        builder.addAll(FLUID_INPUT_0_POI);
        builder.addAll(FLUID_INPUT_1_POI);
        FLUID_INPUT_POIS = builder.build();
    }

    private static List<BlockPos> getPosList(String name) { return Arrays.stream(HeatExchangerShape.DATA.pointsOfInterest).filter(poi -> poi.name.equals(name)).map(poi -> new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2])).collect(ImmutableList.toImmutableList()); }

    private static RelativeBlockFace getFacing(String name) {
        List<RelativeBlockFace> facings = Arrays.stream(HeatExchangerShape.DATA.pointsOfInterest).filter(poi -> poi.name.equals(name)).flatMap(poi -> poi.relativeFaces.stream()).distinct().toList();
        if (facings.size() != 1) { throw new RuntimeException("Inconsistent facings for POI: " + name); }
        return facings.get(0);
    }

    private static final int INPUT_TANK_SIZE = 10000;
    private static final int OUTPUT_TANK_SIZE = 10000;
    private static final int ENERGY_CAPACITY = 2048;
    private static final int ENERGY_MAX_IO = 1024;

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        if (SOUND_POI.isEmpty()) { return; }
        BlockPos soundBlockPos = SOUND_POI.get(0);
        Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(soundBlockPos.getX() + 0.5, soundBlockPos.getY() + 0.5, soundBlockPos.getZ() + 0.5));
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) { return; }
        float distSq = (float) player.distanceToSqr(soundPos);
        float attenuation = Math.max(distSq / 32f, 1f);
        float vol = 1f / attenuation;
        if (state.active && vol > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
            state.isSoundPlaying = ITSound.startSound(
                    () -> state.active, ctx.isValid(), soundPos, ITSounds.heatExchanger,
                    () -> {
                        LocalPlayer p = Minecraft.getInstance().player;
                        if (p == null) { return 0f; }
                        float a = (float) Math.max(p.distanceToSqr(soundPos) / 32f, 1f);
                        return 1f / a;
                    },
                    () -> 1f
            );
        }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        final Level level = ctx.getLevel().getRawLevel();

        state.energy.updateAverage();

        int prevEnergy = state.energy.getEnergyStored();
        CompoundTag prevTanksNBT = state.tanks.toNBT();

        boolean wasActive = state.active;
        state.active = state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx));

        HeatExchangerRecipe recipe = HeatExchangerRecipe.findRecipe(level, state.tanks.input0.getFluid(), state.tanks.input1.getFluid());
        tryEnqueueProcess(state, level, recipe);

        pumpOutputs(ctx);

        boolean activeChanged = wasActive != state.active;
        int currentEnergy = state.energy.getEnergyStored();
        boolean energyChanged = prevEnergy != currentEnergy;
        CompoundTag currentTanksNBT = state.tanks.toNBT();
        boolean tanksChanged = !prevTanksNBT.equals(currentTanksNBT);
        boolean update = activeChanged || energyChanged || tanksChanged;
        if (update) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    private void tryEnqueueProcess(State state, Level level, HeatExchangerRecipe recipe) {
        if (state.processor.getQueueSize() >= state.processor.getMaxQueueSize()) { return; }
        if (recipe == null) { return; }
        int need0 = recipe.input0.getAmount();
        int need1 = recipe.input1 != null ? recipe.input1.getAmount() : 0;
        if (state.tanks.input0.getFluidAmount() < need0 || state.tanks.input1.getFluidAmount() < need1) { return; }
        int space0 = state.tanks.output0.getCapacity() - state.tanks.output0.getFluidAmount();
        int space1 = recipe.output1 != null ? state.tanks.output1.getCapacity() - state.tanks.output1.getFluidAmount() : state.tanks.output1.getCapacity();
        if (space0 < recipe.output0.getAmount() || space1 < (recipe.output1 != null ? recipe.output1.getAmount() : 0)) { return; }
        HeatExchangerProcess process = new HeatExchangerProcess(recipe);
        state.processor.addProcessToQueue(process, level, false);
    }

    @Override public List<BlockPos> getOutputPositions() { return ImmutableList.of(FLUID_OUTPUT_0_POI.get(0), FLUID_OUTPUT_1_POI.get(0)); }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(FLUID_OUTPUT_0_FACING); }

    @Override public List<ITMarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output0, state.tanks.output1); }

    @Override public List<CapabilityReference<IFluidHandler>> getFluidOutputs(State state) { return ImmutableList.of(state.fluidOutput[0], state.fluidOutput[1]); }

    public boolean pumpOutputs(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        List<ITMarkableFluidTank> outputTanks = getOutputTanks(state);
        List<CapabilityReference<IFluidHandler>> fluidOutputs = getFluidOutputs(state);
        for (int i = 0; i < outputTanks.size(); i++) {
            ITMarkableFluidTank tank = outputTanks.get(i);
            if (tank.getFluidAmount() == 0) { continue; }
            IFluidHandler handler = fluidOutputs.get(i).get();
            FluidStack out = tank.getFluid();
            int accepted = handler.fill(out, FluidAction.SIMULATE);
            if (accepted <= 0) { continue; }
            FluidStack drainedStack = out.copy();
            drainedStack.setAmount(accepted);
            int drained = handler.fill(drainedStack, FluidAction.EXECUTE);
            tank.drain(drained, FluidAction.EXECUTE);
        }
        return false;
    }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        BlockPos localPos = position.posInMultiblock();
        RelativeBlockFace side = position.side();
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (FLUID_INPUT_0_POI.contains(localPos) && (side == null || side == FLUID_INPUT_0_FACING)) { return ctx.getState().inputCap[0].cast(ctx); }
            if (FLUID_INPUT_1_POI.contains(localPos) && (side == null || side == FLUID_INPUT_1_FACING)) { return ctx.getState().inputCap[1].cast(ctx); }
            if (FLUID_OUTPUT_0_POI.contains(localPos) && (side == null || side == FLUID_OUTPUT_0_FACING)) { return ctx.getState().outputCap[0].cast(ctx); }
            if (FLUID_OUTPUT_1_POI.contains(localPos) && (side == null || side == FLUID_OUTPUT_1_FACING)) { return ctx.getState().outputCap[1].cast(ctx); }
        } else if (cap == ForgeCapabilities.ENERGY) {
            if (ENERGY_INPUT_POI.contains(localPos) && (side == null || side == ENERGY_INPUT_FACING)) { return ctx.getState().energyCap.cast(ctx); }
        }
        return LazyOptional.empty();
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) {}

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return HeatExchangerShape.GETTER; }

    public static class State implements IMultiblockState, ITDisplayContext, ProcessContext.ProcessContextInMachine<HeatExchangerRecipe> {
        public final HeatExchangerTanks tanks;

        @SuppressWarnings("unchecked")
        public final StoredCapability<IFluidHandler>[] inputCap = new StoredCapability[2];

        @SuppressWarnings("unchecked")
        public final StoredCapability<IFluidHandler>[] outputCap = new StoredCapability[2];

        public final StoredCapability<IEnergyStorage> energyCap;

        @SuppressWarnings("unchecked")
        public final CapabilityReference<IFluidHandler>[] fluidOutput = new CapabilityReference[2];

        public AveragingEnergyStorage energy;
        public boolean active = false;
        public RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final MultiblockProcessor.InMachineProcessor<HeatExchangerRecipe> processor;
        public BooleanSupplier isSoundPlaying = () -> false;

        private static final IItemHandlerModifiable EMPTY_INVENTORY = new IItemHandlerModifiable() {
            @Override public int getSlots() { return 0; }
            @Override @NotNull public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
            @Override @NotNull public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) { return stack; }
            @Override @NotNull public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
            @Override public int getSlotLimit(int slot) { return 0; }
            @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return false; }
            @Override public void setStackInSlot(int slot, @NotNull ItemStack stack) {}
        };

        public State(IInitialMultiblockContext<State> ctx) {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            final Runnable sync = ctx.getSyncRunnable();
            final Runnable onChanged = () -> { markDirty.run(); sync.run(); };
            tanks = new HeatExchangerTanks(v -> onChanged.run());
            energy = new SyncEnergyStorage(ENERGY_CAPACITY, ENERGY_MAX_IO, onChanged);
            inputCap[0] = new StoredCapability<>(new ITArrayFluidHandler(tanks.input0, false, true, onChanged));
            inputCap[1] = new StoredCapability<>(new ITArrayFluidHandler(tanks.input1, false, true, onChanged));
            outputCap[0] = new StoredCapability<>(new ITArrayFluidHandler(tanks.output0, true, false, onChanged));
            outputCap[1] = new StoredCapability<>(new ITArrayFluidHandler(tanks.output1, true, false, onChanged));
            energyCap = new StoredCapability<>(energy);
            if (!FLUID_OUTPUT_0_POI.isEmpty()) {
                MultiblockFace outputMBFace = new MultiblockFace(FLUID_OUTPUT_0_FACING, FLUID_OUTPUT_0_POI.get(0));
                CapabilityPosition opposingCP = CapabilityPosition.opposing(outputMBFace);
                MultiblockFace opposingMBFace = new MultiblockFace(opposingCP.side(), opposingCP.posInMultiblock());
                fluidOutput[0] = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, opposingMBFace);
            }
            if (!FLUID_OUTPUT_1_POI.isEmpty()) {
                MultiblockFace outputMBFace = new MultiblockFace(FLUID_OUTPUT_1_FACING, FLUID_OUTPUT_1_POI.get(0));
                CapabilityPosition opposingCP = CapabilityPosition.opposing(outputMBFace);
                MultiblockFace opposingMBFace = new MultiblockFace(opposingCP.side(), opposingCP.posInMultiblock());
                fluidOutput[1] = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, opposingMBFace);
            }
            processor = new MultiblockProcessor.InMachineProcessor<>(1, 0f, 1, markDirty, HeatExchangerRecipe.RECIPES::getById);
        }

        @Override public void writeSaveNBT(CompoundTag nbt) {
            nbt.put("tanks", tanks.toNBT());
            nbt.put("energy", energy.serializeNBT());
            nbt.put("processor", processor.toNBT());
            rsState.writeSaveNBT(nbt);
        }

        @Override public void readSaveNBT(CompoundTag nbt) {
            tanks.readNBT(nbt.getCompound("tanks"));
            energy.deserializeNBT(nbt.getCompound("energy"));
            processor.fromNBT(nbt.getList("processor", Tag.TAG_COMPOUND), HeatExchangerProcess::new);
            rsState.readSaveNBT(nbt);
        }

        @Override public void writeSyncNBT(CompoundTag nbt) {
            CompoundTag display = new CompoundTag();
            writeDisplaySyncNBT(display);
            nbt.put("display", display);
        }

        @Override public void readSyncNBT(CompoundTag nbt) { if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display")); } }

        @Override public boolean isActive() { return active; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input0, tanks.input1, tanks.output0, tanks.output1}; }

        @Override public List<AveragingEnergyStorage> getEnergies() { return List.of(energy); }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT());
            nbt.put("energy", energy.serializeNBT());
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt) {
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"));
            if (energy == null) { energy = new SyncEnergyStorage(ENERGY_CAPACITY, ENERGY_MAX_IO, () -> {}); }
            energy.deserializeNBT(nbt.get("energy"));
        }

        @Override public AveragingEnergyStorage getEnergy() { return energy; }

        @Override public IItemHandlerModifiable getInventory() { return EMPTY_INVENTORY; }

        @Override public int[] getOutputSlots() { return new int[0]; }

        @Override public int[] getOutputTanks() { return new int[]{2, 3}; }
    }

    private static class SyncEnergyStorage extends AveragingEnergyStorage {
        private final Runnable onChanged;

        public SyncEnergyStorage(int capacity, int maxIO, Runnable onChanged) {
            super(capacity);
            this.maxReceive = maxIO;
            this.maxExtract = maxIO;
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

    public record HeatExchangerTanks(ITMarkableFluidTank input0, ITMarkableFluidTank input1, ITMarkableFluidTank output0, ITMarkableFluidTank output1) {
        public HeatExchangerTanks(Consumer<Void> markDirty) {
            this(new ITMarkableFluidTank(INPUT_TANK_SIZE, markDirty), new ITMarkableFluidTank(INPUT_TANK_SIZE, markDirty),
                    new ITMarkableFluidTank(OUTPUT_TANK_SIZE, markDirty), new ITMarkableFluidTank(OUTPUT_TANK_SIZE, markDirty));
        }

        public static HeatExchangerTanks makeClient() { return new HeatExchangerTanks(v -> {}); }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("input0", this.input0.writeToNBT(new CompoundTag()));
            tag.put("input1", this.input1.writeToNBT(new CompoundTag()));
            tag.put("output0", this.output0.writeToNBT(new CompoundTag()));
            tag.put("output1", this.output1.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) {
            this.input0.readFromNBT(tag.getCompound("input0"));
            this.input1.readFromNBT(tag.getCompound("input1"));
            this.output0.readFromNBT(tag.getCompound("output0"));
            this.output1.readFromNBT(tag.getCompound("output1"));
        }
    }
}
