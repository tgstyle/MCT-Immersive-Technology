package mctmods.immersivetechnology.common.blocks.multiblocks.logic;

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
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.process.DistillerProcess;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.DistillerRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.FullblockShape;
import mctmods.immersivetechnology.core.lib.ITMultiblockSound;
import mctmods.immersivetechnology.core.registration.ITSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemHandlerHelper;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ITDistillerLogic implements IMultiblockLogic<ITDistillerLogic.State>, IServerTickableComponent<ITDistillerLogic.State>, IClientTickableComponent<ITDistillerLogic.State> {
    public static final BlockPos REDSTONE_POS = new BlockPos(2, 1, 0);

    private static final CapabilityPosition FLUID_OUTPUT_CAP = new CapabilityPosition(2, 0, 1, RelativeBlockFace.RIGHT);
    private static final Set<CapabilityPosition> FLUID_INPUT_CAPS = Set.of( new CapabilityPosition(0, 0, 1, RelativeBlockFace.LEFT) );
    private static final Set<BlockPos> FLUID_INPUTS = FLUID_INPUT_CAPS.stream().map(CapabilityPosition::posInMultiblock).collect(Collectors.toSet());
    private static final Set<CapabilityPosition> ENERGY_INPUTS = Set.of(new CapabilityPosition(2,1, 2, RelativeBlockFace.UP));

    public static final int TANK_CAPACITY = 24* FluidType.BUCKET_VOLUME;

    public static final int SLOT_WATER_EMPTY_IN = 0;
    public static final int SLOT_WATER_OUT = 1;
    public static final int SLOT_WATER_EMPTY_OUT = 2;
    public static final int SLOT_WATER_IN = 3;

    @Override
    public State createInitialState(IInitialMultiblockContext<ITDistillerLogic.State> iInitialMultiblockContext) { return new State(iInitialMultiblockContext); }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return FullblockShape.GETTER; }

    @Override
    public<T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        final State state = ctx.getState();
        if (cap == ForgeCapabilities.ENERGY) { if ((position.side()==null || ENERGY_INPUTS.contains(position))) return state.energyCap.cast(ctx); }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (FLUID_OUTPUT_CAP.equals(position)) { return state.outputCapSteam.cast(ctx); }
            else if (FLUID_INPUT_CAPS.contains(position)) { return state.inputCap.cast(ctx); }
        }
        return LazyOptional.empty();
    }

    @Override
    public void tickClient(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        if(!state.isSoundPlaying.getAsBoolean())
        {
            final Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(2.5, 1.5, 1.5));
            state.isSoundPlaying = ITMultiblockSound.startSound(
                    () -> state.active, ctx.isValid(), soundPos, ITSounds.distiller, () -> {
                        LocalPlayer player = Minecraft.getInstance().player;
                        if (player == null) { return 0f; }
                        float attenuation = (float) Math.max(player.distanceToSqr(soundPos) / 8, 1);
                        return attenuation;
                    },
                    () -> 1f
            );
        }
    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        final boolean wasActive = state.active;
        state.energy.receiveEnergy(1, false);
        state.active = state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx));
        DistillerRecipe recipe = DistillerRecipe.findRecipe(ctx.getLevel().getRawLevel(), state.tanks.waterInput.getFluid());

        if (wasActive!=state.active) { ctx.requestMasterBESync(); }
        tryEnqueueProcess(state, ctx.getLevel().getRawLevel(), recipe);
        boolean update = FluidUtils.multiblockFluidOutput(state.fluidOutput, state.tanks.output(), SLOT_WATER_EMPTY_IN, SLOT_WATER_OUT, state.inventory);
        FluidActionResult result = FluidUtil.tryEmptyContainer(state.inventory.getStackInSlot(SLOT_WATER_IN), state.tanks.waterInput, FluidType.BUCKET_VOLUME, null, true);
        if (result.isSuccess()) {
            ItemStack emptyContainer = result.getResult();
            if (!emptyContainer.isEmpty()) {
                if (state.inventory.getStackInSlot(SLOT_WATER_EMPTY_OUT).isEmpty()) { state.inventory.setStackInSlot(SLOT_WATER_EMPTY_OUT, emptyContainer.copy()); }
                else if (ItemHandlerHelper.canItemStacksStack(state.inventory.getStackInSlot(SLOT_WATER_EMPTY_OUT), emptyContainer)) { state.inventory.getStackInSlot(SLOT_WATER_EMPTY_OUT).grow(emptyContainer.getCount()); }
                state.inventory.getStackInSlot(SLOT_WATER_IN).shrink(1);
                if (state.inventory.getStackInSlot(SLOT_WATER_IN).isEmpty()) { state.inventory.setStackInSlot(SLOT_WATER_IN, ItemStack.EMPTY); }
                update = true;
            }
        }
        if (update) { ctx.markMasterDirty(); }
    }

    @Override
    public InteractionResult click(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) {
        if (isClient) { return InteractionResult.SUCCESS; }
        final State state = ctx.getState();
        IFluidHandler tank = null;
        if (FLUID_INPUTS.contains(posInMultiblock)) { tank = state.tanks.waterInput; }
        else if (FLUID_OUTPUT_CAP.posInMultiblock().equals(posInMultiblock)) { tank = state.tanks.output; }
        if (tank!=null) {
            FluidUtils.interactWithFluidHandler(player, hand, tank);
            ctx.markMasterDirty();
        }
        return InteractionResult.SUCCESS;
    }

    private void tryEnqueueProcess(State state, Level level, DistillerRecipe recipe) {
        if (state.processor.getQueueSize() >= state.processor.getMaxQueueSize()) { return; }
        final FluidStack leftInput = state.tanks.waterInput.getFluid();
        if (leftInput.isEmpty()) { return; }
        if (recipe==null) { return; }
        DistillerProcess process = new DistillerProcess(recipe);
        if (!leftInput.isEmpty()) { process.setInputTanks(0); }
        state.processor.addProcessToQueue(process, level, false);
    }

    public static class State implements IMultiblockState, ProcessContext.ProcessContextInMachine<DistillerRecipe> {
        public boolean active;
        private BooleanSupplier isSoundPlaying = () -> false;
        private final StoredCapability<IEnergyStorage> energyCap;
        private final StoredCapability<IFluidHandler> inputCap;
        private final StoredCapability<IFluidHandler> outputCapSteam;
        private final CapabilityReference<IFluidHandler> fluidOutput;
        private final SlotwiseItemHandler inventory;
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        private final DistillerTank tanks = new DistillerTank();
        private final IFluidTank[] tankArray = {tanks.waterInput, tanks.output};
        private final MultiblockProcessor.InMachineProcessor<DistillerRecipe> processor;

        AveragingEnergyStorage energy = new AveragingEnergyStorage(32000);

        public State(IInitialMultiblockContext<ITDistillerLogic.State> ctx) {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            this.inputCap = new StoredCapability<>(new ArrayFluidHandler(false, true, markDirty, this.tanks.waterInput));
            this.outputCapSteam = new StoredCapability<>(new ArrayFluidHandler(true, false, markDirty, this.tanks.output));
            this.energyCap = new StoredCapability<>(this.energy);
            this.processor = new MultiblockProcessor.InMachineProcessor<>(1, 0, 1, markDirty, DistillerRecipe.RECIPES::getById, DistillerProcess::new);

            inventory = new SlotwiseItemHandler(
                    List.of(
                            SlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            SlotwiseItemHandler.IOConstraint.OUTPUT,
                            SlotwiseItemHandler.IOConstraint.ANY_INPUT,
                            SlotwiseItemHandler.IOConstraint.OUTPUT
                    ),
                    ctx.getMarkDirtyRunnable()
            );
            this.fluidOutput = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, new MultiblockFace(FLUID_OUTPUT_CAP.side(), FLUID_OUTPUT_CAP.posInMultiblock().west()));
        }

        public DistillerTank getTanks() { return tanks; }

        @Override
        public void writeSaveNBT(CompoundTag nbt) {
            nbt.put("energy", energy.serializeNBT());
            nbt.put("tanks", this.tanks.toNBT());
            nbt.put("processor", processor.toNBT());
            nbt.put("inventory", inventory.serializeNBT());
        }

        @Override
        public void readSaveNBT(CompoundTag nbt) {
            energy.deserializeNBT(nbt.get("energy"));
            this.tanks.readNBT(nbt.getCompound("tanks"));
            this.processor.fromNBT(nbt.get("processor", DistillerProcess::new));
            this.inventory.deserializeNBT(nbt.getCompound("inventory"));
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt) { nbt.putBoolean("active", active); }

        @Override
        public void readSyncNBT(CompoundTag nbt) { active = nbt.getBoolean("active"); }

        @Override
        public AveragingEnergyStorage getEnergy() { return energy; }

        @Override
        public IFluidTank[] getInternalTanks() { return tankArray; }

        @Override
        public int[] getOutputTanks() { return new int[]{1}; }
    }

    public record DistillerTank(FluidTank output, FluidTank waterInput) {
        public DistillerTank() { this(new FluidTank(TANK_CAPACITY), new FluidTank(TANK_CAPACITY)); }

        public Tag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("waterIn", this.waterInput.writeToNBT(new CompoundTag()));
            tag.put("out", this.output.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) {
            this.waterInput.readFromNBT(tag.getCompound("waterIn"));
            this.output.readFromNBT(tag.getCompound("out"));
        }

        public int getCapacity() { return TANK_CAPACITY; }
    }
}
