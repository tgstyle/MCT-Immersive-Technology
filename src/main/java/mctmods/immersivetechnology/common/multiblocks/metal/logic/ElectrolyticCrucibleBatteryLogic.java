package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.common.multiblocks.helper.*;
import mctmods.immersivetechnology.common.multiblocks.metal.process.ElectrolyticCrucibleBatteryProcess;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.ElectrolyticCrucibleBatteryRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.ElectrolyticCrucibleBatteryShape;
import mctmods.immersivetechnology.common.fluids.helper.ArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.MarkableFluidTank;
import mctmods.immersivetechnology.core.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.core.lib.ModSound;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.util.Utils;
import mctmods.immersivetechnology.core.util.CachedRecipe;

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
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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
import net.minecraftforge.items.IItemHandlerModifiable;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.BiFunction;

public class ElectrolyticCrucibleBatteryLogic implements IMultiblockLogic<ElectrolyticCrucibleBatteryLogic.State>, IServerTickableComponent<ElectrolyticCrucibleBatteryLogic.State>, IClientTickableComponent<ElectrolyticCrucibleBatteryLogic.State>, IPressurizedFluidOutput<ElectrolyticCrucibleBatteryLogic.State> {

    public static int inputTankCapacity() { return ServerConfig.electrolyticCrucibleBatteryInputTankCapacity; }
    public static int outputTankCapacity() { return ServerConfig.electrolyticCrucibleBatteryOutputTankCapacity; }
    public static int energyCapacity() { return ServerConfig.electrolyticCrucibleBatteryEnergyCapacity; }

    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(ElectrolyticCrucibleBatteryShape.DATA.pointsOfInterest);

    public static final BlockPos REDSTONE_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "redstone0").get(0);
    public static final List<BlockPos> COMPARATOR_POSITIONS = MultiblockPOIHelper.getPosList(RAW_POIS, "comparator0");
    public static final List<BlockPos> INPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_input0");
    public static final List<BlockPos> OUTPUT_FLUID_POIS_0 = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output0");
    public static final List<BlockPos> OUTPUT_FLUID_POIS_1 = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output1");
    public static final List<BlockPos> OUTPUT_FLUID_POIS_2 = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output2");
    private static final List<BlockPos> ENERGY_INPUT_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "energy_input0");
    private static final RelativeBlockFace ENERGY_INPUT_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "energy_input0");
    public static final MultiblockFace ITEM_OUTPUT_POI = new MultiblockFace(MultiblockPOIHelper.getFacing(RAW_POIS, "item_output0"), MultiblockPOIHelper.getPosList(RAW_POIS, "item_output0").get(0));
    private static final RelativeBlockFace OUTPUT_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output0");
    private static final List<RelativeBlockFace> OUTPUT_FACINGS = ImmutableList.of(
            MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output0"),
            MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output1"),
            MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output2")
    );

    private static final List<BlockPos> FLUID_OUTPUT_POIS = ImmutableList.of(OUTPUT_FLUID_POIS_0.get(0), OUTPUT_FLUID_POIS_1.get(0), OUTPUT_FLUID_POIS_2.get(0));

    @Override public List<BlockPos> getOutputPositions() { return FLUID_OUTPUT_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FACING); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output0, state.tanks.output1, state.tanks.output2); }

    @Override public List<RelativeBlockFace> getOutputFacings() { return OUTPUT_FACINGS; }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        List<BlockPos> soundPosList = MultiblockPOIHelper.getPosList(RAW_POIS, "sound0");
        if (soundPosList.isEmpty()) { return; }
        BlockPos soundBlockPos = soundPosList.get(0);
        Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(soundBlockPos.getX() + 0.5, soundBlockPos.getY() + 0.5, soundBlockPos.getZ() + 0.5));
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) { return; }
        float distSq = (float) player.distanceToSqr(soundPos);
        float attenuation = Math.max(distSq / 32f, 1f);
        float vol = 1f / attenuation;
        if (state.active && vol > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
            state.isSoundPlaying = ModSound.startSound(() -> state.active, ctx.isValid(), soundPos, Sounds.electrolyticCrucibleBattery, () -> {
                LocalPlayer p = Minecraft.getInstance().player;
                if (p == null) { return 0f; }
                float a = (float) Math.max(p.distanceToSqr(soundPos) / 32f, 1f);
                return 1f / a;
            }, () -> 1f);
        }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        state.energy.updateAverage();
        int prevEnergy = state.energy.getEnergyStored();
        boolean prevTanksDirty = state.tanksDirty;
        boolean prevInventoryDirty = state.inventoryDirty;
        boolean wasActive = state.active;
        ElectrolyticCrucibleBatteryRecipe recipe = state.recipeGetter.apply(ctx.getLevel().getRawLevel(), state.tanks.input.getFluid());
        state.active = state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx));
        tryEnqueueProcess(state, ctx.getLevel().getRawLevel(), recipe);
        pumpOutputs(ctx);
        IItemHandlerModifiable inventory = state.inventory;
        ItemStack itemOutput = inventory.getStackInSlot(0);
        if (!itemOutput.isEmpty()) { itemOutput = Utils.insertStackIntoInventory(state.outputRef, itemOutput, false); inventory.setStackInSlot(0, itemOutput); }
        boolean activeChanged = wasActive != state.active;
        int currentEnergy = state.energy.getEnergyStored();
        boolean energyChanged = prevEnergy != currentEnergy;
        boolean tanksChanged = prevTanksDirty != state.tanksDirty;
        boolean inventoryChanged = prevInventoryDirty != state.inventoryDirty;
        boolean percentsChanged = updateProcessPercents(state, ctx.getLevel().getRawLevel());
        int outputCapacity = state.tanks.output0.getCapacity();
        int newComparatorValue = outputCapacity > 0 ? (15 * state.tanks.output0.getFluidAmount()) / outputCapacity : 0;
        boolean comparatorChanged = newComparatorValue != state.lastComparatorValue;
        if (comparatorChanged) { for (BlockPos pos : COMPARATOR_POSITIONS) { ctx.setComparatorOutputFor(pos, newComparatorValue); } state.lastComparatorValue = newComparatorValue; }
        boolean update = activeChanged || energyChanged || tanksChanged || inventoryChanged || percentsChanged || comparatorChanged;
        if (update) { ctx.markMasterDirty(); ctx.requestMasterBESync(); }
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

    private void tryEnqueueProcess(State state, Level level, ElectrolyticCrucibleBatteryRecipe recipe) {
        if (state.processor.getQueueSize() >= state.processor.getMaxQueueSize()) { return; }
        if (recipe == null) { return; }
        FluidStack inputFluid = state.tanks.input.getFluid();
        if (inputFluid.getAmount() < recipe.fluidInput0.getAmount()) { return; }
        if (recipe.fluidOutput0 != null && state.tanks.output0.getFluidAmount() + recipe.fluidOutput0.getAmount() > state.tanks.output0.getCapacity()) { return; }
        if (recipe.fluidOutput1 != null && state.tanks.output1.getFluidAmount() + recipe.fluidOutput1.getAmount() > state.tanks.output1.getCapacity()) { return; }
        if (recipe.fluidOutput2 != null && state.tanks.output2.getFluidAmount() + recipe.fluidOutput2.getAmount() > state.tanks.output2.getCapacity()) { return; }
        ElectrolyticCrucibleBatteryProcess process = new ElectrolyticCrucibleBatteryProcess(recipe);
        process.setInputTanks(0);
        state.processor.addProcessToQueue(process, level, false);
    }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        State state = ctx.getState();
        BlockPos localPos = position.posInMultiblock();
        RelativeBlockFace side = position.side();
        if (cap == ForgeCapabilities.ENERGY) {
            if (ENERGY_INPUT_POIS.contains(localPos) && (side == null || side == ENERGY_INPUT_FACING)) { return state.energyCap.cast(ctx); }
        } else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (INPUT_FLUID_POIS.contains(localPos) && (side == null || side == MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_input0"))) { return state.inputCap.cast(ctx); }
            if (OUTPUT_FLUID_POIS_0.contains(localPos) && (side == null || side == MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output0"))) { return state.outputCap0.cast(ctx); }
            if (OUTPUT_FLUID_POIS_1.contains(localPos) && (side == null || side == MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output1"))) { return state.outputCap1.cast(ctx); }
            if (OUTPUT_FLUID_POIS_2.contains(localPos) && (side == null || side == MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output2"))) { return state.outputCap2.cast(ctx); }
        } else if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (position.posInMultiblock().equals(ITEM_OUTPUT_POI.posInMultiblock()) && (position.side() == null || position.side() == ITEM_OUTPUT_POI.face())) { return state.itemOutputCap.cast(ctx); }
            return state.invCap.cast(ctx);
        }
        return LazyOptional.empty();
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { MultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ElectrolyticCrucibleBatteryShape.GETTER; }

    public static class State implements IMultiblockState, IProcessContext.ProcessContextInMachine<ElectrolyticCrucibleBatteryRecipe>, IDisplayContext {
        public final BiFunction<Level, FluidStack, ElectrolyticCrucibleBatteryRecipe> recipeGetter = CachedRecipe.cached(ElectrolyticCrucibleBatteryRecipe::findRecipe);
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final ElectrolyticCrucibleBatteryTanks tanks;
        public final StoredCapability<IEnergyStorage> energyCap;
        public final StoredCapability<IFluidHandler> inputCap;
        public final StoredCapability<IFluidHandler> outputCap0;
        public final StoredCapability<IFluidHandler> outputCap1;
        public final StoredCapability<IFluidHandler> outputCap2;
        public final StoredCapability<IItemHandler> invCap;
        public final StoredCapability<IItemHandler> itemOutputCap;
        public final CapabilityReference<IItemHandler> outputRef;
        public final SlotwiseItemHandler inventory;
        private final IFluidTank[] tankArray;
        public final MultiblockProcessor.InMachineProcessor<ElectrolyticCrucibleBatteryRecipe> processor;
        public AveragingEnergyStorage energy;
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
            inventory = new SlotwiseItemHandler(List.of(SlotwiseItemHandler.IOConstraint.OUTPUT), () -> { onChanged.run(); this.inventoryDirty = true; });
            this.inputCap = new StoredCapability<>(new ArrayFluidHandler(tanks.input, false, true, () -> { onChanged.run(); this.tanksDirty = true; }));
            this.outputCap0 = new StoredCapability<>(new ArrayFluidHandler(tanks.output0, true, false, () -> { onChanged.run(); this.tanksDirty = true; }));
            this.outputCap1 = new StoredCapability<>(new ArrayFluidHandler(tanks.output1, true, false, () -> { onChanged.run(); this.tanksDirty = true; }));
            this.outputCap2 = new StoredCapability<>(new ArrayFluidHandler(tanks.output2, true, false, () -> { onChanged.run(); this.tanksDirty = true; }));
            this.invCap = new StoredCapability<>(inventory);
            this.energy = new SyncEnergyStorage(energyCapacity(), onChanged);
            this.energyCap = new StoredCapability<>(this.energy);
            this.processor = new MultiblockProcessor.InMachineProcessor<>(3, 0f, 3, markDirty, ElectrolyticCrucibleBatteryRecipe.RECIPES::getById);
            this.itemOutputCap = new StoredCapability<>(inventory);
            this.outputRef = ctx.getCapabilityAt(ForgeCapabilities.ITEM_HANDLER, ITEM_OUTPUT_POI);
        }

        public SlotwiseItemHandler getInventory() { return inventory; }
        public ElectrolyticCrucibleBatteryTanks getTanks() { return tanks; }

        @Override public void writeSaveNBT(CompoundTag nbt) { nbt.put("energy", energy.serializeNBT()); nbt.put("tanks", this.tanks.toNBT()); nbt.put("processor", processor.toNBT()); nbt.put("inventory", inventory.serializeNBT()); nbt.putBoolean("active", active); }

        @Override public void readSaveNBT(CompoundTag nbt) { energy.deserializeNBT(nbt.get("energy")); this.tanks.readNBT(nbt.getCompound("tanks")); this.processor.fromNBT(nbt.getList("processor", Tag.TAG_COMPOUND), ElectrolyticCrucibleBatteryProcess::new); this.inventory.deserializeNBT(nbt.getCompound("inventory")); active = nbt.getBoolean("active"); tanksDirty = false; inventoryDirty = false; }

        @Override public void writeSyncNBT(CompoundTag nbt) { CompoundTag display = new CompoundTag(); writeDisplaySyncNBT(display); nbt.put("display", display); }

        @Override public void readSyncNBT(CompoundTag nbt) { if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display")); } }

        @Override public AveragingEnergyStorage getEnergy() { return energy; }
        @Override public IFluidTank[] getInternalTanks() { return tankArray; }
        @Override public int[] getOutputTanks() { return new int[]{1, 2, 3}; }
        @Override public int[] getOutputSlots() { return new int[0]; }
        @Override public boolean isActive() { return active; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt) { nbt.putBoolean("active", active); nbt.put("tanks", tanks.toNBT()); nbt.put("energy", energy.serializeNBT()); nbt.put("inventory", inventory.serializeNBT()); nbt.putIntArray("processPercents", processPercents); }

        @Override public void readDisplaySyncNBT(CompoundTag nbt) { active = nbt.getBoolean("active"); tanks.readNBT(nbt.getCompound("tanks")); if (energy == null) { energy = new SyncEnergyStorage(energyCapacity(), () -> {}); } energy.deserializeNBT(nbt.get("energy")); inventory.deserializeNBT(nbt.getCompound("inventory")); int[] percents = nbt.getIntArray("processPercents"); processPercents = percents.length == 3 ? percents : new int[]{-1, -1, -1}; tanksDirty = false; inventoryDirty = false; }
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

    private static class SyncEnergyStorage extends AveragingEnergyStorage {
        private final Runnable onChanged;
        public SyncEnergyStorage(int capacity, Runnable onChanged) { super(capacity); this.onChanged = onChanged; }

        @Override public int receiveEnergy(int maxReceive, boolean simulate) { int received = super.receiveEnergy(maxReceive, simulate); if (received > 0 && !simulate) { onChanged.run(); } return received; }

        @Override public int extractEnergy(int maxExtract, boolean simulate) { int extracted = super.extractEnergy(maxExtract, simulate); if (extracted > 0 && !simulate) { onChanged.run(); } return extracted; }

        public void setStoredEnergy(int energy) {
            int prev = getEnergyStored();
            super.setStoredEnergy(energy);
            if (energy != prev && onChanged != null) { onChanged.run(); }
        }
    }
}
