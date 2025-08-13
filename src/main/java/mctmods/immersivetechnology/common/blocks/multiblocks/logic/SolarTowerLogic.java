package mctmods.immersivetechnology.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl.InitialMultiblockContext;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITMultiBlockInventoryUtils;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITWrappingItemHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.process.SolarTowerProcess;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.SolarTowerRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.SolarTowerShape;
import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SolarTowerLogic implements IMultiblockLogic<SolarTowerLogic.State>, IServerTickableComponent<SolarTowerLogic.State> {
    public static final int SLOT_INPUT_FILLED = 0;
    public static final int SLOT_INPUT_EMPTY = 1;
    public static final int SLOT_OUTPUT_EMPTY = 2;
    public static final int SLOT_OUTPUT_FILLED = 3;

    public static final int TANK_CAPACITY =  12 * FluidType.BUCKET_VOLUME;
    public static final int SOLAR_MAX_RANGE = 50;
    public static final int SOLAR_MIN_RANGE = 10;
    public static final double WORKING_HEAT_LEVEL = 20000.0;
    public static final int PROGRESS_LOSS_PER_TICK = 1;
    public static final float SPEED_MULTIPLIER = 1.0f;
    public static final double HEAT_LOSS_MULTIPLIER = 0.0005;

    public static final CapabilityPosition INPUT_FLUID_POS = new CapabilityPosition(1, 0, 1, RelativeBlockFace.RIGHT);
    public static final CapabilityPosition OUTPUT_FLUID_POS = new CapabilityPosition(1, 0, 1, RelativeBlockFace.LEFT);
    public static final MultiblockFace ITEM_OUTPUT_REF_POS = new MultiblockFace(1, 0, -1, RelativeBlockFace.BACK);

    public static final BlockPos REDSTONE_POS = new BlockPos(1, 0, 0);

    private static final BlockPos RELATIVE_BASE = new BlockPos(1, 0, 1);
    private static final BlockPos RELATIVE_COLLECTOR = new BlockPos(1, 17, 1);

    @Override
    public void tickServer(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        final Level level = ctx.getLevel().getRawLevel();
        final IMultiblockLevel mbLevel = ctx.getLevel();
        if (level.getGameTime() % 60 == 0) { state.reflectorStrength = checkReflectorPositions(mbLevel); }
        boolean update = heatLogic(state, level);
        update |= recipeLogic(state, level, mbLevel);
        if (tryEmptyContainer(state.tanks.input, state.inventory)) { update = true; }
        if (FluidUtils.fillFluidContainer(state.tanks.output, SLOT_OUTPUT_EMPTY, SLOT_OUTPUT_FILLED, state.inventory)) { update = true; }
        if (state.fluidOutput.isPresent()) {
            IFluidHandler outputHandler = state.fluidOutput.get();
            FluidStack fs = state.tanks.output.getFluid();
            if (fs.getAmount() > 0) {
                fs = fs.copy();
                int accepted = outputHandler.fill(fs, FluidAction.SIMULATE);
                if (accepted > 0) {
                    int drained = outputHandler.fill(Utils.copyFluidStackWithAmount(fs, accepted, false), FluidAction.EXECUTE);
                    state.tanks.output.drain(drained, FluidAction.EXECUTE);
                    update = true;
                }
            }
        }
        final IItemHandlerModifiable inventory = state.inventory;
        ItemStack drainedContainer = inventory.getStackInSlot(SLOT_INPUT_EMPTY);
        if (!drainedContainer.isEmpty()) {
            int origCount = drainedContainer.getCount();
            drainedContainer = Utils.insertStackIntoInventory(state.outputRef, drainedContainer, false);
            if (drainedContainer.getCount() < origCount) update = true;
            inventory.setStackInSlot(SLOT_INPUT_EMPTY, drainedContainer);
        }
        ItemStack filledContainer = inventory.getStackInSlot(SLOT_OUTPUT_FILLED);
        if (!filledContainer.isEmpty()) {
            int origCount = filledContainer.getCount();
            filledContainer = Utils.insertStackIntoInventory(state.outputRef, filledContainer, false);
            if (filledContainer.getCount() < origCount) update = true;
            inventory.setStackInSlot(SLOT_OUTPUT_FILLED, filledContainer);
        }
        if (update) { ctx.markMasterDirty(); ctx.requestMasterBESync(); }
        ITLib.IT_LOGGER.info("Reflector strength: " + state.reflectorStrength);
    }

    private double checkReflectorPositions(IMultiblockLevel mbLevel) {
        double totalMirrorStrength = 0;
        final Level level = mbLevel.getRawLevel();
        final BlockPos basePos = mbLevel.toAbsolute(RELATIVE_BASE);
        final BlockPos collectorPos = mbLevel.toAbsolute(RELATIVE_COLLECTOR);
        for (int x = -(SOLAR_MAX_RANGE + 1); x <= (SOLAR_MAX_RANGE + 1); x++) for (int z = -(SOLAR_MAX_RANGE + 1); z <= (SOLAR_MAX_RANGE + 1); z++) {
            double distance = Math.sqrt(x * x + z * z);
            if (distance >= SOLAR_MIN_RANGE && distance <= SOLAR_MAX_RANGE) {
                BlockPos reflectorPos = basePos.offset(x, 0, z);
                BlockEntity be = level.getBlockEntity(reflectorPos);
                if (be instanceof IMultiblockBE<?> mbe) {
                    IMultiblockBEHelper<?> helper = mbe.getHelper();
                    if (helper != null && helper.getState() instanceof SolarReflectorLogic.State reflectorState) {
                        if (reflectorState.setTowerCollectorPosition(collectorPos)) totalMirrorStrength += reflectorState.getSolarCollectorStrength();
                    }
                }
            }
        }
        totalMirrorStrength *= (level.isRaining() ? 0.4f : 1f);
        return totalMirrorStrength;
    }

    private void detachReflectorPositions(State state) {
        final Level level = state.levelSupplier.get();
        if (level == null) return;
        final BlockPos basePos = state.basePos;
        final BlockPos collectorPos = state.collectorPos;
        for (int x = -(SOLAR_MAX_RANGE + 1); x <= (SOLAR_MAX_RANGE + 1); x++) for (int z = -(SOLAR_MAX_RANGE + 1); z <= (SOLAR_MAX_RANGE + 1); z++) {
            double distance = Math.sqrt(x * x + z * z);
            if (distance >= SOLAR_MIN_RANGE && distance <= SOLAR_MAX_RANGE) {
                BlockPos reflectorPos = basePos.offset(x, 0, z);
                BlockEntity be = level.getBlockEntity(reflectorPos);
                if (be instanceof IMultiblockBE<?> mbe) {
                    IMultiblockBEHelper<?> helper = mbe.getHelper();
                    if (helper != null && helper.getState() instanceof SolarReflectorLogic.State reflectorState) {
                        reflectorState.detachTower(collectorPos);
                    }
                }
            }
        }
    }

    private boolean heatLogic(State state, Level level) {
        boolean update = false;
        int section = getSolarIncidenceAngleSection(level);
        double previousHeatLevel = state.heatLevel;
        if (section != 0) {
            state.heatLevel = Math.min(getTemperatureIncrease(state, level) + state.heatLevel, WORKING_HEAT_LEVEL);
        } else {
            state.heatLevel = Math.max(state.heatLevel - HEAT_LOSS_MULTIPLIER, 0);
        }
        if (previousHeatLevel != state.heatLevel) update = true;
        return update;
    }

    private double getTemperatureIncrease(State state, Level level) { return SPEED_MULTIPLIER * (1 + (getSolarIncidenceAngleSection(level) - 1)) * 10 * (state.reflectorStrength) * (level.isRaining() ? 0.1f : level.isThundering() ? 0.05f : 1f); }

    private boolean recipeLogic(State state, Level level, IMultiblockLevel mbLevel) {
        boolean update = false;
        if (state.heatLevel >= WORKING_HEAT_LEVEL) {
            if (state.processor.getQueueSize() >= state.processor.getMaxQueueSize()) return false;
            SolarTowerRecipe recipe = SolarTowerRecipe.findRecipe(level, state.tanks.input.getFluid());
            if (recipe != null) {
                FluidStack inputFluid = state.tanks.input.getFluid();
                if (inputFluid.getAmount() < recipe.input.getAmount()) return false;
                FluidStack outputFluid = recipe.fluidOutput;
                if (outputFluid != null && !outputFluid.isEmpty() && state.tanks.output.getFluidAmount() + outputFluid.getAmount() > state.tanks.output.getCapacity()) return false;
                SolarTowerProcess process = new SolarTowerProcess(recipe);
                process.setInputTanks(0);
                state.processor.addProcessToQueue(process, level, false);
                update = true;
            }
        }
        state.processor.tickServer(state, mbLevel, true);
        return update;
    }

    private boolean tryEmptyContainer(IFluidHandler tank, IItemHandlerModifiable inv) {
        ItemStack filledContainer = inv.getStackInSlot(SLOT_INPUT_FILLED);
        if (filledContainer.isEmpty()) { return false; }
        FluidActionResult result = FluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.SIMULATE);
        if (!result.isSuccess()) { return false; }
        ItemStack emptyContainer = result.getResult();
        ItemStack outputStack = inv.getStackInSlot(SLOT_INPUT_EMPTY);
        if (!outputStack.isEmpty() && !ItemHandlerHelper.canItemStacksStack(outputStack, emptyContainer)) { return false; }
        if (outputStack.getCount() + emptyContainer.getCount() > emptyContainer.getMaxStackSize()) { return false; }
        result = FluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.EXECUTE);
        filledContainer.shrink(1);
        inv.setStackInSlot(SLOT_INPUT_FILLED, filledContainer);
        if (outputStack.isEmpty()) { inv.setStackInSlot(SLOT_INPUT_EMPTY, result.getResult()); }
        else { outputStack.grow(result.getResult().getCount()); }
        return true;
    }

    public static int getSolarIncidenceAngleSection(Level level) {
        int skyDarken = level.getSkyDarken();
        if (skyDarken == 3) return 1;
        else if (skyDarken == 2) return 2;
        else if (skyDarken == 1) return 3;
        else if (skyDarken == 0) return 4;
        return 0;
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        final State state = ctx.getState();
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (position.posInMultiblock().equals(INPUT_FLUID_POS.posInMultiblock()) && (position.side() == null || position.side() == INPUT_FLUID_POS.side())) { return state.inputCap.cast(ctx); }
            if (position.posInMultiblock().equals(OUTPUT_FLUID_POS.posInMultiblock()) && (position.side() == null || position.side() == OUTPUT_FLUID_POS.side())) { return state.outputCap.cast(ctx); }
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (ITEM_OUTPUT_REF_POS.posInMultiblock().equals(position.posInMultiblock())) { return state.itemOutputCap.cast(ctx); }
            return state.invCap.cast(ctx);
        }
        return LazyOptional.empty();
    }

    @Override
    public void dropExtraItems(SolarTowerLogic.State state, Consumer<ItemStack> drop) {
        detachReflectorPositions(state);
        ITMultiBlockInventoryUtils.dropItems(state.inventory, drop);
    }

    @Override
    public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return SolarTowerShape.GETTER; }

    public static class State implements IMultiblockState, ProcessContext.ProcessContextInMachine<SolarTowerRecipe> {
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final SolarTowerTank tanks;
        public final StoredCapability<IFluidHandler> inputCap;
        public final StoredCapability<IFluidHandler> outputCap;
        public final StoredCapability<IItemHandler> invCap;
        public final CapabilityReference<IFluidHandler> fluidOutput;
        public final StoredCapability<IItemHandler> itemOutputCap;
        public final CapabilityReference<IItemHandler> outputRef;
        public final ITSlotwiseItemHandler inventory;
        private final IFluidTank[] tankArray;
        private final MultiblockProcessor.InMachineProcessor<SolarTowerRecipe> processor;
        public double heatLevel = 0;
        public double reflectorStrength = 0;
        private final BlockPos basePos;
        private final BlockPos collectorPos;
        private final Supplier<Level> levelSupplier;

        public State(IInitialMultiblockContext<State> ctx) {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            final Runnable sync = ctx.getSyncRunnable();
            final Runnable onChanged = () -> { markDirty.run(); sync.run(); };
            this.tanks = new SolarTowerTank(v -> onChanged.run());
            this.tankArray = new IFluidTank[]{tanks.input, tanks.output};
            inventory = new ITSlotwiseItemHandler(Lists.newArrayList(ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT, ITSlotwiseItemHandler.IOConstraint.OUTPUT, ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT, ITSlotwiseItemHandler.IOConstraint.OUTPUT), onChanged);
            this.inputCap = new StoredCapability<>(new ITArrayFluidHandler(tanks.input, false, true, onChanged));
            this.outputCap = new StoredCapability<>(new ITArrayFluidHandler(tanks.output, true, false, onChanged));
            this.invCap = new StoredCapability<>(inventory);
            this.processor = new MultiblockProcessor.InMachineProcessor<>(1, 0f, 1, markDirty, SolarTowerRecipe.RECIPES::getById);
            MultiblockFace outputMBFace = new MultiblockFace(OUTPUT_FLUID_POS.side(), OUTPUT_FLUID_POS.posInMultiblock());
            CapabilityPosition opposingCP = CapabilityPosition.opposing(outputMBFace);
            MultiblockFace opposingMBFace = new MultiblockFace(opposingCP.side(), opposingCP.posInMultiblock());
            this.fluidOutput = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, opposingMBFace);
            this.itemOutputCap = new StoredCapability<>(new ITWrappingItemHandler(inventory, false, true, Lists.newArrayList(new ITWrappingItemHandler.IntRange(SLOT_INPUT_EMPTY, SLOT_INPUT_EMPTY + 1), new ITWrappingItemHandler.IntRange(SLOT_OUTPUT_FILLED, SLOT_OUTPUT_FILLED + 1))));
            this.outputRef = ctx.getCapabilityAt(ForgeCapabilities.ITEM_HANDLER, ITEM_OUTPUT_REF_POS);
            InitialMultiblockContext<State> initialContext = (InitialMultiblockContext<State>)ctx;
            MultiblockOrientation orientation = initialContext.orientation();
            BlockPos masterOffset = initialContext.masterOffset();
            BlockPos masterPos = initialContext.masterBE().getBlockPos();
            BlockPos origin = masterPos.subtract(orientation.getAbsoluteOffset(masterOffset));
            this.basePos = origin.offset(orientation.getAbsoluteOffset(RELATIVE_BASE));
            this.collectorPos = origin.offset(orientation.getAbsoluteOffset(RELATIVE_COLLECTOR));
            this.levelSupplier = ctx.levelSupplier();
        }

        public ITSlotwiseItemHandler getInventory() { return inventory; }

        public SolarTowerTank getTanks() { return tanks; }

        @Override
        public void writeSaveNBT(CompoundTag nbt) {
            nbt.put("tanks", this.tanks.toNBT());
            nbt.put("processor", processor.toNBT());
            nbt.put("inventory", inventory.serializeNBT());
            nbt.putDouble("heatLevel", heatLevel);
            nbt.putDouble("reflectorStrength", reflectorStrength);
        }

        @Override
        public void readSaveNBT(CompoundTag nbt) {
            this.tanks.readNBT(nbt.getCompound("tanks"));
            this.processor.fromNBT(nbt.getList("processor", Tag.TAG_COMPOUND), SolarTowerProcess::new);
            this.inventory.deserializeNBT(nbt.getCompound("inventory"));
            heatLevel = nbt.getDouble("heatLevel");
            reflectorStrength = nbt.getDouble("reflectorStrength");
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt) {
            nbt.put("tanks", this.tanks.toNBT());
            nbt.putDouble("heatLevel", heatLevel);
        }

        @Override
        public void readSyncNBT(CompoundTag nbt) {
            this.tanks.readNBT(nbt.getCompound("tanks"));
            heatLevel = nbt.getDouble("heatLevel");
        }

        @Override
        public @Nullable AveragingEnergyStorage getEnergy() { return new AveragingEnergyStorage(0); }

        @Override
        public IFluidTank[] getInternalTanks() { return tankArray; }

        @Override
        public int[] getOutputTanks() { return new int[]{1}; }

        @Override
        public int[] getOutputSlots() { return null; }
    }

    public record SolarTowerTank(ITMarkableFluidTank input, ITMarkableFluidTank output) {
        public SolarTowerTank(Consumer<Void> markDirty) { this(new ITMarkableFluidTank(TANK_CAPACITY, markDirty), new ITMarkableFluidTank(TANK_CAPACITY, markDirty)); }

        public static SolarTowerTank makeClient() { return new SolarTowerTank(v -> {}); }

        public Tag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("input", this.input.writeToNBT(new CompoundTag()));
            tag.put("output", this.output.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) {
            this.input.readFromNBT(tag.getCompound("input"));
            this.output.readFromNBT(tag.getCompound("output"));
        }

        public int getCapacity() { return TANK_CAPACITY; }
    }
}
