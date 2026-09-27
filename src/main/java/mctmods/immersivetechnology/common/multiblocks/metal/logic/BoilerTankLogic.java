package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerTankRecipe;
import mctmods.immersivetechnology.core.CommonConfig;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.util.FluidContainers;
import mctmods.immersivetechnology.core.util.IDisplaySyncState;

import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
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
import com.immersiveconvergence.api.util.MarkableFluidTank;
import com.immersiveconvergence.api.util.MultiBlockInventoryUtils;
import com.immersiveconvergence.api.util.MultiTankFluidHandler;
import com.immersiveconvergence.api.util.RecipeCache;
import com.immersiveconvergence.api.util.TankPair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandlerModifiable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

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
        if (ctx.getLevel().toAbsolute(HEAT_INPUT_FACING) != null && state.heatSource.isPresent()) { heatLevel = state.heatSource.get().getHeatLevel(); }
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
        if (state.tanks.output().getFluidAmount() > 0 && FluidUtils.fillFluidContainer(state.tanks.output(), OUTPUT_SLOT_EMPTY, OUTPUT_SLOT_FILLED, state.inventory)) { update = true; }
        pumpOutputs(ctx);
        if (FluidContainers.emptyBucket(state.tanks.input(), state.inventory, INPUT_SLOT_FILLED, INPUT_SLOT_EMPTY)) { update = true; }
        int newComparatorValue = state.workingHeatLevel > 0 ? (int) Math.min(15, (15 * state.heatLevel) / state.workingHeatLevel) : 0;
        if (newComparatorValue != state.lastComparatorValue) {
            for (BlockPos pos : COMPARATOR_POSITIONS) { ctx.setComparatorOutputFor(pos, newComparatorValue); }
            state.lastComparatorValue = newComparatorValue;
            update = true;
        }
        if (update) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
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
        if (recipe == null || recipe.input.getAmount() > state.tanks.input().getFluidAmount() || recipe.output.getAmount() > state.tanks.output().getCapacity() - state.tanks.output().getFluidAmount() || heatLevel < recipe.requiredHeat) { return false; }
        int reqAmount = recipe.input.getAmount();
        FluidStack drained = state.tanks.input().drain(reqAmount, FluidAction.EXECUTE);
        if (drained.getAmount() != reqAmount || !recipe.input.testIgnoringAmount(drained)) { return false; }
        state.totalProcessTime = recipe.getTotalProcessTime();
        state.recipeTimeRemaining = state.totalProcessTime - 1;
        return true;
    }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        BlockPos localPos = position.posInMultiblock();
        RelativeBlockFace side = position.side();
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (INPUT_FLUID_POIS.contains(localPos) && (side == null || side == INPUT_FLUID_FACING)) { return ctx.getState().inputCap.cast(ctx); }
            if (OUTPUT_FLUID_POIS.contains(localPos) && (side == null || side == OUTPUT_FLUID_FACING)) { return ctx.getState().outputCap.cast(ctx); }
        }
        else if (cap == HeatCapabilities.HEAT_CONSUMER_CAPABILITY && HEAT_INPUT_POIS.contains(localPos) && (side == null || side == HEAT_INPUT_FACING)) { return ctx.getState().boilerInputCap.cast(ctx); }
        return LazyOptional.empty();
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { MultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get("boiler_tank").getter; }

    public static class State implements IDisplaySyncState, IDataReloadAware {
        public final BiFunction<Level, FluidStack, BoilerTankRecipe> recipeGetter = RecipeCache.cached(BoilerTankRecipe::findRecipe);
        public final TankPair tanks;
        public final StoredCapability<IFluidHandler> inputCap;
        public final StoredCapability<IFluidHandler> outputCap;
        public final StoredCapability<IHeatConsumer> boilerInputCap;
        public CapabilityReference<IHeatProvider> heatSource;
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
            inputCap = new StoredCapability<>(new MultiTankFluidHandler(tanks.input(), false, true, onChanged));
            outputCap = new StoredCapability<>(new MultiTankFluidHandler(tanks.output(), true, false, onChanged));
            boilerInputCap = new StoredCapability<>(tanks.input()::getFluidAmount);
            bindOutputs(ctx);
        }

        @Override public void onDataReload(IInitialMultiblockContext<?> context) {
            lastComparatorValue = -1;
            bindOutputs(context);
        }

        private void bindOutputs(IInitialMultiblockContext<?> context) { heatSource = context.getCapabilityAt(HeatCapabilities.HEAT_PROVIDER_CAPABILITY, MultiblockPOIHelper.opposing(HEAT_INPUT_FACING, HEAT_INPUT_POIS.get(0))); }

        public double getWorkingHeatLevel() { return workingHeatLevel; }

        @Override public void writeSaveNBT(CompoundTag nbt) {
            nbt.put("tanks", tanks.toNBT());
            nbt.putInt("recipeTimeRemaining", recipeTimeRemaining);
            nbt.putInt("totalProcessTime", totalProcessTime);
            nbt.put("inventory", inventory.serializeNBT());
        }

        @Override public void readSaveNBT(CompoundTag nbt) {
            tanks.readNBT(nbt.getCompound("tanks"));
            recipeTimeRemaining = nbt.getInt("recipeTimeRemaining");
            totalProcessTime = nbt.getInt("totalProcessTime");
            inventory.deserializeNBT(nbt.getCompound("inventory"));
        }

        @Override public boolean isActive() { return active; }

        @Override public IItemHandlerModifiable getInventory() { return inventory; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input(), tanks.output()}; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.putDouble("heatLevel", heatLevel);
            nbt.put("tanks", tanks.toNBT());
            nbt.putDouble("workingHeatLevel", workingHeatLevel);
            nbt.putInt("recipeTimeRemaining", recipeTimeRemaining);
            nbt.putInt("totalProcessTime", totalProcessTime);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt) {
            active = nbt.getBoolean("active");
            heatLevel = nbt.getDouble("heatLevel");
            tanks.readNBT(nbt.getCompound("tanks"));
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
