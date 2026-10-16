package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.client.util.ClientUtils;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.process.ElectrolyticCrucibleBatteryProcess;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.ElectrolyticCrucibleBatteryRecipe;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.util.IDisplaySyncState;
import mctmods.immersivetechnology.core.util.ItemOutputs;
import mctmods.immersivetechnology.core.util.RedstoneInput;
import mctmods.immersivetechnology.core.util.SyncEnergyStorage;

import com.immersiveconvergence.api.energy.ICEnergyStorageAveraging;
import com.immersiveconvergence.api.multiblock.blocks.component.IClientTickableComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.IMultiblockComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.IServerTickableComponent;
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
import com.immersiveconvergence.api.util.MarkableFluidTank;
import com.immersiveconvergence.api.util.MultiBlockInventoryUtils;
import com.immersiveconvergence.api.util.MultiTankFluidHandler;
import com.immersiveconvergence.api.util.RecipeCache;
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
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ElectrolyticCrucibleBatteryLogic implements IMultiblockLogic<ElectrolyticCrucibleBatteryLogic.State>, IServerTickableComponent<ElectrolyticCrucibleBatteryLogic.State>, IClientTickableComponent<ElectrolyticCrucibleBatteryLogic.State>, IFluidOutputPump<ElectrolyticCrucibleBatteryLogic.State> {
    public static int inputTankCapacity() { return ServerConfig.electrolyticCrucibleBatteryInputTankCapacity; }
    public static int outputTankCapacity() { return ServerConfig.electrolyticCrucibleBatteryOutputTankCapacity; }
    public static int energyCapacity() { return ServerConfig.electrolyticCrucibleBatteryEnergyCapacity; }
    public static int energyMaxIo() { return ServerConfig.electrolyticCrucibleBatteryEnergyMaxIO; }

    private static List<PoIJSONSchema> RAW_POIS;

    public static BlockPos REDSTONE_POI;
    public static List<BlockPos> COMPARATOR_POSITIONS;
    public static List<BlockPos> INPUT_FLUID_POIS;
    public static List<BlockPos> OUTPUT_FLUID_POIS_0;
    public static List<BlockPos> OUTPUT_FLUID_POIS_1;
    public static List<BlockPos> OUTPUT_FLUID_POIS_2;
    private static List<BlockPos> ENERGY_INPUT_POIS;
    private static RelativeBlockFace ENERGY_INPUT_FACING;
    private static RelativeBlockFace INPUT_FLUID_FACING;
    private static RelativeBlockFace OUTPUT_FLUID_FACING_0;
    private static RelativeBlockFace OUTPUT_FLUID_FACING_1;
    private static RelativeBlockFace OUTPUT_FLUID_FACING_2;
    public static MultiblockFace ITEM_OUTPUT_POI;
    private static RelativeBlockFace OUTPUT_FACING;
    private static List<RelativeBlockFace> OUTPUT_FACINGS;

    private static List<BlockPos> FLUID_OUTPUT_POIS;

    static { ITShapes.readPois("electrolytic_crucible_battery", ElectrolyticCrucibleBatteryLogic::loadPois); }

    @Override public List<BlockPos> getOutputPositions() { return FLUID_OUTPUT_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FACING); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output0, state.tanks.output1, state.tanks.output2); }

    @Override public List<RelativeBlockFace> getOutputFacings() { return OUTPUT_FACINGS; }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        List<BlockPos> soundPosList = MultiblockPOIHelper.getPosList(RAW_POIS, "sound0");
        if (soundPosList.isEmpty()) { return; }
        BlockPos soundBlockPos = soundPosList.getFirst();
        Vec3 soundPos = ctx.getLevel().toAbsolute(Vec3.atCenterOf(soundBlockPos));
        if (Minecraft.getInstance().player == null) { return; }
        if (state.active && ClientUtils.attenuated(soundPos, 32, 1) > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
            state.isSoundPlaying = MachineSound.startSound(() -> state.active, ctx.isValid(), soundPos, Sounds.electrolyticCrucibleBattery, () -> ClientUtils.attenuated(soundPos, 32, 1), () -> 1f);
        }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        state.energy.updateAverage();
        int prevEnergy = state.energy.getEnergyStored();
        boolean prevTanksDirty = state.tanksDirty;
        boolean prevInventoryDirty = state.inventoryDirty;
        boolean wasActive = state.active;
        RecipeHolder<ElectrolyticCrucibleBatteryRecipe> holder = state.recipeGetter.apply(ctx.getLevel().getRawLevel(), state.tanks.input.getFluid());
        state.active = state.processor.tickServer(state, ctx.getLevel(), RedstoneInput.unpowered(ctx, REDSTONE_POI));
        tryEnqueueProcess(state, ctx.getLevel().getRawLevel(), holder);
        pumpOutputs(ctx);
        ItemOutputs.eject(state.inventory, state.outputRef, 0);
        boolean activeChanged = wasActive != state.active;
        int currentEnergy = state.energy.getEnergyStored();
        boolean energyChanged = prevEnergy != currentEnergy;
        boolean tanksChanged = prevTanksDirty != state.tanksDirty;
        boolean inventoryChanged = prevInventoryDirty != state.inventoryDirty;
        boolean percentsChanged = updateProcessPercents(state, ctx.getLevel().getRawLevel());
        int outputCapacity = state.tanks.output0.getCapacity();
        int newComparatorValue = outputCapacity > 0 ? (15 * state.tanks.output0.getFluidAmount()) / outputCapacity : 0;
        boolean comparatorChanged = ctx.updateComparatorOutputs(state.lastComparatorValue, newComparatorValue, COMPARATOR_POSITIONS);
        state.lastComparatorValue = newComparatorValue;
        boolean update = activeChanged || energyChanged || tanksChanged || inventoryChanged || percentsChanged || comparatorChanged;
        if (update) { ctx.markDirtyAndSync(); }
    }

    private boolean updateProcessPercents(State state, Level level) {
        var queue = state.processor.getQueue();
        boolean changed = false;
        for (int i = 0; i < state.processPercents.length; i++) {
            int newPercent = -1;
            if (i < queue.size()) {
                int maxTicks = queue.get(i).getMaxTicks(level);
                newPercent = maxTicks > 0 ? queue.get(i).processTick * 100 / maxTicks : 0;
            }
            if (newPercent != state.processPercents[i]) { state.processPercents[i] = newPercent; changed = true; }
        }
        return changed;
    }

    private void tryEnqueueProcess(State state, Level level, RecipeHolder<ElectrolyticCrucibleBatteryRecipe> holder) {
        if (state.processor.getQueueSize() >= state.processor.getMaxQueueSize()) { return; }
        if (holder == null) { return; }
        ElectrolyticCrucibleBatteryRecipe recipe = holder.value();
        FluidStack inputFluid = state.tanks.input.getFluid();
        if (inputFluid.getAmount() < recipe.getInputAmount()) { return; }
        if (recipe.fluidOutput0 != null && state.tanks.output0.getFluidAmount() + recipe.fluidOutput0.getAmount() > state.tanks.output0.getCapacity()) { return; }
        if (recipe.fluidOutput1 != null && state.tanks.output1.getFluidAmount() + recipe.fluidOutput1.getAmount() > state.tanks.output1.getCapacity()) { return; }
        if (recipe.fluidOutput2 != null && state.tanks.output2.getFluidAmount() + recipe.fluidOutput2.getAmount() > state.tanks.output2.getCapacity()) { return; }
        state.processor.addProcessToQueue(new ElectrolyticCrucibleBatteryProcess(holder), level, false);
    }

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.register(Capabilities.EnergyStorage.BLOCK, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (ENERGY_INPUT_POIS.contains(localPos) && (side == null || side == ENERGY_INPUT_FACING)) { return state.energy; }
            return null;
        });
        register.register(Capabilities.FluidHandler.BLOCK, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (INPUT_FLUID_POIS.contains(localPos) && (side == null || side == INPUT_FLUID_FACING)) { return state.inputCap; }
            if (OUTPUT_FLUID_POIS_0.contains(localPos) && (side == null || side == OUTPUT_FLUID_FACING_0)) { return state.outputCap0; }
            if (OUTPUT_FLUID_POIS_1.contains(localPos) && (side == null || side == OUTPUT_FLUID_FACING_1)) { return state.outputCap1; }
            if (OUTPUT_FLUID_POIS_2.contains(localPos) && (side == null || side == OUTPUT_FLUID_FACING_2)) { return state.outputCap2; }
            return null;
        });
        register.register(Capabilities.ItemHandler.BLOCK, (state, position) -> {
            if (position.posInMultiblock().equals(ITEM_OUTPUT_POI.posInMultiblock()) && (position.side() == null || position.side() == ITEM_OUTPUT_POI.face())) { return state.itemOutputCap; }
            return state.invCap;
        });
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { MultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get("electrolytic_crucible_battery").getter; }

    public static class State implements IDisplaySyncState, IProcessContext.IProcessContextInMachine<ElectrolyticCrucibleBatteryRecipe>, IDataReloadAware {
        public final BiFunction<Level, FluidStack, RecipeHolder<ElectrolyticCrucibleBatteryRecipe>> recipeGetter = RecipeCache.cached(ElectrolyticCrucibleBatteryRecipe::findRecipe);
        public final ElectrolyticCrucibleBatteryTanks tanks;
        public IFluidHandler inputCap;
        public IFluidHandler outputCap0;
        public IFluidHandler outputCap1;
        public IFluidHandler outputCap2;
        public IItemHandler invCap;
        public IItemHandler itemOutputCap;
        public Supplier<IItemHandler> outputRef;
        public final ConstrainedItemHandler inventory;
        private final IFluidTank[] tankArray;
        public final MultiblockProcessor.InMachineProcessor<ElectrolyticCrucibleBatteryRecipe> processor;
        public final ICEnergyStorageAveraging energy;
        public boolean active;
        public BooleanSupplier isSoundPlaying = () -> false;
        public boolean tanksDirty = false;
        public boolean inventoryDirty = false;
        public int[] processPercents = new int[]{-1, -1, -1};
        public int lastComparatorValue = -1;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); this.tanksDirty = true; this.inventoryDirty = true; };
            this.tanks = new ElectrolyticCrucibleBatteryTanks(v -> { onChanged.run(); this.tanksDirty = true; });
            this.tankArray = new IFluidTank[]{tanks.input, tanks.output0, tanks.output1, tanks.output2};
            inventory = new ConstrainedItemHandler(List.of(ConstrainedItemHandler.IOConstraint.OUTPUT), () -> { onChanged.run(); this.inventoryDirty = true; });
            this.inputCap = new MultiTankFluidHandler(tanks.input, false, true, () -> { onChanged.run(); this.tanksDirty = true; });
            this.outputCap0 = new MultiTankFluidHandler(tanks.output0, true, false, () -> { onChanged.run(); this.tanksDirty = true; });
            this.outputCap1 = new MultiTankFluidHandler(tanks.output1, true, false, () -> { onChanged.run(); this.tanksDirty = true; });
            this.outputCap2 = new MultiTankFluidHandler(tanks.output2, true, false, () -> { onChanged.run(); this.tanksDirty = true; });
            this.invCap = inventory;
            this.itemOutputCap = inventory;
            this.energy = new SyncEnergyStorage(energyCapacity(), energyMaxIo(), onChanged);
            this.processor = new MultiblockProcessor.InMachineProcessor<>(3, 0f, 3, markDirty, ElectrolyticCrucibleBatteryRecipe.RECIPES::getById);
            bindOutputs(ctx);
        }

        @Override public void onDataReload(IInitialMultiblockContext<?> context) { lastComparatorValue = -1; bindOutputs(context); }

        private void bindOutputs(IInitialMultiblockContext<?> context) { this.outputRef = context.getCapabilityAt(Capabilities.ItemHandler.BLOCK, ITEM_OUTPUT_POI); }

        public ConstrainedItemHandler getInventory() { return inventory; }
        public ElectrolyticCrucibleBatteryTanks getTanks() { return tanks; }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            energy.save(nbt, "energy");
            nbt.put("tanks", this.tanks.toNBT(provider));
            nbt.put("processor", processor.toNBT());
            nbt.put("inventory", inventory.serializeNBT(provider));
            nbt.putBoolean("active", active);
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            energy.load(nbt, "energy");
            this.tanks.readNBT(nbt.getCompound("tanks"), provider);
            this.processor.fromNBT(nbt.getList("processor", Tag.TAG_COMPOUND), ElectrolyticCrucibleBatteryProcess::new);
            this.inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
            active = nbt.getBoolean("active");
            tanksDirty = false;
            inventoryDirty = false;
        }

        @Override public ICEnergyStorageAveraging getEnergyStorage() { return energy; }
        @Override public IFluidTank[] getInternalTanks() { return tankArray; }
        @Override public int[] getOutputTanks() { return new int[]{1, 2, 3}; }
        @Override public int[] getOutputSlots() { return new int[0]; }
        @Override public boolean isActive() { return active; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT(provider));
            energy.save(nbt, "energy");
            nbt.put("inventory", inventory.serializeNBT(provider));
            nbt.putIntArray("processPercents", processPercents);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            energy.load(nbt, "energy");
            inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
            int[] percents = nbt.getIntArray("processPercents");
            processPercents = percents.length == 3 ? percents : new int[]{-1, -1, -1};
            tanksDirty = false;
            inventoryDirty = false;
        }

        @Override public void addDisplayLines(Level level, DisplayLines lines) {
            for (int percent : processPercents) { if (percent >= 0) { lines.percent(percent); } }
        }
    }

    public record ElectrolyticCrucibleBatteryTanks(MarkableFluidTank input, MarkableFluidTank output0, MarkableFluidTank output1, MarkableFluidTank output2) {
        public ElectrolyticCrucibleBatteryTanks(Consumer<Void> markDirty) {
            this(new MarkableFluidTank(inputTankCapacity(), markDirty), new MarkableFluidTank(outputTankCapacity(), markDirty), new MarkableFluidTank(outputTankCapacity(), markDirty), new MarkableFluidTank(outputTankCapacity(), markDirty));
        }

        public CompoundTag toNBT(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.put("input", input.writeToNBT(provider, new CompoundTag()));
            tag.put("output0", output0.writeToNBT(provider, new CompoundTag()));
            tag.put("output1", output1.writeToNBT(provider, new CompoundTag()));
            tag.put("output2", output2.writeToNBT(provider, new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag, HolderLookup.Provider provider) {
            input.readFromNBT(provider, tag.getCompound("input"));
            output0.readFromNBT(provider, tag.getCompound("output0"));
            output1.readFromNBT(provider, tag.getCompound("output1"));
            output2.readFromNBT(provider, tag.getCompound("output2"));
        }
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        RAW_POIS = pois;
        REDSTONE_POI = MultiblockPOIHelper.getPosList(pois, "redstone0").getFirst();
        COMPARATOR_POSITIONS = MultiblockPOIHelper.getPosList(pois, "comparator0");
        INPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_input0");
        OUTPUT_FLUID_POIS_0 = MultiblockPOIHelper.getPosList(pois, "fluid_output0");
        OUTPUT_FLUID_POIS_1 = MultiblockPOIHelper.getPosList(pois, "fluid_output1");
        OUTPUT_FLUID_POIS_2 = MultiblockPOIHelper.getPosList(pois, "fluid_output2");
        ENERGY_INPUT_POIS = MultiblockPOIHelper.getPosList(pois, "energy_input0");
        ENERGY_INPUT_FACING = MultiblockPOIHelper.getFacing(pois, "energy_input0");
        INPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_input0");
        OUTPUT_FLUID_FACING_0 = MultiblockPOIHelper.getFacing(pois, "fluid_output0");
        OUTPUT_FLUID_FACING_1 = MultiblockPOIHelper.getFacing(pois, "fluid_output1");
        OUTPUT_FLUID_FACING_2 = MultiblockPOIHelper.getFacing(pois, "fluid_output2");
        ITEM_OUTPUT_POI = new MultiblockFace(MultiblockPOIHelper.getFacing(pois, "item_output0"), MultiblockPOIHelper.getPosList(pois, "item_output0").getFirst());
        OUTPUT_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_output0");
        OUTPUT_FACINGS = ImmutableList.of(MultiblockPOIHelper.getFacing(pois, "fluid_output0"), MultiblockPOIHelper.getFacing(pois, "fluid_output1"), MultiblockPOIHelper.getFacing(pois, "fluid_output2"));
        FLUID_OUTPUT_POIS = ImmutableList.of(OUTPUT_FLUID_POIS_0.getFirst(), OUTPUT_FLUID_POIS_1.getFirst(), OUTPUT_FLUID_POIS_2.getFirst());
    }
}
