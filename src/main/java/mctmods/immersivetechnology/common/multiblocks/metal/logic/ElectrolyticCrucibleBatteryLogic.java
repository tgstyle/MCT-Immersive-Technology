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
import mctmods.immersivetechnology.core.util.CachedRecipe;
import mctmods.immersivetechnology.core.util.Utils;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.BiFunction;

public class ElectrolyticCrucibleBatteryLogic implements IMultiblockLogic<ElectrolyticCrucibleBatteryLogic.State>, IServerTickableComponent<ElectrolyticCrucibleBatteryLogic.State>, IClientTickableComponent<ElectrolyticCrucibleBatteryLogic.State>, IPressurizedFluidOutput<ElectrolyticCrucibleBatteryLogic.State> {

    public static final int INPUT_TANK_CAPACITY = ServerConfig.electrolyticCrucibleBatteryInputTankCapacity;
    public static final int OUTPUT_TANK_CAPACITY = ServerConfig.electrolyticCrucibleBatteryOutputTankCapacity;
    public static final int ENERGY_CAPACITY = ServerConfig.electrolyticCrucibleBatteryEnergyCapacity;

    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(ElectrolyticCrucibleBatteryShape.DATA.pointsOfInterest);

    public static final BlockPos REDSTONE_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "redstone0").getFirst();
    public static final List<BlockPos> INPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_input0");
    public static final List<BlockPos> OUTPUT_FLUID_POIS_0 = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output0");
    public static final List<BlockPos> OUTPUT_FLUID_POIS_1 = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output1");
    public static final List<BlockPos> OUTPUT_FLUID_POIS_2 = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output2");
    private static final List<BlockPos> ENERGY_INPUT_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "energy_input0");
    private static final RelativeBlockFace ENERGY_INPUT_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "energy_input0");
    private static final RelativeBlockFace INPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_input0");
    private static final RelativeBlockFace OUTPUT_FLUID_FACING_0 = MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output0");
    private static final RelativeBlockFace OUTPUT_FLUID_FACING_1 = MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output1");
    private static final RelativeBlockFace OUTPUT_FLUID_FACING_2 = MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output2");
    public static final MultiblockFace ITEM_OUTPUT_POI = new MultiblockFace(MultiblockPOIHelper.getFacing(RAW_POIS, "item_output0"), MultiblockPOIHelper.getPosList(RAW_POIS, "item_output0").getFirst());
    private static final RelativeBlockFace OUTPUT_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output0");
    private static final List<RelativeBlockFace> OUTPUT_FACINGS = ImmutableList.of(
            MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output0"),
            MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output1"),
            MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output2")
    );

    private static final List<BlockPos> FLUID_OUTPUT_POIS = ImmutableList.of(OUTPUT_FLUID_POIS_0.getFirst(), OUTPUT_FLUID_POIS_1.getFirst(), OUTPUT_FLUID_POIS_2.getFirst());

    @Override public List<BlockPos> getOutputPositions() { return FLUID_OUTPUT_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FACING); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output0, state.tanks.output1, state.tanks.output2); }

    @Override public List<RelativeBlockFace> getOutputFacings() { return OUTPUT_FACINGS; }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        List<BlockPos> soundPosList = MultiblockPOIHelper.getPosList(RAW_POIS, "sound0");
        if (soundPosList.isEmpty()) { return; }
        BlockPos soundBlockPos = soundPosList.getFirst();
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
        RecipeHolder<ElectrolyticCrucibleBatteryRecipe> holder = state.recipeGetter.apply(ctx.getLevel().getRawLevel(), state.tanks.input.getFluid());
        state.active = state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx));
        tryEnqueueProcess(state, ctx.getLevel().getRawLevel(), holder);
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
        boolean update = activeChanged || energyChanged || tanksChanged || inventoryChanged || percentsChanged;
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

    private void tryEnqueueProcess(State state, Level level, RecipeHolder<ElectrolyticCrucibleBatteryRecipe> holder) {
        if (state.processor.getQueueSize() >= state.processor.getMaxQueueSize()) { return; }
        if (holder == null) { return; }
        ElectrolyticCrucibleBatteryRecipe recipe = holder.value();
        FluidStack inputFluid = state.tanks.input.getFluid();
        if (inputFluid.getAmount() < recipe.getInputAmount()) { return; }
        if (recipe.fluidOutput0 != null && state.tanks.output0.getFluidAmount() + recipe.fluidOutput0.getAmount() > state.tanks.output0.getCapacity()) { return; }
        if (recipe.fluidOutput1 != null && state.tanks.output1.getFluidAmount() + recipe.fluidOutput1.getAmount() > state.tanks.output1.getCapacity()) { return; }
        if (recipe.fluidOutput2 != null && state.tanks.output2.getFluidAmount() + recipe.fluidOutput2.getAmount() > state.tanks.output2.getCapacity()) { return; }
        ElectrolyticCrucibleBatteryProcess process = new ElectrolyticCrucibleBatteryProcess(holder);
        process.setInputTanks(0);
        state.processor.addProcessToQueue(process, level, false);
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

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ElectrolyticCrucibleBatteryShape.GETTER; }

    public static class State implements IMultiblockState, IProcessContext.IProcessContextInMachine<ElectrolyticCrucibleBatteryRecipe>, IDisplayContext {
        public final BiFunction<Level, FluidStack, RecipeHolder<ElectrolyticCrucibleBatteryRecipe>> recipeGetter = CachedRecipe.cached(ElectrolyticCrucibleBatteryRecipe::findRecipe);
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final ElectrolyticCrucibleBatteryTanks tanks;
        public IFluidHandler inputCap;
        public IFluidHandler outputCap0;
        public IFluidHandler outputCap1;
        public IFluidHandler outputCap2;
        public IItemHandler invCap;
        public IItemHandler itemOutputCap;
        public Supplier<IItemHandler> outputRef;
        public final SlotwiseItemHandler inventory;
        private final IFluidTank[] tankArray;
        public final MultiblockProcessor.InMachineProcessor<ElectrolyticCrucibleBatteryRecipe> processor;
        public AveragingEnergyStorage energy;
        public boolean active;
        public BooleanSupplier isSoundPlaying = () -> false;
        public boolean tanksDirty = false;
        public boolean inventoryDirty = false;
        public int[] processPercents = new int[]{-1, -1, -1};

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); this.tanksDirty = true; this.inventoryDirty = true; };
            this.tanks = new ElectrolyticCrucibleBatteryTanks(v -> { onChanged.run(); this.tanksDirty = true; });
            this.tankArray = new IFluidTank[]{tanks.input, tanks.output0, tanks.output1, tanks.output2};
            inventory = new SlotwiseItemHandler(List.of(SlotwiseItemHandler.IOConstraint.OUTPUT), () -> { onChanged.run(); this.inventoryDirty = true; });
            this.inputCap = new ArrayFluidHandler(tanks.input, false, true, () -> { onChanged.run(); this.tanksDirty = true; });
            this.outputCap0 = new ArrayFluidHandler(tanks.output0, true, false, () -> { onChanged.run(); this.tanksDirty = true; });
            this.outputCap1 = new ArrayFluidHandler(tanks.output1, true, false, () -> { onChanged.run(); this.tanksDirty = true; });
            this.outputCap2 = new ArrayFluidHandler(tanks.output2, true, false, () -> { onChanged.run(); this.tanksDirty = true; });
            this.invCap = inventory;
            this.itemOutputCap = inventory;
            this.energy = new SyncEnergyStorage(ENERGY_CAPACITY, onChanged);
            this.processor = new MultiblockProcessor.InMachineProcessor<>(3, 0f, 3, markDirty, ElectrolyticCrucibleBatteryRecipe.RECIPES::getById);
            this.outputRef = ctx.getCapabilityAt(Capabilities.ItemHandler.BLOCK, ITEM_OUTPUT_POI);
        }

        public SlotwiseItemHandler getInventory() { return inventory; }
        public ElectrolyticCrucibleBatteryTanks getTanks() { return tanks; }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.put("energy", energy.serializeNBT(provider));
            nbt.put("tanks", this.tanks.toNBT(provider));
            nbt.put("processor", processor.toNBT(provider));
            nbt.put("inventory", inventory.serializeNBT(provider));
            nbt.putBoolean("active", active);
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            CompoundTag energyTag = nbt.getCompound("energy");
            if (!energyTag.isEmpty()) { energy.deserializeNBT(provider, energyTag); }
            this.tanks.readNBT(nbt.getCompound("tanks"), provider);
            this.processor.fromNBT(nbt.getList("processor", Tag.TAG_COMPOUND), ElectrolyticCrucibleBatteryProcess::new, provider);
            this.inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
            active = nbt.getBoolean("active");
            tanksDirty = false;
            inventoryDirty = false;
        }

        @Override public void writeSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            CompoundTag display = new CompoundTag();
            writeDisplaySyncNBT(display, provider);
            nbt.put("display", display);
        }

        @Override public void readSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display"), provider); }
        }

        @Override public AveragingEnergyStorage getEnergy() { return energy; }
        @Override public IFluidTank[] getInternalTanks() { return tankArray; }
        @Override public int[] getOutputTanks() { return new int[]{1, 2, 3}; }
        @Override public int[] getOutputSlots() { return new int[0]; }
        @Override public boolean isActive() { return active; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.put("energy", energy.serializeNBT(provider));
            nbt.put("inventory", inventory.serializeNBT(provider));
            nbt.putIntArray("processPercents", processPercents);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            if (energy == null) { energy = new SyncEnergyStorage(ENERGY_CAPACITY, () -> {}); }
            CompoundTag energyTag = nbt.getCompound("energy");
            if (!energyTag.isEmpty()) { energy.deserializeNBT(provider, energyTag); }
            inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
            int[] percents = nbt.getIntArray("processPercents");
            processPercents = percents.length == 3 ? percents : new int[]{-1, -1, -1};
            tanksDirty = false;
            inventoryDirty = false;
        }
    }

    public record ElectrolyticCrucibleBatteryTanks(MarkableFluidTank input, MarkableFluidTank output0, MarkableFluidTank output1, MarkableFluidTank output2) {
        public ElectrolyticCrucibleBatteryTanks(Consumer<Void> markDirty) {
            this(new MarkableFluidTank(INPUT_TANK_CAPACITY, markDirty), new MarkableFluidTank(OUTPUT_TANK_CAPACITY, markDirty), new MarkableFluidTank(OUTPUT_TANK_CAPACITY, markDirty), new MarkableFluidTank(OUTPUT_TANK_CAPACITY, markDirty));
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
