package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.api.HeatCapabilities;
import mctmods.immersivetechnology.api.capability.IHeatConsumer;
import mctmods.immersivetechnology.api.capability.IHeatProvider;
import mctmods.immersivetechnology.common.multiblocks.helper.ITDisplayContext;
import mctmods.immersivetechnology.common.multiblocks.helper.ITMultiBlockInventoryUtils;
import mctmods.immersivetechnology.common.multiblocks.helper.ITPressurizedFluidOutput;
import mctmods.immersivetechnology.common.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerTankRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.BoilerTankShape;
import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
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
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class BoilerTankLogic implements IMultiblockLogic<BoilerTankLogic.State>, IServerTickableComponent<BoilerTankLogic.State>, ITPressurizedFluidOutput<BoilerTankLogic.State> {
    public static final int INPUT_SLOT_FILLED = 0;
    public static final int INPUT_SLOT_EMPTY = 1;
    public static final int OUTPUT_SLOT_EMPTY = 2;
    public static final int OUTPUT_SLOT_FILLED = 3;
    public static final int TANK_CAPACITY = 24 * FluidType.BUCKET_VOLUME;
    private static final int PROGRESS_LOSS_PER_TICK = 1;
    private static final double WORKING_HEAT_LEVEL = 100.0;
    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(BoilerTankShape.DATA.pointsOfInterest);

    public static final List<BlockPos> FLUID_INPUT_POI = getPosList("fluid_input");
    private static final List<BlockPos> FLUID_OUTPUT_POI = getPosList("fluid_output");
    private static final List<BlockPos> HEAT_INPUT_POI = getPosList("heat_input");
    private static final RelativeBlockFace FLUID_INPUT_FACING = getFacing("fluid_input");
    private static final RelativeBlockFace FLUID_OUTPUT_FACING = getFacing("fluid_output");
    private static final RelativeBlockFace HEAT_INPUT_FACING = getFacing("heat_input");

    private static List<BlockPos> getPosList(String name) { return RAW_POIS.stream().filter(poi -> poi.name.equals(name)).map(poi -> new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2])).collect(ImmutableList.toImmutableList()); }

    private static RelativeBlockFace getFacing(String name) {
        List<RelativeBlockFace> facings = RAW_POIS.stream().filter(poi -> poi.name.equals(name)).flatMap(poi -> poi.relativeFaces.stream()).distinct().toList();
        if (facings.size() != 1) { throw new RuntimeException("Inconsistent facings for POI: " + name); }
        return facings.get(0);
    }

    @Override
    public List<BlockPos> getOutputPositions() { return FLUID_OUTPUT_POI; }

    @Override
    public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(FLUID_OUTPUT_FACING); }

    @Override
    public List<ITMarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output); }

    @Override
    public List<CapabilityReference<IFluidHandler>> getFluidOutputs(State state) { return ImmutableList.of(state.fluidOutput); }

    @Override
    public void tickServer(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        final Level level = ctx.getLevel().getRawLevel();
        boolean update = false;
        double heatLevel = 0;
        if (state.heatSource.isPresent()) { heatLevel = state.heatSource.get().getHeatLevel(); }
        double previousHeatLevel = state.heatLevel;
        state.heatLevel = heatLevel;
        boolean isActive = heatLevel >= WORKING_HEAT_LEVEL && state.recipeTimeRemaining > 0;
        if (state.active != isActive) { state.active = isActive; update = true; }
        if (previousHeatLevel != state.heatLevel) { update = true; }
        if (heatLevel >= WORKING_HEAT_LEVEL) {
            if (state.recipeTimeRemaining > 0) {
                if (state.lastRecipe == null) { state.recipeTimeRemaining = 0; update = true; }
                else {
                    state.recipeTimeRemaining--;
                    if (state.recipeTimeRemaining == 0) {
                        state.tanks.input.drain(state.lastRecipe.input.getAmount(), FluidAction.EXECUTE);
                        state.tanks.output.fill(state.lastRecipe.output.copy(), IFluidHandler.FluidAction.EXECUTE);
                        update = true;
                    }
                }
            } else if (state.tanks.input.getFluidAmount() > 0) {
                state.lastRecipe = BoilerTankRecipe.findRecipe(level, state.tanks.input.getFluid());
                if (state.lastRecipe != null && state.lastRecipe.input.getAmount() <= state.tanks.input.getFluidAmount() && state.lastRecipe.output.getAmount() <= state.tanks.output.getCapacity() - state.tanks.output.getFluidAmount()) {
                    state.recipeTimeRemaining = state.lastRecipe.getTotalProcessTime();
                    state.recipeTimeRemaining--;
                    if (state.recipeTimeRemaining == 0) {
                        state.tanks.input.drain(state.lastRecipe.input.getAmount(), FluidAction.EXECUTE);
                        state.tanks.output.fill(state.lastRecipe.output.copy(), FluidAction.EXECUTE);
                    }
                    update = true;
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
        if (state.tanks.output.getFluidAmount() > 0) {
            if (FluidUtils.fillFluidContainer(state.tanks.output, OUTPUT_SLOT_EMPTY, OUTPUT_SLOT_FILLED, state.inventory)) { update = true; }
        }
        pumpOutputs(ctx);
        if (tryEmptyContainer(state.tanks.input, state.inventory)) { update = true; }
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

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        BlockPos localPos = position.posInMultiblock();
        RelativeBlockFace side = position.side();
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (FLUID_INPUT_POI.contains(localPos) && (side == null || side == FLUID_INPUT_FACING)) { return ctx.getState().inputCap.cast(ctx); }
            if (FLUID_OUTPUT_POI.contains(localPos) && (side == null || side == FLUID_OUTPUT_FACING)) { return ctx.getState().outputCap.cast(ctx); }
        } else if (cap == HeatCapabilities.HEAT_CONSUMER_CAPABILITY) {
            if (HEAT_INPUT_POI.contains(localPos) && (side == null || side == HEAT_INPUT_FACING)) { return ctx.getState().boilerInputCap.cast(ctx); }
        }
        return LazyOptional.empty();
    }

    @Override
    public void dropExtraItems(State state, Consumer<ItemStack> drop) { ITMultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override
    public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return BoilerTankShape.GETTER; }

    public static class State implements IMultiblockState, ITDisplayContext {
        public final BoilerTanks tanks;
        public StoredCapability<IFluidHandler> inputCap;
        public StoredCapability<IFluidHandler> outputCap;
        public StoredCapability<IHeatConsumer> boilerInputCap;
        public CapabilityReference<IFluidHandler> fluidOutput;
        public CapabilityReference<IHeatProvider> heatSource;
        public ITSlotwiseItemHandler inventory;
        public int recipeTimeRemaining = 0;
        public BoilerTankRecipe lastRecipe;
        public double heatLevel = 0;
        public boolean active = false;

        public State(IInitialMultiblockContext<State> ctx) {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            final Runnable sync = ctx.getSyncRunnable();
            final Runnable onChanged = () -> { markDirty.run(); sync.run(); };
            tanks = new BoilerTanks(v -> onChanged.run());
            inventory = new ITSlotwiseItemHandler(
                    List.of(
                            ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            ITSlotwiseItemHandler.IOConstraint.OUTPUT,
                            ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            ITSlotwiseItemHandler.IOConstraint.OUTPUT
                    ),
                    onChanged
            );
            inputCap = new StoredCapability<>(new ITArrayFluidHandler(tanks.input, false, true, onChanged));
            outputCap = new StoredCapability<>(new ITArrayFluidHandler(tanks.output, true, false, onChanged));
            boilerInputCap = new StoredCapability<>(new BoilerInputImpl(tanks.input));
            MultiblockFace outputMBFace = new MultiblockFace(FLUID_OUTPUT_FACING, FLUID_OUTPUT_POI.get(0)); // Assuming at least one
            CapabilityPosition opposingCP = CapabilityPosition.opposing(outputMBFace);
            MultiblockFace opposingMBFace = new MultiblockFace(opposingCP.side(), opposingCP.posInMultiblock());
            fluidOutput = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, opposingMBFace);
            MultiblockFace heatMBFace = new MultiblockFace(HEAT_INPUT_FACING, HEAT_INPUT_POI.get(0)); // Assuming at least one
            CapabilityPosition heatOpposingCP = CapabilityPosition.opposing(heatMBFace);
            MultiblockFace heatOpposingMBFace = new MultiblockFace(heatOpposingCP.side(), heatOpposingCP.posInMultiblock());
            heatSource = ctx.getCapabilityAt(HeatCapabilities.HEAT_PROVIDER_CAPABILITY, heatOpposingMBFace);
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt) {
            nbt.put("tanks", tanks.toNBT());
            nbt.putInt("recipeTimeRemaining", recipeTimeRemaining);
            nbt.put("inventory", inventory.serializeNBT());
        }

        @Override
        public void readSaveNBT(CompoundTag nbt) {
            tanks.readNBT(nbt.getCompound("tanks"));
            recipeTimeRemaining = nbt.getInt("recipeTimeRemaining");
            inventory.deserializeNBT(nbt.getCompound("inventory"));
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt) {
            CompoundTag display = new CompoundTag();
            writeDisplaySyncNBT(display);
            nbt.put("display", display);
        }

        @Override
        public void readSyncNBT(CompoundTag nbt) {
            if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display")); }
        }

        @Override
        public boolean isActive() { return active; }

        @Override
        public IItemHandlerModifiable getInventory() { return inventory; }

        @Override
        public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input, tanks.output}; }

        @Override
        public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.putDouble("heatLevel", heatLevel);
            nbt.put("tanks", tanks.toNBT());
        }

        @Override
        public void readDisplaySyncNBT(CompoundTag nbt) {
            active = nbt.getBoolean("active");
            heatLevel = nbt.getDouble("heatLevel");
            tanks.readNBT(nbt.getCompound("tanks"));
        }
    }

    @SuppressWarnings("unused")
    private record BoilerInputImpl(ITMarkableFluidTank tank) implements IHeatConsumer {
        @Override
        public int getFluidAmount() { return tank.getFluidAmount(); }
    }

    public record BoilerTanks(ITMarkableFluidTank input, ITMarkableFluidTank output) {
        public BoilerTanks(Consumer<Void> markDirty) {
            this(new ITMarkableFluidTank(TANK_CAPACITY, markDirty), new ITMarkableFluidTank(TANK_CAPACITY, markDirty));
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
        public int getCapacity() { return TANK_CAPACITY; }
    }
}
