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
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler;
import blusunrize.immersiveengineering.common.util.inventory.WrappingItemHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.process.DistillerProcess;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.DistillerRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.FullblockShape;
import mctmods.immersivetechnology.core.lib.ITMultiblockSound;
import mctmods.immersivetechnology.core.lib.ITLib;
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
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ITDistillerLogic implements IMultiblockLogic<ITDistillerLogic.State>, IServerTickableComponent<ITDistillerLogic.State>, IClientTickableComponent<ITDistillerLogic.State> {
    public static final BlockPos REDSTONE_POS = new BlockPos(2, 1, 0);
    private static final CapabilityPosition FLUID_INPUT_CAP = new CapabilityPosition(0, 0, 1, RelativeBlockFace.RIGHT);
    private static final CapabilityPosition FLUID_OUTPUT_CAP = new CapabilityPosition(2, 0, 1, RelativeBlockFace.LEFT);
    private static final Set<CapabilityPosition> FLUID_INPUT_CAPS = Set.of(FLUID_INPUT_CAP);
    private static final Set<BlockPos> FLUID_INPUTS = FLUID_INPUT_CAPS.stream().map(CapabilityPosition::posInMultiblock).collect(Collectors.toSet());
    private static final Set<CapabilityPosition> ENERGY_INPUTS = Set.of(new CapabilityPosition(2, 1, 2, RelativeBlockFace.UP));
    private static final MultiblockFace ITEM_OUTPUT_POS = new MultiblockFace(1, 0, 0, RelativeBlockFace.BACK);
    public static final int TANK_CAPACITY = 24 * FluidType.BUCKET_VOLUME;
    public static final int SLOT_INPUT_FILLED = 0;
    public static final int SLOT_INPUT_EMPTY = 1;
    public static final int SLOT_OUTPUT_EMPTY = 2;
    public static final int SLOT_OUTPUT_FILLED = 3;
    public static final int OUTPUT_SLOT = 4;

    @Override
    public State createInitialState(IInitialMultiblockContext<ITDistillerLogic.State> iInitialMultiblockContext) { return new State(iInitialMultiblockContext); }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return FullblockShape.GETTER; }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        final State state = ctx.getState();
        if (cap == ForgeCapabilities.ENERGY) { if ((position.side() == null || ENERGY_INPUTS.contains(position))) return state.energyCap.cast(ctx); }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (FLUID_OUTPUT_CAP.equals(position)) { return state.outputCapSteam.cast(ctx); }
            else if (FLUID_INPUT_CAPS.contains(position)) { return state.inputCap.cast(ctx); }
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (ITEM_OUTPUT_POS.posInMultiblock().equals(position.posInMultiblock())) { return state.itemOutputCap.cast(ctx); }
        }
        return LazyOptional.empty();
    }

    @Override
    public void tickClient(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        if (!state.isSoundPlaying.getAsBoolean()) {
            final Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(1, 1, 1));
            state.isSoundPlaying = ITMultiblockSound.startSound(
                    () -> state.active,
                    ctx.isValid(),
                    soundPos,
                    ITSounds.distiller,
                    () -> {
                        LocalPlayer player = Minecraft.getInstance().player;
                        if (player == null) return 0f;
                        return (float) Math.max(player.distanceToSqr(soundPos) / 8, 1);
                    },
                    () -> 1f
            );
        }
    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        final boolean wasActive = state.active;
        Level world = ctx.getLevel().getRawLevel();
        BlockPos masterPos = ctx.getLevel().toAbsolute(BlockPos.ZERO);
        state.active = state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx));
        DistillerRecipe recipe = DistillerRecipe.findRecipe(ctx.getLevel().getRawLevel(), state.tanks.input.getFluid());
        if (wasActive != state.active) { ctx.requestMasterBESync(); }
        tryEnqueueProcess(state, ctx.getLevel().getRawLevel(), recipe);
        boolean update = FluidUtils.multiblockFluidOutput(state.fluidOutput, state.tanks.output, SLOT_OUTPUT_EMPTY, SLOT_OUTPUT_FILLED, state.inventory);
        FluidActionResult result = FluidUtil.tryEmptyContainer(state.inventory.getStackInSlot(SLOT_INPUT_FILLED), state.tanks.input, FluidType.BUCKET_VOLUME, null, true);
        if (result.isSuccess()) {
            ItemStack emptyContainer = result.getResult();
            if (!emptyContainer.isEmpty()) {
                if (state.inventory.getStackInSlot(SLOT_INPUT_EMPTY).isEmpty()) { state.inventory.setStackInSlot(SLOT_INPUT_EMPTY, emptyContainer.copy()); }
                else if (ItemHandlerHelper.canItemStacksStack(state.inventory.getStackInSlot(SLOT_INPUT_EMPTY), emptyContainer)) { state.inventory.getStackInSlot(SLOT_INPUT_EMPTY).grow(emptyContainer.getCount()); }
                state.inventory.getStackInSlot(SLOT_INPUT_FILLED).shrink(1);
                if (state.inventory.getStackInSlot(SLOT_INPUT_FILLED).isEmpty()) { state.inventory.setStackInSlot(SLOT_INPUT_FILLED, ItemStack.EMPTY); }
                update = true;
            }
        }
        final IItemHandlerModifiable inventory = state.getInventory();
        ItemStack stack = inventory.getStackInSlot(SLOT_OUTPUT_FILLED);
        if (!stack.isEmpty()) {
            stack = Utils.insertStackIntoInventory(state.outputRef, stack, false);
            inventory.setStackInSlot(SLOT_OUTPUT_FILLED, stack);
        }
        ItemStack itemStack = inventory.getStackInSlot(OUTPUT_SLOT);
        if (!itemStack.isEmpty()) {
            itemStack = Utils.insertStackIntoInventory(state.outputRef, itemStack, false);
            inventory.setStackInSlot(OUTPUT_SLOT, itemStack);
        }
        if (update) { ctx.markMasterDirty(); }
    }

    @Override
    public InteractionResult click(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) {
        if (isClient) { return InteractionResult.SUCCESS; }
        final State state = ctx.getState();
        IFluidHandler tank = null;
        if (FLUID_INPUTS.contains(posInMultiblock)) { tank = state.tanks.input; }
        else if (FLUID_OUTPUT_CAP.posInMultiblock().equals(posInMultiblock)) { tank = state.tanks.output; }
        if (tank != null) {
            FluidUtils.interactWithFluidHandler(player, hand, tank);
            ctx.markMasterDirty();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void dropExtraItems(State state, Consumer<ItemStack> drop) { MBInventoryUtils.dropItems(state.getInventory(), drop); }

    private void tryEnqueueProcess(State state, Level level, DistillerRecipe recipe) {
        FluidStack inputFluid = state.tanks.input.getFluid();
        if (state.processor.getQueueSize() >= state.processor.getMaxQueueSize()) return;
        if (inputFluid.isEmpty()) return;
        if (recipe == null) return;
        DistillerProcess process = new DistillerProcess(recipe);
        process.setInputTanks(0);
    }

    public static class State implements IMultiblockState, ProcessContext.ProcessContextInMachine<DistillerRecipe> {
        public boolean active;
        public BooleanSupplier isSoundPlaying = () -> false;
        private final StoredCapability<IEnergyStorage> energyCap;
        private final StoredCapability<IFluidHandler> inputCap;
        private final StoredCapability<IFluidHandler> outputCapSteam;
        private final CapabilityReference<IFluidHandler> fluidOutput;
        private final StoredCapability<IItemHandler> itemOutputCap;
        private final CapabilityReference<IItemHandler> outputRef;
        private final SlotwiseItemHandler inventory;
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        private final DistillerTank tanks = new DistillerTank();
        private final IFluidTank[] tankArray = {tanks.input, tanks.output};
        private final MultiblockProcessor.InMachineProcessor<DistillerRecipe> processor;
        public AveragingEnergyStorage energy = new AveragingEnergyStorage(32000);

        public State(IInitialMultiblockContext<ITDistillerLogic.State> ctx) {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            this.inputCap = new StoredCapability<>(new ArrayFluidHandler(true, false, markDirty, this.tanks.output));
            this.outputCapSteam = new StoredCapability<>(new ArrayFluidHandler(false, true, markDirty, this.tanks.input));
            this.energyCap = new StoredCapability<>(this.energy);
            this.processor = new MultiblockProcessor.InMachineProcessor<>(1, 0f, 1, markDirty, DistillerRecipe.RECIPES::getById);
            inventory = new SlotwiseItemHandler(
                    List.of(
                            SlotwiseItemHandler.IOConstraint.NO_CONSTRAINT,
                            SlotwiseItemHandler.IOConstraint.OUTPUT,
                            SlotwiseItemHandler.IOConstraint.NO_CONSTRAINT,
                            SlotwiseItemHandler.IOConstraint.OUTPUT,
                            SlotwiseItemHandler.IOConstraint.NO_CONSTRAINT
                    ),
                    ctx.getMarkDirtyRunnable()
            );
            this.fluidOutput = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, new MultiblockFace(FLUID_OUTPUT_CAP.side(), FLUID_OUTPUT_CAP.posInMultiblock().west()));
            this.itemOutputCap = new StoredCapability<>(new WrappingItemHandler(
                    getInventory(), false, true, new WrappingItemHandler.IntRange(OUTPUT_SLOT, OUTPUT_SLOT + 1)
            ));
            this.outputRef = ctx.getCapabilityAt(ForgeCapabilities.ITEM_HANDLER, ITEM_OUTPUT_POS);
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
            this.processor.fromNBT(nbt.getList("processor", Tag.TAG_COMPOUND), DistillerProcess::new);
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

        @Override
        public int[] getOutputSlots() { return new int[]{OUTPUT_SLOT}; }
    }

    public record DistillerTank(FluidTank input, FluidTank output) {
        public DistillerTank() { this(new FluidTank(TANK_CAPACITY), new FluidTank(TANK_CAPACITY)); }

        public Tag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("in", this.input.writeToNBT(new CompoundTag()));
            tag.put("out", this.output.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) {
            this.input.readFromNBT(tag.getCompound("in"));
            this.output.readFromNBT(tag.getCompound("out"));
        }

        public int getCapacity() { return TANK_CAPACITY; }
    }
}
