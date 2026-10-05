package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.client.util.ClientUtils;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.ISolarRecipe;
import mctmods.immersivetechnology.core.util.IDisplaySyncState;
import mctmods.immersivetechnology.core.util.ItemOutputs;
import mctmods.immersivetechnology.core.util.solarregistry.SolarRegistry;
import mctmods.immersivetechnology.core.util.solarregistry.SolarRegistryData;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl.InitialMultiblockContext;
import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.client.MachineSound;
import com.immersiveconvergence.api.integration.DisplayLines;
import com.immersiveconvergence.api.multiblock.IDataReloadAware;
import com.immersiveconvergence.api.multiblock.IFluidOutputPump;
import com.immersiveconvergence.api.util.ConstrainedItemHandler;
import com.immersiveconvergence.api.util.MarkableFluidTank;
import com.immersiveconvergence.api.util.MultiBlockInventoryUtils;
import com.immersiveconvergence.api.util.MultiTankFluidHandler;
import com.immersiveconvergence.api.util.RecipeCache;
import com.immersiveconvergence.api.util.TankPair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static mctmods.immersivetechnology.core.util.solarregistry.SolarRegistry.SOLAR_MAX_RANGE;
import static mctmods.immersivetechnology.core.util.solarregistry.SolarRegistry.SOLAR_MIN_RANGE;

public abstract class SolarCollectorLogic<R extends MultiblockRecipe & ISolarRecipe, S extends SolarCollectorLogic.CollectorState<R>> implements IMultiblockLogic<S>, IServerTickableComponent<S>, IClientTickableComponent<S>, IFluidOutputPump<S> {
    public static final int SLOT_INPUT_FILLED = 0;
    public static final int SLOT_INPUT_EMPTY = 1;
    public static final int SLOT_OUTPUT_EMPTY = 2;
    public static final int SLOT_OUTPUT_FILLED = 3;

    protected abstract String shapeName();
    protected abstract BlockPos redstonePoi();
    protected abstract boolean enabled(IMultiblockContext<S> ctx);
    protected abstract BlockPos soundPoi();
    protected abstract BlockPos linkPoi();
    protected abstract BlockPos reflectorPoi();
    protected abstract List<BlockPos> inputPois();
    protected abstract RelativeBlockFace inputFacing();
    protected abstract RelativeBlockFace outputFacing();
    protected abstract Supplier<SoundEvent> sound();
    protected abstract double soundFalloff();
    protected abstract double dayMinHeatLoss();
    protected abstract double lossPerSectionDrop();
    protected abstract double tempDependentLossFactor();
    protected abstract double heatIncreaseFactor();
    protected abstract double tempToMinReflectorsDivisor();
    protected abstract double reflectorTierOffset();
    protected abstract int progressLossOffTemp();
    protected abstract float speedMultiplier();

    @Override public Direction getOutputDirection(IMultiblockContext<S> ctx) { return ctx.getLevel().toAbsolute(outputFacing()); }

    @Override public List<MarkableFluidTank> getOutputTanks(S state) { return ImmutableList.of(state.tanks.output()); }

    @Override public void tickClient(IMultiblockContext<S> ctx) {
        CollectorState<R> state = ctx.getState();
        Level level = ctx.getLevel().getRawLevel();
        if (state.isSoundPlaying.getAsBoolean() || !state.isHeated(level)) { return; }
        Vec3 soundVec = ctx.getLevel().toAbsolute(Vec3.atCenterOf(soundPoi()));
        if (soundVolume(state, level, soundVec) > 0.01f) {
            int thisId = ++state.soundId;
            state.isSoundPlaying = MachineSound.startSound(() -> state.isHeated(level) && state.soundId == thisId, ctx.isValid(), soundVec, sound(), () -> soundVolume(state, level, soundVec), () -> 1f);
        }
    }

    private float soundVolume(CollectorState<R> state, Level level, Vec3 pos) { return ClientUtils.attenuated(pos, soundFalloff(), 2 * (float) (state.heatLevel / state.maxHeat(level))); }

    @Override public void tickServer(IMultiblockContext<S> ctx) {
        CollectorState<R> state = ctx.getState();
        IMultiblockLevel mlevel = ctx.getLevel();
        Level level = mlevel.getRawLevel();
        boolean update = false;
        state.loadTicks++;
        if (state.loadTicks > 10 && !state.isLoaded && !level.isClientSide) {
            state.isLoaded = true;
            updatePortNeighbors(mlevel);
            state.registerTower(level);
            if (state.registered) { state.reflectorStrength = checkReflectorPositions(mlevel, state); }
            update = true;
        }
        if (state.loadTicks > 20 && state.reCheckOnLoad && !level.isClientSide) {
            state.reCheckOnLoad = false;
            if (state.registered) { state.reflectorStrength = checkReflectorPositions(mlevel, state); }
            update = true;
        }
        if (!state.registered) { return; }
        boolean oldVisible = state.sunVisible;
        state.sunVisible = level.canSeeSky(state.sunPos);
        if (oldVisible != state.sunVisible) { update = true; }
        boolean enabled = enabled(ctx);
        if (!enabled && state.reflectorStrength > 0) {
            detachReflectorPositions(state);
            state.reflectorStrength = 0;
            update = true;
        }
        if (enabled && (level.getGameTime() % 60 == 0 || state.reflectorStrength == 0)) { state.reflectorStrength = checkReflectorPositions(mlevel, state); }
        boolean wasActive = state.active;
        update |= heatLogic(state, level, enabled);
        update |= recipeLogic(state, level, enabled);
        state.active = enabled && state.activeRecipe != null && state.heatLevel >= state.activeRecipe.requiredTemp();
        if (wasActive != state.active) { update = true; }
        update |= emptyInputContainer(state);
        update |= fillOutputContainer(state);
        pumpOutputs(ctx);
        double workingLevel = state.activeRecipe != null ? state.activeRecipe.requiredTemp() : state.workingHeat();
        int newComparatorValue = workingLevel > 0 ? (int) Math.min(15, (15 * state.heatLevel) / workingLevel) : 0;
        if (newComparatorValue != state.lastComparatorValue) {
            ctx.setComparatorOutputFor(redstonePoi(), newComparatorValue);
            state.lastComparatorValue = newComparatorValue;
            update = true;
        }
        if (update) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    private static boolean emptyInputContainer(CollectorState<?> state) {
        ItemStack inputFilled = state.inventory.getStackInSlot(SLOT_INPUT_FILLED);
        if (inputFilled.isEmpty()) { return false; }
        FluidActionResult res = FluidUtils.tryEmptyContainer(inputFilled, state.tanks.input(), Integer.MAX_VALUE, FluidAction.SIMULATE);
        if (!res.isSuccess()) { return false; }
        ItemStack inputEmpty = state.inventory.getStackInSlot(SLOT_INPUT_EMPTY);
        if (ItemOutputs.overflows(inputEmpty, res.getResult())) { return false; }
        res = FluidUtils.tryEmptyContainer(inputFilled, state.tanks.input(), Integer.MAX_VALUE, FluidAction.EXECUTE);
        if (!res.isSuccess()) { return false; }
        inputFilled.shrink(1);
        if (inputFilled.isEmpty()) { state.inventory.setStackInSlot(SLOT_INPUT_FILLED, ItemStack.EMPTY); }
        ItemOutputs.place(state.inventory, SLOT_INPUT_EMPTY, inputEmpty, res.getResult());
        return true;
    }

    private static boolean fillOutputContainer(CollectorState<?> state) {
        ItemStack outputEmpty = state.inventory.getStackInSlot(SLOT_OUTPUT_EMPTY);
        if (outputEmpty.isEmpty()) { return false; }
        ItemStack resultItem = FluidUtils.fillFluidContainer(state.tanks.output(), outputEmpty, state.inventory.getStackInSlot(SLOT_OUTPUT_FILLED), null);
        if (resultItem.isEmpty()) { return false; }
        ItemStack outputFilled = state.inventory.getStackInSlot(SLOT_OUTPUT_FILLED);
        if (ItemOutputs.overflows(outputFilled, resultItem)) { return false; }
        outputEmpty.shrink(1);
        if (outputEmpty.isEmpty()) { state.inventory.setStackInSlot(SLOT_OUTPUT_EMPTY, ItemStack.EMPTY); }
        ItemOutputs.place(state.inventory, SLOT_OUTPUT_FILLED, outputFilled, resultItem);
        return true;
    }

    private void updatePortNeighbors(IMultiblockLevel mlevel) {
        Level level = mlevel.getRawLevel();
        BlockPos inputPos = mlevel.toAbsolute(inputPois().get(0));
        level.updateNeighborsAt(inputPos, level.getBlockState(inputPos).getBlock());
        BlockPos outputPos = mlevel.toAbsolute(getOutputPositions().get(0));
        level.updateNeighborsAt(outputPos, level.getBlockState(outputPos).getBlock());
    }

    private double checkReflectorPositions(IMultiblockLevel mlevel, CollectorState<R> state) {
        double totalMirrorStrength = 0;
        int count = 0;
        byte[] dirCountsTemp = new byte[4];
        Level level = mlevel.getRawLevel();
        BlockPos basePos = mlevel.toAbsolute(linkPoi());
        BlockPos collectorPos = mlevel.toAbsolute(reflectorPoi());
        Set<BlockPos> unattached = new HashSet<>();
        for (BlockPos poiPos : SolarRegistry.getReflectorsInRange(level, basePos, SOLAR_MIN_RANGE, SOLAR_MAX_RANGE)) {
            if (!(stateAt(level, poiPos) instanceof SolarReflectorLogic.State reflector)) { continue; }
            if (!reflector.getTowerCollectorPosition().equals(collectorPos)) { unattached.add(poiPos); }
            else if (reflector.setTowerCollectorPosition(collectorPos)) {
                totalMirrorStrength += reflector.getSolarCollectorStrength();
                dirCountsTemp[getReflectorDir(poiPos, basePos)]++;
                count++;
            }
        }
        for (BlockPos poiPos : unattached) {
            if (count >= 24) { break; }
            if (stateAt(level, poiPos) instanceof SolarReflectorLogic.State reflector && !reflector.isMirrorTaken && reflector.setTowerCollectorPosition(collectorPos)) {
                totalMirrorStrength += reflector.getSolarCollectorStrength();
                dirCountsTemp[getReflectorDir(poiPos, basePos)]++;
                count++;
            }
        }
        state.dirCounts = dirCountsTemp;
        state.reflectorCount = (byte) count;
        return totalMirrorStrength;
    }

    private static int getReflectorDir(BlockPos poiPos, BlockPos basePos) {
        int dx = poiPos.getX() - basePos.getX();
        int dz = poiPos.getZ() - basePos.getZ();
        if (Math.abs(dx) > Math.abs(dz)) { return dx > 0 ? 1 : 3; }
        return dz > 0 ? 2 : 0;
    }

    static IMultiblockState stateAt(Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof IMultiblockBE<?> mbe)) { return null; }
        IMultiblockBEHelper<?> helper = mbe.getHelper();
        return helper != null ? helper.getState() : null;
    }

    private boolean heatLogic(CollectorState<R> state, Level level, boolean enabled) {
        double inc = enabled ? getTemperatureIncrease(state, level) : 0;
        double loss = dayMinHeatLoss() + lossPerSectionDrop() * (4 - getSolarIncidenceAngleSection(level)) + state.heatLevel * tempDependentLossFactor();
        double oldHeat = state.heatLevel;
        state.heatLevel = Math.max(0, state.heatLevel + inc - loss);
        double maxHeat = state.activeRecipe != null ? state.activeRecipe.requiredTemp() : state.workingHeat();
        state.heatLevel = Math.min(maxHeat, state.heatLevel);
        return oldHeat != state.heatLevel;
    }

    private double getTemperatureIncrease(CollectorState<R> state, Level level) {
        if (!state.registered || state.reflectorStrength <= 0 || !level.isDay() || level.isRaining() || !state.sunVisible) { return 0; }
        double effectiveStrength = state.reflectorStrength;
        if (state.activeRecipe != null) { effectiveStrength = Math.min(state.reflectorStrength, state.activeRecipe.requiredTemp() / tempToMinReflectorsDivisor() + 2 * reflectorTierOffset()); }
        return effectiveStrength * heatIncreaseFactor() * getSolarIncidenceAngleSection(level);
    }

    private boolean recipeLogic(CollectorState<R> state, Level level, boolean enabled) {
        FluidStack fs = state.tanks.input().getFluid();
        if (fs.getAmount() <= 0) {
            state.activeRecipe = null;
            state.processProgress = 0;
            state.totalProcessTime = 0;
            return false;
        }
        if (state.activeRecipe == null && state.activeRecipeId != null) {
            state.activeRecipe = state.recipeList.getById(level, state.activeRecipeId);
            state.activeRecipeId = null;
        }
        if (state.activeRecipe == null || !state.activeRecipe.input().testIgnoringAmount(fs)) {
            state.activeRecipe = state.recipeGetter.apply(level, fs);
            state.processProgress = 0;
            state.totalProcessTime = 0;
            if (state.activeRecipe == null) { return false; }
        }
        if (enabled && state.heatLevel >= state.activeRecipe.requiredTemp()) { state.processProgress += (int) speedMultiplier(); }
        else { state.processProgress = Math.max(0, state.processProgress - progressLossOffTemp()); }
        int total = state.activeRecipe.getTotalProcessTime();
        if (state.processProgress >= total) {
            assert state.activeRecipe.fluidOutput() != null;
            FluidStack out = state.activeRecipe.fluidOutput().copy();
            if (state.tanks.output().fill(out, FluidAction.SIMULATE) == out.getAmount()) {
                int amount = state.activeRecipe.input().getAmount();
                FluidStack drained = state.tanks.input().drain(amount, FluidAction.EXECUTE);
                if (drained.getAmount() == amount && state.activeRecipe.input().testIgnoringAmount(drained)) {
                    state.tanks.output().fill(out, FluidAction.EXECUTE);
                    state.processProgress = 0;
                    state.totalProcessTime = 0;
                    return true;
                }
            }
        }
        boolean changed = state.totalProcessTime != total;
        state.totalProcessTime = total;
        return changed;
    }

    private static void detachReflectorPositions(CollectorState<?> state) {
        Level level = state.levelSupplier.get();
        if (level == null || level.isClientSide) { return; }
        for (BlockPos poiPos : SolarRegistry.getReflectorsInRange(level, state.basePos, SOLAR_MIN_RANGE, SOLAR_MAX_RANGE)) {
            if (stateAt(level, poiPos) instanceof SolarReflectorLogic.State reflector) { reflector.detachTower(state.collectorPos); }
        }
    }

    public static int getSolarIncidenceAngleSection(Level level) {
        return switch (level.getSkyDarken()) {
            case 3 -> 1;
            case 2 -> 2;
            case 1 -> 3;
            case 0 -> 4;
            default -> 0;
        };
    }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<S> ctx, CapabilityPosition position, Capability<T> cap) {
        S state = ctx.getState();
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (inputPois().contains(localPos) && (side == null || side == inputFacing())) { return state.inputCap.cast(ctx); }
            if (getOutputPositions().contains(localPos) && (side == null || side == outputFacing())) { return state.outputCap.cast(ctx); }
        }
        return LazyOptional.empty();
    }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get(shapeName()).getter; }

    @Override public void dropExtraItems(S state, Consumer<ItemStack> drop) {
        Level level = state.levelSupplier.get();
        if (level != null && !level.isClientSide) {
            detachReflectorPositions(state);
            SolarRegistry.unregisterTower(level, state.basePos);
        }
        MultiBlockInventoryUtils.dropItems(state.inventory, drop);
        try {
            state.inputCap.get(null).invalidate();
            state.outputCap.get(null).invalidate();
        }
        catch (Exception ignored) {}
    }

    public abstract static class CollectorState<R extends MultiblockRecipe & ISolarRecipe> implements ISolarMultiblockState, IDisplaySyncState, IDataReloadAware {
        public final TankPair tanks;
        public final StoredCapability<IFluidHandler> inputCap;
        public final StoredCapability<IFluidHandler> outputCap;
        public final ConstrainedItemHandler inventory;
        public double heatLevel = 0;
        public double reflectorStrength = 0;
        public byte reflectorCount = 0;
        public final BlockPos basePos;
        public final BlockPos collectorPos;
        public final BlockPos sunPos;
        public final Supplier<Level> levelSupplier;
        public byte[] dirCounts = new byte[4];
        public int processProgress = 0;
        public int totalProcessTime = 0;
        public R activeRecipe = null;
        private ResourceLocation activeRecipeId;
        private final RecipeCache.HintedGetter2<Level, FluidStack, R> recipeFinder;
        private final BiFunction<Level, FluidStack, R> recipeGetter;
        private final CachedRecipeList<R> recipeList;
        public boolean isLoaded = false;
        public boolean registered;
        public boolean failVertical = false;
        public int requiredMove = 0;
        public boolean active;
        public int lastComparatorValue = -1;
        public BooleanSupplier isSoundPlaying = () -> false;
        private int soundId = 0;
        public boolean sunVisible = true;
        private int loadTicks = 0;
        private boolean reCheckOnLoad = false;
        private transient boolean savedRegistered = false;

        protected CollectorState(IInitialMultiblockContext<?> ctx, int inputCapacity, int outputCapacity, RecipeCache.HintedGetter2<Level, FluidStack, R> recipeFinder, CachedRecipeList<R> recipeList, BlockPos linkPoi, BlockPos reflectorPoi, BlockPos sunPoi) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> {
                markDirty.run();
                sync.run();
            };
            this.recipeFinder = recipeFinder;
            this.recipeGetter = RecipeCache.cached(recipeFinder);
            this.recipeList = recipeList;
            tanks = new TankPair(v -> onChanged.run(), inputCapacity, outputCapacity);
            inventory = new ConstrainedItemHandler(List.of(ConstrainedItemHandler.IOConstraint.FLUID_INPUT, ConstrainedItemHandler.IOConstraint.OUTPUT, ConstrainedItemHandler.IOConstraint.FLUID_INPUT, ConstrainedItemHandler.IOConstraint.OUTPUT), onChanged);
            inputCap = new StoredCapability<>(new MultiTankFluidHandler(tanks.input(), false, true, onChanged));
            outputCap = new StoredCapability<>(new MultiTankFluidHandler(tanks.output(), true, false, onChanged));
            InitialMultiblockContext<?> initialContext = (InitialMultiblockContext<?>) ctx;
            MultiblockOrientation orientation = initialContext.orientation();
            BlockPos origin = initialContext.masterBE().getBlockPos().subtract(orientation.getAbsoluteOffset(initialContext.masterOffset()));
            this.basePos = origin.offset(orientation.getAbsoluteOffset(linkPoi));
            this.collectorPos = origin.offset(orientation.getAbsoluteOffset(reflectorPoi));
            this.sunPos = origin.offset(orientation.getAbsoluteOffset(sunPoi));
            this.levelSupplier = ctx.levelSupplier();
            Level level = levelSupplier.get();
            SolarRegistry.RegisterResult result = level != null && !level.isClientSide ? SolarRegistry.registerTower(level, basePos) : new SolarRegistry.RegisterResult();
            this.registered = result.success;
            if (!this.registered) {
                this.failVertical = result.vertical;
                this.requiredMove = result.requiredMove;
            }
        }

        protected abstract double workingHeat();

        private double maxHeat(Level level) {
            FluidStack fluid = tanks.input().getFluid();
            R recipe = fluid.getAmount() > 0 ? recipeGetter.apply(level, fluid) : null;
            return recipe != null ? recipe.requiredTemp() : workingHeat();
        }

        protected boolean isHeated(Level level) { return heatLevel >= maxHeat(level) && sunVisible && reflectorStrength > 0; }

        private void registerTower(Level level) {
            SolarRegistry.RegisterResult result = SolarRegistry.registerTower(level, basePos);
            registered = result.success;
            failVertical = result.vertical;
            requiredMove = result.requiredMove;
            if (!registered && savedRegistered) {
                SolarRegistryData data = SolarRegistry.getData(level);
                data.towerBasesByY.computeIfAbsent(basePos.getY(), k -> new HashSet<>()).add(basePos);
                data.setDirty();
                registered = true;
                failVertical = false;
                requiredMove = 0;
            }
        }

        @Override public void onDataReload(IInitialMultiblockContext<?> context) { lastComparatorValue = -1; }

        @Override public double getHeatLevel() { return heatLevel; }

        @Override public byte[] getDirCounts() { return dirCounts; }

        @Override public int getProcessProgress() { return processProgress; }

        @Override public boolean isSunVisible() { return sunVisible; }

        @Override public TankPair getTanks() { return tanks; }

        @Override public ConstrainedItemHandler getInventory() { return inventory; }

        @Override public void writeSaveNBT(CompoundTag nbt) {
            nbt.put("tanks", this.tanks.toNBT());
            nbt.put("inventory", inventory.serializeNBT());
            nbt.putDouble("heatLevel", heatLevel);
            nbt.putDouble("reflectorStrength", reflectorStrength);
            nbt.putByte("reflectorCount", reflectorCount);
            nbt.putByteArray("dirCounts", dirCounts);
            nbt.putInt("processProgress", processProgress);
            nbt.putInt("totalProcessTime", totalProcessTime);
            if (activeRecipe != null) { nbt.putString("activeRecipe", activeRecipe.getId().toString()); }
            nbt.putBoolean("registered", registered);
            nbt.putBoolean("failVertical", failVertical);
            nbt.putInt("requiredMove", requiredMove);
            nbt.putBoolean("active", active);
        }

        @Override public void readSaveNBT(CompoundTag nbt) {
            this.tanks.readNBT(nbt.getCompound("tanks"));
            this.inventory.deserializeNBT(nbt.getCompound("inventory"));
            heatLevel = nbt.getDouble("heatLevel");
            reflectorStrength = nbt.getDouble("reflectorStrength");
            reflectorCount = nbt.getByte("reflectorCount");
            dirCounts = nbt.getByteArray("dirCounts");
            processProgress = nbt.getInt("processProgress");
            totalProcessTime = nbt.getInt("totalProcessTime");
            if (nbt.contains("activeRecipe")) { activeRecipeId = ResourceLocation.tryParse(nbt.getString("activeRecipe")); }
            registered = nbt.getBoolean("registered");
            failVertical = nbt.getBoolean("failVertical");
            requiredMove = nbt.getInt("requiredMove");
            active = nbt.getBoolean("active");
            savedRegistered = nbt.getBoolean("registered");
            reCheckOnLoad = nbt.getBoolean("registered");
            isLoaded = false;
            Level level = levelSupplier.get();
            if (level != null && !level.isClientSide) { registerTower(level); }
        }

        @Override public boolean isActive() { return active; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input(), tanks.output()}; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.put("tanks", this.tanks.toNBT());
            nbt.putDouble("heatLevel", heatLevel);
            nbt.putDouble("reflectorStrength", reflectorStrength);
            nbt.putByteArray("dirCounts", dirCounts);
            nbt.putBoolean("sunVisible", sunVisible);
            nbt.putBoolean("active", active);
            nbt.putInt("processProgress", processProgress);
            nbt.putInt("totalProcessTime", totalProcessTime);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt) {
            this.tanks.readNBT(nbt.getCompound("tanks"));
            heatLevel = nbt.getDouble("heatLevel");
            reflectorStrength = nbt.getDouble("reflectorStrength");
            dirCounts = nbt.getByteArray("dirCounts");
            sunVisible = nbt.getBoolean("sunVisible");
            active = nbt.getBoolean("active");
            processProgress = nbt.getInt("processProgress");
            totalProcessTime = nbt.getInt("totalProcessTime");
        }

        @Override public void addDisplayLines(Level level, DisplayLines lines) {
            FluidStack input = tanks.input().getFluid();
            R recipe = input.isEmpty() ? null : recipeFinder.get(level, input, null);
            lines.temperature(heatLevel, recipe != null ? recipe.requiredTemp() : workingHeat()).percent(totalProcessTime > 0 ? processProgress * 100 / totalProcessTime : 0);
            if (input.isEmpty()) { lines.fuelEmpty(); }
        }
    }
}
