package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.common.multiblocks.helper.IDisplayContext;
import mctmods.immersivetechnology.common.multiblocks.helper.MultiblockPOIHelper;
import mctmods.immersivetechnology.common.multiblocks.helper.IPressurizedFluidOutput;
import mctmods.immersivetechnology.common.multiblocks.metal.process.HeatExchangerProcess;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.HeatExchangerRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.HeatExchangerShape;
import mctmods.immersivetechnology.common.fluids.helper.ArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.MarkableFluidTank;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.lib.ModSound;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.core.util.CachedRecipe;

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
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
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
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class HeatExchangerLogic implements IMultiblockLogic<HeatExchangerLogic.State>, IServerTickableComponent<HeatExchangerLogic.State>, IClientTickableComponent<HeatExchangerLogic.State>, IPressurizedFluidOutput<HeatExchangerLogic.State> {
    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(HeatExchangerShape.DATA.pointsOfInterest);

    public static final BlockPos REDSTONE_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "redstone0").getFirst();
    public static final List<BlockPos> COMPARATOR_POSITIONS = MultiblockPOIHelper.getPosList(RAW_POIS, "comparator0");

    public static final List<BlockPos> INPUT_FLUID_0_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_input0");
    public static final List<BlockPos> INPUT_FLUID_1_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_input1");
    public static final List<BlockPos> OUTPUT_FLUID_0_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output0");
    public static final List<BlockPos> OUTPUT_FLUID_1_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output1");
    public static final List<BlockPos> ENERGY_INPUT_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "energy_input0");
    public static final List<BlockPos> SOUND_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "sound0");

    public static final List<BlockPos> INPUT_FLUID_POIS = ImmutableList.<BlockPos>builder().addAll(INPUT_FLUID_0_POIS).addAll(INPUT_FLUID_1_POIS).build();

    private static final RelativeBlockFace INPUT_FLUID_0_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_input0");
    private static final RelativeBlockFace INPUT_FLUID_1_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_input1");
    private static final RelativeBlockFace OUTPUT_FLUID_0_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output0");
    private static final RelativeBlockFace OUTPUT_FLUID_1_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output1");
    private static final RelativeBlockFace ENERGY_INPUT_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "energy_input0");

    private static int inputTankCapacity() { return ServerConfig.heatExchangerInputTankCapacity; }
    private static int outputTankCapacity() { return ServerConfig.heatExchangerOutputTankCapacity; }
    private static int energyCapacity() { return ServerConfig.heatExchangerEnergyCapacity; }
    private static int energyMaxIo() { return ServerConfig.heatExchangerEnergyMaxIO; }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        if (SOUND_POIS.isEmpty()) { return; }
        BlockPos soundBlockPos = SOUND_POIS.getFirst();
        Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(soundBlockPos.getX() + 0.5, soundBlockPos.getY() + 0.5, soundBlockPos.getZ() + 0.5));
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) { return; }
        float distSq = (float) player.distanceToSqr(soundPos);
        float attenuation = Math.max(distSq / 32f, 1f);
        float vol = 1f / attenuation;
        if (state.active && vol > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
            state.isSoundPlaying = ModSound.startSound(
                    () -> state.active, ctx.isValid(), soundPos, Sounds.heatExchanger,
                    () -> {
                        LocalPlayer p = Minecraft.getInstance().player;
                        if (p == null) { return 0f; }
                        float a = (float) Math.max(p.distanceToSqr(soundPos) / 32f, 1f);
                        return 1f / a;
                    },
                    () -> 1f
            );
        }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        final Level level = ctx.getLevel().getRawLevel();

        state.energy.updateAverage();

        int prevEnergy = state.energy.getEnergyStored();
        HolderLookup.Provider provider = level.registryAccess();
        CompoundTag prevTanksNBT = state.tanks.toNBT(provider);

        boolean wasActive = state.active;
        state.active = state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx));

        RecipeHolder<HeatExchangerRecipe> recipeHolder = state.recipeGetter.apply(level, state.tanks.input0.getFluid(), state.tanks.input1.getFluid());
        tryEnqueueProcess(state, level, recipeHolder);

        boolean progressChanged = false;
        if (!state.processor.getQueue().isEmpty()) {
            HeatExchangerProcess current = (HeatExchangerProcess) state.processor.getQueue().getFirst();
            int newProg = current.getCurrentTick();
            int newTotal = current.getMaxTicks(level);
            if (newProg != state.processProgress || newTotal != state.totalProcessTime) {
                state.processProgress = newProg;
                state.totalProcessTime = newTotal;
                progressChanged = true;
            }
        } else if (state.processProgress > 0 || state.totalProcessTime > 0) {
            state.processProgress = 0;
            state.totalProcessTime = 0;
            progressChanged = true;
        }

        pumpOutputs(ctx);

        boolean activeChanged = wasActive != state.active;
        int currentEnergy = state.energy.getEnergyStored();
        boolean energyChanged = prevEnergy != currentEnergy;
        CompoundTag currentTanksNBT = state.tanks.toNBT(provider);
        boolean tanksChanged = !prevTanksNBT.equals(currentTanksNBT);
        int newQueueSize = state.processor.getQueueSize();
        boolean queueSizeChanged = newQueueSize != state.queueSize;
        if (queueSizeChanged) { state.queueSize = newQueueSize; }
        int maxEnergy = state.energy.getMaxEnergyStored();
        int newComparatorValue = maxEnergy > 0 ? (15 * state.energy.getEnergyStored()) / maxEnergy : 0;
        boolean comparatorChanged = newComparatorValue != state.lastComparatorValue;
        if (comparatorChanged) { for (BlockPos pos : COMPARATOR_POSITIONS) { ctx.setComparatorOutputFor(pos, newComparatorValue); } state.lastComparatorValue = newComparatorValue; }
        boolean update = activeChanged || energyChanged || tanksChanged || progressChanged || queueSizeChanged || comparatorChanged;
        if (update) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    private void tryEnqueueProcess(State state, Level level, RecipeHolder<HeatExchangerRecipe> recipeHolder) {
        if (recipeHolder == null) { return; }
        if (state.processor.getQueueSize() >= state.processor.getMaxQueueSize()) { return; }
        HeatExchangerRecipe recipe = recipeHolder.value();
        int need0 = recipe.getInput0Amount();
        int need1 = recipe.getInput1Amount();
        if (state.tanks.input0.getFluidAmount() < need0 || (need1 > 0 && state.tanks.input1.getFluidAmount() < need1)) { return; }
        int space0 = state.tanks.output0.getCapacity() - state.tanks.output0.getFluidAmount();
        int space1 = (recipe.output1() != null) ? state.tanks.output1.getCapacity() - state.tanks.output1.getFluidAmount() : state.tanks.output1.getCapacity();
        FluidStack out1 = recipe.output1();
        int out1Amt = (out1 != null) ? out1.getAmount() : 0;
        if (space0 < recipe.output0().getAmount() || space1 < out1Amt) { return; }
        HeatExchangerProcess process = new HeatExchangerProcess(recipeHolder);
        state.processor.addProcessToQueue(process, level, false);
    }

    @Override public List<BlockPos> getOutputPositions() { return ImmutableList.of(OUTPUT_FLUID_0_POIS.getFirst(), OUTPUT_FLUID_1_POIS.getFirst()); }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return null; }

    @Override public List<RelativeBlockFace> getOutputFacings() { return ImmutableList.of(OUTPUT_FLUID_0_FACING, OUTPUT_FLUID_1_FACING); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output0, state.tanks.output1); }

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.register(Capabilities.FluidHandler.BLOCK, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (INPUT_FLUID_0_POIS.contains(localPos) && (side == null || side == INPUT_FLUID_0_FACING)) { return state.inputCap[0]; }
            if (INPUT_FLUID_1_POIS.contains(localPos) && (side == null || side == INPUT_FLUID_1_FACING)) { return state.inputCap[1]; }
            if (OUTPUT_FLUID_0_POIS.contains(localPos) && (side == null || side == OUTPUT_FLUID_0_FACING)) { return state.outputCap[0]; }
            if (OUTPUT_FLUID_1_POIS.contains(localPos) && (side == null || side == OUTPUT_FLUID_1_FACING)) { return state.outputCap[1]; }
            return null;
        });
        register.register(Capabilities.EnergyStorage.BLOCK, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (ENERGY_INPUT_POIS.contains(localPos) && (side == null || side == ENERGY_INPUT_FACING)) { return state.energy; }
            return null;
        });
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) {}

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return HeatExchangerShape.GETTER; }

    public static class State implements IMultiblockState, IDisplayContext, ProcessContext.ProcessContextInMachine<HeatExchangerRecipe> {
        public final CachedRecipe.TriFunction<Level, FluidStack, FluidStack, RecipeHolder<HeatExchangerRecipe>> recipeGetter = CachedRecipe.cached3(HeatExchangerRecipe::findRecipe);
        public final HeatExchangerTanks tanks;

        public final IFluidHandler[] inputCap = new IFluidHandler[2];
        public final IFluidHandler[] outputCap = new IFluidHandler[2];

        public AveragingEnergyStorage energy;
        public boolean active = false;
        public RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final MultiblockProcessor.InMachineProcessor<HeatExchangerRecipe> processor;
        public BooleanSupplier isSoundPlaying = () -> false;
        public int processProgress = 0;
        public int totalProcessTime = 0;
        public int queueSize = 0;
        public int lastComparatorValue = -1;

        private static final IItemHandlerModifiable EMPTY_INVENTORY = new IItemHandlerModifiable() {
            @Override public int getSlots() { return 0; }
            @Override @NotNull public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
            @Override @NotNull public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) { return stack; }
            @Override @NotNull public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
            @Override public int getSlotLimit(int slot) { return 0; }
            @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return false; }
            @Override public void setStackInSlot(int slot, @NotNull ItemStack stack) {}
        };

        public State(IInitialMultiblockContext<State> ctx) {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            final Runnable sync = ctx.getSyncRunnable();
            final Runnable onChanged = () -> { markDirty.run(); sync.run(); };
            tanks = new HeatExchangerTanks(onChanged);
            energy = new SyncEnergyStorage(energyCapacity(), energyMaxIo(), onChanged);
            inputCap[0] = new ArrayFluidHandler(tanks.input0, false, true, onChanged);
            inputCap[1] = new ArrayFluidHandler(tanks.input1, false, true, onChanged);
            outputCap[0] = new ArrayFluidHandler(tanks.output0, true, false, onChanged);
            outputCap[1] = new ArrayFluidHandler(tanks.output1, true, false, onChanged);
            processor = new MultiblockProcessor.InMachineProcessor<>(1, 0f, 1, markDirty, HeatExchangerRecipe.RECIPES::getById);
        }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.put("energy", energy.serializeNBT(provider));
            nbt.put("processor", processor.toNBT(provider));
            nbt.putInt("processProgress", processProgress);
            nbt.putInt("totalProcessTime", totalProcessTime);
            rsState.writeSaveNBT(nbt, provider);
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            CompoundTag energyTag = nbt.getCompound("energy");
            if (!energyTag.isEmpty()) { energy.deserializeNBT(provider, energyTag); }
            processor.fromNBT(nbt.getList("processor", Tag.TAG_COMPOUND), HeatExchangerProcess::new, provider);
            processProgress = nbt.getInt("processProgress");
            totalProcessTime = nbt.getInt("totalProcessTime");
            rsState.readSaveNBT(nbt, provider);
        }

        @Override public void writeSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            CompoundTag display = new CompoundTag();
            writeDisplaySyncNBT(display, provider);
            nbt.put("display", display);
        }

        @Override public void readSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display"), provider); }
        }

        @Override public boolean isActive() { return active; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input0, tanks.input1, tanks.output0, tanks.output1}; }

        @Override public List<AveragingEnergyStorage> getEnergies() { return List.of(energy); }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.put("energy", energy.serializeNBT(provider));
            nbt.putInt("processProgress", processProgress);
            nbt.putInt("totalProcessTime", totalProcessTime);
            nbt.putInt("queueSize", queueSize);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            if (energy == null) { energy = new SyncEnergyStorage(energyCapacity(), energyMaxIo(), () -> {}); }
            CompoundTag energyTag = nbt.getCompound("energy");
            if (!energyTag.isEmpty()) { energy.deserializeNBT(provider, energyTag); }
            processProgress = nbt.getInt("processProgress");
            totalProcessTime = nbt.getInt("totalProcessTime");
            queueSize = nbt.getInt("queueSize");
        }

        @Override public AveragingEnergyStorage getEnergy() { return energy; }

        @Override public IItemHandlerModifiable getInventory() { return EMPTY_INVENTORY; }

        @Override public int[] getOutputSlots() { return new int[0]; }

        @Override public int[] getOutputTanks() { return new int[]{2, 3}; }
    }

    private static class SyncEnergyStorage extends AveragingEnergyStorage {
        private final Runnable onChanged;

        public SyncEnergyStorage(int capacity, int maxIO, Runnable onChanged) {
            super(capacity);
            this.maxReceive = maxIO;
            this.maxExtract = maxIO;
            this.onChanged = onChanged;
        }

        @Override public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (received > 0 && !simulate) { onChanged.run(); }
            return received;
        }

        @Override public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (extracted > 0 && !simulate) { onChanged.run(); }
            return extracted;
        }

        public void setStoredEnergy(int energy) {
            int prev = getEnergyStored();
            super.setStoredEnergy(energy);
            if (energy != prev && onChanged != null) { onChanged.run(); }
        }
    }

    public record HeatExchangerTanks(MarkableFluidTank input0, MarkableFluidTank input1, MarkableFluidTank output0, MarkableFluidTank output1) {

        public HeatExchangerTanks(Runnable onChanged) {
            this(
                    new MarkableFluidTank(inputTankCapacity(), v -> onChanged.run()),
                    new MarkableFluidTank(inputTankCapacity(), v -> onChanged.run()),
                    new MarkableFluidTank(outputTankCapacity(), v -> onChanged.run()),
                    new MarkableFluidTank(outputTankCapacity(), v -> onChanged.run())
            );
        }

        public static HeatExchangerTanks makeClient() {
            return new HeatExchangerTanks(
                    new MarkableFluidTank(10000, v -> {}),
                    new MarkableFluidTank(10000, v -> {}),
                    new MarkableFluidTank(10000, v -> {}),
                    new MarkableFluidTank(10000, v -> {})
            );
        }

        public CompoundTag toNBT(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.put("input0", this.input0.writeToNBT(provider, new CompoundTag()));
            tag.put("input1", this.input1.writeToNBT(provider, new CompoundTag()));
            tag.put("output0", this.output0.writeToNBT(provider, new CompoundTag()));
            tag.put("output1", this.output1.writeToNBT(provider, new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag, HolderLookup.Provider provider) {
            this.input0.readFromNBT(provider, tag.getCompound("input0"));
            this.input1.readFromNBT(provider, tag.getCompound("input1"));
            this.output0.readFromNBT(provider, tag.getCompound("output0"));
            this.output1.readFromNBT(provider, tag.getCompound("output1"));
        }
    }
}
