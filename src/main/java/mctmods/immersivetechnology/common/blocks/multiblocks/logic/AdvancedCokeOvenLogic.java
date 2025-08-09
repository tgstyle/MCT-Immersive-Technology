package mctmods.immersivetechnology.common.blocks.multiblocks.logic;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
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
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import mctmods.immersivetechnology.common.blocks.metal.CokeOvenHeaterBlockEntity;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITFurnaceHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITWrappingItemHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.process.AdvancedCokeOvenProcess;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.AdvancedCokeOvenFuel;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.AdvancedCokeOvenShape;
import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.core.lib.ITMultiblockSound;
import mctmods.immersivetechnology.core.registration.ITSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

public class AdvancedCokeOvenLogic implements IMultiblockLogic<AdvancedCokeOvenLogic.State>, IServerTickableComponent<AdvancedCokeOvenLogic.State>, IClientTickableComponent<AdvancedCokeOvenLogic.State> {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_EMPTY_CONTAINER = 2;
    public static final int SLOT_FILLED_CONTAINER = 3;

    public static final MultiblockFace ITEM_OUTPUT_REF_POS = new MultiblockFace(1, 0, 3, RelativeBlockFace.FRONT);
    public static final BlockPos[] HEATER_OFFSETS = { new BlockPos(-1, 0, 1), new BlockPos(3, 0, 1) };

    public static final int TANK_CAPACITY = 12 * FluidType.BUCKET_VOLUME;

    public static final CapabilityPosition OUTPUT_FLUID_POS = new CapabilityPosition(1, 0, 0, RelativeBlockFace.FRONT);
    public static final CapabilityPosition ITEM_OUTPUT_POS = new CapabilityPosition(1, 0, 2, RelativeBlockFace.BACK);

    private static final Vec3 SMOKE_POS = new Vec3(1.5, 3.9, 1.5);

    @Override
    public void tickClient(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        final IMultiblockLevel level = ctx.getLevel();
        if (state.active) {
            final Vec3 particlePos = level.toAbsolute(SMOKE_POS);
            level.getRawLevel().addAlwaysVisibleParticle(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    particlePos.x, particlePos.y, particlePos.z,
                    ApiUtils.RANDOM.nextDouble(-0.00625, 0.00625), 0.05, ApiUtils.RANDOM.nextDouble(-0.00625, 0.00625)
            );
        }
        if (!state.isSoundPlaying.getAsBoolean()) {
            final Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(1, 1, 1));
            state.isSoundPlaying = ITMultiblockSound.startSound(
                    () -> state.active, ctx.isValid(), soundPos, ITSounds.advancedCokeOven, () -> {
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
        final State state = ctx.getState();
        final boolean wasActive = state.active;
        state.active = state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx));
        boolean update = wasActive != state.active;
        AdvancedCokeOvenRecipe recipe = AdvancedCokeOvenRecipe.findRecipe(ctx.getLevel().getRawLevel(), state.inventory.getStackInSlot(SLOT_INPUT));
        if (wasActive != state.active || recipe != null) { ctx.requestMasterBESync(); }
        tryEnqueueProcess(state, ctx.getLevel().getRawLevel(), recipe);
        if (state.tanks.output.getFluidAmount() > 0 && FluidUtils.fillFluidContainer(state.tanks.output, SLOT_EMPTY_CONTAINER, SLOT_FILLED_CONTAINER, state.inventory)) { update = true; }
        if (state.tanks.output.getFluidAmount() > 0) {
            IFluidHandler output = state.fluidOutput.getNullable();
            if (output != null) {
                FluidStack fs = state.tanks.output.getFluid().copy();
                int accepted = output.fill(fs, FluidAction.SIMULATE);
                if (accepted > 0) {
                    int drained = output.fill(Utils.copyFluidStackWithAmount(fs, accepted, false), FluidAction.EXECUTE);
                    state.tanks.output.drain(drained, FluidAction.EXECUTE);
                    update = true;
                }
            }
        }
        final IItemHandlerModifiable inventory = state.inventory;
        ItemStack itemOutput = inventory.getStackInSlot(SLOT_OUTPUT);
        if (!itemOutput.isEmpty()) {
            int origCount = itemOutput.getCount();
            itemOutput = Utils.insertStackIntoInventory(state.outputRef, itemOutput, false);
            if (itemOutput.getCount() < origCount) { update = true; }
            inventory.setStackInSlot(SLOT_OUTPUT, itemOutput);
        }
        ItemStack filledContainer = inventory.getStackInSlot(SLOT_FILLED_CONTAINER);
        if (!filledContainer.isEmpty()) {
            int origCount = filledContainer.getCount();
            filledContainer = Utils.insertStackIntoInventory(state.outputRef, filledContainer, false);
            if (filledContainer.getCount() < origCount) { update = true; }
            inventory.setStackInSlot(SLOT_FILLED_CONTAINER, filledContainer);
        }
        if (update) { ctx.markMasterDirty(); ctx.requestMasterBESync(); }
    }

    private void tryEnqueueProcess(State state, Level level, @Nullable AdvancedCokeOvenRecipe recipe) {
        if (state.processor.getQueueSize() >= state.processor.getMaxQueueSize()) { return; }
        if (recipe == null) { return; }
        ItemStack inputStack = state.inventory.getStackInSlot(SLOT_INPUT);
        if (inputStack.getCount() < recipe.input.getCount()) { return; }
        ItemStack currentOutputStack = state.inventory.getStackInSlot(SLOT_OUTPUT);
        boolean canOutputItem = currentOutputStack.isEmpty() || (ItemHandlerHelper.canItemStacksStack(currentOutputStack, recipe.output.get()) && currentOutputStack.getCount() + recipe.output.get().getCount() <= currentOutputStack.getMaxStackSize());
        if (!canOutputItem) { return; }
        if (state.tanks.output.getFluidAmount() + recipe.creosoteOutput > state.tanks.output.getCapacity()) { return; }
        AdvancedCokeOvenProcess process = new AdvancedCokeOvenProcess(recipe);
        state.processor.addProcessToQueue(process, level, false);
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        final State state = ctx.getState();
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (ITEM_OUTPUT_POS.equals(position)) { return state.itemOutputCap.cast(ctx); }
            return state.invCap.cast(ctx);
        } else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (position.posInMultiblock().equals(OUTPUT_FLUID_POS.posInMultiblock()) && (position.side() == null || position.side() == OUTPUT_FLUID_POS.side())) { return state.fluidCap.cast(ctx); }
        }
        return LazyOptional.empty();
    }

    @Override
    public void dropExtraItems(State state, Consumer<ItemStack> drop) { for (ItemStack stack : state.inventory) { drop.accept(stack); } }

    @Override
    public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return AdvancedCokeOvenShape.GETTER; }

    @Override
    public InteractionResult click(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) {
        return InteractionResult.SUCCESS;
    }

    public static class State implements IMultiblockState, ContainerData, ITFurnaceHandler.IFurnaceEnvironment<AdvancedCokeOvenRecipe>, ProcessContext.ProcessContextInMachine<AdvancedCokeOvenRecipe> {
        public static final int MAX_BURN_TIME = 0;
        public static final int BURN_TIME = 1;
        public static final int NUM_SLOTS = 2;

        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public boolean active;
        public final AdvancedCokeOvenTank tanks;
        private final IFluidTank[] tankArray;
        public final ITSlotwiseItemHandler inventory;
        private final MultiblockProcessor.InMachineProcessor<AdvancedCokeOvenRecipe> processor;
        private final StoredCapability<IItemHandler> invCap;
        private final StoredCapability<IFluidHandler> fluidCap;
        private final StoredCapability<IItemHandler> itemOutputCap;
        private final CapabilityReference<IFluidHandler> fluidOutput;
        private final CapabilityReference<IItemHandler> outputRef;
        public BooleanSupplier isSoundPlaying = () -> false;
        private final AveragingEnergyStorage energy = new AveragingEnergyStorage(0);

        public State(IInitialMultiblockContext<State> ctx) {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            final Runnable onChanged = () -> { markDirty.run(); ctx.getSyncRunnable().run(); };
            this.tanks = new AdvancedCokeOvenTank(v -> onChanged.run());
            this.tankArray = new IFluidTank[]{tanks.output};
            this.inventory = new ITSlotwiseItemHandler(
                    List.of(
                            ITSlotwiseItemHandler.IOConstraint.input(i -> AdvancedCokeOvenRecipe.findRecipe(ctx.levelSupplier().get(), i) != null),
                            ITSlotwiseItemHandler.IOConstraint.OUTPUT,
                            ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            ITSlotwiseItemHandler.IOConstraint.OUTPUT
                    ),
                    onChanged
            );
            this.processor = new MultiblockProcessor.InMachineProcessor<>(1, 0f, 1, markDirty, AdvancedCokeOvenRecipe.RECIPES::getById);
            this.invCap = new StoredCapability<>(this.inventory);
            this.fluidCap = new StoredCapability<>(new ITArrayFluidHandler(tanks.output, true, false, onChanged));
            this.itemOutputCap = new StoredCapability<>(new ITWrappingItemHandler(
                    inventory,
                    false,
                    true,
                    List.of(
                            new ITWrappingItemHandler.IntRange(SLOT_OUTPUT, SLOT_OUTPUT + 1),
                            new ITWrappingItemHandler.IntRange(SLOT_FILLED_CONTAINER, SLOT_FILLED_CONTAINER + 1)
                    )
            ));
            MultiblockFace outputMBFace = new MultiblockFace(OUTPUT_FLUID_POS.side(), OUTPUT_FLUID_POS.posInMultiblock());
            CapabilityPosition opposingCP = CapabilityPosition.opposing(outputMBFace);
            MultiblockFace opposingMBFace = new MultiblockFace(opposingCP.side(), opposingCP.posInMultiblock());
            this.fluidOutput = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, opposingMBFace);
            this.outputRef = ctx.getCapabilityAt(ForgeCapabilities.ITEM_HANDLER, ITEM_OUTPUT_REF_POS);
        }

        public ITSlotwiseItemHandler getInventory() { return inventory; }

        public AdvancedCokeOvenTank getTanks() { return tanks; }

        @Override
        public void writeSaveNBT(CompoundTag nbt) {
            nbt.put("tanks", tanks.toNBT());
            nbt.put("processor", processor.toNBT());
            nbt.put("inventory", inventory.serializeNBT());
        }

        @Override
        public void readSaveNBT(CompoundTag nbt) {
            tanks.readNBT(nbt.getCompound("tanks"));
            processor.fromNBT(nbt.getList("processor", Tag.TAG_COMPOUND), AdvancedCokeOvenProcess::new);
            inventory.deserializeNBT(nbt.getCompound("inventory"));
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT());
        }

        @Override
        public void readSyncNBT(CompoundTag nbt) {
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"));
        }

        @Override
        public int get(int index) {
            if (processor.getQueue().isEmpty()) { return 0; }
            AdvancedCokeOvenProcess process = (AdvancedCokeOvenProcess) processor.getQueue().get(0);
            return switch (index) {
                case MAX_BURN_TIME -> process.getMaxProcessTime();
                case BURN_TIME -> process.getMaxProcessTime() - process.getCurrentProcessTime();
                default -> throw new IllegalArgumentException("Unknown index " + index);
            };
        }

        @Override
        public void set(int index, int value) {
            if (processor.getQueue().isEmpty()) { return; }
            switch (index) {
                case MAX_BURN_TIME, BURN_TIME -> { }
                default -> throw new IllegalArgumentException("Unknown index " + index);
            }
        }

        @Override
        public int getCount() { return NUM_SLOTS; }

        @Override
        public AveragingEnergyStorage getEnergy() { return energy; }

        @Override
        public IFluidTank[] getInternalTanks() { return tankArray; }

        @Override
        public int[] getOutputSlots() { return new int[]{SLOT_OUTPUT}; }

        @Override
        public int[] getOutputTanks() { return new int[]{0}; }

        @Nullable
        @Override
        public AdvancedCokeOvenRecipe getRecipeForInput(Level level) { return AdvancedCokeOvenRecipe.findRecipe(level, inventory.getStackInSlot(SLOT_INPUT)); }

        @Override
        public int getBurnTimeOf(Level level, ItemStack fuel) { return AdvancedCokeOvenFuel.getAdvancedCokeOvenFuelTime(level, fuel); }

        @Override
        public double getProcessSpeed(IMultiblockLevel level) {
            int numActive = 0;
            for (BlockPos offset : HEATER_OFFSETS) {
                CokeOvenHeaterBlockEntity heater = getHeater(level, offset);
                if (heater != null) numActive += heater.doSpeedup();
            }
            return 1.0 + numActive * 0.25;
        }

        @Override
        public void turnOff(IMultiblockLevel level) {
            for (BlockPos offset : HEATER_OFFSETS) {
                CokeOvenHeaterBlockEntity heater = getHeater(level, offset);
                if (heater != null) { heater.turnOff(); }
            }
        }

        @Nullable
        public CokeOvenHeaterBlockEntity getHeater(IMultiblockLevel level, BlockPos pos) {
            BlockEntity te = level.getBlockEntity(pos);
            return te instanceof CokeOvenHeaterBlockEntity heater ? heater : null;
        }

        @SuppressWarnings("unused")
        public GetterAndSetter<Boolean> preheaterActive(IMultiblockLevel level, int index) {
            return GetterAndSetter.getterOnly(() -> {
                CokeOvenHeaterBlockEntity heater = getHeater(level, HEATER_OFFSETS[index]);
                return heater != null && heater.active;
            });
        }
    }

    public record AdvancedCokeOvenTank(ITMarkableFluidTank output) {
        public AdvancedCokeOvenTank(Consumer<Void> markDirty) { this(new ITMarkableFluidTank(TANK_CAPACITY, markDirty)); }

        public static AdvancedCokeOvenTank makeClient() { return new AdvancedCokeOvenTank(v -> {}); }

        public Tag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("out", this.output.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) { this.output.readFromNBT(tag.getCompound("out")); }

        @SuppressWarnings("unused")
        public int getCapacity() { return TANK_CAPACITY; }
    }
}
