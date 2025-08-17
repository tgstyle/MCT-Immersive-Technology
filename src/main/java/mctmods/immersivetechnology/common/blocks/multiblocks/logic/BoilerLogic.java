package mctmods.immersivetechnology.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.client.particles.ColoredSmokeData;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITMultiBlockInventoryUtils;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerFuelRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.BoilerShape;
import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.core.lib.ITMultiblockSound;
import mctmods.immersivetechnology.core.registration.ITSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class BoilerLogic implements IMultiblockLogic<BoilerLogic.State>, IServerTickableComponent<BoilerLogic.State>, IClientTickableComponent<BoilerLogic.State> {
    public static final int INPUT_FUEL_SLOT_FILLED = 0;
    public static final int INPUT_FUEL_SLOT_EMPTY = 1;
    public static final int INPUT_SLOT_FILLED = 2;
    public static final int INPUT_SLOT_EMPTY = 3;
    public static final int OUTPUT_SLOT_EMPTY = 4;
    public static final int OUTPUT_SLOT_FILLED = 5;
    public static final int TANK_CAPACITY = 24 * FluidType.BUCKET_VOLUME;
    public static final CapabilityPosition INPUT_FLUID_POI1 = new CapabilityPosition(4, 0, 1, RelativeBlockFace.LEFT);
    public static final CapabilityPosition INPUT_FLUID_POI2 = new CapabilityPosition(0, 0, 1, RelativeBlockFace.RIGHT);
    public static final CapabilityPosition OUTPUT_FLUID_POI = new CapabilityPosition(0, 2, 1, RelativeBlockFace.UP);
    public static final BlockPos REDSTONE_POI = new BlockPos(4, 1, 2);
    private static final double HEAT_LOSS_PER_TICK = 0.2;
    private static final int PROGRESS_LOSS_PER_TICK = 1;
    private static final double WORKING_HEAT_LEVEL = 100.0;
    public static final double PILOT_HEAT = 20.0;
    private static final BlockPos SMOKE_POI = new BlockPos(4, 2, 1);

    @Override
    public void tickClient(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        if (state.heatLevel == 0) {
            state.isSoundPlaying = () -> false;
        }
        else {
            Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(2.5, 1.5, 1.5));
            if (!state.isSoundPlaying.getAsBoolean()) {
                state.isSoundPlaying = ITMultiblockSound.startSound(
                        () -> state.heatLevel > 0,
                        ctx.isValid(),
                        soundPos,
                        ITSounds.boiler,
                        () -> {
                            LocalPlayer player = Minecraft.getInstance().player;
                            if (player == null) { return 0f; }
                            float attenuation = (float) Math.max(player.distanceToSqr(soundPos) / 8, 1);
                            float currentLevel = (float) (state.heatLevel / WORKING_HEAT_LEVEL);
                            return (2 * currentLevel) / attenuation;
                        },
                        () -> (float) (state.heatLevel / WORKING_HEAT_LEVEL)
                );
            }
        }
        final Level level = ctx.getLevel().getRawLevel();
        if (state.pilotLit) {
            BlockPos exhaustAbs = ctx.getLevel().toAbsolute(SMOKE_POI);
            Vec3 flamePos = new Vec3(exhaustAbs.getX() + 0.5, exhaustAbs.getY() + 0.1, exhaustAbs.getZ() + 0.5);
            double velX = (level.random.nextFloat() * 0.0625 - 0.03125);
            double velY = 0.0625;
            double velZ = (level.random.nextFloat() * 0.0625 - 0.03125);
            if (level.isClientSide) {
                level.addParticle(ParticleTypes.FLAME, flamePos.x, flamePos.y, flamePos.z, velX, velY, velZ);
            }
        }
        if (state.pilotLit && state.heatLevel > PILOT_HEAT && state.rsState.isEnabled(ctx) && state.tanks.input2.getFluidAmount() > 0) {
            BlockPos exhaustAbs = ctx.getLevel().toAbsolute(SMOKE_POI);
            Vec3 smokePos = new Vec3(exhaustAbs.getX() + 0.5, exhaustAbs.getY() + 1.25, exhaustAbs.getZ() + 0.5);
            double velX = 0;
            double velY = 0.125;
            double velZ = 0;
            float r = 0.2F, g = 0.2F, b = 0.2F;
            level.addAlwaysVisibleParticle(new ColoredSmokeData(r, g, b), smokePos.x, smokePos.y, smokePos.z, velX, velY, velZ);
        }
    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        final Level level = ctx.getLevel().getRawLevel();
        boolean update = false;
        double heatTransferMultiplier = 1.0;
        double previousHeatLevel = state.heatLevel;
        if (state.tanks.input1.getFluidAmount() <= 0) {
            state.pilotLit = false;
        }
        double delta = HEAT_LOSS_PER_TICK * heatTransferMultiplier;
        if (!state.pilotLit) {
            state.heatLevel = Math.max(state.heatLevel - delta, 0);
            state.burnRemaining = 0;
            if (previousHeatLevel != state.heatLevel) { update = true; }
        }
        else {
            if (state.burnRemaining > 0) {
                state.burnRemaining--;
                if (state.lastFuel != null) {
                    if (state.rsState.isEnabled(ctx) && state.tanks.input2.getFluidAmount() > 0) {
                        state.heatLevel = Math.min(state.heatLevel + state.lastFuel.getHeatPerTick(), WORKING_HEAT_LEVEL);
                    }
                    else {
                        state.heatLevel = Math.max(state.heatLevel - delta, PILOT_HEAT);
                    }
                    if (previousHeatLevel != state.heatLevel) { update = true; }
                }
                else {
                    state.burnRemaining = 0;
                }
            }
            else {
                state.lastFuel = BoilerFuelRecipe.findRecipe(level, state.tanks.input1.getFluid());
                if (state.lastFuel != null) {
                    boolean fullMode = state.rsState.isEnabled(ctx) && state.tanks.input2.getFluidAmount() > 0;
                    FluidStack drained;
                    if (fullMode) {
                        int drainAmount = state.lastFuel.input.getAmount();
                        drained = state.tanks.input1.drain(drainAmount, FluidAction.EXECUTE);
                        if (drained.getAmount() == drainAmount) {
                            state.burnRemaining = state.lastFuel.getTotalProcessTime() - 1;
                            state.heatLevel = Math.min(state.heatLevel + state.lastFuel.getHeatPerTick(), WORKING_HEAT_LEVEL);
                        }
                        else {
                            drained = state.tanks.input1.drain(1, FluidAction.EXECUTE);
                            if (drained.getAmount() >= 1) {
                                state.heatLevel = Math.max(state.heatLevel - delta, PILOT_HEAT);
                            }
                            else {
                                state.pilotLit = false;
                                state.heatLevel = Math.max(state.heatLevel - delta, 0);
                            }
                        }
                    }
                    else {
                        drained = state.tanks.input1.drain(1, FluidAction.EXECUTE);
                        if (drained.getAmount() >= 1) {
                            state.heatLevel = Math.max(state.heatLevel - delta, PILOT_HEAT);
                        }
                        else {
                            state.pilotLit = false;
                            state.heatLevel = Math.max(state.heatLevel - delta, 0);
                        }
                    }
                }
                else {
                    state.pilotLit = false;
                    state.heatLevel = Math.max(state.heatLevel - delta, 0);
                }
                if (previousHeatLevel != state.heatLevel) { update = true; }
            }
        }
        if (state.heatLevel >= WORKING_HEAT_LEVEL) {
            if (state.recipeTimeRemaining > 0) {
                if (state.lastRecipe == null) {
                    state.recipeTimeRemaining = 0;
                    update = true;
                }
                else {
                    state.recipeTimeRemaining--;
                    if (state.recipeTimeRemaining == 0) {
                        state.tanks.input2.drain(state.lastRecipe.input.getAmount(), FluidAction.EXECUTE);
                        state.tanks.output.fill(state.lastRecipe.output.copy(), FluidAction.EXECUTE);
                        update = true;
                    }
                }
            }
            else if (state.tanks.input2.getFluidAmount() > 0) {
                state.lastRecipe = BoilerRecipe.findRecipe(level, state.tanks.input2.getFluid());
                if (state.lastRecipe != null && state.lastRecipe.input.getAmount() <= state.tanks.input2.getFluidAmount() && state.lastRecipe.output.getAmount() <= state.tanks.output.getCapacity() - state.tanks.output.getFluidAmount()) {
                    state.recipeTimeRemaining = state.lastRecipe.getTotalProcessTime();
                    state.recipeTimeRemaining--;
                    if (state.recipeTimeRemaining == 0) {
                        state.tanks.input2.drain(state.lastRecipe.input.getAmount(), FluidAction.EXECUTE);
                        state.tanks.output.fill(state.lastRecipe.output.copy(), FluidAction.EXECUTE);
                    }
                    update = true;
                }
            }
        }
        else if (state.recipeTimeRemaining > 0) {
            int previousProgress = state.recipeTimeRemaining;
            if (state.lastRecipe == null) {
                state.recipeTimeRemaining = 0;
                update = true;
            }
            else {
                state.recipeTimeRemaining = Math.min(state.recipeTimeRemaining + PROGRESS_LOSS_PER_TICK, state.lastRecipe.getTotalProcessTime());
                if (previousProgress != state.recipeTimeRemaining) { update = true; }
            }
        }
        if (state.tanks.output.getFluidAmount() > 0) {
            if (FluidUtils.fillFluidContainer(state.tanks.output, OUTPUT_SLOT_EMPTY, OUTPUT_SLOT_FILLED, state.inventory)) { update = true; }
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
        }
        if (tryEmptyContainer(state.tanks.input1, INPUT_FUEL_SLOT_FILLED, INPUT_FUEL_SLOT_EMPTY, state.inventory)) { update = true; }
        if (tryEmptyContainer(state.tanks.input2, INPUT_SLOT_FILLED, INPUT_SLOT_EMPTY, state.inventory)) { update = true; }
        if (update) {
            ctx.markMasterDirty();
            if (state.clientUpdateCooldown == 1) {
                ctx.requestMasterBESync();
                state.clientUpdateCooldown = 20;
            }
            else {
                state.clientUpdateCooldown--;
            }
        }
    }

    private boolean tryEmptyContainer(IFluidHandler tank, int slotFilled, int slotEmpty, IItemHandlerModifiable inv) {
        ItemStack filledContainer = inv.getStackInSlot(slotFilled);
        if (filledContainer.isEmpty()) { return false; }
        FluidActionResult simResult = FluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.SIMULATE);
        if (!simResult.isSuccess()) { return false; }
        ItemStack emptyContainer = simResult.getResult();
        ItemStack outputStack = inv.getStackInSlot(slotEmpty);
        if (!outputStack.isEmpty() && !ItemHandlerHelper.canItemStacksStack(outputStack, emptyContainer)) { return false; }
        if (outputStack.getCount() + emptyContainer.getCount() > emptyContainer.getMaxStackSize()) { return false; }
        FluidActionResult execResult = FluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.EXECUTE);
        filledContainer.shrink(1);
        inv.setStackInSlot(slotFilled, filledContainer);
        if (outputStack.isEmpty()) { inv.setStackInSlot(slotEmpty, execResult.getResult()); }
        else { outputStack.grow(execResult.getResult().getCount()); }
        return true;
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        final State state = ctx.getState();
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return state.invCap.cast(ctx);
        }
        else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (position.posInMultiblock().equals(INPUT_FLUID_POI1.posInMultiblock()) && (position.side() == null || position.side() == INPUT_FLUID_POI1.side())) { return state.inputFuelCap.cast(ctx); }
            if (position.posInMultiblock().equals(INPUT_FLUID_POI2.posInMultiblock()) && (position.side() == null || position.side() == INPUT_FLUID_POI2.side())) { return state.inputWaterCap.cast(ctx); }
            if (position.posInMultiblock().equals(OUTPUT_FLUID_POI.posInMultiblock()) && (position.side() == null || position.side() == OUTPUT_FLUID_POI.side())) { return state.outputCap.cast(ctx); }
        }
        return LazyOptional.empty();
    }

    @Override
    public void dropExtraItems(State state, Consumer<ItemStack> drop) { ITMultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override
    public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return BoilerShape.GETTER; }

    public static class State implements IMultiblockState {
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final BoilerTank tanks;
        public StoredCapability<IFluidHandler> inputFuelCap;
        public StoredCapability<IFluidHandler> inputWaterCap;
        public StoredCapability<IFluidHandler> outputCap;
        public StoredCapability<IItemHandler> invCap;
        public CapabilityReference<IFluidHandler> fluidOutput;
        public ITSlotwiseItemHandler inventory;
        public double heatLevel = 0;
        public int burnRemaining = 0;
        public int recipeTimeRemaining = 0;
        public BoilerFuelRecipe lastFuel;
        public BoilerRecipe lastRecipe;
        public boolean pilotLit = false;
        public BooleanSupplier isSoundPlaying = () -> false;
        public int clientUpdateCooldown = 20;

        public State(IInitialMultiblockContext<State> ctx) {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            final Runnable sync = ctx.getSyncRunnable();
            final Runnable onChanged = () -> {
                markDirty.run();
                sync.run();
            };
            tanks = new BoilerTank(v -> onChanged.run());
            inventory = new ITSlotwiseItemHandler(
                    List.of(
                            ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            ITSlotwiseItemHandler.IOConstraint.OUTPUT,
                            ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            ITSlotwiseItemHandler.IOConstraint.OUTPUT,
                            ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            ITSlotwiseItemHandler.IOConstraint.OUTPUT
                    ),
                    onChanged
            );
            inputFuelCap = new StoredCapability<>(new ITArrayFluidHandler(tanks.input1, false, true, onChanged));
            inputWaterCap = new StoredCapability<>(new ITArrayFluidHandler(tanks.input2, false, true, onChanged));
            outputCap = new StoredCapability<>(new ITArrayFluidHandler(tanks.output, true, false, onChanged));
            invCap = new StoredCapability<>(inventory);
            MultiblockFace outputMBFace = new MultiblockFace(OUTPUT_FLUID_POI.side(), OUTPUT_FLUID_POI.posInMultiblock());
            CapabilityPosition opposingCP = CapabilityPosition.opposing(outputMBFace);
            MultiblockFace opposingMBFace = new MultiblockFace(opposingCP.side(), opposingCP.posInMultiblock());
            fluidOutput = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, opposingMBFace);
        }

        public ITSlotwiseItemHandler getInventory() { return inventory; }

        public BoilerTank getTanks() { return tanks; }

        @Override
        public void writeSaveNBT(CompoundTag nbt) {
            nbt.put("tanks", tanks.toNBT());
            nbt.putDouble("heatLevel", heatLevel);
            nbt.putInt("burnRemaining", burnRemaining);
            nbt.putInt("recipeTimeRemaining", recipeTimeRemaining);
            nbt.putBoolean("pilotLit", pilotLit);
            nbt.put("inventory", inventory.serializeNBT());
        }

        @Override
        public void readSaveNBT(CompoundTag nbt) {
            tanks.readNBT(nbt.getCompound("tanks"));
            heatLevel = nbt.getDouble("heatLevel");
            burnRemaining = nbt.getInt("burnRemaining");
            recipeTimeRemaining = nbt.getInt("recipeTimeRemaining");
            pilotLit = nbt.getBoolean("pilotLit");
            inventory.deserializeNBT(nbt.getCompound("inventory"));
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt) {
            nbt.putDouble("heatLevel", heatLevel);
            nbt.putBoolean("pilotLit", pilotLit);
            nbt.put("tanks", tanks.toNBT());
        }

        @Override
        public void readSyncNBT(CompoundTag nbt) {
            heatLevel = nbt.getDouble("heatLevel");
            pilotLit = nbt.getBoolean("pilotLit");
            tanks.readNBT(nbt.getCompound("tanks"));
        }
    }

    public record BoilerTank(ITMarkableFluidTank input1, ITMarkableFluidTank input2, ITMarkableFluidTank output) {
        public BoilerTank(Consumer<Void> markDirty) {
            this(new ITMarkableFluidTank(TANK_CAPACITY, markDirty), new ITMarkableFluidTank(TANK_CAPACITY, markDirty), new ITMarkableFluidTank(TANK_CAPACITY, markDirty));
        }

        public static BoilerTank makeClient() { return new BoilerTank(v -> {}); }

        public Tag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("input1", this.input1.writeToNBT(new CompoundTag()));
            tag.put("input2", this.input2.writeToNBT(new CompoundTag()));
            tag.put("output", this.output.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) {
            this.input1.readFromNBT(tag.getCompound("input1"));
            this.input2.readFromNBT(tag.getCompound("input2"));
            this.output.readFromNBT(tag.getCompound("output"));
        }

        @SuppressWarnings("unused")
        public int getCapacity() { return TANK_CAPACITY; }
    }
}
