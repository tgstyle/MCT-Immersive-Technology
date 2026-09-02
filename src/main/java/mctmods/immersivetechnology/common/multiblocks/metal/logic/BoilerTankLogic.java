package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import com.immersiveconvergence.api.integration.DisplayLines;
import com.immersiveconvergence.api.multiblock.IDisplayContext;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.util.MultiBlockInventoryUtils;
import com.immersiveconvergence.api.multiblock.IFluidOutputPump;
import com.immersiveconvergence.api.util.ConstrainedItemHandler;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerTankRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.BoilerTankShape;
import com.immersiveconvergence.api.util.MultiTankFluidHandler;
import com.immersiveconvergence.api.util.MarkableFluidTank;
import mctmods.immersivetechnology.core.CommonConfig;
import mctmods.immersivetechnology.core.ServerConfig;
import com.immersiveconvergence.api.util.RecipeCache;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.capability.HeatCapabilities;
import com.immersiveconvergence.api.capability.IHeatConsumer;
import com.immersiveconvergence.api.capability.IHeatProvider;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.BiFunction;

public class BoilerTankLogic implements IMultiblockLogic<BoilerTankLogic.State>, IServerTickableComponent<BoilerTankLogic.State>, IFluidOutputPump<BoilerTankLogic.State> {
    public static final int INPUT_SLOT_FILLED = 0;
    public static final int INPUT_SLOT_EMPTY = 1;
    public static final int OUTPUT_SLOT_EMPTY = 2;
    public static final int OUTPUT_SLOT_FILLED = 3;

    public static int tankCapacity() { return ServerConfig.boilerTankCapacity; }
    public static int progressLossPerTick() { return ServerConfig.boilerTankProgressLossPerTick; }
    public static double defaultWorkingHeatLevel() { return CommonConfig.boilerDefaultWorkingHeat; }

    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(BoilerTankShape.DATA.pointsOfInterest);

    public static final List<BlockPos> INPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_input0");
    public static final List<BlockPos> OUTPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output0");
    public static final List<BlockPos> HEAT_INPUT_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "heat_input0");
    public static final List<BlockPos> COMPARATOR_POSITIONS = MultiblockPOIHelper.getPosList(RAW_POIS, "comparator0");
    private static final RelativeBlockFace INPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_input0");
    private static final RelativeBlockFace OUTPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output0");
    private static final RelativeBlockFace HEAT_INPUT_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "heat_input0");

    @Override public List<BlockPos> getOutputPositions() { return OUTPUT_FLUID_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FLUID_FACING); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output); }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        final Level level = ctx.getLevel().getRawLevel();
        boolean update = false;
        double heatLevel = 0;
        Direction heatFacing = ctx.getLevel().toAbsolute(HEAT_INPUT_FACING);
        if (heatFacing != null && state.heatSource.isPresent()) { heatLevel = state.heatSource.get().getHeatLevel(); }
        double previousHeatLevel = state.heatLevel;
        state.heatLevel = heatLevel;

        double displayMax = defaultWorkingHeatLevel();
        if (state.lastRecipe != null) {
            displayMax = Math.max(displayMax, state.lastRecipe.requiredHeat);
        } else if (state.tanks.input.getFluidAmount() > 0) {
            BoilerTankRecipe potentialRecipe = state.recipeGetter.apply(level, state.tanks.input.getFluid());
            if (potentialRecipe != null) {
                displayMax = Math.max(displayMax, potentialRecipe.requiredHeat);
            }
        }
        state.workingHeatLevel = Math.max(displayMax, heatLevel);

        boolean isActive = heatLevel >= state.getWorkingHeatLevel() && state.recipeTimeRemaining > 0;
        if (state.active != isActive) { state.active = isActive; update = true; }
        if (previousHeatLevel != state.heatLevel) { update = true; }
        double required = state.getWorkingHeatLevel();
        if (heatLevel >= required) {
            if (state.recipeTimeRemaining > 0) {
                if (state.lastRecipe == null) { state.recipeTimeRemaining = 0; update = true; }
                else {
                    state.recipeTimeRemaining--;
                    if (state.recipeTimeRemaining == 0) {
                        state.tanks.output.fill(state.lastRecipe.output.copy(), FluidAction.EXECUTE);
                        state.totalProcessTime = 0;
                        update = true;
                    }
                }
            } else if (state.tanks.input.getFluidAmount() > 0) {
                state.lastRecipe = state.recipeGetter.apply(level, state.tanks.input.getFluid());
                if (state.lastRecipe != null && state.lastRecipe.input.getAmount() <= state.tanks.input.getFluidAmount() && state.lastRecipe.output.getAmount() <= state.tanks.output.getCapacity() - state.tanks.output.getFluidAmount()) {
                    if (heatLevel >= state.lastRecipe.requiredHeat) {
                        int reqAmount = state.lastRecipe.input.getAmount();
                        FluidStack drained = state.tanks.input.drain(reqAmount, FluidAction.EXECUTE);
                        if (drained.getAmount() == reqAmount && state.lastRecipe.input.testIgnoringAmount(drained)) {
                            state.recipeTimeRemaining = state.lastRecipe.getTotalProcessTime();
                            state.totalProcessTime = state.lastRecipe.getTotalProcessTime();
                            state.recipeTimeRemaining--;
                            update = true;
                        }
                    }
                }
            }
        } else if (state.recipeTimeRemaining > 0) {
            int previousProgress = state.recipeTimeRemaining;
            if (state.lastRecipe == null) { state.recipeTimeRemaining = 0; update = true; }
            else {
                state.recipeTimeRemaining = Math.min(state.recipeTimeRemaining + progressLossPerTick(), state.lastRecipe.getTotalProcessTime());
                if (previousProgress != state.recipeTimeRemaining) { update = true; }
            }
        }
        if (state.tanks.output.getFluidAmount() > 0) {
            if (FluidUtils.fillFluidContainer(state.tanks.output, OUTPUT_SLOT_EMPTY, OUTPUT_SLOT_FILLED, state.inventory)) { update = true; }
        }
        pumpOutputs(ctx);
        if (tryEmptyContainer(state.tanks.input, state.inventory)) { update = true; }
        int newComparatorValue = state.workingHeatLevel > 0 ? (int) Math.min(15, (15 * state.heatLevel) / state.workingHeatLevel) : 0;
        if (newComparatorValue != state.lastComparatorValue) { for (BlockPos pos : COMPARATOR_POSITIONS) { ctx.setComparatorOutputFor(pos, newComparatorValue); } state.lastComparatorValue = newComparatorValue; update = true; }
        if (update) { ctx.markMasterDirty(); ctx.requestMasterBESync(); }
    }

    private boolean tryEmptyContainer(IFluidHandler tank, IItemHandlerModifiable inv) {
        ItemStack filledContainer = inv.getStackInSlot(BoilerTankLogic.INPUT_SLOT_FILLED);
        if (filledContainer.isEmpty()) { return false; }
        FluidActionResult simResult = FluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.SIMULATE);
        if (!simResult.isSuccess()) { return false; }
        ItemStack emptyContainer = simResult.getResult();
        ItemStack outputStack = inv.getStackInSlot(BoilerTankLogic.INPUT_SLOT_EMPTY);
        if (!outputStack.isEmpty() && !ItemHandlerHelper.canItemStacksStack(outputStack, emptyContainer)) { return false; }
        if (outputStack.getCount() + emptyContainer.getCount() > emptyContainer.getMaxStackSize()) { return false; }
        FluidActionResult execResult = FluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.EXECUTE);
        filledContainer.shrink(1);
        inv.setStackInSlot(BoilerTankLogic.INPUT_SLOT_FILLED, filledContainer);
        if (outputStack.isEmpty()) { inv.setStackInSlot(BoilerTankLogic.INPUT_SLOT_EMPTY, execResult.getResult()); }
        else { outputStack.grow(execResult.getResult().getCount()); }
        return true;
    }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        BlockPos localPos = position.posInMultiblock();
        RelativeBlockFace side = position.side();
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (INPUT_FLUID_POIS.contains(localPos) && (side == null || side == INPUT_FLUID_FACING)) { return ctx.getState().inputCap.cast(ctx); }
            if (OUTPUT_FLUID_POIS.contains(localPos) && (side == null || side == OUTPUT_FLUID_FACING)) { return ctx.getState().outputCap.cast(ctx); }
        } else if (cap == HeatCapabilities.HEAT_CONSUMER_CAPABILITY) {
            if (HEAT_INPUT_POIS.contains(localPos) && (side == null || side == HEAT_INPUT_FACING)) { return ctx.getState().boilerInputCap.cast(ctx); }
        }
        return LazyOptional.empty();
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { MultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return BoilerTankShape.GETTER; }

    public static class State implements IMultiblockState, IDisplayContext {
        public final BiFunction<Level, FluidStack, BoilerTankRecipe> recipeGetter = RecipeCache.cached(BoilerTankRecipe::findRecipe);
        public final BoilerTanks tanks;
        public StoredCapability<IFluidHandler> inputCap;
        public StoredCapability<IFluidHandler> outputCap;
        public StoredCapability<IHeatConsumer> boilerInputCap;
        public CapabilityReference<IHeatProvider> heatSource;
        public ConstrainedItemHandler inventory;
        public int recipeTimeRemaining = 0;
        public int totalProcessTime = 0;
        public BoilerTankRecipe lastRecipe;
        public double heatLevel = 0;
        public double workingHeatLevel = defaultWorkingHeatLevel();
        public int lastComparatorValue = -1;
        public boolean active = false;
        public boolean tanksDirty = false;
        public boolean inventoryDirty = false;

        public State(IInitialMultiblockContext<State> ctx) {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            final Runnable sync = ctx.getSyncRunnable();
            final Runnable onChanged = () -> { markDirty.run(); sync.run(); this.tanksDirty = true; this.inventoryDirty = true; };
            tanks = new BoilerTanks(v -> { onChanged.run(); this.tanksDirty = true; });
            inventory = new ConstrainedItemHandler(
                    List.of(
                            ConstrainedItemHandler.IOConstraint.FLUID_INPUT,
                            ConstrainedItemHandler.IOConstraint.OUTPUT,
                            ConstrainedItemHandler.IOConstraint.FLUID_INPUT,
                            ConstrainedItemHandler.IOConstraint.OUTPUT
                    ),
                    () -> { onChanged.run(); this.inventoryDirty = true; }
            );
            inputCap = new StoredCapability<>(new MultiTankFluidHandler(tanks.input, false, true, () -> { onChanged.run(); this.tanksDirty = true; }));
            outputCap = new StoredCapability<>(new MultiTankFluidHandler(tanks.output, true, false, () -> { onChanged.run(); this.tanksDirty = true; }));
            boilerInputCap = new StoredCapability<>(new BoilerInputImpl(tanks.input));
            MultiblockFace heatMBFace = new MultiblockFace(HEAT_INPUT_FACING, HEAT_INPUT_POIS.get(0));
            CapabilityPosition heatOpposingCP = CapabilityPosition.opposing(heatMBFace);
            MultiblockFace heatOpposingMBFace = new MultiblockFace(heatOpposingCP.side(), heatOpposingCP.posInMultiblock());
            heatSource = ctx.getCapabilityAt(HeatCapabilities.HEAT_PROVIDER_CAPABILITY, heatOpposingMBFace);
        }

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
            tanksDirty = false;
            inventoryDirty = false;
        }

        @Override public void writeSyncNBT(CompoundTag nbt) {
            CompoundTag display = new CompoundTag();
            writeDisplaySyncNBT(display);
            nbt.put("display", display);
        }

        @Override public void readSyncNBT(CompoundTag nbt) {
            if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display")); }
        }

        @Override public boolean isActive() { return active; }

        @Override public IItemHandlerModifiable getInventory() { return inventory; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input, tanks.output}; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.putDouble("heatLevel", heatLevel);
            nbt.put("tanks", tanks.toNBT());
            nbt.putDouble("workingHeatLevel", getWorkingHeatLevel());
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
            tanksDirty = false;
            inventoryDirty = false;
        }
    

        @Override public void addDisplayLines(Level level, DisplayLines lines) { lines.temperature(heatLevel, getWorkingHeatLevel()).percent((totalProcessTime > 0 && recipeTimeRemaining > 0) ? (totalProcessTime - recipeTimeRemaining) * 100 / totalProcessTime : 0); }
}

    private record BoilerInputImpl(MarkableFluidTank tank) implements IHeatConsumer {
        @Override public int getFluidAmount() { return tank.getFluidAmount(); }
    }

    public record BoilerTanks(MarkableFluidTank input, MarkableFluidTank output) {
        public BoilerTanks(Consumer<Void> markDirty) {
            this(new MarkableFluidTank(tankCapacity(), markDirty), new MarkableFluidTank(tankCapacity(), markDirty));
        }

        public static BoilerTanks makeClient() { return new BoilerTanks(v -> {}); }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("input", this.input.writeToNBT(new CompoundTag()));
            tag.put("output", this.output.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) {
            this.input.readFromNBT(tag.getCompound("input"));
            this.output.readFromNBT(tag.getCompound("output"));
        }

        @SuppressWarnings("unused")
        public int getCapacity() { return tankCapacity(); }
    }
}
