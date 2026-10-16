package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerTankRecipe;
import mctmods.immersivetechnology.core.CommonConfig;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.util.FluidContainers;
import mctmods.immersivetechnology.core.util.IDisplaySyncState;

import com.immersiveconvergence.api.multiblock.blocks.component.IMultiblockComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.IServerTickableComponent;
import com.immersiveconvergence.api.multiblock.blocks.env.IInitialMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.logic.IMultiblockLogic;
import com.immersiveconvergence.api.multiblock.blocks.util.*;
import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.capability.HeatCapabilities;
import com.immersiveconvergence.api.capability.IHeatConsumer;
import com.immersiveconvergence.api.capability.IHeatProvider;
import com.immersiveconvergence.api.integration.DisplayLines;
import com.immersiveconvergence.api.multiblock.IDataReloadAware;
import com.immersiveconvergence.api.multiblock.IFluidOutputPump;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.util.ConstrainedItemHandler;
import com.immersiveconvergence.api.util.ICFluidUtils;
import com.immersiveconvergence.api.util.MarkableFluidTank;
import com.immersiveconvergence.api.util.MultiBlockInventoryUtils;
import com.immersiveconvergence.api.util.MultiTankFluidHandler;
import com.immersiveconvergence.api.util.RecipeCache;
import com.immersiveconvergence.api.util.TankPair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BoilerTankLogic implements IMultiblockLogic<BoilerTankLogic.State>, IServerTickableComponent<BoilerTankLogic.State>, IFluidOutputPump<BoilerTankLogic.State> {
    public static final int INPUT_SLOT_FILLED = 0;
    public static final int INPUT_SLOT_EMPTY = 1;
    public static final int OUTPUT_SLOT_EMPTY = 2;
    public static final int OUTPUT_SLOT_FILLED = 3;
    public static List<BlockPos> INPUT_FLUID_POIS;
    public static List<BlockPos> OUTPUT_FLUID_POIS;
    public static List<BlockPos> HEAT_INPUT_POIS;
    public static List<BlockPos> COMPARATOR_POSITIONS;
    private static RelativeBlockFace INPUT_FLUID_FACING;
    private static RelativeBlockFace OUTPUT_FLUID_FACING;
    public static RelativeBlockFace HEAT_INPUT_FACING;

    static { ITShapes.readPois("boiler_tank", BoilerTankLogic::loadPois); }

    public static int tankCapacity() { return ServerConfig.boilerTankCapacity; }

    public static double defaultWorkingHeatLevel() { return CommonConfig.boilerDefaultWorkingHeat(); }

    @Override public List<BlockPos> getOutputPositions() { return OUTPUT_FLUID_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FLUID_FACING); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output()); }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        Level level = ctx.getLevel().getRawLevel();
        double heatLevel = 0;
        if (ctx.getLevel().toAbsolute(HEAT_INPUT_FACING) != null && state.heatSource != null) {
            IHeatProvider provider = state.heatSource.get();
            if (provider != null) { heatLevel = provider.getHeatLevel(); }
        }
        boolean update = state.heatLevel != heatLevel;
        state.heatLevel = heatLevel;
        double displayMax = defaultWorkingHeatLevel();
        if (state.lastRecipe != null) { displayMax = Math.max(displayMax, state.lastRecipe.requiredHeat); }
        else if (state.tanks.input().getFluidAmount() > 0) {
            BoilerTankRecipe potentialRecipe = state.recipeGetter.apply(level, state.tanks.input().getFluid());
            if (potentialRecipe != null) { displayMax = Math.max(displayMax, potentialRecipe.requiredHeat); }
        }
        state.workingHeatLevel = Math.max(displayMax, heatLevel);
        boolean isActive = heatLevel >= state.workingHeatLevel && state.recipeTimeRemaining > 0;
        if (state.active != isActive) {
            state.active = isActive;
            update = true;
        }
        if (heatLevel >= state.workingHeatLevel) { update |= process(state, level, heatLevel); }
        else if (state.recipeTimeRemaining > 0) {
            int previousProgress = state.recipeTimeRemaining;
            if (state.lastRecipe == null) { state.recipeTimeRemaining = 0; }
            else { state.recipeTimeRemaining = Math.min(state.recipeTimeRemaining + ServerConfig.boilerTankProgressLossPerTick, state.lastRecipe.getTotalProcessTime()); }
            if (previousProgress != state.recipeTimeRemaining) { update = true; }
        }
        if (state.tanks.output().getFluidAmount() > 0 && ICFluidUtils.fillFluidContainer(state.tanks.output(), OUTPUT_SLOT_EMPTY, OUTPUT_SLOT_FILLED, state.inventory)) { update = true; }
        pumpOutputs(ctx);
        if (FluidContainers.emptyBucket(state.tanks.input(), state.inventory, INPUT_SLOT_FILLED, INPUT_SLOT_EMPTY)) { update = true; }
        int newComparatorValue = state.workingHeatLevel > 0 ? (int) Math.min(15, (15 * state.heatLevel) / state.workingHeatLevel) : 0;
        if (ctx.updateComparatorOutputs(state.lastComparatorValue, newComparatorValue, COMPARATOR_POSITIONS)) { update = true; }
        state.lastComparatorValue = newComparatorValue;
        if (update) { ctx.markDirtyAndSync(); }
    }

    private static boolean process(State state, Level level, double heatLevel) {
        if (state.recipeTimeRemaining > 0) {
            if (state.lastRecipe == null) {
                state.recipeTimeRemaining = 0;
                return true;
            }
            state.recipeTimeRemaining--;
            if (state.recipeTimeRemaining != 0) { return false; }
            state.tanks.output().fill(state.lastRecipe.output.copy(), FluidAction.EXECUTE);
            state.totalProcessTime = 0;
            return true;
        }
        if (state.tanks.input().getFluidAmount() <= 0) { return false; }
        state.lastRecipe = state.recipeGetter.apply(level, state.tanks.input().getFluid());
        BoilerTankRecipe recipe = state.lastRecipe;
        if (recipe == null || recipe.getInputAmount() > state.tanks.input().getFluidAmount() || recipe.output.getAmount() > state.tanks.output().getCapacity() - state.tanks.output().getFluidAmount() || heatLevel < recipe.requiredHeat) { return false; }
        int reqAmount = recipe.getInputAmount();
        FluidStack drained = state.tanks.input().drain(reqAmount, FluidAction.EXECUTE);
        if (drained.getAmount() != reqAmount || !recipe.matches(drained)) { return false; }
        state.totalProcessTime = recipe.getTotalProcessTime();
        state.recipeTimeRemaining = state.totalProcessTime - 1;
        return true;
    }

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.register(Capabilities.FluidHandler.BLOCK, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (INPUT_FLUID_POIS.contains(localPos) && (side == null || side == INPUT_FLUID_FACING)) { return state.inputCap; }
            if (OUTPUT_FLUID_POIS.contains(localPos) && (side == null || side == OUTPUT_FLUID_FACING)) { return state.outputCap; }
            return null;
        });
        register.register(HeatCapabilities.HEAT_CONSUMER, (state, position) -> {
            RelativeBlockFace side = position.side();
            return HEAT_INPUT_POIS.contains(position.posInMultiblock()) && (side == null || side == HEAT_INPUT_FACING) ? state.boilerInputCap : null;
        });
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { MultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get("boiler_tank").getter; }

    public static class State implements IDisplaySyncState, IDataReloadAware {
        public final BiFunction<Level, FluidStack, BoilerTankRecipe> recipeGetter = RecipeCache.cached(BoilerTankRecipe::findRecipe);
        public final TankPair tanks;
        public final IFluidHandler inputCap;
        public final IFluidHandler outputCap;
        public final IHeatConsumer boilerInputCap;
        public Supplier<IHeatProvider> heatSource;
        public final ConstrainedItemHandler inventory;
        public int recipeTimeRemaining = 0;
        public int totalProcessTime = 0;
        public BoilerTankRecipe lastRecipe;
        public double heatLevel = 0;
        public double workingHeatLevel = defaultWorkingHeatLevel();
        public int lastComparatorValue = -1;
        public boolean active = false;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); };
            tanks = new TankPair(v -> onChanged.run(), tankCapacity(), tankCapacity());
            inventory = new ConstrainedItemHandler(List.of(ConstrainedItemHandler.IOConstraint.FLUID_INPUT, ConstrainedItemHandler.IOConstraint.OUTPUT, ConstrainedItemHandler.IOConstraint.FLUID_INPUT, ConstrainedItemHandler.IOConstraint.OUTPUT), onChanged);
            inputCap = new MultiTankFluidHandler(tanks.input(), false, true, onChanged);
            outputCap = new MultiTankFluidHandler(tanks.output(), true, false, onChanged);
            boilerInputCap = tanks.input()::getFluidAmount;
            bindOutputs(ctx);
        }

        @Override public void onDataReload(IInitialMultiblockContext<?> context) {
            lastComparatorValue = -1;
            bindOutputs(context);
        }

        private void bindOutputs(IInitialMultiblockContext<?> context) { heatSource = context.getCapabilityAt(HeatCapabilities.HEAT_PROVIDER, MultiblockPOIHelper.opposing(HEAT_INPUT_FACING, HEAT_INPUT_POIS.getFirst())); }

        public double getWorkingHeatLevel() { return workingHeatLevel; }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.putInt("recipeTimeRemaining", recipeTimeRemaining);
            nbt.putInt("totalProcessTime", totalProcessTime);
            nbt.put("inventory", inventory.serializeNBT(provider));
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            recipeTimeRemaining = nbt.getInt("recipeTimeRemaining");
            totalProcessTime = nbt.getInt("totalProcessTime");
            inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
        }

        @Override public boolean isActive() { return active; }

        @Override public IItemHandlerModifiable getInventory() { return inventory; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input(), tanks.output()}; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("active", active);
            nbt.putDouble("heatLevel", heatLevel);
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.putDouble("workingHeatLevel", workingHeatLevel);
            nbt.putInt("recipeTimeRemaining", recipeTimeRemaining);
            nbt.putInt("totalProcessTime", totalProcessTime);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            active = nbt.getBoolean("active");
            heatLevel = nbt.getDouble("heatLevel");
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            recipeTimeRemaining = nbt.getInt("recipeTimeRemaining");
            totalProcessTime = nbt.getInt("totalProcessTime");
            if (nbt.contains("workingHeatLevel")) { workingHeatLevel = nbt.getDouble("workingHeatLevel"); }
        }

        @Override public void addDisplayLines(Level level, DisplayLines lines) { lines.temperature(heatLevel, workingHeatLevel).percent((totalProcessTime > 0 && recipeTimeRemaining > 0) ? (totalProcessTime - recipeTimeRemaining) * 100 / totalProcessTime : 0); }
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        INPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_input0");
        OUTPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_output0");
        HEAT_INPUT_POIS = MultiblockPOIHelper.getPosList(pois, "heat_input0");
        COMPARATOR_POSITIONS = MultiblockPOIHelper.getPosList(pois, "comparator0");
        INPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_input0");
        OUTPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_output0");
        HEAT_INPUT_FACING = MultiblockPOIHelper.getFacing(pois, "heat_input0");
    }
}
