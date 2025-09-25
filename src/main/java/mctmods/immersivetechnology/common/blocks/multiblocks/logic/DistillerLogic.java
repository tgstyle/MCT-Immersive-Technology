package mctmods.immersivetechnology.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
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
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITMultiBlockInventoryUtils;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITWrappingItemHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.process.DistillerProcess;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.DistillerRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.DistillerShape;
import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.core.lib.ITSound;
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
import net.minecraftforge.fluids.IFluidTank;
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

public class DistillerLogic implements IMultiblockLogic<DistillerLogic.State>, IServerTickableComponent<DistillerLogic.State>, IClientTickableComponent<DistillerLogic.State> {
    public static final int SLOT_INPUT_FILLED = 0;
    public static final int SLOT_INPUT_EMPTY = 1;
    public static final int SLOT_OUTPUT_EMPTY = 2;
    public static final int SLOT_OUTPUT_FILLED = 3;
    public static final int OUTPUT_SLOT = 4;
    public static final int TANK_CAPACITY = 24 * FluidType.BUCKET_VOLUME;
    public static final CapabilityPosition INPUT_FLUID_POI = new CapabilityPosition(0, 0, 1, RelativeBlockFace.RIGHT);
    public static final CapabilityPosition OUTPUT_FLUID_POI = new CapabilityPosition(2, 0, 1, RelativeBlockFace.LEFT);
    private static final Set<CapabilityPosition> ENERGY_POI = Set.of(new CapabilityPosition(0, 1, 2, RelativeBlockFace.UP));
    private static final MultiblockFace ITEM_OUTPUT_POI = new MultiblockFace(1, 0, -1, RelativeBlockFace.BACK);
    public static final BlockPos REDSTONE_POI = new BlockPos(2, 1, 2);

    @Override
    public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        if (!state.isSoundPlaying.getAsBoolean()) {
            Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(1, 1, 1));
            state.isSoundPlaying = ITSound.startSound(
                    () -> state.active, ctx.isValid(), soundPos, ITSounds.distiller,
                    () -> {
                        LocalPlayer player = Minecraft.getInstance().player;
                        if (player == null) { return 0f; }
                        return (float) Math.max(player.distanceToSqr(soundPos) / 8, 1);
                    },
                    () -> 1f
            );
        }
    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        boolean wasActive = state.active;
        state.active = state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx));
        boolean update = wasActive != state.active;
        DistillerRecipe recipe = DistillerRecipe.findRecipe(ctx.getLevel().getRawLevel(), state.tanks.input.getFluid());
        if (wasActive != state.active || recipe != null) { ctx.requestMasterBESync(); }
        tryEnqueueProcess(state, ctx.getLevel().getRawLevel(), recipe);
        if (tryEmptyContainer(state.tanks.input, state.inventory)) { update = true; }
        if (FluidUtils.fillFluidContainer(state.tanks.output, SLOT_OUTPUT_EMPTY, SLOT_OUTPUT_FILLED, state.inventory)) { update = true; }
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
        IItemHandlerModifiable inventory = state.inventory;
        ItemStack drainedContainer = inventory.getStackInSlot(SLOT_INPUT_EMPTY);
        if (!drainedContainer.isEmpty()) {
            int origCount = drainedContainer.getCount();
            drainedContainer = Utils.insertStackIntoInventory(state.outputRef, drainedContainer, false);
            if (drainedContainer.getCount() < origCount) { update = true; }
            inventory.setStackInSlot(SLOT_INPUT_EMPTY, drainedContainer);
        }
        ItemStack filledContainer = inventory.getStackInSlot(SLOT_OUTPUT_FILLED);
        if (!filledContainer.isEmpty()) {
            int origCount = filledContainer.getCount();
            filledContainer = Utils.insertStackIntoInventory(state.outputRef, filledContainer, false);
            if (filledContainer.getCount() < origCount) { update = true; }
            inventory.setStackInSlot(SLOT_OUTPUT_FILLED, filledContainer);
        }
        ItemStack itemOutput = inventory.getStackInSlot(OUTPUT_SLOT);
        if (!itemOutput.isEmpty()) {
            int origCount = itemOutput.getCount();
            itemOutput = Utils.insertStackIntoInventory(state.outputRef, itemOutput, false);
            if (itemOutput.getCount() < origCount) { update = true; }
            inventory.setStackInSlot(OUTPUT_SLOT, itemOutput);
        }
        if (update) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    private boolean tryEmptyContainer(IFluidHandler tank, IItemHandlerModifiable inv) {
        ItemStack filledContainer = inv.getStackInSlot(DistillerLogic.SLOT_INPUT_FILLED);
        if (filledContainer.isEmpty()) { return false; }
        FluidActionResult result = FluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.SIMULATE);
        if (!result.isSuccess()) { return false; }
        ItemStack emptyContainer = result.getResult();
        ItemStack outputStack = inv.getStackInSlot(DistillerLogic.SLOT_INPUT_EMPTY);
        if (!outputStack.isEmpty() && !ItemHandlerHelper.canItemStacksStack(outputStack, emptyContainer)) { return false; }
        if (outputStack.getCount() + emptyContainer.getCount() > emptyContainer.getMaxStackSize()) { return false; }
        result = FluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.EXECUTE);
        filledContainer.shrink(1);
        inv.setStackInSlot(DistillerLogic.SLOT_INPUT_FILLED, filledContainer);
        if (outputStack.isEmpty()) { inv.setStackInSlot(DistillerLogic.SLOT_INPUT_EMPTY, result.getResult()); }
        else { outputStack.grow(result.getResult().getCount()); }
        return true;
    }

    private void tryEnqueueProcess(State state, Level level, DistillerRecipe recipe) {
        if (state.processor.getQueueSize() >= state.processor.getMaxQueueSize()) { return; }
        if (recipe == null) { return; }
        FluidStack inputFluid = state.tanks.input.getFluid();
        if (inputFluid.getAmount() < recipe.input.getAmount()) { return; }
        FluidStack outputFluid = recipe.fluidOutput;
        if (outputFluid != null && !outputFluid.isEmpty() && state.tanks.output.getFluidAmount() + outputFluid.getAmount() > state.tanks.output.getCapacity()) { return; }
        ItemStack itemOutput = recipe.itemOutput;
        if (!itemOutput.isEmpty()) {
            ItemStack currentOutput = state.inventory.getStackInSlot(OUTPUT_SLOT);
            if (!currentOutput.isEmpty() && (!ItemHandlerHelper.canItemStacksStack(currentOutput, itemOutput) || currentOutput.getCount() + itemOutput.getCount() > currentOutput.getMaxStackSize())) { return; }
        }
        DistillerProcess process = new DistillerProcess(recipe);
        process.setInputTanks(0);
        state.processor.addProcessToQueue(process, level, false);
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        State state = ctx.getState();
        if (cap == ForgeCapabilities.ENERGY) {
            if (ENERGY_POI.contains(position)) { return state.energyCap.cast(ctx); }
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (position.posInMultiblock().equals(INPUT_FLUID_POI.posInMultiblock()) && (position.side() == null || position.side() == INPUT_FLUID_POI.side())) { return state.inputCap.cast(ctx); }
            if (position.posInMultiblock().equals(OUTPUT_FLUID_POI.posInMultiblock()) && (position.side() == null || position.side() == OUTPUT_FLUID_POI.side())) { return state.outputCapSteam.cast(ctx); }
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (ITEM_OUTPUT_POI.posInMultiblock().equals(position.posInMultiblock())) { return state.itemOutputCap.cast(ctx); }
            return state.invCap.cast(ctx);
        }
        return LazyOptional.empty();
    }

    @Override
    public void dropExtraItems(State state, Consumer<ItemStack> drop) { ITMultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override
    public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return DistillerShape.GETTER; }

    @Override
    public InteractionResult click(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) { return InteractionResult.SUCCESS; }

    public static class State implements IMultiblockState, ProcessContext.ProcessContextInMachine<DistillerRecipe> {
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final DistillerTank tanks;
        public final StoredCapability<IEnergyStorage> energyCap;
        public final StoredCapability<IFluidHandler> inputCap;
        public final StoredCapability<IFluidHandler> outputCapSteam;
        public final StoredCapability<IItemHandler> invCap;
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
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); };
            this.tanks = new DistillerTank(v -> onChanged.run());
            this.tankArray = new IFluidTank[]{tanks.input, tanks.output};
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
            this.inputCap = new StoredCapability<>(new ITArrayFluidHandler(tanks.input, false, true, onChanged));
            this.outputCapSteam = new StoredCapability<>(new ITArrayFluidHandler(tanks.output, true, false, onChanged));
            this.invCap = new StoredCapability<>(inventory);
            this.energyCap = new StoredCapability<>(this.energy);
            this.processor = new MultiblockProcessor.InMachineProcessor<>(1, 0f, 1, markDirty, DistillerRecipe.RECIPES::getById);
            MultiblockFace outputMBFace = new MultiblockFace(OUTPUT_FLUID_POI.side(), OUTPUT_FLUID_POI.posInMultiblock());
            CapabilityPosition opposingCP = CapabilityPosition.opposing(outputMBFace);
            MultiblockFace opposingMBFace = new MultiblockFace(opposingCP.side(), opposingCP.posInMultiblock());
            this.fluidOutput = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, opposingMBFace);
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
            this.outputRef = ctx.getCapabilityAt(ForgeCapabilities.ITEM_HANDLER, ITEM_OUTPUT_POI);
        }

        public ITSlotwiseItemHandler getInventory() { return inventory; }

        public DistillerTank getTanks() { return tanks; }

        @Override
        public void writeSaveNBT(CompoundTag nbt) {
            nbt.put("energy", energy.serializeNBT());
            nbt.put("tanks", this.tanks.toNBT());
            nbt.put("processor", processor.toNBT());
            nbt.put("inventory", inventory.serializeNBT());
            nbt.putBoolean("active", active);
        }

        @Override
        public void readSaveNBT(CompoundTag nbt) {
            energy.deserializeNBT(nbt.get("energy"));
            this.tanks.readNBT(nbt.getCompound("tanks"));
            this.processor.fromNBT(nbt.getList("processor", Tag.TAG_COMPOUND), DistillerProcess::new);
            this.inventory.deserializeNBT(nbt.getCompound("inventory"));
            active = nbt.getBoolean("active");
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

        @SuppressWarnings("unused")
        public int getCapacity() { return TANK_CAPACITY; }
    }
}
