package mctmods.immersivetechnology.common.multiblocks.metal.logic;

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
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.common.multiblocks.helper.*;
import mctmods.immersivetechnology.common.multiblocks.metal.process.ElectrolyticCrucibleBatteryProcess;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.ElectrolyticCrucibleBatteryRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.ElectrolyticCrucibleBatteryShape;
import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.core.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.core.lib.ITSound;
import mctmods.immersivetechnology.core.registration.ITSounds;
import mctmods.immersivetechnology.core.ITServerConfig;
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

public class ElectrolyticCrucibleBatteryLogic implements IMultiblockLogic<ElectrolyticCrucibleBatteryLogic.State>, IServerTickableComponent<ElectrolyticCrucibleBatteryLogic.State>, IClientTickableComponent<ElectrolyticCrucibleBatteryLogic.State>, ITPressurizedFluidOutput<ElectrolyticCrucibleBatteryLogic.State> {

    public static final int TANK_CAPACITY = ITServerConfig.electrolyticCrucibleBatteryTankCapacity;
    public static final int ENERGY_CAPACITY = ITServerConfig.electrolyticCrucibleBatteryEnergyCapacity;

    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(ElectrolyticCrucibleBatteryShape.DATA.pointsOfInterest);

    public static final BlockPos REDSTONE_POI = safeGetPos("redstone0");
    public static final CapabilityPosition INPUT_FLUID_POI = safeGetCapabilityPos("fluid_input0");
    public static final CapabilityPosition OUTPUT_FLUID_POI_0 = safeGetCapabilityPos("fluid_output0");
    public static final CapabilityPosition OUTPUT_FLUID_POI_1 = safeGetCapabilityPos("fluid_output1");
    public static final CapabilityPosition OUTPUT_FLUID_POI_2 = safeGetCapabilityPos("fluid_output2");
    public static final List<CapabilityPosition> ENERGY_POIS = getEnergyPOIs();
    public static final MultiblockFace ITEM_OUTPUT_POI = new MultiblockFace(getFacingSafe("item_output0"), safeGetPos("item_output0"));
    private static final List<BlockPos> FLUID_OUTPUT_POIS = ImmutableList.of(safeGetPos("fluid_output0"), safeGetPos("fluid_output1"), safeGetPos("fluid_output2"));
    private static final RelativeBlockFace OUTPUT_FACING = getFacingSafe("fluid_output0");
    private static final List<RelativeBlockFace> OUTPUT_FACINGS = ImmutableList.of(getFacingSafe("fluid_output0"), getFacingSafe("fluid_output1"), getFacingSafe("fluid_output2"));

    private static List<BlockPos> getPosList(String name) { return RAW_POIS.stream().filter(poi -> poi.name.equals(name)).map(poi -> new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2])).collect(ImmutableList.toImmutableList()); }

    private static BlockPos safeGetPos(String name) { List<BlockPos> list = getPosList(name); return list.isEmpty() ? BlockPos.ZERO : list.get(0); }

    private static RelativeBlockFace getFacingSafe(String name) { try { List<RelativeBlockFace> facings = RAW_POIS.stream().filter(poi -> poi.name.equals(name)).flatMap(poi -> poi.relativeFaces.stream()).distinct().toList(); if (facings.size() != 1) { throw new RuntimeException("Inconsistent facings for POI: " + name); } return facings.get(0); } catch (Exception e) { return RelativeBlockFace.FRONT; } }

    private static CapabilityPosition safeGetCapabilityPos(String name) { BlockPos pos = safeGetPos(name); RelativeBlockFace facing = getFacingSafe(name); return new CapabilityPosition(pos, facing); }

    private static List<CapabilityPosition> getEnergyPOIs() { return RAW_POIS.stream().filter(poi -> poi.name.startsWith("energy_input")).map(poi -> safeGetCapabilityPos(poi.name)).collect(ImmutableList.toImmutableList()); }

    @Override public List<BlockPos> getOutputPositions() { return FLUID_OUTPUT_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FACING); }

    @Override public List<ITMarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output0, state.tanks.output1, state.tanks.output2); }

    @Override public List<RelativeBlockFace> getOutputFacings() { return OUTPUT_FACINGS; }

    @Override public void tickClient(IMultiblockContext<State> ctx) { State state = ctx.getState(); List<BlockPos> soundPosList = getPosList("sound0"); if (soundPosList.isEmpty()) { return; } BlockPos soundBlockPos = soundPosList.get(0); Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(soundBlockPos.getX() + 0.5, soundBlockPos.getY() + 0.5, soundBlockPos.getZ() + 0.5)); LocalPlayer player = Minecraft.getInstance().player; if (player == null) { return; } float distSq = (float) player.distanceToSqr(soundPos); float attenuation = Math.max(distSq / 32f, 1f); float vol = 1f / attenuation; if (state.active && vol > 0.01f && !state.isSoundPlaying.getAsBoolean()) { state.isSoundPlaying = ITSound.startSound(() -> state.active, ctx.isValid(), soundPos, ITSounds.electrolyticCrucibleBattery, () -> { LocalPlayer p = Minecraft.getInstance().player; if (p == null) { return 0f; } float a = (float) Math.max(p.distanceToSqr(soundPos) / 32f, 1f); return 1f / a; }, () -> 1f); } }

    @Override public void tickServer(IMultiblockContext<State> ctx) { State state = ctx.getState(); state.energy.updateAverage(); int prevEnergy = state.energy.getEnergyStored(); CompoundTag prevTanksNBT = state.tanks.toNBT(); ElectrolyticCrucibleBatteryRecipe recipe = ElectrolyticCrucibleBatteryRecipe.findRecipe(ctx.getLevel().getRawLevel(), state.tanks.input.getFluid()); boolean wasActive = state.active; state.active = state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx)); tryEnqueueProcess(state, ctx.getLevel().getRawLevel(), recipe); pumpOutputs(ctx); IItemHandlerModifiable inventory = state.inventory; ItemStack itemOutput = inventory.getStackInSlot(0); if (!itemOutput.isEmpty()) { itemOutput = Utils.insertStackIntoInventory(state.outputRef, itemOutput, false); inventory.setStackInSlot(0, itemOutput); } boolean activeChanged = wasActive != state.active; int currentEnergy = state.energy.getEnergyStored(); boolean energyChanged = prevEnergy != currentEnergy; CompoundTag currentTanksNBT = state.tanks.toNBT(); boolean tanksChanged = !prevTanksNBT.equals(currentTanksNBT); boolean update = activeChanged || energyChanged || tanksChanged; if (update) { ctx.markMasterDirty(); ctx.requestMasterBESync(); } }

    private void tryEnqueueProcess(State state, Level level, ElectrolyticCrucibleBatteryRecipe recipe) { if (state.processor.getQueueSize() >= state.processor.getMaxQueueSize()) { return; } if (recipe == null) { return; } FluidStack inputFluid = state.tanks.input.getFluid(); if (inputFluid.getAmount() < recipe.fluidInput0.getAmount()) { return; } if (recipe.fluidOutput0 != null && state.tanks.output0.getFluidAmount() + recipe.fluidOutput0.getAmount() > state.tanks.output0.getCapacity()) { return; } if (recipe.fluidOutput1 != null && state.tanks.output1.getFluidAmount() + recipe.fluidOutput1.getAmount() > state.tanks.output1.getCapacity()) { return; } if (recipe.fluidOutput2 != null && state.tanks.output2.getFluidAmount() + recipe.fluidOutput2.getAmount() > state.tanks.output2.getCapacity()) { return; } ElectrolyticCrucibleBatteryProcess process = new ElectrolyticCrucibleBatteryProcess(recipe); process.setInputTanks(0); state.processor.addProcessToQueue(process, level, false); }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) { State state = ctx.getState(); if (cap == ForgeCapabilities.ENERGY) { for (CapabilityPosition poi : ENERGY_POIS) { if (position.posInMultiblock().equals(poi.posInMultiblock()) && (position.side() == null || position.side() == poi.side())) { return state.energyCap.cast(ctx); } } } else if (cap == ForgeCapabilities.FLUID_HANDLER) { if (position.posInMultiblock().equals(INPUT_FLUID_POI.posInMultiblock()) && (position.side() == null || position.side() == INPUT_FLUID_POI.side())) { return state.inputCap.cast(ctx); } if (position.posInMultiblock().equals(OUTPUT_FLUID_POI_0.posInMultiblock()) && (position.side() == null || position.side() == OUTPUT_FLUID_POI_0.side())) { return state.outputCap0.cast(ctx); } if (position.posInMultiblock().equals(OUTPUT_FLUID_POI_1.posInMultiblock()) && (position.side() == null || position.side() == OUTPUT_FLUID_POI_1.side())) { return state.outputCap1.cast(ctx); } if (position.posInMultiblock().equals(OUTPUT_FLUID_POI_2.posInMultiblock()) && (position.side() == null || position.side() == OUTPUT_FLUID_POI_2.side())) { return state.outputCap2.cast(ctx); } } else if (cap == ForgeCapabilities.ITEM_HANDLER) { if (position.posInMultiblock().equals(ITEM_OUTPUT_POI.posInMultiblock()) && (position.side() == null || position.side() == ITEM_OUTPUT_POI.face())) { return state.itemOutputCap.cast(ctx); } return state.invCap.cast(ctx); } return LazyOptional.empty(); }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { ITMultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ElectrolyticCrucibleBatteryShape.GETTER; }

    public static class State implements IMultiblockState, ITProcessContext.ProcessContextInMachine<ElectrolyticCrucibleBatteryRecipe>, ITDisplayContext {
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
        public final ITSlotwiseItemHandler inventory;
        private final IFluidTank[] tankArray;
        public final MultiblockProcessor.InMachineProcessor<ElectrolyticCrucibleBatteryRecipe> processor;
        public AveragingEnergyStorage energy;
        public boolean active;
        public BooleanSupplier isSoundPlaying = () -> false;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); };
            this.tanks = new ElectrolyticCrucibleBatteryTanks(v -> onChanged.run());
            this.tankArray = new IFluidTank[]{tanks.input, tanks.output0, tanks.output1, tanks.output2};
            inventory = new ITSlotwiseItemHandler(List.of(ITSlotwiseItemHandler.IOConstraint.OUTPUT), onChanged);
            this.inputCap = new StoredCapability<>(new ITArrayFluidHandler(tanks.input, false, true, onChanged));
            this.outputCap0 = new StoredCapability<>(new ITArrayFluidHandler(tanks.output0, true, false, onChanged));
            this.outputCap1 = new StoredCapability<>(new ITArrayFluidHandler(tanks.output1, true, false, onChanged));
            this.outputCap2 = new StoredCapability<>(new ITArrayFluidHandler(tanks.output2, true, false, onChanged));
            this.invCap = new StoredCapability<>(inventory);
            this.energy = new SyncEnergyStorage(ENERGY_CAPACITY, onChanged);
            this.energyCap = new StoredCapability<>(this.energy);
            this.processor = new MultiblockProcessor.InMachineProcessor<>(3, 0f, 1, markDirty, ElectrolyticCrucibleBatteryRecipe.RECIPES::getById);
            this.itemOutputCap = new StoredCapability<>(inventory);
            this.outputRef = ctx.getCapabilityAt(ForgeCapabilities.ITEM_HANDLER, ITEM_OUTPUT_POI);
        }

        public ITSlotwiseItemHandler getInventory() { return inventory; }
        public ElectrolyticCrucibleBatteryTanks getTanks() { return tanks; }

        @Override public void writeSaveNBT(CompoundTag nbt) { nbt.put("energy", energy.serializeNBT()); nbt.put("tanks", this.tanks.toNBT()); nbt.put("processor", processor.toNBT()); nbt.put("inventory", inventory.serializeNBT()); nbt.putBoolean("active", active); }

        @Override public void readSaveNBT(CompoundTag nbt) { energy.deserializeNBT(nbt.get("energy")); this.tanks.readNBT(nbt.getCompound("tanks")); this.processor.fromNBT(nbt.getList("processor", Tag.TAG_COMPOUND), ElectrolyticCrucibleBatteryProcess::new); this.inventory.deserializeNBT(nbt.getCompound("inventory")); active = nbt.getBoolean("active"); }

        @Override public void writeSyncNBT(CompoundTag nbt) { CompoundTag display = new CompoundTag(); writeDisplaySyncNBT(display); nbt.put("display", display); }

        @Override public void readSyncNBT(CompoundTag nbt) { if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display")); } }

        @Override public AveragingEnergyStorage getEnergy() { return energy; }
        @Override public IFluidTank[] getInternalTanks() { return tankArray; }
        @Override public int[] getOutputTanks() { return new int[]{1, 2, 3}; }
        @Override public int[] getOutputSlots() { return new int[0]; }
        @Override public boolean isActive() { return active; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt) { nbt.putBoolean("active", active); nbt.put("tanks", tanks.toNBT()); nbt.put("energy", energy.serializeNBT()); nbt.put("inventory", inventory.serializeNBT()); }

        @Override public void readDisplaySyncNBT(CompoundTag nbt) { active = nbt.getBoolean("active"); tanks.readNBT(nbt.getCompound("tanks")); if (energy == null) { energy = new SyncEnergyStorage(ENERGY_CAPACITY, () -> {}); } energy.deserializeNBT(nbt.get("energy")); inventory.deserializeNBT(nbt.getCompound("inventory")); }
    }

    public record ElectrolyticCrucibleBatteryTanks(ITMarkableFluidTank input, ITMarkableFluidTank output0, ITMarkableFluidTank output1, ITMarkableFluidTank output2) {
        public ElectrolyticCrucibleBatteryTanks(Consumer<Void> markDirty) { this(new ITMarkableFluidTank(TANK_CAPACITY, markDirty), new ITMarkableFluidTank(TANK_CAPACITY, markDirty), new ITMarkableFluidTank(TANK_CAPACITY, markDirty), new ITMarkableFluidTank(TANK_CAPACITY, markDirty)); }

        public CompoundTag toNBT() { CompoundTag tag = new CompoundTag(); tag.put("input", input.writeToNBT(new CompoundTag())); tag.put("output0", output0.writeToNBT(new CompoundTag())); tag.put("output1", output1.writeToNBT(new CompoundTag())); tag.put("output2", output2.writeToNBT(new CompoundTag())); return tag; }

        public void readNBT(CompoundTag tag) { input.readFromNBT(tag.getCompound("input")); output0.readFromNBT(tag.getCompound("output0")); output1.readFromNBT(tag.getCompound("output1")); output2.readFromNBT(tag.getCompound("output2")); }
    }

    private static class SyncEnergyStorage extends AveragingEnergyStorage {
        private final Runnable onChanged;
        public SyncEnergyStorage(int capacity, Runnable onChanged) { super(capacity); this.onChanged = onChanged; }

        @Override public int receiveEnergy(int maxReceive, boolean simulate) { int received = super.receiveEnergy(maxReceive, simulate); if (received > 0 && !simulate) { onChanged.run(); } return received; }

        @Override public int extractEnergy(int maxExtract, boolean simulate) { int extracted = super.extractEnergy(maxExtract, simulate); if (extracted > 0 && !simulate) { onChanged.run(); } return extracted; }
    }
}
