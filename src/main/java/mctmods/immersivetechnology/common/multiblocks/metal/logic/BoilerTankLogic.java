package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.common.multiblocks.helper.ITIDisplayContext;
import mctmods.immersivetechnology.common.multiblocks.helper.ITMultiblockPOIHelper;
import mctmods.immersivetechnology.common.multiblocks.helper.ITMultiBlockInventoryUtils;
import mctmods.immersivetechnology.common.multiblocks.helper.ITIPressurizedFluidOutput;
import mctmods.immersivetechnology.common.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerTankRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.BoilerTankShape;
import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.core.ITCommonConfig;
import mctmods.immersivetechnology.core.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.core.ITServerConfig;
import mctmods.immersivetechnology.core.util.ITCachedRecipe;

import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.HeatCapabilities;
import com.immersiveconvergence.api.capability.IHeatConsumer;
import com.immersiveconvergence.api.capability.IHeatProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.BiFunction;

public class BoilerTankLogic implements IMultiblockLogic<BoilerTankLogic.State>, IServerTickableComponent<BoilerTankLogic.State>, ITIPressurizedFluidOutput<BoilerTankLogic.State> {
    public static final int INPUT_SLOT_FILLED = 0;
    public static final int INPUT_SLOT_EMPTY = 1;
    public static final int OUTPUT_SLOT_EMPTY = 2;
    public static final int OUTPUT_SLOT_FILLED = 3;

    public static final int TANK_CAPACITY = ITServerConfig.boilerTankCapacity;
    public static final int PROGRESS_LOSS_PER_TICK = ITServerConfig.boilerTankProgressLossPerTick;
    public static final double DEFAULT_WORKING_HEAT_LEVEL = ITCommonConfig.boilerDefaultWorkingHeat;

    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(BoilerTankShape.DATA.pointsOfInterest);

    public static final List<BlockPos> INPUT_FLUID_POIS = ITMultiblockPOIHelper.getPosList(RAW_POIS, "fluid_input0");
    public static final List<BlockPos> OUTPUT_FLUID_POIS = ITMultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output0");
    public static final List<BlockPos> HEAT_INPUT_POIS = ITMultiblockPOIHelper.getPosList(RAW_POIS, "heat_input0");
    private static final RelativeBlockFace INPUT_FLUID_FACING = ITMultiblockPOIHelper.getFacing(RAW_POIS, "fluid_input0");
    private static final RelativeBlockFace OUTPUT_FLUID_FACING = ITMultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output0");
    private static final RelativeBlockFace HEAT_INPUT_FACING = ITMultiblockPOIHelper.getFacing(RAW_POIS, "heat_input0");

    @Override public List<BlockPos> getOutputPositions() { return OUTPUT_FLUID_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FLUID_FACING); }

    @Override public List<ITMarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output()); }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        final Level level = ctx.getLevel().getRawLevel();
        boolean update = false;
        double heatLevel = 0;
        Direction heatFacing = ctx.getLevel().toAbsolute(HEAT_INPUT_FACING);
        if (heatFacing != null && state.heatSource != null) { IHeatProvider p = state.heatSource.get(); if (p != null) heatLevel = p.getHeatLevel(); }
        double previousHeatLevel = state.heatLevel;
        state.heatLevel = heatLevel;

        double displayMax = DEFAULT_WORKING_HEAT_LEVEL;
        if (state.lastRecipe != null) {
            displayMax = Math.max(displayMax, state.lastRecipe.requiredHeat);
        } else if (state.tanks.input().getFluidAmount() > 0) {
            BoilerTankRecipe potentialRecipe = state.recipeGetter.apply(level, state.tanks.input().getFluid());
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
                        state.tanks.output().fill(state.lastRecipe.output.copy(), FluidAction.EXECUTE);
                        state.totalProcessTime = 0;
                        update = true;
                    }
                }
            } else if (state.tanks.input().getFluidAmount() > 0) {
                state.lastRecipe = state.recipeGetter.apply(level, state.tanks.input().getFluid());
                if (state.lastRecipe != null && state.lastRecipe.getInputAmount() <= state.tanks.input().getFluidAmount() && state.lastRecipe.output.getAmount() <= state.tanks.output().getCapacity() - state.tanks.output().getFluidAmount()) {
                    if (heatLevel >= state.lastRecipe.requiredHeat) {
                        int reqAmount = state.lastRecipe.getInputAmount();
                        FluidStack drained = state.tanks.input().drain(reqAmount, FluidAction.EXECUTE);
                        if (drained.getAmount() == reqAmount && state.lastRecipe.matches(drained)) {
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
                state.recipeTimeRemaining = Math.min(state.recipeTimeRemaining + PROGRESS_LOSS_PER_TICK, state.lastRecipe.getTotalProcessTime());
                if (previousProgress != state.recipeTimeRemaining) { update = true; }
            }
        }
        if (state.tanks.output().getFluidAmount() > 0) {
            if (FluidUtils.fillFluidContainer(state.tanks.output(), OUTPUT_SLOT_EMPTY, OUTPUT_SLOT_FILLED, state.inventory)) { update = true; }
        }
        pumpOutputs(ctx);
        if (tryEmptyContainer(state.tanks.input(), state.inventory)) { update = true; }
        if (update) { ctx.markMasterDirty(); ctx.requestMasterBESync(); }
    }

    private boolean tryEmptyContainer(IFluidHandler tank, IItemHandlerModifiable inv) {
        ItemStack filledContainer = inv.getStackInSlot(BoilerTankLogic.INPUT_SLOT_FILLED);
        if (filledContainer.isEmpty()) { return false; }
        FluidActionResult simResult = FluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.SIMULATE);
        if (!simResult.isSuccess()) { return false; }
        ItemStack emptyContainer = simResult.getResult();
        ItemStack outputStack = inv.getStackInSlot(BoilerTankLogic.INPUT_SLOT_EMPTY);
        if (!outputStack.isEmpty() && !ItemStack.isSameItemSameComponents(outputStack, emptyContainer)) { return false; }
        if (outputStack.getCount() + emptyContainer.getCount() > emptyContainer.getMaxStackSize()) { return false; }
        FluidActionResult execResult = FluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.EXECUTE);
        filledContainer.shrink(1);
        inv.setStackInSlot(BoilerTankLogic.INPUT_SLOT_FILLED, filledContainer);
        if (outputStack.isEmpty()) { inv.setStackInSlot(BoilerTankLogic.INPUT_SLOT_EMPTY, execResult.getResult()); }
        else { outputStack.grow(execResult.getResult().getCount()); }
        return true;
    }

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
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
        register.register(HeatCapabilities.HEAT_CONSUMER, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (HEAT_INPUT_POIS.contains(localPos) && (side == null || side == HEAT_INPUT_FACING)) {
                return state.boilerInputCap;
            }
            return null;
        });
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { ITMultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return BoilerTankShape.GETTER; }

    public static class State implements IMultiblockState, ITIDisplayContext {
        public final BiFunction<Level, FluidStack, BoilerTankRecipe> recipeGetter = ITCachedRecipe.cached(BoilerTankRecipe::findRecipe);
        public final BoilerTanks tanks;
        public IFluidHandler inputCap;
        public IFluidHandler outputCap;
        public IHeatConsumer boilerInputCap;
        public Supplier<IHeatProvider> heatSource;
        public ITSlotwiseItemHandler inventory;
        public int recipeTimeRemaining = 0;
        public int totalProcessTime = 0;
        public BoilerTankRecipe lastRecipe;
        public double heatLevel = 0;
        public double workingHeatLevel = DEFAULT_WORKING_HEAT_LEVEL;
        public boolean active = false;
        public boolean tanksDirty = false;
        public boolean inventoryDirty = false;

        public State(IInitialMultiblockContext<State> ctx) {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            final Runnable sync = ctx.getSyncRunnable();
            final Runnable onChanged = () -> { markDirty.run(); sync.run(); this.tanksDirty = true; this.inventoryDirty = true; };
            tanks = new BoilerTanks(() -> { onChanged.run(); this.tanksDirty = true; });
            inventory = new ITSlotwiseItemHandler(
                    List.of(
                            ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            ITSlotwiseItemHandler.IOConstraint.OUTPUT,
                            ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            ITSlotwiseItemHandler.IOConstraint.OUTPUT
                    ),
                    () -> { onChanged.run(); this.inventoryDirty = true; }
            );
            inputCap = new ITArrayFluidHandler(tanks.input(), false, true, () -> { onChanged.run(); this.tanksDirty = true; });
            outputCap = new ITArrayFluidHandler(tanks.output(), true, false, () -> { onChanged.run(); this.tanksDirty = true; });
            boilerInputCap = new BoilerInputImpl(tanks.input());
            MultiblockFace heatMBFace = new MultiblockFace(HEAT_INPUT_FACING, HEAT_INPUT_POIS.getFirst());
            CapabilityPosition heatOpposingCP = CapabilityPosition.opposing(heatMBFace);
            MultiblockFace heatOpposingMBFace = new MultiblockFace(heatOpposingCP.side(), heatOpposingCP.posInMultiblock());
            heatSource = ctx.getCapabilityAt(HeatCapabilities.HEAT_PROVIDER, heatOpposingMBFace);
        }

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
            tanksDirty = false;
            inventoryDirty = false;
        }

        @Override public void writeSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            CompoundTag display = new CompoundTag();
            writeDisplaySyncNBT(display, provider);
            nbt.put("display", display);
        }

        @Override public void readSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            if (nbt.contains("display", Tag.TAG_COMPOUND)) {
                readDisplaySyncNBT(nbt.getCompound("display"), provider);
            }
        }

        @Override public boolean isActive() { return active; }

        @Override public IItemHandlerModifiable getInventory() { return inventory; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input(), tanks.output()}; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("active", active);
            nbt.putDouble("heatLevel", heatLevel);
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.putDouble("workingHeatLevel", getWorkingHeatLevel());
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
            tanksDirty = false;
            inventoryDirty = false;
        }
    }

    private record BoilerInputImpl(ITMarkableFluidTank tank) implements IHeatConsumer {
        @Override public int getFluidAmount() { return tank.getFluidAmount(); }
    }

    public record BoilerTanks(ITMarkableFluidTank input, ITMarkableFluidTank output) {
        public BoilerTanks(Runnable markDirty) {
            this(new ITMarkableFluidTank(TANK_CAPACITY, v -> markDirty.run()), new ITMarkableFluidTank(TANK_CAPACITY, v -> markDirty.run()));
        }

        public static BoilerTanks makeClient() { return new BoilerTanks(() -> {}); }

        public CompoundTag toNBT(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.put("input", this.input().writeToNBT(provider, new CompoundTag()));
            tag.put("output", this.output().writeToNBT(provider, new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag, HolderLookup.Provider provider) {
            this.input().readFromNBT(provider, tag.getCompound("input"));
            this.output().readFromNBT(provider, tag.getCompound("output"));
        }

        @SuppressWarnings("unused")
        public int getCapacity() { return TANK_CAPACITY; }
    }
}
