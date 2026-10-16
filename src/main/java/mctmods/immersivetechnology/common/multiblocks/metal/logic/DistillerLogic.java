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

import com.immersiveconvergence.api.energy.ICEnergyStorageAveraging;
import com.immersiveconvergence.api.multiblock.blocks.component.IClientTickableComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.IMultiblockComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.IServerTickableComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.RedstoneControl;
import com.immersiveconvergence.api.multiblock.blocks.env.IInitialMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.logic.IMultiblockLogic;
import com.immersiveconvergence.api.multiblock.blocks.util.*;
import com.immersiveconvergence.api.multiblock.process.MultiblockProcessor;
import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.client.MachineSound;
import com.immersiveconvergence.api.integration.DisplayLines;
import com.immersiveconvergence.api.multiblock.IDataReloadAware;
import com.immersiveconvergence.api.multiblock.IFluidOutputPump;
import com.immersiveconvergence.api.multiblock.IProcessContext;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.util.ConstrainedItemHandler;
import com.immersiveconvergence.api.util.ICFluidUtils;
import com.immersiveconvergence.api.util.MarkableFluidTank;
import com.immersiveconvergence.api.util.MultiBlockInventoryUtils;
import com.immersiveconvergence.api.util.MultiTankFluidHandler;
import com.immersiveconvergence.api.util.RecipeCache;
import com.immersiveconvergence.api.util.SlotRangeItemHandler;
import com.immersiveconvergence.api.util.TankPair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DistillerLogic implements IMultiblockLogic<DistillerLogic.State>, IServerTickableComponent<DistillerLogic.State>, IClientTickableComponent<DistillerLogic.State>, IFluidOutputPump<DistillerLogic.State> {
    public static final int SLOT_INPUT_FILLED = 0;
    public static final int SLOT_INPUT_EMPTY = 1;
    public static final int SLOT_OUTPUT_EMPTY = 2;
    public static final int SLOT_OUTPUT_FILLED = 3;
    public static final int OUTPUT_SLOT = 4;

    public static int inputTankCapacity() { return ServerConfig.distillerInputTankCapacity; }
    public static int outputTankCapacity() { return ServerConfig.distillerOutputTankCapacity; }
    public static int energyCapacity() { return ServerConfig.distillerEnergyCapacity; }
    public static int energyMaxIo() { return ServerConfig.distillerEnergyMaxIO; }

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

    @Override public List<BlockPos> getOutputPositions() { return OUTPUT_FLUID_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FLUID_FACING); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output()); }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        List<BlockPos> soundPosList = MultiblockPOIHelper.getPosList(RAW_POIS, "sound0");
        BlockPos soundBlockPos = soundPosList.getFirst();
        Vec3 soundPos = ctx.getLevel().toAbsolute(Vec3.atCenterOf(soundBlockPos));
        if (Minecraft.getInstance().player == null) { return; }
        if (state.active && ClientUtils.attenuated(soundPos, 32, 1) > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
            state.isSoundPlaying = MachineSound.startSound(() -> state.active, ctx.isValid(), soundPos, Sounds.distiller, () -> ClientUtils.attenuated(soundPos, 32, 1), () -> 1f);
        }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        state.energy.updateAverage();
        int prevEnergy = state.energy.getEnergyStored();
        boolean prevTanksDirty = state.tanksDirty;
        boolean prevInventoryDirty = state.inventoryDirty;
        boolean wasActive = state.active;
        RecipeHolder<DistillerRecipe> recipeHolder = state.lastRecipeCache;
        FluidStack currentFluid = state.tanks.input().getFluid();
        if (recipeHolder == null || !recipeHolder.value().matches(currentFluid)) {
            recipeHolder = state.recipeGetter.apply(ctx.getLevel().getRawLevel(), currentFluid);
            state.lastRecipeCache = recipeHolder;
        }
        state.active = state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx));
        tryEnqueueProcess(state, ctx.getLevel().getRawLevel(), recipeHolder);
        FluidContainers.emptyBucket(state.tanks.input(), state.inventory, SLOT_INPUT_FILLED, SLOT_INPUT_EMPTY);
        ICFluidUtils.fillFluidContainer(state.tanks.output(), SLOT_OUTPUT_EMPTY, SLOT_OUTPUT_FILLED, state.inventory);
        pumpOutputs(ctx);
        ItemOutputs.eject(state.inventory, state.outputRef, SLOT_INPUT_EMPTY, SLOT_OUTPUT_FILLED, OUTPUT_SLOT);
        boolean activeChanged = wasActive != state.active;
        int currentEnergy = state.energy.getEnergyStored();
        boolean energyChanged = prevEnergy != currentEnergy;
        boolean tanksChanged = prevTanksDirty != state.tanksDirty;
        boolean inventoryChanged = prevInventoryDirty != state.inventoryDirty;
        var queue = state.processor.getQueue();
        int newPercent = -1;
        if (!queue.isEmpty()) {
            int maxTicks = queue.getFirst().getMaxTicks(ctx.getLevel().getRawLevel());
            newPercent = maxTicks > 0 ? queue.getFirst().processTick * 100 / maxTicks : 0;
        }
        boolean percentChanged = newPercent != state.processPercent;
        if (percentChanged) { state.processPercent = newPercent; }
        int maxEnergy = state.energy.getMaxEnergyStored();
        int newComparatorValue = maxEnergy > 0 ? (15 * state.energy.getEnergyStored()) / maxEnergy : 0;
        boolean comparatorChanged = ctx.updateComparatorOutputs(state.lastComparatorValue, newComparatorValue, List.of(REDSTONE_POI));
        state.lastComparatorValue = newComparatorValue;
        boolean update = activeChanged || energyChanged || tanksChanged || inventoryChanged || percentChanged || comparatorChanged;
        if (update) { ctx.markDirtyAndSync(); }
    }

    private void tryEnqueueProcess(State state, Level level, RecipeHolder<DistillerRecipe> recipeHolder) {
        if (state.processor.getQueueSize() >= state.processor.getMaxQueueSize()) { return; }
        if (recipeHolder == null) { return; }
        DistillerRecipe recipe = recipeHolder.value();
        FluidStack inputFluid = state.tanks.input().getFluid();
        if (inputFluid.getAmount() < recipe.getInputAmount()) { return; }
        FluidStack outputFluid = recipe.fluidOutput;
        if (outputFluid != null && !outputFluid.isEmpty() && state.tanks.output().getFluidAmount() + outputFluid.getAmount() > state.tanks.output().getCapacity()) { return; }
        if (!recipe.itemOutput.isEmpty() && ItemOutputs.overflows(state.inventory.getStackInSlot(OUTPUT_SLOT), recipe.itemOutput)) { return; }
        state.processor.addProcessToQueue(new DistillerProcess(recipeHolder), level, false);
    }

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.register(Capabilities.EnergyStorage.BLOCK, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (ENERGY_INPUT_POIS.contains(localPos) && (side == null || side == ENERGY_INPUT_FACING)) {
                return state.energy;
            }
            return null;
        });
        register.register(Capabilities.FluidHandler.BLOCK, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (INPUT_FLUID_POIS.contains(localPos) && (side == null || side == INPUT_FLUID_FACING)) {
                return state.inputCap;
            }
            if (OUTPUT_FLUID_POIS.contains(localPos) && (side == null || side == OUTPUT_FLUID_FACING)) {
                return state.outputCap;
            }
            return null;
        });
        register.register(Capabilities.ItemHandler.BLOCK, (state, position) -> {
            if (position.posInMultiblock().equals(ITEM_OUTPUT_POI.posInMultiblock()) && (position.side() == null || position.side() == ITEM_OUTPUT_POI.face())) {
                return state.itemOutputCap;
            }
            return state.invCap;
        });
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { MultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get("distiller").getter; }

    public static class State implements IDisplaySyncState, IProcessContext.IProcessContextInMachine<DistillerRecipe>, IDataReloadAware {
        public final BiFunction<Level, FluidStack, RecipeHolder<DistillerRecipe>> recipeGetter = RecipeCache.cached(DistillerRecipe::findRecipe);
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final TankPair tanks;
        public IFluidHandler inputCap;
        public IFluidHandler outputCap;
        public IItemHandler invCap;
        public IItemHandler itemOutputCap;
        public Supplier<IItemHandler> outputRef;
        public final ConstrainedItemHandler inventory;
        private final IFluidTank[] tankArray;
        public final MultiblockProcessor.InMachineProcessor<DistillerRecipe> processor;
        public final ICEnergyStorageAveraging energy;
        public boolean active;
        public BooleanSupplier isSoundPlaying = () -> false;
        public boolean tanksDirty = false;
        public boolean inventoryDirty = false;
        public int processPercent = -1;
        public int lastComparatorValue = -1;
        public RecipeHolder<DistillerRecipe> lastRecipeCache;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); this.tanksDirty = true; this.inventoryDirty = true; };
            this.tanks = new TankPair(v -> { onChanged.run(); this.tanksDirty = true; }, inputTankCapacity(), outputTankCapacity());
            this.tankArray = new IFluidTank[]{tanks.input(), tanks.output()};
            inventory = new ConstrainedItemHandler(
                    List.of(
                            ConstrainedItemHandler.IOConstraint.FLUID_INPUT,
                            ConstrainedItemHandler.IOConstraint.OUTPUT,
                            ConstrainedItemHandler.IOConstraint.FLUID_INPUT,
                            ConstrainedItemHandler.IOConstraint.OUTPUT,
                            ConstrainedItemHandler.IOConstraint.OUTPUT
                    ),
                    () -> { onChanged.run(); this.inventoryDirty = true; }
            );
            this.inputCap = new MultiTankFluidHandler(tanks.input(), false, true, () -> { onChanged.run(); this.tanksDirty = true; });
            this.outputCap = new MultiTankFluidHandler(tanks.output(), true, false, () -> { onChanged.run(); this.tanksDirty = true; });
            this.invCap = inventory;
            this.energy = new SyncEnergyStorage(energyCapacity(), energyMaxIo(), onChanged);
            this.processor = new MultiblockProcessor.InMachineProcessor<>(1, 0f, 1, markDirty, DistillerRecipe.RECIPES::getById);
            this.itemOutputCap = new SlotRangeItemHandler(
                    inventory,
                    false,
                    true,
                    List.of(
                            new SlotRangeItemHandler.IntRange(SLOT_INPUT_EMPTY, SLOT_INPUT_EMPTY + 1),
                            new SlotRangeItemHandler.IntRange(SLOT_OUTPUT_FILLED, SLOT_OUTPUT_FILLED + 1),
                            new SlotRangeItemHandler.IntRange(OUTPUT_SLOT, OUTPUT_SLOT + 1)
                    )
            );
            bindOutputs(ctx);
        }

        @Override public void onDataReload(IInitialMultiblockContext<?> context) { lastComparatorValue = -1; bindOutputs(context); }

        private void bindOutputs(IInitialMultiblockContext<?> context) { this.outputRef = context.getCapabilityAt(Capabilities.ItemHandler.BLOCK, ITEM_OUTPUT_POI); }

        public ConstrainedItemHandler getInventory() { return inventory; }

        public TankPair getTanks() { return tanks; }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            rsState.writeSaveNBT(nbt, provider);
            energy.save(nbt, "energy");
            nbt.put("tanks", this.tanks.toNBT(provider));
            nbt.put("processor", processor.toNBT());
            nbt.put("inventory", inventory.serializeNBT(provider));
            nbt.putBoolean("active", active);
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            rsState.readSaveNBT(nbt, provider);
            energy.load(nbt, "energy");
            this.tanks.readNBT(nbt.getCompound("tanks"), provider);
            this.processor.fromNBT(nbt.getList("processor", Tag.TAG_COMPOUND), DistillerProcess::new);
            this.inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
            active = nbt.getBoolean("active");
            tanksDirty = false;
            inventoryDirty = false;
        }

        @Override public ICEnergyStorageAveraging getEnergyStorage() { return energy; }

        @Override public IFluidTank[] getInternalTanks() { return tankArray; }

        @Override public int[] getOutputTanks() { return new int[]{1}; }

        @Override public int[] getOutputSlots() { return new int[]{OUTPUT_SLOT}; }

        @Override public boolean isActive() { return active; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT(provider));
            energy.save(nbt, "energy");
            nbt.put("inventory", inventory.serializeNBT(provider));
            nbt.putInt("processPercent", processPercent);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            energy.load(nbt, "energy");
            inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
            processPercent = nbt.contains("processPercent") ? nbt.getInt("processPercent") : -1;
            tanksDirty = false;
            inventoryDirty = false;
        }

        @Override public void addDisplayLines(Level level, DisplayLines lines) {
            if (processPercent >= 0) { lines.percent(processPercent); }
        }
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        RAW_POIS = pois;
        REDSTONE_POI = MultiblockPOIHelper.getPosList(pois, "redstone0").getFirst();
        INPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_input0");
        OUTPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_output0");
        ENERGY_INPUT_POIS = MultiblockPOIHelper.getPosList(pois, "energy_input0");
        ENERGY_INPUT_FACING = MultiblockPOIHelper.getFacing(pois, "energy_input0");
        ITEM_OUTPUT_POI = new MultiblockFace(MultiblockPOIHelper.getFacing(pois, "item_output0"), MultiblockPOIHelper.getPosList(pois, "item_output0").getFirst());
        INPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_input0");
        OUTPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_output0");
    }
}
