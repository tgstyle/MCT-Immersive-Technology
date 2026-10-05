package mctmods.immersivetechnology.common.multiblocks.stone.logic;

import mctmods.immersivetechnology.client.util.ClientUtils;
import mctmods.immersivetechnology.common.blocks.metal.logic.AdvancedCokeOvenBaseHeaterBlockEntity;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.stone.process.AdvancedCokeOvenProcess;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.registration.Particles;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.util.IDisplaySyncState;
import mctmods.immersivetechnology.core.util.ItemOutputs;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.client.MachineSound;
import com.immersiveconvergence.api.multiblock.BurnProcessHandler;
import com.immersiveconvergence.api.multiblock.IDataReloadAware;
import com.immersiveconvergence.api.multiblock.IFluidOutputPump;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.util.ConstrainedItemHandler;
import com.immersiveconvergence.api.util.MarkableFluidTank;
import com.immersiveconvergence.api.util.MultiBlockInventoryUtils;
import com.immersiveconvergence.api.util.MultiTankFluidHandler;
import com.immersiveconvergence.api.util.RecipeCache;
import com.immersiveconvergence.api.util.SlotRangeItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;

public class AdvancedCokeOvenLogic implements IMultiblockLogic<AdvancedCokeOvenLogic.State>, IServerTickableComponent<AdvancedCokeOvenLogic.State>, IClientTickableComponent<AdvancedCokeOvenLogic.State>, IFluidOutputPump<AdvancedCokeOvenLogic.State> {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_EMPTY_CONTAINER = 2;
    public static final int SLOT_FILLED_CONTAINER = 3;
    public static CapabilityPosition OUTPUT_FLUID_POI;
    public static MultiblockFace ITEM_OUTPUT_POI;
    public static MultiblockFace ITEM_INPUT_POI;
    public static BlockPos SMOKE_POI;
    public static BlockPos SOUND_POI;
    public static List<BlockPos> COMPARATOR_POSITIONS;
    public static BlockPos BASEHEATER0_POI;
    public static BlockPos BASEHEATER1_POI;

    static { ITShapes.readPois("advanced_coke_oven", AdvancedCokeOvenLogic::loadPois); }

    public static int tankCapacity() { return ServerConfig.advancedCokeOvenTankCapacity; }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        IMultiblockLevel level = ctx.getLevel();
        if (!state.active) {
            state.isSoundPlaying = () -> false;
            return;
        }
        Vec3 particlePos = level.toAbsolute(new Vec3(SMOKE_POI.getX() + 0.5, SMOKE_POI.getY() + 0.9, SMOKE_POI.getZ() + 0.5));
        if (ClientUtils.particlesVisible(particlePos)) { level.getRawLevel().addAlwaysVisibleParticle(Particles.CAMPFIRE_SMOKE.get(), particlePos.x, particlePos.y, particlePos.z, ApiUtils.RANDOM.nextDouble(-0.00625, 0.00625), 0.05, ApiUtils.RANDOM.nextDouble(-0.00625, 0.00625)); }
        Vec3 soundPos = level.toAbsolute(Vec3.atCenterOf(SOUND_POI));
        if (ClientUtils.attenuated(soundPos, 8, 1) > 0.01f && !state.isSoundPlaying.getAsBoolean()) { state.isSoundPlaying = MachineSound.startSound(() -> state.active, ctx.isValid(), soundPos, Sounds.advancedCokeOven, () -> ClientUtils.attenuated(soundPos, 8, 1), () -> 1f); }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        Level level = ctx.getLevel().getRawLevel();
        boolean prevTanksDirty = state.tanksDirty;
        boolean wasActive = state.active;
        state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx));
        tryEnqueueProcess(state, level, state.recipeGetter.apply(level, state.inventory.getStackInSlot(SLOT_INPUT)));
        state.active = !state.processor.getQueue().isEmpty();
        FluidUtils.fillFluidContainer(state.tanks.output, SLOT_EMPTY_CONTAINER, SLOT_FILLED_CONTAINER, state.inventory);
        pumpOutputs(ctx);
        ItemOutputs.eject(state.inventory, state.outputRef, SLOT_OUTPUT, SLOT_FILLED_CONTAINER);
        var queue = state.processor.getQueue();
        int newComparatorValue = 0;
        if (!queue.isEmpty()) {
            int maxTicks = queue.get(0).getMaxTicks(level);
            newComparatorValue = maxTicks > 0 ? (15 * queue.get(0).processTick) / maxTicks : 0;
        }
        boolean comparatorChanged = newComparatorValue != state.lastComparatorValue;
        if (comparatorChanged) {
            for (BlockPos pos : COMPARATOR_POSITIONS) { ctx.setComparatorOutputFor(pos, newComparatorValue); }
            state.lastComparatorValue = newComparatorValue;
        }
        if (wasActive != state.active || prevTanksDirty != state.tanksDirty || comparatorChanged) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    @Override public List<BlockPos> getOutputPositions() { return List.of(OUTPUT_FLUID_POI.posInMultiblock()); }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FLUID_POI.side()); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output); }

    private static void tryEnqueueProcess(State state, Level level, @Nullable AdvancedCokeOvenRecipe recipe) {
        if (recipe == null || state.processor.getQueueSize() >= state.processor.getMaxQueueSize()) { return; }
        if (state.inventory.getStackInSlot(SLOT_INPUT).getCount() < recipe.input.getCount() || ItemOutputs.overflows(state.inventory.getStackInSlot(SLOT_OUTPUT), recipe.itemOutput.get())) { return; }
        if (state.tanks.output.getFluidAmount() + recipe.creosoteOutput > state.tanks.output.getCapacity()) { return; }
        ItemStack input = state.inventory.getStackInSlot(SLOT_INPUT);
        if (state.processor.addProcessToQueue(new AdvancedCokeOvenProcess(recipe), level, false)) { state.inventory.setStackInSlot(SLOT_INPUT, input.copyWithCount(input.getCount() - recipe.input.getCount())); }
    }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        State state = ctx.getState();
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (ITEM_INPUT_POI.posInMultiblock().equals(position.posInMultiblock()) && (position.side() == null || position.side() == ITEM_INPUT_POI.face())) { return state.itemInputCap.cast(ctx); }
            if (ITEM_OUTPUT_POI.posInMultiblock().equals(position.posInMultiblock()) && (position.side() == null || position.side() == ITEM_OUTPUT_POI.face())) { return state.itemOutputCap.cast(ctx); }
            return state.invCap.cast(ctx);
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER && position.posInMultiblock().equals(OUTPUT_FLUID_POI.posInMultiblock()) && (position.side() == null || position.side() == OUTPUT_FLUID_POI.side())) { return state.fluidCap.cast(ctx); }
        return LazyOptional.empty();
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { MultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get("advanced_coke_oven").getter; }

    @Override public InteractionResult click(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) { return InteractionResult.SUCCESS; }

    public static class State implements IDisplaySyncState, ContainerData, ProcessContext.ProcessContextInMachine<AdvancedCokeOvenRecipe>, BurnProcessHandler.IFurnaceEnvironment<AdvancedCokeOvenRecipe>, IDataReloadAware {
        public static final int MAX_PROCESS_TIME = 0;
        public static final int REMAINING_PROCESS_TIME = 1;
        public static final int NUM_SLOTS = 2;
        public final BiFunction<Level, ItemStack, AdvancedCokeOvenRecipe> recipeGetter = RecipeCache.cached(AdvancedCokeOvenRecipe::findRecipe);
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public boolean active;
        public final AdvancedCokeOvenTank tanks;
        private final IFluidTank[] tankArray;
        public final ConstrainedItemHandler inventory;
        private final MultiblockProcessor.InMachineProcessor<AdvancedCokeOvenRecipe> processor;
        private final StoredCapability<IItemHandler> invCap;
        private final StoredCapability<IFluidHandler> fluidCap;
        private final StoredCapability<IItemHandler> itemOutputCap;
        private final StoredCapability<IItemHandler> itemInputCap;
        private CapabilityReference<IItemHandler> outputRef;
        public BooleanSupplier isSoundPlaying = () -> false;
        private final AveragingEnergyStorage energy = new AveragingEnergyStorage(0);
        public boolean tanksDirty = false;
        public int lastComparatorValue = -1;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> {
                markDirty.run();
                sync.run();
                this.tanksDirty = true;
            };
            this.tanks = new AdvancedCokeOvenTank(v -> onChanged.run());
            this.tankArray = new IFluidTank[]{tanks.output};
            this.inventory = new ConstrainedItemHandler(List.of(ConstrainedItemHandler.IOConstraint.input(i -> AdvancedCokeOvenRecipe.findRecipe(ctx.levelSupplier().get(), i, null) != null), ConstrainedItemHandler.IOConstraint.OUTPUT, ConstrainedItemHandler.IOConstraint.FLUID_INPUT, ConstrainedItemHandler.IOConstraint.OUTPUT), onChanged);
            this.processor = new MultiblockProcessor.InMachineProcessor<>(1, 0f, 1, markDirty, AdvancedCokeOvenRecipe::getById);
            this.invCap = new StoredCapability<>(this.inventory);
            this.fluidCap = new StoredCapability<>(new MultiTankFluidHandler(tanks.output, true, false, onChanged));
            this.itemOutputCap = new StoredCapability<>(new SlotRangeItemHandler(inventory, false, true, List.of(new SlotRangeItemHandler.IntRange(SLOT_OUTPUT, SLOT_OUTPUT + 1), new SlotRangeItemHandler.IntRange(SLOT_FILLED_CONTAINER, SLOT_FILLED_CONTAINER + 1))));
            this.itemInputCap = new StoredCapability<>(new SlotRangeItemHandler(inventory, true, false, List.of(new SlotRangeItemHandler.IntRange(SLOT_INPUT, SLOT_INPUT + 1))));
            bindOutputs(ctx);
        }

        @Override public void onDataReload(IInitialMultiblockContext<?> context) {
            lastComparatorValue = -1;
            bindOutputs(context);
        }

        private void bindOutputs(IInitialMultiblockContext<?> context) { this.outputRef = context.getCapabilityAt(ForgeCapabilities.ITEM_HANDLER, ITEM_OUTPUT_POI); }

        @Override public boolean isActive() { return active; }

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
            if (index != MAX_PROCESS_TIME && index != REMAINING_PROCESS_TIME) { throw new IllegalArgumentException("Unknown index " + index); }
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
            int heaters = activeHeater(level, BASEHEATER0_POI) + activeHeater(level, BASEHEATER1_POI);
            return (ServerConfig.advancedCokeOvenSpeedBase + heaters * ServerConfig.advancedCokeOvenBaseheaterSpeedIncrease) * (1 + heaters * (ServerConfig.advancedCokeOvenBaseheaterSpeedMultiplier - 1));
        }

        private static int activeHeater(IMultiblockLevel level, BlockPos poi) { return level.getRawLevel().getBlockEntity(level.toAbsolute(poi)) instanceof AdvancedCokeOvenBaseHeaterBlockEntity heater && heater.doSpeedup() ? 1 : 0; }

        @Override public void turnOff(IMultiblockLevel level) { }
    }

    public record AdvancedCokeOvenTank(MarkableFluidTank output) {
        public AdvancedCokeOvenTank(Consumer<Void> markDirty) { this(new MarkableFluidTank(tankCapacity(), markDirty)); }

        public static AdvancedCokeOvenTank makeClient() { return new AdvancedCokeOvenTank(v -> {}); }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("out", this.output.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) { this.output.readFromNBT(tag.getCompound("out")); }
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        OUTPUT_FLUID_POI = new CapabilityPosition(MultiblockPOIHelper.getPosList(pois, "fluid_output0").get(0), MultiblockPOIHelper.getFacing(pois, "fluid_output0"));
        ITEM_OUTPUT_POI = new MultiblockFace(MultiblockPOIHelper.getFacing(pois, "item_output0"), MultiblockPOIHelper.getPosList(pois, "item_output0").get(0));
        ITEM_INPUT_POI = new MultiblockFace(MultiblockPOIHelper.getFacing(pois, "item_input0"), MultiblockPOIHelper.getPosList(pois, "item_input0").get(0));
        SMOKE_POI = MultiblockPOIHelper.getPosList(pois, "smoke0").get(0);
        SOUND_POI = MultiblockPOIHelper.getPosList(pois, "sound0").get(0);
        COMPARATOR_POSITIONS = MultiblockPOIHelper.getPosList(pois, "comparator0");
        BASEHEATER0_POI = MultiblockPOIHelper.getPosList(pois, "baseheater0").get(0);
        BASEHEATER1_POI = MultiblockPOIHelper.getPosList(pois, "baseheater1").get(0);
    }
}
