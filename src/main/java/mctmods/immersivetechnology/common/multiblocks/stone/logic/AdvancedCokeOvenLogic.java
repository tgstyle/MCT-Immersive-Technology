package mctmods.immersivetechnology.common.multiblocks.stone.logic;

import mctmods.immersivetechnology.common.blocks.metal.logic.AdvancedCokeOvenBaseHeaterIBlockEntity;
import mctmods.immersivetechnology.common.multiblocks.helper.ITIDisplayContext;
import mctmods.immersivetechnology.common.multiblocks.helper.ITFurnaceHandler;
import mctmods.immersivetechnology.common.multiblocks.helper.ITMultiBlockInventoryUtils;
import mctmods.immersivetechnology.common.multiblocks.helper.ITMultiblockPOIHelper;
import mctmods.immersivetechnology.common.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.multiblocks.helper.ITWrappingItemHandler;
import mctmods.immersivetechnology.common.multiblocks.stone.process.AdvancedCokeOvenProcess;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetechnology.common.multiblocks.stone.shapes.AdvancedCokeOvenShape;
import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.core.ITServerConfig;
import mctmods.immersivetechnology.core.lib.ITSound;
import mctmods.immersivetechnology.core.registration.ITSounds;
import mctmods.immersivetechnology.core.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.core.util.ITUtils;
import mctmods.immersivetechnology.core.util.ITCachedRecipe;

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
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import com.google.common.collect.ImmutableList;
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
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.BiFunction;

public class AdvancedCokeOvenLogic implements IMultiblockLogic<AdvancedCokeOvenLogic.State>, IServerTickableComponent<AdvancedCokeOvenLogic.State>, IClientTickableComponent<AdvancedCokeOvenLogic.State> {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_EMPTY_CONTAINER = 2;
    public static final int SLOT_FILLED_CONTAINER = 3;
    public static final int TANK_CAPACITY = 12 * FluidType.BUCKET_VOLUME;

    public static final double BASE_SPEED = ITServerConfig.advancedCokeOvenSpeedBase;
    public static final double BASEHEATER_ADD = ITServerConfig.advancedCokeOvenBaseheaterSpeedIncrease;
    public static final double BASEHEATER_MULT = ITServerConfig.advancedCokeOvenBaseheaterSpeedMultiplier;

    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(AdvancedCokeOvenShape.DATA.pointsOfInterest);

    public static final CapabilityPosition OUTPUT_FLUID_POI = new CapabilityPosition(ITMultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output0").get(0), ITMultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output0"));
    public static final MultiblockFace ITEM_OUTPUT_POI = new MultiblockFace(ITMultiblockPOIHelper.getFacing(RAW_POIS, "item_output0"), ITMultiblockPOIHelper.getPosList(RAW_POIS, "item_output0").get(0));
    public static final MultiblockFace ITEM_INPUT_POI = new MultiblockFace(ITMultiblockPOIHelper.getFacing(RAW_POIS, "item_input0"), ITMultiblockPOIHelper.getPosList(RAW_POIS, "item_input0").get(0));
    public static final BlockPos SMOKE_POI = ITMultiblockPOIHelper.getPosList(RAW_POIS, "smoke0").get(0);
    public static final BlockPos SOUND_POI = ITMultiblockPOIHelper.getPosList(RAW_POIS, "sound0").get(0);
    public static final BlockPos BASEHEATER0_POI = ITMultiblockPOIHelper.getPosList(RAW_POIS, "baseheater0").get(0);
    public static final BlockPos BASEHEATER1_POI = ITMultiblockPOIHelper.getPosList(RAW_POIS, "baseheater1").get(0);

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        final IMultiblockLevel level = ctx.getLevel();
        if (state.active) {
            final Vec3 particlePos = level.toAbsolute(new Vec3(SMOKE_POI.getX() + 0.5, SMOKE_POI.getY() + 0.9, SMOKE_POI.getZ() + 0.5));
            level.getRawLevel().addAlwaysVisibleParticle(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    ApiUtils.RANDOM.nextDouble(-0.00625, 0.00625),
                    0.05,
                    ApiUtils.RANDOM.nextDouble(-0.00625, 0.00625)
            );
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            if (!state.active) {
                state.isSoundPlaying = () -> false;
            }
            return;
        }
        if (state.active) {
            final Vec3 soundPos = level.toAbsolute(new Vec3(SOUND_POI.getX() + 0.5, SOUND_POI.getY() + 0.5, SOUND_POI.getZ() + 0.5));
            float att = (float) Math.max(player.distanceToSqr(soundPos) / 8, 1);
            float vol = 1f / att;
            if (vol > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
                state.isSoundPlaying = ITSound.startSound(
                        () -> state.active,
                        ctx.isValid(),
                        soundPos,
                        ITSounds.advancedCokeOven,
                        () -> {
                            LocalPlayer p = Minecraft.getInstance().player;
                            if (p == null) { return 0f; }
                            float attenuation = (float) Math.max(p.distanceToSqr(soundPos) / 8, 1);
                            return 1f / attenuation;
                        },
                        () -> 1f
                );
            }
        } else {
            state.isSoundPlaying = () -> false;
        }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        final Level level = ctx.getLevel().getRawLevel();
        boolean prevTanksDirty = state.tanksDirty;
        boolean wasActive = state.active;
        state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx));
        state.active = !state.processor.getQueue().isEmpty();
        AdvancedCokeOvenRecipe recipe = state.recipeGetter.apply(level, state.inventory.getStackInSlot(SLOT_INPUT));
        tryEnqueueProcess(state, level, recipe);
        if (!state.processor.getQueue().isEmpty()) {
            state.active = true;
        }
        FluidUtils.fillFluidContainer(state.tanks.output, SLOT_EMPTY_CONTAINER, SLOT_FILLED_CONTAINER, state.inventory);
        if (state.tanks.output.getFluidAmount() > 0) {
            IFluidHandler output = state.fluidOutput.getNullable();
            if (output != null) {
                FluidStack fs = state.tanks.output.getFluid().copy();
                int accepted = output.fill(fs, FluidAction.SIMULATE);
                if (accepted > 0) {
                    int drained = output.fill(ITUtils.copyFluidStackWithAmount(fs, accepted, false), FluidAction.EXECUTE);
                    state.tanks.output.drain(drained, FluidAction.EXECUTE);
                }
            }
        }
        final IItemHandlerModifiable inventory = state.inventory;
        ItemStack itemOutput = inventory.getStackInSlot(SLOT_OUTPUT);
        if (!itemOutput.isEmpty()) {
            itemOutput = ITUtils.insertStackIntoInventory(state.outputRef, itemOutput, false);
            inventory.setStackInSlot(SLOT_OUTPUT, itemOutput);
        }
        ItemStack filledContainer = inventory.getStackInSlot(SLOT_FILLED_CONTAINER);
        if (!filledContainer.isEmpty()) {
            filledContainer = ITUtils.insertStackIntoInventory(state.outputRef, filledContainer, false);
            inventory.setStackInSlot(SLOT_FILLED_CONTAINER, filledContainer);
        }
        boolean activeChanged = wasActive != state.active;
        boolean tanksChanged = prevTanksDirty != state.tanksDirty;
        if (activeChanged || tanksChanged) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    private void tryEnqueueProcess(State state, Level level, @Nullable AdvancedCokeOvenRecipe recipe) {
        if (state.processor.getQueueSize() >= state.processor.getMaxQueueSize()) { return; }
        if (recipe == null) { return; }
        ItemStack inputStack = state.inventory.getStackInSlot(SLOT_INPUT);
        if (inputStack.getCount() < recipe.input.getCount()) { return; }
        ItemStack currentOutputStack = state.inventory.getStackInSlot(SLOT_OUTPUT);
        boolean canOutputItem = currentOutputStack.isEmpty() || (ItemHandlerHelper.canItemStacksStack(currentOutputStack, recipe.itemOutput.get()) && currentOutputStack.getCount() + recipe.itemOutput.get().getCount() <= currentOutputStack.getMaxStackSize());
        if (!canOutputItem) { return; }
        if (state.tanks.output.getFluidAmount() + recipe.creosoteOutput > state.tanks.output.getCapacity()) { return; }
        AdvancedCokeOvenProcess process = new AdvancedCokeOvenProcess(recipe);
        state.processor.addProcessToQueue(process, level, false);
    }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        final State state = ctx.getState();
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (ITEM_INPUT_POI.posInMultiblock().equals(position.posInMultiblock()) && (position.side() == null || position.side() == ITEM_INPUT_POI.face())) { return state.itemInputCap.cast(ctx); }
            if (ITEM_OUTPUT_POI.posInMultiblock().equals(position.posInMultiblock()) && (position.side() == null || position.side() == ITEM_OUTPUT_POI.face())) { return state.itemOutputCap.cast(ctx); }
            return state.invCap.cast(ctx);
        }
        else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (position.posInMultiblock().equals(OUTPUT_FLUID_POI.posInMultiblock()) && (position.side() == null || position.side() == OUTPUT_FLUID_POI.side())) { return state.fluidCap.cast(ctx); }
        }
        return LazyOptional.empty();
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { ITMultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return AdvancedCokeOvenShape.GETTER; }

    @Override public InteractionResult click(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) { return InteractionResult.SUCCESS; }

    public static class State implements IMultiblockState, ContainerData, ProcessContext.ProcessContextInMachine<AdvancedCokeOvenRecipe>, ITFurnaceHandler.IFurnaceEnvironment<AdvancedCokeOvenRecipe>, ITIDisplayContext {
        public final BiFunction<Level, ItemStack, AdvancedCokeOvenRecipe> recipeGetter = ITCachedRecipe.cached(AdvancedCokeOvenRecipe::findRecipe);
        public static final int MAX_PROCESS_TIME = 0;
        public static final int REMAINING_PROCESS_TIME = 1;
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
        private final StoredCapability<IItemHandler> itemInputCap;
        private final CapabilityReference<IFluidHandler> fluidOutput;
        private final CapabilityReference<IItemHandler> outputRef;
        public BooleanSupplier isSoundPlaying = () -> false;
        private final AveragingEnergyStorage energy = new AveragingEnergyStorage(0);
        public boolean tanksDirty = false;

        public State(IInitialMultiblockContext<State> ctx) {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            final Runnable sync = ctx.getSyncRunnable();
            final Runnable onChanged = () -> { markDirty.run(); sync.run(); this.tanksDirty = true; };
            this.tanks = new AdvancedCokeOvenTank(v -> onChanged.run());
            this.tankArray = new IFluidTank[]{tanks.output};
            this.inventory = new ITSlotwiseItemHandler(
                    List.of(
                            ITSlotwiseItemHandler.IOConstraint.input(i -> AdvancedCokeOvenRecipe.findRecipe(ctx.levelSupplier().get(), i, null) != null),
                            ITSlotwiseItemHandler.IOConstraint.OUTPUT,
                            ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            ITSlotwiseItemHandler.IOConstraint.OUTPUT
                    ),
                    onChanged
            );
            this.processor = new MultiblockProcessor.InMachineProcessor<>(1, 0f, 1, markDirty, AdvancedCokeOvenRecipe::getById);
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
            this.itemInputCap = new StoredCapability<>(new ITWrappingItemHandler(
                    inventory,
                    true,
                    false,
                    List.of(new ITWrappingItemHandler.IntRange(SLOT_INPUT, SLOT_INPUT + 1))
            ));
            MultiblockFace outputMBFace = new MultiblockFace(OUTPUT_FLUID_POI.side(), OUTPUT_FLUID_POI.posInMultiblock());
            CapabilityPosition opposingCP = CapabilityPosition.opposing(outputMBFace);
            MultiblockFace opposingMBFace = new MultiblockFace(opposingCP.side(), opposingCP.posInMultiblock());
            this.fluidOutput = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, opposingMBFace);
            this.outputRef = ctx.getCapabilityAt(ForgeCapabilities.ITEM_HANDLER, ITEM_OUTPUT_POI);
        }

        public AdvancedCokeOvenTank getTanks() { return tanks; }

        public boolean isActive() { return active; }

        @Override public void writeSaveNBT(CompoundTag nbt) {
            nbt.put("tanks", tanks.toNBT());
            nbt.put("processor", processor.toNBT());
            nbt.put("inventory", inventory.serializeNBT());
            nbt.putBoolean("active", active);
        }

        @Override public void readSaveNBT(CompoundTag nbt) {
            tanks.readNBT(nbt.getCompound("tanks"));
            processor.fromNBT(nbt.getList("processor", Tag.TAG_COMPOUND), AdvancedCokeOvenProcess::new);
            inventory.deserializeNBT(nbt.getCompound("inventory"));
            active = nbt.getBoolean("active");
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

        @Override public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT());
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt) {
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"));
            tanksDirty = false;
        }

        @Override public int get(int index) {
            if (processor.getQueue().isEmpty()) { return 0; }
            AdvancedCokeOvenProcess process = (AdvancedCokeOvenProcess) processor.getQueue().get(0);
            return switch (index) {
                case MAX_PROCESS_TIME -> process.getMaxProcessTime();
                case REMAINING_PROCESS_TIME -> process.getMaxProcessTime() - process.getCurrentProcessTime();
                default -> throw new IllegalArgumentException("Unknown index " + index);
            };
        }

        @Override public void set(int index, int value) {
            if (processor.getQueue().isEmpty()) { return; }
            switch (index) {
                case MAX_PROCESS_TIME, REMAINING_PROCESS_TIME -> {}
                default -> throw new IllegalArgumentException("Unknown index " + index);
            }
        }

        @Override public int getCount() { return NUM_SLOTS; }

        @Override public AveragingEnergyStorage getEnergy() { return energy; }

        @Override public IFluidTank[] getInternalTanks() { return tankArray; }

        @Override public int[] getOutputSlots() { return new int[]{SLOT_OUTPUT}; }

        @Override public int[] getOutputTanks() { return new int[]{0}; }

        @Override public IItemHandlerModifiable getInventory() { return inventory; }

        @Override @Nullable public AdvancedCokeOvenRecipe getRecipeForInput(Level level) { return AdvancedCokeOvenRecipe.findRecipe(level, inventory.getStackInSlot(SLOT_INPUT), null); }

        @Override public int getBurnTimeOf(Level level, ItemStack fuel) { return 0; }

        @Override public double getProcessSpeed(IMultiblockLevel level) {
            int activeBaseheaters = 0;

            BlockPos heater1World = level.toAbsolute(BASEHEATER0_POI);
            BlockEntity be1 = level.getRawLevel().getBlockEntity(heater1World);
            if (be1 instanceof AdvancedCokeOvenBaseHeaterIBlockEntity heater && heater.doSpeedup()) {
                activeBaseheaters++;
            }

            BlockPos heater2World = level.toAbsolute(BASEHEATER1_POI);
            BlockEntity be2 = level.getRawLevel().getBlockEntity(heater2World);
            if (be2 instanceof AdvancedCokeOvenBaseHeaterIBlockEntity heater && heater.doSpeedup()) {
                activeBaseheaters++;
            }

            return (BASE_SPEED + activeBaseheaters * BASEHEATER_ADD) * (1 + activeBaseheaters * (BASEHEATER_MULT - 1));
        }

        @Override public void turnOff(IMultiblockLevel level) { }
    }

    public record AdvancedCokeOvenTank(ITMarkableFluidTank output) {
        public AdvancedCokeOvenTank(Consumer<Void> markDirty) {
            this(new ITMarkableFluidTank(TANK_CAPACITY, markDirty));
        }

        public static AdvancedCokeOvenTank makeClient() { return new AdvancedCokeOvenTank(v -> {}); }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("out", this.output.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) { this.output.readFromNBT(tag.getCompound("out")); }

        @SuppressWarnings("unused")
        public int getCapacity() { return TANK_CAPACITY; }
    }
}
