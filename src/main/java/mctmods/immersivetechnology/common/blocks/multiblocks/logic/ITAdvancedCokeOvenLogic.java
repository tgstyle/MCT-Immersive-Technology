package mctmods.immersivetechnology.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.metal.BlastFurnacePreheaterBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.NonMirrorableWithActiveBlock;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.WrappingItemHandler;
import blusunrize.immersiveengineering.common.util.sound.MultiblockSound;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITFurnaceHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.AdvCokeOvenShape;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.util.CachedRecipe;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler.IOConstraint;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import mctmods.immersivetechnology.core.registration.ITSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("all")
public class ITAdvancedCokeOvenLogic implements IMultiblockLogic<ITAdvancedCokeOvenLogic.State>, IServerTickableComponent<ITAdvancedCokeOvenLogic.State>, IClientTickableComponent<ITAdvancedCokeOvenLogic.State> {
    private static final Vec3 SMOKE_POS = new Vec3(1.5, 3.9, 1.5);
    public static final BlockPos MASTER_OFFSET = new BlockPos(0, 0, 0);
    public static final MultiblockFace OUTPUT_TANK_OFFSET = new MultiblockFace(1, 0, 0, RelativeBlockFace.BACK);
    private static final MultiblockFace OUTPUT_POS = new MultiblockFace(1,0,3, RelativeBlockFace.FRONT);
    private static final BlockPos[] HEATER_OFFSETS = {
            new BlockPos(-1, 0, 1), new BlockPos(3, 0, 1)
    };

    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int EMPTY_CONTAINER_SLOT = 2;
    public static final int FULL_CONTAINER_SLOT = 3;
    public static final int NUM_SLOTS = 4;
    public static final int TANK_CAPACITY = 12*FluidType.BUCKET_VOLUME;
    private static final CapabilityPosition FLUID_OUTPUT_CAP;
    private static final CapabilityPosition ITEM_OUTPUT_CAP;

    static {
        FLUID_OUTPUT_CAP = new CapabilityPosition(1, 0, 0, RelativeBlockFace.FRONT);
        ITEM_OUTPUT_CAP = new CapabilityPosition(1, 0, 2, RelativeBlockFace.BACK);
    }

    @Override
    public State createInitialState(IInitialMultiblockContext<ITAdvancedCokeOvenLogic.State> capabilitySource) {
        return new ITAdvancedCokeOvenLogic.State(capabilitySource);
    }

    @Override
    public void tickServer(IMultiblockContext<ITAdvancedCokeOvenLogic.State> context) {
        final ITAdvancedCokeOvenLogic.State state = context.getState();
        final BlockState masterBlockState = context.getLevel().getBlockState(MASTER_OFFSET);
        final boolean activeBeforeTick = masterBlockState.getValue(NonMirrorableWithActiveBlock.ACTIVE);
        boolean active = activeBeforeTick;
        if (state.tank.getFluid().getAmount() > 0) {
            drainOutputTank(state, context, state.fluidOutput);
        }

        if (state.process > 0) {
            if (state.inventory.getStackInSlot(INPUT_SLOT).isEmpty()) {
                state.process = 0;
                state.processMax = 0;
            } else {
                AdvancedCokeOvenRecipe recipe = getRecipe(context);
                if (recipe==null||recipe.time!=state.processMax) {
                    state.process = 0;
                    state.processMax = 0;
                    active = false;
                } else {
                    state.process--;
                }
            }
            context.markMasterDirty();
        } else {
            if (activeBeforeTick) {
                AdvancedCokeOvenRecipe recipe = getRecipe(context);
                if (recipe!=null) {
                    state.inventory.getStackInSlot(INPUT_SLOT).grow(-recipe.input.getCount());
                    final ItemStack outputStack = state.inventory.getStackInSlot(OUTPUT_SLOT);
                    if (!outputStack.isEmpty()) { outputStack.grow(recipe.output.get().copy().getCount()); }
                    else if (outputStack.isEmpty()) { state.inventory.setStackInSlot(OUTPUT_SLOT, recipe.output.get().copy()); }
                    state.tank.fill( new FluidStack(IEFluids.CREOSOTE.getStill(), recipe.creosoteOutput), FluidAction.EXECUTE );
                }
                state.processMax = 0;
                active = false;
            }
            AdvancedCokeOvenRecipe recipe = getRecipe(context);
            if (recipe!=null) {
                state.process = recipe.time;
                state.processMax = state.process;
                active = true;
            }
        }
        if (state.tank.getFluidAmount() > 0&&FluidUtils.fillFluidContainer(state.tank, EMPTY_CONTAINER_SLOT, FULL_CONTAINER_SLOT, state.inventory)) { context.markMasterDirty(); }
        final IItemHandlerModifiable inventory = state.getInventory();
        ItemStack stack = inventory.getStackInSlot(1);
        if (!stack.isEmpty()) {
            stack = Utils.insertStackIntoInventory(state.outputRef, stack, false);
            inventory.setStackInSlot(1, stack);
        }
        if (activeBeforeTick!=active) { NonMirrorableWithActiveBlock.setActive(context.getLevel(), ITMultiblockProvider.getMBTemplate.apply("coke_oven_advanced"), active); }
    }

    @Nullable
    public AdvancedCokeOvenRecipe getRecipe(IMultiblockContext<ITAdvancedCokeOvenLogic.State> context) {
        final ITAdvancedCokeOvenLogic.State state = context.getState();
        AdvancedCokeOvenRecipe recipe = state.cachedRecipe.apply(context.getLevel().getRawLevel());
        if (recipe==null) { return null; }
        final ItemStack currentOutputStack = state.inventory.getStackInSlot(OUTPUT_SLOT);
        final boolean canOutputItem;
        if (currentOutputStack.isEmpty()) { canOutputItem = true; }
        else if (!ItemHandlerHelper.canItemStacksStack(currentOutputStack, recipe.output.get())) { canOutputItem = false; }
        else { canOutputItem = currentOutputStack.getCount()+recipe.output.get().getCount() <= 64; }
        if (canOutputItem&&state.tank.getFluidAmount()+recipe.creosoteOutput <= state.tank.getCapacity()) { return recipe; }
        return null;
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        final State state = ctx.getState();
        if (cap==ForgeCapabilities.ITEM_HANDLER) { return state.invCap.cast(ctx);
        } else if (cap==ForgeCapabilities.FLUID_HANDLER) {
            if (FLUID_OUTPUT_CAP.equals(position)) { return state.fluidCap.cast(ctx); }
        } else if (cap==ForgeCapabilities.ITEM_HANDLER) {
            if (ITEM_OUTPUT_CAP.equals(position)) { return state.itemOutputCap.cast(ctx); }
        } else { return LazyOptional.empty(); }
        return LazyOptional.empty();
    }

    @Override
    public void dropExtraItems(State state, Consumer<ItemStack> drop) {
        MBInventoryUtils.dropItems(state.getInventory(), drop);
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType) {
        return AdvCokeOvenShape.GETTER;
    }

    private void drainOutputTank(ITAdvancedCokeOvenLogic.State state, IMultiblockContext<ITAdvancedCokeOvenLogic.State> context, CapabilityReference<IFluidHandler> outputRef) {
        int outSize = Math.min(FluidType.BUCKET_VOLUME, state.tank.getFluidAmount());
        FluidStack out = Utils.copyFluidStackWithAmount(state.tank.getFluid(), outSize, false);
        IFluidHandler output = outputRef.getNullable();
        if (output==null) { return; }
        int accepted = output.fill(out, FluidAction.SIMULATE);
        if (accepted > 0) {
            int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), FluidAction.EXECUTE);
            state.tank.drain(drained, FluidAction.EXECUTE);
            context.markMasterDirty();
            context.requestMasterBESync();
        }
    }

    @Override
    public void tickClient(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();

        final IMultiblockLevel level = ctx.getLevel();
        if (isActive(level)) {
            final Vec3 particlePos = level.toAbsolute(SMOKE_POS);
            level.getRawLevel().addAlwaysVisibleParticle(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    particlePos.x, particlePos.y, particlePos.z,
                    ApiUtils.RANDOM.nextDouble(-0.00625, 0.00625), .05, ApiUtils.RANDOM.nextDouble(-0.00625, 0.00625)
            );
        }

        if (!state.isSoundPlaying.getAsBoolean()) {
            final Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(1.5, 1.5, 1.5));
            state.isSoundPlaying = MultiblockSound.startSound(
                    () -> isActive(level), ctx.isValid(), soundPos, ITSounds.advCokeOven, 1f
            );
        }
    }

    private boolean isActive(IMultiblockLevel level) {
        return level.getBlockState(ITMultiblockProvider.ADV_COKE_OVEN.masterPosInMB())
                .getValue(NonMirrorableWithActiveBlock.ACTIVE);
    }

    public static class State implements IMultiblockState, ContainerData, ITFurnaceHandler.IFurnaceEnvironment, ProcessContext.ProcessContextInWorld<AdvancedCokeOvenRecipe> {
        public static final int MAX_BURN_TIME = 0;
        public static final int BURN_TIME = 1;
        public static final int NUM_SLOTS = 2;

        private BooleanSupplier isSoundPlaying = () -> false;

        private final StoredCapability<IItemHandler> itemOutputCap;

        private final FluidTank tank = new FluidTank(TANK_CAPACITY);
        private final SlotwiseItemHandler inventory;

        private final Function<Level, AdvancedCokeOvenRecipe> cachedRecipe;
        private int process = 0;
        private int processMax = 0;

        private final StoredCapability<IItemHandler> invCap;
        private final StoredCapability<IFluidHandler> fluidCap;

        private final CapabilityReference<IFluidHandler> fluidOutput;
        private final CapabilityReference<IItemHandler> outputRef;

        public State(IInitialMultiblockContext<ITAdvancedCokeOvenLogic.State> ctx) {
            final Supplier<@Nullable Level> levelGetter = ctx.levelSupplier();
            inventory = new SlotwiseItemHandler(
                    List.of(
                            IOConstraint.input(i -> CokeOvenRecipe.findRecipe(levelGetter.get(), i)!=null),
                            IOConstraint.OUTPUT,
                            IOConstraint.FLUID_INPUT,
                            IOConstraint.OUTPUT
                    ),
                    ctx.getMarkDirtyRunnable()
            );
            cachedRecipe = CachedRecipe.cachedSkip1(
                    AdvancedCokeOvenRecipe::findRecipe, () -> inventory.getStackInSlot(INPUT_SLOT)
            );
            this.invCap = new StoredCapability<>(this.inventory);
            this.fluidCap = new StoredCapability<>(
                    new ArrayFluidHandler(new IFluidTank[]{tank}, true, false, ctx.getMarkDirtyRunnable())
            );
            this.itemOutputCap = new StoredCapability<>(new WrappingItemHandler(
                    getInventory(), false, true, new WrappingItemHandler.IntRange(2, 3)
            ));
            this.fluidOutput = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, new MultiblockFace(FLUID_OUTPUT_CAP.side(), FLUID_OUTPUT_CAP.posInMultiblock().north()));
            this.outputRef = ctx.getCapabilityAt(ForgeCapabilities.ITEM_HANDLER, OUTPUT_POS);
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt) {
            nbt.put("tank", tank.writeToNBT(new CompoundTag()));
            nbt.putInt("process", process);
            nbt.putInt("processMax", processMax);
            nbt.put("inventory", inventory.serializeNBT());
        }

        @Override
        public void readSaveNBT(CompoundTag nbt) {
            tank.readFromNBT(nbt.getCompound("tank"));
            process = nbt.getInt("process");
            processMax = nbt.getInt("processMax");
            inventory.deserializeNBT(nbt.getCompound("inventory"));
        }

        @Override
        public int get(int index) {
            return switch(index) {
                case MAX_BURN_TIME -> processMax;
                case BURN_TIME -> process;
                default -> throw new IllegalArgumentException("Unknown index "+index);
            };
        }

        @Override
        public void set(int index, int value) {
            switch(index) {
                case MAX_BURN_TIME -> processMax = value;
                case BURN_TIME -> process = value;
                default -> throw new IllegalArgumentException("Unknown index "+index);
            }
        }

        @Override
        public int getCount() { return NUM_SLOTS; }

        public FluidTank getTank() { return tank; }

        @Override
        public AveragingEnergyStorage getEnergy() { return null; }

        public SlotwiseItemHandler getInventory() { return inventory; }

        @Override
        public IItemHandlerModifiable getInventory(int furnaceIndex) {
            return null;
        }

        @Nullable
        @Override
        public IESerializableRecipe getRecipeForInput(int furnaceIndex) {
            return null;
        }

        @Override
        public int getBurnTimeOf(Level level, ItemStack fuel) {
            return 0;
        }

        @Override
        public int getProcessSpeed(IMultiblockLevel level) {
            int i = 1;
            for (final BlockPos offset : HEATER_OFFSETS) {
                final BlastFurnacePreheaterBlockEntity preheater = getPreheater(level, offset);
                if (preheater == null)
                    ITLib.IT_LOGGER.error("Preheater is Null!");
                if (preheater!=null)
                    i += preheater.doSpeedup();
            }
            return i;
        }

        @Override
        public void turnOff(IMultiblockLevel level) {
            for (final BlockPos offset : HEATER_OFFSETS) {
                final BlastFurnacePreheaterBlockEntity preheater = getPreheater(level, offset);
                if (preheater!=null) { preheater.turnOff(); }
            }
        }

        @Nullable
        public BlastFurnacePreheaterBlockEntity getPreheater(IMultiblockLevel level, BlockPos pos) {
            BlockEntity te = level.getBlockEntity(pos);
            return te instanceof BlastFurnacePreheaterBlockEntity heater?heater: null;
        }

        public GetterAndSetter<Boolean> preheaterActive(IMultiblockLevel level, int index) {
            return GetterAndSetter.getterOnly(() -> {
                final BlastFurnacePreheaterBlockEntity heater = getPreheater(level, HEATER_OFFSETS[index]);
                return heater!=null&&heater.active;
            });
        }
    }

    public record AdvCokeOvenTank(FluidTank output) {
        public AdvCokeOvenTank() { this(new FluidTank(TANK_CAPACITY)); }

        public AdvCokeOvenTank(FluidTank output) { this.output = output; }

        public Tag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("out", this.output.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) { this.output.readFromNBT(tag.getCompound("out")); }

        public FluidTank output() { return this.output; }

        public BlockPos getLeftTankPos(boolean isMirrored) {
            BlockPos pos = new BlockPos(-4, 1, 0);
            if (isMirrored) pos = new BlockPos(3, 1, 0);
            return pos;
        }

        public BlockPos getRightTankPos(boolean isMirrored) {
            BlockPos pos = new BlockPos(3, 1, 1);
            if (isMirrored) pos = new BlockPos(-4, 1, 1);
            return pos;
        }

        public BlockPos getBackTankPos(boolean isMirrored) {
            BlockPos pos = new BlockPos(0, 1, -3);
            if (isMirrored) pos = new BlockPos(-1, 1, -3);
            return pos;
        }

        public BlockPos getOutputTankPos(boolean isMirrored) {
            BlockPos pos = new BlockPos(-1, 1, 4);
            if (isMirrored) pos = new BlockPos(0, 1, 4);
            return pos;
        }

        public int getCapacity() { return TANK_CAPACITY; }
    }
}
