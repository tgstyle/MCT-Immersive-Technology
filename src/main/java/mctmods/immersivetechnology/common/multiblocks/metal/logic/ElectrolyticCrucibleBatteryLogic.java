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

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
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
import net.minecraftforge.items.IItemHandler;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class ElectrolyticCrucibleBatteryLogic implements IMultiblockLogic<ElectrolyticCrucibleBatteryLogic.State>, IServerTickableComponent<ElectrolyticCrucibleBatteryLogic.State>, IClientTickableComponent<ElectrolyticCrucibleBatteryLogic.State>, IFluidOutputPump<ElectrolyticCrucibleBatteryLogic.State> {
    private static List<PoIJSONSchema> RAW_POIS;
    public static BlockPos REDSTONE_POI;
    public static List<BlockPos> COMPARATOR_POSITIONS;
    public static List<BlockPos> INPUT_FLUID_POIS;
    public static List<BlockPos> OUTPUT_FLUID_POIS_0;
    public static List<BlockPos> OUTPUT_FLUID_POIS_1;
    public static List<BlockPos> OUTPUT_FLUID_POIS_2;
    private static List<BlockPos> ENERGY_INPUT_POIS;
    private static RelativeBlockFace ENERGY_INPUT_FACING;
    public static MultiblockFace ITEM_OUTPUT_POI;
    private static RelativeBlockFace OUTPUT_FACING;
    private static List<RelativeBlockFace> OUTPUT_FACINGS;
    private static List<BlockPos> FLUID_OUTPUT_POIS;

    static { ITShapes.readPois("electrolytic_crucible_battery", ElectrolyticCrucibleBatteryLogic::loadPois); }

    public static int inputTankCapacity() { return ServerConfig.electrolyticCrucibleBatteryInputTankCapacity; }

    public static int outputTankCapacity() { return ServerConfig.electrolyticCrucibleBatteryOutputTankCapacity; }

    public static int energyCapacity() { return ServerConfig.electrolyticCrucibleBatteryEnergyCapacity; }

    public static int energyMaxIo() { return ServerConfig.electrolyticCrucibleBatteryEnergyMaxIO; }

    @Override public List<BlockPos> getOutputPositions() { return FLUID_OUTPUT_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FACING); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output0, state.tanks.output1, state.tanks.output2); }

    @Override public List<RelativeBlockFace> getOutputFacings() { return OUTPUT_FACINGS; }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        List<BlockPos> soundPosList = MultiblockPOIHelper.getPosList(RAW_POIS, "sound0");
        if (soundPosList.isEmpty()) { return; }
        Vec3 soundPos = ctx.getLevel().toAbsolute(Vec3.atCenterOf(soundPosList.get(0)));
        if (state.active && ClientUtils.attenuated(soundPos, 32, 1) > 0.01f && !state.isSoundPlaying.getAsBoolean()) { state.isSoundPlaying = MachineSound.startSound(() -> state.active, ctx.isValid(), soundPos, Sounds.electrolyticCrucibleBattery, () -> ClientUtils.attenuated(soundPos, 32, 1), () -> 1f); }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        state.energy.updateAverage();
        int prevEnergy = state.energy.getEnergyStored();
        boolean prevTanksDirty = state.tanksDirty;
        boolean prevInventoryDirty = state.inventoryDirty;
        boolean wasActive = state.active;
        ElectrolyticCrucibleBatteryRecipe recipe = state.recipeGetter.apply(ctx.getLevel().getRawLevel(), state.tanks.input.getFluid());
        state.active = state.processor.tickServer(state, ctx.getLevel(), RedstoneInput.unpowered(ctx, REDSTONE_POI));
        tryEnqueueProcess(state, ctx.getLevel().getRawLevel(), recipe);
        pumpOutputs(ctx);
        ItemOutputs.eject(state.inventory, state.outputRef, 0);
        boolean percentsChanged = updateProcessPercents(state, ctx.getLevel().getRawLevel());
        int outputCapacity = state.tanks.output0.getCapacity();
        int newComparatorValue = outputCapacity > 0 ? (15 * state.tanks.output0.getFluidAmount()) / outputCapacity : 0;
        boolean comparatorChanged = newComparatorValue != state.lastComparatorValue;
        if (comparatorChanged) {
            for (BlockPos pos : COMPARATOR_POSITIONS) { ctx.setComparatorOutputFor(pos, newComparatorValue); }
            state.lastComparatorValue = newComparatorValue;
        }
        if (wasActive != state.active || prevEnergy != state.energy.getEnergyStored() || prevTanksDirty != state.tanksDirty || prevInventoryDirty != state.inventoryDirty || percentsChanged || comparatorChanged) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    private static boolean updateProcessPercents(State state, Level level) {
        var queue = state.processor.getQueue();
        boolean changed = false;
        for (int i = 0; i < state.processPercents.length; i++) {
            int newPercent = -1;
            if (i < queue.size()) {
                int maxTicks = queue.get(i).getMaxTicks(level);
                newPercent = maxTicks > 0 ? queue.get(i).processTick * 100 / maxTicks : 0;
            }
            if (newPercent != state.processPercents[i]) {
                state.processPercents[i] = newPercent;
                changed = true;
            }
        }
        return changed;
    }

    private static void tryEnqueueProcess(State state, Level level, ElectrolyticCrucibleBatteryRecipe recipe) {
        if (state.processor.getQueueSize() >= state.processor.getMaxQueueSize() || recipe == null || state.tanks.input.getFluid().getAmount() < recipe.fluidInput0.getAmount()) { return; }
        if (recipe.fluidOutput0 != null && state.tanks.output0.getFluidAmount() + recipe.fluidOutput0.getAmount() > state.tanks.output0.getCapacity()) { return; }
        if (recipe.fluidOutput1 != null && state.tanks.output1.getFluidAmount() + recipe.fluidOutput1.getAmount() > state.tanks.output1.getCapacity()) { return; }
        if (recipe.fluidOutput2 != null && state.tanks.output2.getFluidAmount() + recipe.fluidOutput2.getAmount() > state.tanks.output2.getCapacity()) { return; }
        state.processor.addProcessToQueue(new ElectrolyticCrucibleBatteryProcess(recipe), level, false);
    }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        State state = ctx.getState();
        BlockPos localPos = position.posInMultiblock();
        RelativeBlockFace side = position.side();
        if (cap == ForgeCapabilities.ENERGY) {
            if (ENERGY_INPUT_POIS.contains(localPos) && (side == null || side == ENERGY_INPUT_FACING)) { return state.energyCap.cast(ctx); }
        }
        else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (INPUT_FLUID_POIS.contains(localPos) && (side == null || side == MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_input0"))) { return state.inputCap.cast(ctx); }
            if (OUTPUT_FLUID_POIS_0.contains(localPos) && (side == null || side == MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output0"))) { return state.outputCap0.cast(ctx); }
            if (OUTPUT_FLUID_POIS_1.contains(localPos) && (side == null || side == MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output1"))) { return state.outputCap1.cast(ctx); }
            if (OUTPUT_FLUID_POIS_2.contains(localPos) && (side == null || side == MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output2"))) { return state.outputCap2.cast(ctx); }
        }
        else if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (localPos.equals(ITEM_OUTPUT_POI.posInMultiblock()) && (side == null || side == ITEM_OUTPUT_POI.face())) { return state.itemOutputCap.cast(ctx); }
            return state.invCap.cast(ctx);
        }
        return LazyOptional.empty();
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { MultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get("electrolytic_crucible_battery").getter; }

    public static class State implements IDisplaySyncState, IProcessContext.ProcessContextInMachine<ElectrolyticCrucibleBatteryRecipe>, IDataReloadAware {
        public final BiFunction<Level, FluidStack, ElectrolyticCrucibleBatteryRecipe> recipeGetter = RecipeCache.cached(ElectrolyticCrucibleBatteryRecipe::findRecipe);
        public final ElectrolyticCrucibleBatteryTanks tanks;
        public final StoredCapability<IEnergyStorage> energyCap;
        public final StoredCapability<IFluidHandler> inputCap;
        public final StoredCapability<IFluidHandler> outputCap0;
        public final StoredCapability<IFluidHandler> outputCap1;
        public final StoredCapability<IFluidHandler> outputCap2;
        public final StoredCapability<IItemHandler> invCap;
        public final StoredCapability<IItemHandler> itemOutputCap;
        public CapabilityReference<IItemHandler> outputRef;
        public final ConstrainedItemHandler inventory;
        private final IFluidTank[] tankArray;
        public final MultiblockProcessor.InMachineProcessor<ElectrolyticCrucibleBatteryRecipe> processor;
        public final AveragingEnergyStorage energy;
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
            this.tanks = new ElectrolyticCrucibleBatteryTanks(v -> onChanged.run());
            this.tankArray = new IFluidTank[]{tanks.input, tanks.output0, tanks.output1, tanks.output2};
            inventory = new ConstrainedItemHandler(List.of(ConstrainedItemHandler.IOConstraint.OUTPUT), onChanged);
            this.inputCap = new StoredCapability<>(new MultiTankFluidHandler(tanks.input, false, true, onChanged));
            this.outputCap0 = new StoredCapability<>(new MultiTankFluidHandler(tanks.output0, true, false, onChanged));
            this.outputCap1 = new StoredCapability<>(new MultiTankFluidHandler(tanks.output1, true, false, onChanged));
            this.outputCap2 = new StoredCapability<>(new MultiTankFluidHandler(tanks.output2, true, false, onChanged));
            this.invCap = new StoredCapability<>(inventory);
            this.energy = new SyncEnergyStorage(energyCapacity(), energyMaxIo(), onChanged);
            this.energyCap = new StoredCapability<>(this.energy);
            this.processor = new MultiblockProcessor.InMachineProcessor<>(3, 0f, 3, markDirty, ElectrolyticCrucibleBatteryRecipe.RECIPES::getById);
            this.itemOutputCap = new StoredCapability<>(inventory);
            bindOutputs(ctx);
        }

        @Override public void onDataReload(IInitialMultiblockContext<?> context) {
            lastComparatorValue = -1;
            bindOutputs(context);
        }

        private void bindOutputs(IInitialMultiblockContext<?> context) { this.outputRef = context.getCapabilityAt(ForgeCapabilities.ITEM_HANDLER, ITEM_OUTPUT_POI); }

        public ConstrainedItemHandler getInventory() { return inventory; }

        public ElectrolyticCrucibleBatteryTanks getTanks() { return tanks; }

        @Override public void writeSaveNBT(CompoundTag nbt) {
            nbt.put("energy", energy.serializeNBT());
            nbt.put("tanks", this.tanks.toNBT());
            nbt.put("processor", processor.toNBT());
            nbt.put("inventory", inventory.serializeNBT());
            nbt.putBoolean("active", active);
        }

        @Override public void readSaveNBT(CompoundTag nbt) {
            energy.deserializeNBT(nbt.get("energy"));
            this.tanks.readNBT(nbt.getCompound("tanks"));
            this.processor.fromNBT(nbt.getList("processor", Tag.TAG_COMPOUND), ElectrolyticCrucibleBatteryProcess::new);
            this.inventory.deserializeNBT(nbt.getCompound("inventory"));
            active = nbt.getBoolean("active");
            tanksDirty = false;
            inventoryDirty = false;
        }

        @Override public AveragingEnergyStorage getEnergy() { return energy; }

        @Override public IFluidTank[] getInternalTanks() { return tankArray; }

        @Override public int[] getOutputTanks() { return new int[]{1, 2, 3}; }

        @Override public int[] getOutputSlots() { return new int[0]; }

        @Override public boolean isActive() { return active; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT());
            nbt.put("energy", energy.serializeNBT());
            nbt.put("inventory", inventory.serializeNBT());
            nbt.putIntArray("processPercents", processPercents);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt) {
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"));
            energy.deserializeNBT(nbt.get("energy"));
            inventory.deserializeNBT(nbt.getCompound("inventory"));
            int[] percents = nbt.getIntArray("processPercents");
            processPercents = percents.length == 3 ? percents : new int[]{-1, -1, -1};
            tanksDirty = false;
            inventoryDirty = false;
        }

        @Override public void addDisplayLines(Level level, DisplayLines lines) {
            for (int percent : processPercents) {
                if (percent >= 0) { lines.percent(percent); }
            }
        }
    }

    public record ElectrolyticCrucibleBatteryTanks(MarkableFluidTank input, MarkableFluidTank output0, MarkableFluidTank output1, MarkableFluidTank output2) {
        public ElectrolyticCrucibleBatteryTanks(Consumer<Void> markDirty) { this(new MarkableFluidTank(inputTankCapacity(), markDirty), new MarkableFluidTank(outputTankCapacity(), markDirty), new MarkableFluidTank(outputTankCapacity(), markDirty), new MarkableFluidTank(outputTankCapacity(), markDirty)); }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("input", input.writeToNBT(new CompoundTag()));
            tag.put("output0", output0.writeToNBT(new CompoundTag()));
            tag.put("output1", output1.writeToNBT(new CompoundTag()));
            tag.put("output2", output2.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) {
            input.readFromNBT(tag.getCompound("input"));
            output0.readFromNBT(tag.getCompound("output0"));
            output1.readFromNBT(tag.getCompound("output1"));
            output2.readFromNBT(tag.getCompound("output2"));
        }
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        RAW_POIS = pois;
        REDSTONE_POI = MultiblockPOIHelper.getPosList(pois, "redstone0").get(0);
        COMPARATOR_POSITIONS = MultiblockPOIHelper.getPosList(pois, "comparator0");
        INPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_input0");
        OUTPUT_FLUID_POIS_0 = MultiblockPOIHelper.getPosList(pois, "fluid_output0");
        OUTPUT_FLUID_POIS_1 = MultiblockPOIHelper.getPosList(pois, "fluid_output1");
        OUTPUT_FLUID_POIS_2 = MultiblockPOIHelper.getPosList(pois, "fluid_output2");
        ENERGY_INPUT_POIS = MultiblockPOIHelper.getPosList(pois, "energy_input0");
        ENERGY_INPUT_FACING = MultiblockPOIHelper.getFacing(pois, "energy_input0");
        ITEM_OUTPUT_POI = new MultiblockFace(MultiblockPOIHelper.getFacing(pois, "item_output0"), MultiblockPOIHelper.getPosList(pois, "item_output0").get(0));
        OUTPUT_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_output0");
        OUTPUT_FACINGS = ImmutableList.of(MultiblockPOIHelper.getFacing(pois, "fluid_output0"), MultiblockPOIHelper.getFacing(pois, "fluid_output1"), MultiblockPOIHelper.getFacing(pois, "fluid_output2"));
        FLUID_OUTPUT_POIS = ImmutableList.of(OUTPUT_FLUID_POIS_0.get(0), OUTPUT_FLUID_POIS_1.get(0), OUTPUT_FLUID_POIS_2.get(0));
    }
}
