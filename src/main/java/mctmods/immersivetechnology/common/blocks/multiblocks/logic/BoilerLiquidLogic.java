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
import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.api.HeatCapabilities;
import mctmods.immersivetechnology.api.capability.IHeatConsumer;
import mctmods.immersivetechnology.api.capability.IHeatProvider;
import mctmods.immersivetechnology.client.particles.ColoredSmoke;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITDisplayContext;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITMultiBlockInventoryUtils;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerLiquidRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.BoilerLiquidShape;
import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.core.lib.ITSound;
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
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class BoilerLiquidLogic implements IMultiblockLogic<BoilerLiquidLogic.State>, IServerTickableComponent<BoilerLiquidLogic.State>, IClientTickableComponent<BoilerLiquidLogic.State> {
    public static final int INPUT_FUEL_SLOT_FILLED = 0;
    public static final int INPUT_FUEL_SLOT_EMPTY = 1;
    public static final int TANK_CAPACITY = 24 * FluidType.BUCKET_VOLUME;
    private static final double HEAT_LOSS_PER_TICK = 0.2;
    public static final double WORKING_HEAT_LEVEL = 100.0;
    public static final double PILOT_HEAT = 20.0;
    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(BoilerLiquidShape.DATA.pointsOfInterest);

    public static final BlockPos REDSTONE_POI = getPosList("redstone").get(0);
    public static final List<BlockPos> IGNITION_POI = getPosList("ignition");
    private static final List<BlockPos> FLUID_INPUT_POI = getPosList("fluid_input");
    private static final List<BlockPos> HEAT_OUTPUT_POI = getPosList("heat_output");
    private static final List<BlockPos> SOUND_POI = getPosList("sound");
    private static final List<BlockPos> EXHAUST_POI = getPosList("exhaust");
    private static final RelativeBlockFace FLUID_INPUT_FACING = getFacing("fluid_input");
    private static final RelativeBlockFace HEAT_OUTPUT_FACING = getFacing("heat_output");
    public static final RelativeBlockFace IGNITION_FACING = getFacing("ignition");

    private static List<BlockPos> getPosList(String name) { return RAW_POIS.stream().filter(poi -> poi.name.equals(name)).map(poi -> new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2])).collect(ImmutableList.toImmutableList()); }
    private static RelativeBlockFace getFacing(String name) {
        List<RelativeBlockFace> facings = RAW_POIS.stream().filter(poi -> poi.name.equals(name)).map(poi -> poi.relativeFace).distinct().toList();
        if (facings.size() != 1) { throw new RuntimeException("Inconsistent facings for POI: " + name); }
        return facings.get(0);
    }

    @Override
    public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        if (state.heatLevel == 0) { state.isSoundPlaying = () -> false; }
        else {
            BlockPos soundAbs = ctx.getLevel().toAbsolute(SOUND_POI.get(0));
            Vec3 soundPos = new Vec3(soundAbs.getX() + 0.5, soundAbs.getY() + 0.5, soundAbs.getZ() + 0.5);
            if (!state.isSoundPlaying.getAsBoolean()) {
                state.isSoundPlaying = ITSound.startSound(
                        () -> state.heatLevel > 0,
                        ctx.isValid(),
                        soundPos,
                        ITSounds.boiler_liquid,
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
        Level level = ctx.getLevel().getRawLevel();
        if (state.pilotLit) {
            BlockPos exhaustAbs = ctx.getLevel().toAbsolute(EXHAUST_POI.get(0));
            Vec3 flamePos = new Vec3(exhaustAbs.getX() + 0.5, exhaustAbs.getY() + 0.1, exhaustAbs.getZ() + 0.5);
            double velX = (level.random.nextFloat() * 0.0625 - 0.03125);
            double velY = 0.0625;
            double velZ = (level.random.nextFloat() * 0.0625 - 0.03125);
            level.addParticle(ParticleTypes.FLAME, flamePos.x, flamePos.y, flamePos.z, velX, velY, velZ);
        }
        boolean hasWater = state.boilerInput.isPresent() && state.boilerInput.get().getFluidAmount() > 0;
        if (state.pilotLit && state.heatLevel > PILOT_HEAT && state.rsState.isEnabled(ctx) && hasWater) {
            BlockPos exhaustAbs = ctx.getLevel().toAbsolute(EXHAUST_POI.get(0));
            Vec3 smokePos = new Vec3(exhaustAbs.getX() + 0.5, exhaustAbs.getY() + 1.25, exhaustAbs.getZ() + 0.5);
            double velX = 0;
            double velY = 0.125;
            double velZ = 0;
            float r = 0.2F, g = 0.2F, b = 0.2F;
            level.addAlwaysVisibleParticle(new ColoredSmoke(r, g, b), smokePos.x, smokePos.y, smokePos.z, velX, velY, velZ);
        }
    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        Level level = ctx.getLevel().getRawLevel();
        CompoundTag prevTanksNBT = state.tanks.toNBT();
        Tag prevInventoryNBT = state.inventory.serializeNBT();
        double prevHeatLevel = state.heatLevel;
        boolean prevPilotLit = state.pilotLit;
        boolean wasActive = state.active;
        if (state.tanks.input1.getFluidAmount() <= 0) { state.pilotLit = false; }
        double delta = HEAT_LOSS_PER_TICK;
        boolean hasWater = state.boilerInput.isPresent() && state.boilerInput.get().getFluidAmount() > 0;
        boolean fullMode = state.rsState.isEnabled(ctx) && hasWater;
        if (!state.pilotLit) {
            state.heatLevel = Math.max(state.heatLevel - delta, 0);
            state.burnRemaining = 0;
        } else {
            if (state.burnRemaining > 0) {
                state.burnRemaining--;
                if (state.lastFuel != null) {
                    if (!fullMode) { state.heatLevel = Math.max(state.heatLevel - delta, PILOT_HEAT); }
                } else { state.burnRemaining = 0; }
            } else {
                state.lastFuel = null;
                if (state.tanks.input1.getFluidAmount() > 0) {
                    FluidStack dummy = state.tanks.input1.getFluid().copy();
                    dummy = new FluidStack(dummy, Integer.MAX_VALUE);
                    state.lastFuel = BoilerLiquidRecipe.findRecipe(level, dummy);
                }
                if (state.lastFuel != null) {
                    FluidStack drained;
                    if (fullMode) {
                        int drainAmount = state.lastFuel.input.getAmount();
                        drained = state.tanks.input1.drain(drainAmount, FluidAction.EXECUTE);
                        if (drained.getAmount() == drainAmount) { state.heatLevel = Math.min(state.heatLevel + state.lastFuel.getHeatPerTick(), WORKING_HEAT_LEVEL); }
                        else {
                            drained = state.tanks.input1.drain(1, FluidAction.EXECUTE);
                            if (drained.getAmount() >= 1) { state.heatLevel = Math.max(state.heatLevel - delta, PILOT_HEAT); }
                            else { state.pilotLit = false; state.heatLevel = Math.max(state.heatLevel - delta, 0); }
                        }
                    } else {
                        drained = state.tanks.input1.drain(1, FluidAction.EXECUTE);
                        if (drained.getAmount() >= 1) { state.heatLevel = Math.max(state.heatLevel - delta, PILOT_HEAT); }
                        else { state.pilotLit = false; state.heatLevel = Math.max(state.heatLevel - delta, 0); }
                    }
                    state.pilotLit = true;
                } else {
                    state.pilotLit = false;
                    state.heatLevel = Math.max(state.heatLevel - delta, 0);
                }
            }
        }
        tryEmptyContainer(state.tanks.input1, state.inventory);
        state.active = state.pilotLit && fullMode && state.heatLevel >= WORKING_HEAT_LEVEL;
        boolean heatLevelChanged = prevHeatLevel != state.heatLevel;
        boolean pilotLitChanged = prevPilotLit != state.pilotLit;
        boolean activeChanged = wasActive != state.active;
        CompoundTag currentTanksNBT = state.tanks.toNBT();
        boolean tanksChanged = !prevTanksNBT.equals(currentTanksNBT);
        Tag currentInventoryNBT = state.inventory.serializeNBT();
        boolean inventoryChanged = !prevInventoryNBT.equals(currentInventoryNBT);
        boolean update = heatLevelChanged || pilotLitChanged || activeChanged || tanksChanged || inventoryChanged;
        if (update) { ctx.markMasterDirty(); ctx.requestMasterBESync(); }
    }

    private void tryEmptyContainer(IFluidHandler tank, IItemHandlerModifiable inv) {
        ItemStack filledContainer = inv.getStackInSlot(INPUT_FUEL_SLOT_FILLED);
        if (filledContainer.isEmpty()) { return; }
        FluidActionResult simResult = FluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.SIMULATE);
        if (!simResult.isSuccess()) { return; }
        ItemStack emptyContainer = simResult.getResult();
        ItemStack outputStack = inv.getStackInSlot(INPUT_FUEL_SLOT_EMPTY);
        if (!outputStack.isEmpty() && !ItemHandlerHelper.canItemStacksStack(outputStack, emptyContainer)) { return; }
        if (outputStack.getCount() + emptyContainer.getCount() > emptyContainer.getMaxStackSize()) { return; }
        FluidActionResult execResult = FluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.EXECUTE);
        filledContainer.shrink(1);
        inv.setStackInSlot(INPUT_FUEL_SLOT_FILLED, filledContainer);
        if (outputStack.isEmpty()) { inv.setStackInSlot(INPUT_FUEL_SLOT_EMPTY, execResult.getResult()); }
        else { outputStack.grow(execResult.getResult().getCount()); }
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        BlockPos localPos = position.posInMultiblock();
        RelativeBlockFace side = position.side();
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (FLUID_INPUT_POI.contains(localPos) && (side == null || side == FLUID_INPUT_FACING)) { return ctx.getState().inputFuelCap.cast(ctx); }
        } else if (cap == HeatCapabilities.HEAT_PROVIDER_CAPABILITY) {
            if (HEAT_OUTPUT_POI.contains(localPos) && (side == null || side == HEAT_OUTPUT_FACING)) { return ctx.getState().heatSourceCap.cast(ctx); }
        }
        return LazyOptional.empty();
    }

    @Override
    public void dropExtraItems(State state, Consumer<ItemStack> drop) { ITMultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override
    public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return BoilerLiquidShape.GETTER; }

    public static class State implements IMultiblockState, ITDisplayContext {
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final BoilerTank tanks;
        public StoredCapability<IFluidHandler> inputFuelCap;
        public StoredCapability<IHeatProvider> heatSourceCap;
        public CapabilityReference<IHeatConsumer> boilerInput;
        public ITSlotwiseItemHandler inventory;
        public double heatLevel = 0;
        public int burnRemaining = 0;
        public BoilerLiquidRecipe lastFuel;
        public boolean pilotLit = false;
        public boolean active = false;
        public BooleanSupplier isSoundPlaying = () -> false;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); };
            tanks = new BoilerTank(v -> onChanged.run());
            inventory = new ITSlotwiseItemHandler(
                    List.of(
                            ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            ITSlotwiseItemHandler.IOConstraint.OUTPUT
                    ),
                    onChanged
            );
            inputFuelCap = new StoredCapability<>(new ITArrayFluidHandler(tanks.input1, false, true, onChanged));
            heatSourceCap = new StoredCapability<>(new HeatSourceImpl(this));
            MultiblockFace heatMBFace = new MultiblockFace(HEAT_OUTPUT_FACING, HEAT_OUTPUT_POI.get(0));
            CapabilityPosition opposingCP = CapabilityPosition.opposing(heatMBFace);
            MultiblockFace opposingMBFace = new MultiblockFace(opposingCP.side(), opposingCP.posInMultiblock());
            boilerInput = ctx.getCapabilityAt(HeatCapabilities.HEAT_CONSUMER_CAPABILITY, opposingMBFace);
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt) {
            nbt.put("tanks", tanks.toNBT());
            nbt.putDouble("heatLevel", heatLevel);
            nbt.putInt("burnRemaining", burnRemaining);
            nbt.putBoolean("pilotLit", pilotLit);
            nbt.put("inventory", inventory.serializeNBT());
        }

        @Override
        public void readSaveNBT(CompoundTag nbt) {
            tanks.readNBT(nbt.getCompound("tanks"));
            heatLevel = nbt.getDouble("heatLevel");
            burnRemaining = nbt.getInt("burnRemaining");
            pilotLit = nbt.getBoolean("pilotLit");
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
        public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input1}; }

        @Override
        public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.putDouble("heatLevel", heatLevel);
            nbt.putBoolean("pilotLit", pilotLit);
            nbt.put("tanks", tanks.toNBT());
            nbt.put("inventory", inventory.serializeNBT());
        }

        @Override
        public void readDisplaySyncNBT(CompoundTag nbt) {
            active = nbt.getBoolean("active");
            heatLevel = nbt.getDouble("heatLevel");
            pilotLit = nbt.getBoolean("pilotLit");
            tanks.readNBT(nbt.getCompound("tanks"));
            inventory.deserializeNBT(nbt.getCompound("inventory"));
        }
    }

    private record HeatSourceImpl(State state) implements IHeatProvider {
        @Override
        public double getHeatLevel() { return state.heatLevel; }
    }

    public record BoilerTank(ITMarkableFluidTank input1) {
        public BoilerTank(Consumer<Void> markDirty) { this(new ITMarkableFluidTank(TANK_CAPACITY, markDirty)); }

        public static BoilerTank makeClient() { return new BoilerTank(v -> {}); }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("input1", this.input1.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) { this.input1.readFromNBT(tag.getCompound("input1")); }

        @SuppressWarnings("unused")
        public int getCapacity() { return TANK_CAPACITY; }
    }
}
