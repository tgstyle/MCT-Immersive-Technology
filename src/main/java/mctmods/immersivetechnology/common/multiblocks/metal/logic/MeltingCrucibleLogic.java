package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.client.util.ClientUtils;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.process.MeltingCrucibleProcess;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.MeltingRecipe;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.util.FluidContainers;
import mctmods.immersivetechnology.core.util.IDisplaySyncState;
import mctmods.immersivetechnology.core.util.RecipeIds;
import mctmods.immersivetechnology.core.util.SyncEnergyStorage;

import com.immersiveconvergence.api.energy.ICEnergyStorageAveraging;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
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
import com.immersiveconvergence.api.util.ICFluidUtils;
import com.immersiveconvergence.api.util.MarkableFluidTank;
import com.immersiveconvergence.api.util.MultiBlockInventoryUtils;
import com.immersiveconvergence.api.util.MultiTankFluidHandler;
import com.immersiveconvergence.api.util.RecipeCache;
import com.immersiveconvergence.api.util.TankPair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
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

public class MeltingCrucibleLogic implements IMultiblockLogic<MeltingCrucibleLogic.State>, IServerTickableComponent<MeltingCrucibleLogic.State>, IClientTickableComponent<MeltingCrucibleLogic.State>, IFluidOutputPump<MeltingCrucibleLogic.State> {
    public static final int SLOT_INPUT_FILLED = 0;
    public static final int SLOT_INPUT_EMPTY = 1;
    public static final int SLOT_OUTPUT_EMPTY = 2;
    public static final int SLOT_OUTPUT_FILLED = 3;

    public static int inputTankCapacity() { return ServerConfig.meltingCrucibleInputTankCapacity; }
    public static int outputTankCapacity() { return ServerConfig.meltingCrucibleOutputTankCapacity; }
    public static int energyCapacity() { return ServerConfig.meltingCrucibleEnergyCapacity; }
    public static int energyMaxIo() { return ServerConfig.meltingCrucibleEnergyMaxIO; }

    public static double workingHeatLevel() { return ServerConfig.meltingCrucibleHeatWorkingLevel; }
    private static double heatLossMultiplier() { return ServerConfig.meltingCrucibleHeatLossMultiplier; }
    private static double heatGainBase() { return ServerConfig.meltingCrucibleHeatGainBase; }
    private static int energyPerTickToHeat() { return ServerConfig.meltingCrucibleEnergyPerTickToHeat; }
    private static int energyPerTickToMaintain() { return ServerConfig.meltingCrucibleEnergyPerTickToMaintain; }

    private static List<PoIJSONSchema> RAW_POIS;

    public static BlockPos REDSTONE_POI;
    public static List<BlockPos> INPUT_FLUID_POIS;
    public static List<BlockPos> OUTPUT_FLUID_POIS;
    public static CapabilityPosition ENERGY_INPUT_POI;
    private static List<BlockPos> FLUID_OUTPUT_POIS;
    private static RelativeBlockFace OUTPUT_FACING;
    private static RelativeBlockFace INPUT_FLUID_FACING;
    private static RelativeBlockFace OUTPUT_FLUID_FACING;

    static { ITShapes.readPois("melting_crucible", MeltingCrucibleLogic::loadPois); }

    @Override public List<BlockPos> getOutputPositions() { return FLUID_OUTPUT_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FACING); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output()); }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        List<BlockPos> soundPosList = MultiblockPOIHelper.getPosList(RAW_POIS, "sound0");
        if (soundPosList.isEmpty()) { return; }
        BlockPos soundBlockPos = soundPosList.getFirst();
        Vec3 soundPos = ctx.getLevel().toAbsolute(Vec3.atCenterOf(soundBlockPos));
        if (Minecraft.getInstance().player == null) { return; }
        if (state.active && ClientUtils.attenuated(soundPos, 32, 1) > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
            state.isSoundPlaying = MachineSound.startSound(() -> state.active, ctx.isValid(), soundPos, Sounds.meltingCrucible, () -> ClientUtils.attenuated(soundPos, 32, 1), () -> 1f);
        }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        state.energy.updateAverage();
        int prevEnergy = state.energy.getEnergyStored();
        boolean prevTanksDirty = state.tanksDirty;
        boolean wasActive = state.active;

        Level level = ctx.getLevel().getRawLevel();
        FluidStack fs = state.tanks.input().getFluid();
        MeltingRecipe recipe = fs.getAmount() > 0 ? state.recipeGetter.apply(level, fs) : null;

        if (state.activeRecipe == null && state.activeRecipeId != null) { state.activeRecipe = MeltingRecipe.RECIPES.getById(level, state.activeRecipeId); }
        MultiblockProcess<MeltingRecipe, ?> running = state.processor.getQueue().isEmpty() ? null : state.processor.getQueue().getFirst();
        MeltingRecipe runningRecipe = running != null ? running.getRecipe(level) : null;
        if (runningRecipe != null) {
            state.activeRecipe = runningRecipe;
            state.activeRecipeId = running.getRecipeId();
        }
        else if (state.activeRecipe == null || !state.activeRecipe.matches(fs)) {
            state.activeRecipe = recipe;
            state.activeRecipeId = RecipeIds.holderId(MeltingRecipe.RECIPES, level, recipe);
        }

        boolean shouldRun = state.rsState.isEnabled(ctx);
        int energyThisTick = state.heatLevel >= workingHeatLevel() ? energyPerTickToMaintain() : energyPerTickToHeat();
        boolean heating = shouldRun && state.energy.extractEnergy(energyThisTick, true) >= energyThisTick;
        if (heating) { state.energy.extractEnergy(energyThisTick, false); }
        heatLogic(ctx, heating, state);

        boolean canProcess = shouldRun && state.activeRecipe != null && state.heatLevel >= state.activeRecipe.requiredTemp;
        state.active = state.processor.tickServer(state, ctx.getLevel(), canProcess);

        if (state.activeRecipe != null && recipe != null) {
            tryEnqueueProcess(state, level, fs);
        }

        FluidContainers.emptyBucket(state.tanks.input(), state.inventory, SLOT_INPUT_FILLED, SLOT_INPUT_EMPTY);
        ICFluidUtils.fillFluidContainer(state.tanks.output(), SLOT_OUTPUT_EMPTY, SLOT_OUTPUT_FILLED, state.inventory);
        pumpOutputs(ctx);

        boolean activeChanged = wasActive != state.active;
        int currentEnergy = state.energy.getEnergyStored();
        boolean energyChanged = prevEnergy != currentEnergy;
        boolean tanksChanged = prevTanksDirty != state.tanksDirty;
        int newQueueSize = state.processor.getQueueSize();
        boolean queueSizeChanged = newQueueSize != state.queueSize;
        if (queueSizeChanged) { state.queueSize = newQueueSize; }
        int newComparatorValue = (int) Math.min(15, (15 * state.heatLevel) / workingHeatLevel());
        boolean comparatorChanged = newComparatorValue != state.lastComparatorValue;
        if (comparatorChanged) { ctx.setComparatorOutputFor(REDSTONE_POI, newComparatorValue); state.lastComparatorValue = newComparatorValue; }
        boolean update = activeChanged || energyChanged || tanksChanged || queueSizeChanged || comparatorChanged;

        if (update) { ctx.markMasterDirty(); ctx.requestMasterBESync(); }
    }

    private void heatLogic(IMultiblockContext<State> ctx, boolean heating, State state) {
        double prev = state.heatLevel;
        state.heatLevel -= getCooldownAmount(ctx);
        state.heatLevel = Math.max(state.heatLevel, 0);
        if (heating) { state.heatLevel += heatGainBase(); }
        double maxHeat = state.activeRecipe != null ? state.activeRecipe.requiredTemp : workingHeatLevel();
        state.heatLevel = Math.min(state.heatLevel, maxHeat);
        if (prev != state.heatLevel) { ctx.markMasterDirty(); }
    }

    private double getCooldownAmount(IMultiblockContext<State> ctx) {
        Level level = ctx.getLevel().getRawLevel();
        BlockPos pos = ctx.getLevel().toAbsolute(BlockPos.ZERO);
        float biomeTemp = level.getBiome(pos).value().getBaseTemperature();
        double heatLost = biomeTemp > 0 ? biomeTemp : 0.1;
        return (1 / heatLost) * heatLossMultiplier();
    }

    private void tryEnqueueProcess(State state, Level level, FluidStack currentInput) {
        if (state.processor.getQueueSize() >= state.processor.getMaxQueueSize()) { return; }
        RecipeHolder<MeltingRecipe> recipeHolder = state.recipeHolderGetter.apply(level, currentInput);
        if (recipeHolder == null) { return; }
        MeltingRecipe recipe = recipeHolder.value();
        if (state.heatLevel < recipe.requiredTemp) { return; }
        FluidStack inputFluid = state.tanks.input().getFluid();
        if (inputFluid.getAmount() < recipe.getInputAmount()) { return; }
        FluidStack outputFluid = recipe.fluidOutput();
        if (outputFluid != null && !outputFluid.isEmpty() && state.tanks.output().getFluidAmount() + outputFluid.getAmount() > state.tanks.output().getCapacity()) { return; }
        state.processor.addProcessToQueue(new MeltingCrucibleProcess(recipeHolder), level, false);
    }

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.register(Capabilities.EnergyStorage.BLOCK, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (localPos.equals(ENERGY_INPUT_POI.posInMultiblock()) && (side == null || side == ENERGY_INPUT_POI.side())) {
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
        register.register(Capabilities.ItemHandler.BLOCK, (state, position) -> state.invCap);
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { MultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get("melting_crucible").getter; }

    public static class State implements IDisplaySyncState, IProcessContext.IProcessContextInMachine<MeltingRecipe>, IDataReloadAware {
        public final BiFunction<Level, FluidStack, MeltingRecipe> recipeGetter = RecipeCache.cached(MeltingRecipe::findRecipe);
        public final BiFunction<Level, FluidStack, RecipeHolder<MeltingRecipe>> recipeHolderGetter = RecipeCache.cached(MeltingRecipe::findRecipeHolder);
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final TankPair tanks;
        public IFluidHandler inputCap;
        public IFluidHandler outputCap;
        public IItemHandler invCap;
        public final ConstrainedItemHandler inventory;
        private final IFluidTank[] tankArray;
        public final MultiblockProcessor.InMachineProcessor<MeltingRecipe> processor;
        public final ICEnergyStorageAveraging energy;
        public boolean active;
        public BooleanSupplier isSoundPlaying = () -> false;
        public double heatLevel = 0.0;
        public MeltingRecipe activeRecipe = null;
        private ResourceLocation activeRecipeId;
        public boolean tanksDirty = false;
        public int queueSize = 0;
        public int lastComparatorValue = -1;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); this.tanksDirty = true; };
            this.tanks = new TankPair(v -> onChanged.run(), inputTankCapacity(), outputTankCapacity());
            this.tankArray = new IFluidTank[]{tanks.input(), tanks.output()};
            inventory = new ConstrainedItemHandler(
                    List.of(
                            ConstrainedItemHandler.IOConstraint.FLUID_INPUT,
                            ConstrainedItemHandler.IOConstraint.OUTPUT,
                            ConstrainedItemHandler.IOConstraint.FLUID_INPUT,
                            ConstrainedItemHandler.IOConstraint.OUTPUT
                    ),
                    onChanged
            );
            this.inputCap = new MultiTankFluidHandler(tanks.input(), false, true, onChanged);
            this.outputCap = new MultiTankFluidHandler(tanks.output(), true, false, onChanged);
            this.invCap = inventory;
            this.energy = new SyncEnergyStorage(energyCapacity(), energyMaxIo(), onChanged);
            this.processor = new MultiblockProcessor.InMachineProcessor<>(1, 0f, 1, markDirty, MeltingRecipe.RECIPES::getById);
        }

        @Override public void onDataReload(IInitialMultiblockContext<?> context) { lastComparatorValue = -1; }

        public ConstrainedItemHandler getInventory() { return inventory; }
        public TankPair getTanks() { return tanks; }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            rsState.writeSaveNBT(nbt, provider);
            nbt.put("energy", energy.serializeNBT(provider));
            nbt.put("tanks", this.tanks.toNBT(provider));
            nbt.put("processor", processor.toNBT(provider));
            nbt.put("inventory", inventory.serializeNBT(provider));
            nbt.putBoolean("active", active);
            nbt.putDouble("heatLevel", heatLevel);
            if (activeRecipeId != null) { nbt.putString("activeRecipe", activeRecipeId.toString()); }
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            rsState.readSaveNBT(nbt, provider);
            CompoundTag energyTag = nbt.getCompound("energy");
            if (!energyTag.isEmpty()) {
                energy.deserializeNBT(provider, energyTag);
            }
            this.tanks.readNBT(nbt.getCompound("tanks"), provider);
            this.processor.fromNBT(nbt.getList("processor", Tag.TAG_COMPOUND), MeltingCrucibleProcess::new, provider);
            this.inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
            active = nbt.getBoolean("active");
            heatLevel = nbt.getDouble("heatLevel");
            if (nbt.contains("activeRecipe")) { activeRecipeId = ResourceLocation.tryParse(nbt.getString("activeRecipe")); }
            tanksDirty = false;
        }

        @Override public ICEnergyStorageAveraging getEnergyStorage() { return energy; }
        @Override public IFluidTank[] getInternalTanks() { return tankArray; }
        @Override public int[] getOutputTanks() { return new int[]{1}; }
        @Override public int[] getOutputSlots() { return new int[0]; }
        @Override public boolean isActive() { return active; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.put("energy", energy.serializeNBT(provider));
            nbt.put("inventory", inventory.serializeNBT(provider));
            nbt.putDouble("heatLevel", heatLevel);
            if (activeRecipeId != null) { nbt.putString("activeRecipe", activeRecipeId.toString()); }
            nbt.putInt("queueSize", queueSize);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            CompoundTag energyTag = nbt.getCompound("energy");
            if (!energyTag.isEmpty()) {
                energy.deserializeNBT(provider, energyTag);
            }
            inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
            heatLevel = nbt.getDouble("heatLevel");
            if (nbt.contains("activeRecipe")) { activeRecipeId = ResourceLocation.tryParse(nbt.getString("activeRecipe")); }
            queueSize = nbt.getInt("queueSize");
            tanksDirty = false;
        }

        @Override public void addDisplayLines(Level level, DisplayLines lines) {
            FluidStack input = tanks.input().getFluid();
            MeltingRecipe recipe = input.isEmpty() ? null : MeltingRecipe.findRecipe(level, input);
            lines.temperature(heatLevel, recipe != null ? recipe.requiredTemp : workingHeatLevel());
            if (queueSize > 0) { lines.text("Processing (" + queueSize + " queued)"); }
            if (input.isEmpty()) { lines.fuelEmpty(); }
        }
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        RAW_POIS = pois;
        REDSTONE_POI = MultiblockPOIHelper.getPosList(pois, "redstone0").getFirst();
        INPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_input0");
        OUTPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_output0");
        ENERGY_INPUT_POI = MultiblockPOIHelper.getCapabilityPosition(pois, "energy_input0");
        FLUID_OUTPUT_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_output0");
        OUTPUT_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_output0");
        INPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_input0");
        OUTPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_output0");
    }
}
