package mctmods.immersivetechnology.common.blocks.multiblocks.logic;

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
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITWrappingItemHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.process.DistillerProcess;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.DistillerRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.FullblockShape;
import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
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
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class ITDistillerLogic implements IMultiblockLogic<ITDistillerLogic.State>, IServerTickableComponent<ITDistillerLogic.State>, IClientTickableComponent<ITDistillerLogic.State> {
    public static final int SLOT_INPUT_FILLED = 0;
    public static final int SLOT_INPUT_EMPTY = 1;
    public static final int SLOT_OUTPUT_EMPTY = 2;
    public static final int SLOT_OUTPUT_FILLED = 3;
    public static final int OUTPUT_SLOT = 4;

    private static final CapabilityPosition FLUID_POS1 = new CapabilityPosition(0, 0, 1, RelativeBlockFace.RIGHT);
    private static final CapabilityPosition FLUID_POS2 = new CapabilityPosition(2, 0, 1, RelativeBlockFace.LEFT);
    private static final Set<CapabilityPosition> ENERGY_POS = Set.of(new CapabilityPosition(0, 1, 2, RelativeBlockFace.UP));
    private static final MultiblockFace ITEM_OUTPUT_POS = new MultiblockFace(1, 0, -1, RelativeBlockFace.BACK);

    public static final BlockPos REDSTONE_POS = new BlockPos(2, 1, 2);

    public static final int TANK_CAPACITY = 24 * FluidType.BUCKET_VOLUME;

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
        state.active = state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx));
        DistillerRecipe recipe = DistillerRecipe.findRecipe(ctx.getLevel().getRawLevel(), state.tanks.input.getFluid());
        if (wasActive != state.active || recipe != null) ctx.requestMasterBESync();
        tryEnqueueProcess(state, ctx.getLevel().getRawLevel(), recipe);
        boolean update = false;
        ItemStack inputFilled = state.inventory.getStackInSlot(SLOT_INPUT_FILLED);
        if (!inputFilled.isEmpty() && Utils.isFluidRelatedItemStack(inputFilled)) {
            FluidActionResult result = FluidUtil.tryEmptyContainer(inputFilled, state.tanks.input, FluidType.BUCKET_VOLUME, null, false);
            if (result.isSuccess()) {
                ItemStack emptyContainer = result.getResult();
                ItemStack inputEmpty = state.inventory.getStackInSlot(SLOT_INPUT_EMPTY);
                if (inputEmpty.isEmpty() || (ItemHandlerHelper.canItemStacksStack(inputEmpty, emptyContainer) && inputEmpty.getCount() + 1 <= inputEmpty.getMaxStackSize())) {
                    FluidUtil.tryEmptyContainer(inputFilled, state.tanks.input, FluidType.BUCKET_VOLUME, null, true);
                    inputFilled.shrink(1);
                    state.inventory.setStackInSlot(SLOT_INPUT_FILLED, inputFilled);
                    if (inputEmpty.isEmpty()) { state.inventory.setStackInSlot(SLOT_INPUT_EMPTY, emptyContainer); }
                    else { inputEmpty.grow(1); state.inventory.setStackInSlot(SLOT_INPUT_EMPTY, inputEmpty); }
                    update = true;
                }
            }
        }
        ItemStack outputEmpty = state.inventory.getStackInSlot(SLOT_OUTPUT_EMPTY);
        if (!outputEmpty.isEmpty() && Utils.isFluidRelatedItemStack(outputEmpty)) {
            FluidActionResult fillResult = FluidUtil.tryFillContainer(outputEmpty, state.tanks.output, FluidType.BUCKET_VOLUME, null, false);
            if (fillResult.isSuccess()) {
                ItemStack filledContainer = fillResult.getResult();
                ItemStack outputFilled = state.inventory.getStackInSlot(SLOT_OUTPUT_FILLED);
                if (outputFilled.isEmpty() || (ItemHandlerHelper.canItemStacksStack(outputFilled, filledContainer) && outputFilled.getCount() + 1 <= outputFilled.getMaxStackSize())) {
                    FluidUtil.tryFillContainer(outputEmpty, state.tanks.output, FluidType.BUCKET_VOLUME, null, true);
                    outputEmpty.shrink(1);
                    state.inventory.setStackInSlot(SLOT_OUTPUT_EMPTY, outputEmpty);
                    if (outputFilled.isEmpty()) { state.inventory.setStackInSlot(SLOT_OUTPUT_FILLED, filledContainer); }
                    else { outputFilled.grow(1); state.inventory.setStackInSlot(SLOT_OUTPUT_FILLED, outputFilled); }
                    update = true;
                }
            }
        }
        if (state.fluidOutput.isPresent()) {
            IFluidHandler outputHandler = state.fluidOutput.get();
            FluidStack fs = state.tanks.output.getFluid();
            if (fs.getAmount() > 0) {
                fs = fs.copy();
                int accepted = outputHandler.fill(fs, FluidAction.SIMULATE);
                if (accepted > 0) {
                    int drained = outputHandler.fill(Utils.copyFluidStackWithAmount(fs, accepted, false), FluidAction.EXECUTE);
                    state.tanks.output.drain(drained, FluidAction.EXECUTE);
                    update = true;
                }
            }
        }
        final IItemHandlerModifiable inventory = state.inventory;
        ItemStack drainedContainer = inventory.getStackInSlot(SLOT_INPUT_EMPTY);
        if (!drainedContainer.isEmpty()) {
            int origCount = drainedContainer.getCount();
            drainedContainer = Utils.insertStackIntoInventory(state.outputRef, drainedContainer, false);
            if (drainedContainer.getCount() < origCount) update = true;
            inventory.setStackInSlot(SLOT_INPUT_EMPTY, drainedContainer);
        }
        ItemStack filledContainer = inventory.getStackInSlot(SLOT_OUTPUT_FILLED);
        if (!filledContainer.isEmpty()) {
            int origCount = filledContainer.getCount();
            filledContainer = Utils.insertStackIntoInventory(state.outputRef, filledContainer, false);
            if (filledContainer.getCount() < origCount) update = true;
            inventory.setStackInSlot(SLOT_OUTPUT_FILLED, filledContainer);
        }
        ItemStack itemOutput = inventory.getStackInSlot(OUTPUT_SLOT);
        if (!itemOutput.isEmpty()) {
            int origCount = itemOutput.getCount();
            itemOutput = Utils.insertStackIntoInventory(state.outputRef, itemOutput, false);
            if (itemOutput.getCount() < origCount) update = true;
            inventory.setStackInSlot(OUTPUT_SLOT, itemOutput);
        }
        if (update) { ctx.markMasterDirty(); ctx.requestMasterBESync(); }
    }

    private void tryEnqueueProcess(State state, Level level, DistillerRecipe recipe) {
        if (state.processor.getQueueSize() >= state.processor.getMaxQueueSize()) return;
        if (recipe == null) return;
        FluidStack inputFluid = state.tanks.input.getFluid();
        if (inputFluid.getAmount() < recipe.water.getAmount()) return;
        FluidStack outputFluid = recipe.fluidOutput;
        if (outputFluid != null && !outputFluid.isEmpty() && state.tanks.output.getFluidAmount() + outputFluid.getAmount() > state.tanks.output.getCapacity()) return;
        ItemStack itemOutput = recipe.itemOutput;
        if (!itemOutput.isEmpty()) {
            ItemStack currentOutput = state.inventory.getStackInSlot(OUTPUT_SLOT);
            if (!currentOutput.isEmpty() && (!ItemHandlerHelper.canItemStacksStack(currentOutput, itemOutput) || currentOutput.getCount() + itemOutput.getCount() > currentOutput.getMaxStackSize())) return;
        }
        DistillerProcess process = new DistillerProcess(recipe);
        process.setInputTanks(0);
        state.processor.addProcessToQueue(process, level, false);
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        final State state = ctx.getState();
        if (cap == ForgeCapabilities.ENERGY) {
            if (position.side() == null || ENERGY_POS.contains(position)) { return state.energyCap.cast(ctx); }
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (FLUID_POS1.equals(position)) { return state.inputCap.cast(ctx); }
            else if (FLUID_POS2.equals(position)) { return state.outputCapSteam.cast(ctx); }
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (ITEM_OUTPUT_POS.posInMultiblock().equals(position.posInMultiblock())) { return state.itemOutputCap.cast(ctx); }
        }
        return LazyOptional.empty();
    }

    @Override
    public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return FullblockShape.GETTER; }

    @Override
    public InteractionResult click(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) {
        if (isClient) return InteractionResult.SUCCESS;
        final State state = ctx.getState();
        IFluidHandler tank = null;
        if (FLUID_POS1.posInMultiblock().equals(posInMultiblock)) tank = state.tanks.input;
        else if (FLUID_POS2.posInMultiblock().equals(posInMultiblock)) tank = state.tanks.output;
        if (tank != null) { FluidUtil.interactWithFluidHandler(player, hand, tank); ctx.markMasterDirty(); ctx.requestMasterBESync(); }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void dropExtraItems(State state, Consumer<ItemStack> drop) { MBInventoryUtils.dropItems(state.inventory, drop); }

    public static class State implements IMultiblockState, ProcessContext.ProcessContextInMachine<DistillerRecipe> {
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final DistillerTank tanks;
        public final StoredCapability<IEnergyStorage> energyCap;
        public final StoredCapability<IFluidHandler> inputCap;
        public final StoredCapability<IFluidHandler> outputCapSteam;
        public final CapabilityReference<IFluidHandler> fluidOutput;
        public final StoredCapability<IItemHandler> itemOutputCap;
        public final CapabilityReference<IItemHandler> outputRef;
        public final ITSlotwiseItemHandler inventory;
        private final IFluidTank[] tankArray;
        private final MultiblockProcessor.InMachineProcessor<DistillerRecipe> processor;
        public AveragingEnergyStorage energy = new AveragingEnergyStorage(32000);
        public boolean active;
        public BooleanSupplier isSoundPlaying = () -> false;

        public State(IInitialMultiblockContext<State> ctx) {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            final Runnable onChanged = () -> { markDirty.run(); ctx.getSyncRunnable().run(); };
            this.tanks = new DistillerTank(v -> onChanged.run());
            this.tankArray = new IFluidTank[]{tanks.input, tanks.output};
            this.inputCap = new StoredCapability<>(new ITArrayFluidHandler(tanks.input, false, true, onChanged));
            this.outputCapSteam = new StoredCapability<>(new ITArrayFluidHandler(tanks.output, true, false, onChanged));
            this.energyCap = new StoredCapability<>(this.energy);
            this.processor = new MultiblockProcessor.InMachineProcessor<>(1, 0f, 1, markDirty, DistillerRecipe.RECIPES::getById);
            inventory = new ITSlotwiseItemHandler(
                    List.of(
                            ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            ITSlotwiseItemHandler.IOConstraint.OUTPUT,
                            ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            ITSlotwiseItemHandler.IOConstraint.OUTPUT,
                            ITSlotwiseItemHandler.IOConstraint.OUTPUT
                    ),
                    onChanged
            );
            CapabilityPosition opposingPos = CapabilityPosition.opposing(new MultiblockFace(FLUID_POS2.side(), FLUID_POS2.posInMultiblock()));
            this.fluidOutput = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, new MultiblockFace(opposingPos.side(), opposingPos.posInMultiblock()));
            this.itemOutputCap = new StoredCapability<>(
                    new ITWrappingItemHandler(
                            inventory,
                            false,
                            true,
                            List.of(
                                    new ITWrappingItemHandler.IntRange(SLOT_INPUT_EMPTY, SLOT_INPUT_EMPTY + 1),
                                    new ITWrappingItemHandler.IntRange(SLOT_OUTPUT_FILLED, SLOT_OUTPUT_FILLED + 1),
                                    new ITWrappingItemHandler.IntRange(OUTPUT_SLOT, OUTPUT_SLOT + 1)
                            )
                    )
            );
            this.outputRef = ctx.getCapabilityAt(ForgeCapabilities.ITEM_HANDLER, ITEM_OUTPUT_POS);
        }

        public ITSlotwiseItemHandler getInventory() { return inventory; }

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
        public void writeSyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", this.tanks.toNBT());
        }

        @Override
        public void readSyncNBT(CompoundTag nbt) {
            active = nbt.getBoolean("active");
            this.tanks.readNBT(nbt.getCompound("tanks"));
        }

        @Override
        public AveragingEnergyStorage getEnergy() { return energy; }

        @Override
        public IFluidTank[] getInternalTanks() { return tankArray; }

        @Override
        public int[] getOutputTanks() { return new int[]{1}; }

        @Override
        public int[] getOutputSlots() { return new int[]{OUTPUT_SLOT}; }
    }

    public record DistillerTank(ITMarkableFluidTank input, ITMarkableFluidTank output) {
        public DistillerTank(Consumer<Void> markDirty) {
            this(new ITMarkableFluidTank(TANK_CAPACITY, markDirty), new ITMarkableFluidTank(TANK_CAPACITY, markDirty));
        }

        public static DistillerTank makeClient() { return new DistillerTank(v -> {}); }

        public Tag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("input", this.input.writeToNBT(new CompoundTag()));
            tag.put("output", this.output.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) {
            this.input.readFromNBT(tag.getCompound("input"));
            this.output.readFromNBT(tag.getCompound("output"));
        }

        public int getCapacity() { return TANK_CAPACITY; }
    }
}