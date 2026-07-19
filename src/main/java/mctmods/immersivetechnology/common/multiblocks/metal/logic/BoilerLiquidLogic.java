package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.client.particles.ColoredSmoke;
import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.common.multiblocks.helper.ITIDisplayContext;
import mctmods.immersivetechnology.common.multiblocks.helper.ITMultiblockPOIHelper;
import mctmods.immersivetechnology.common.multiblocks.helper.ITMultiBlockInventoryUtils;
import mctmods.immersivetechnology.common.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerLiquidRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.BoilerLiquidShape;
import mctmods.immersivetechnology.core.ITCommonConfig;
import mctmods.immersivetechnology.core.ITServerConfig;
import mctmods.immersivetechnology.core.lib.ITSound;
import mctmods.immersivetechnology.core.registration.ITSounds;
import mctmods.immersivetechnology.core.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.core.util.ITCachedRecipe;

import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.HeatCapabilities;
import com.immersiveconvergence.api.capability.IHeatConsumer;
import com.immersiveconvergence.api.capability.IHeatProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.BiFunction;

public class BoilerLiquidLogic implements IMultiblockLogic<BoilerLiquidLogic.State>, IServerTickableComponent<BoilerLiquidLogic.State>, IClientTickableComponent<BoilerLiquidLogic.State> {
    public static final int INPUT_FUEL_SLOT_FILLED = 0;
    public static final int INPUT_FUEL_SLOT_EMPTY = 1;

    public static final double HEAT_LOSS_PER_TICK = ITServerConfig.boilerLiquidHeatLossPerTick;
    public static final double DEFAULT_WORKING_HEAT_LEVEL = ITCommonConfig.boilerDefaultWorkingHeat;
    public static final double PILOT_HEAT = ITServerConfig.boilerLiquidPilotHeat;

    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(BoilerLiquidShape.DATA.pointsOfInterest);

    public static final BlockPos REDSTONE_POI = ITMultiblockPOIHelper.getPosList(RAW_POIS, "redstone0").getFirst();
    public static final List<BlockPos> IGNITION_POIS = ITMultiblockPOIHelper.getPosList(RAW_POIS, "ignition0");
    public static final List<BlockPos> INPUT_FLUID_POIS = ITMultiblockPOIHelper.getPosList(RAW_POIS, "fluid_input0");
    public static final List<BlockPos> HEAT_OUTPUT_POIS = ITMultiblockPOIHelper.getPosList(RAW_POIS, "heat_output0");
    public static final BlockPos SOUND_POI = ITMultiblockPOIHelper.getPosList(RAW_POIS, "sound0").getFirst();
    public static final List<BlockPos> EXHAUST_POIS = ITMultiblockPOIHelper.getPosList(RAW_POIS, "exhaust0");
    private static final RelativeBlockFace INPUT_FLUID_FACING = ITMultiblockPOIHelper.getFacing(RAW_POIS, "fluid_input0");
    private static final RelativeBlockFace HEAT_OUTPUT_FACING = ITMultiblockPOIHelper.getFacing(RAW_POIS, "heat_output0");
    public static final RelativeBlockFace IGNITION_FACING = ITMultiblockPOIHelper.getFacing(RAW_POIS, "ignition0");

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        BlockPos soundAbs = ctx.getLevel().toAbsolute(SOUND_POI);
        Vec3 soundPos = new Vec3(soundAbs.getX() + 0.5, soundAbs.getY() + 0.5, soundAbs.getZ() + 0.5);
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) { return; }
        float attenuation = (float) Math.max(player.distanceToSqr(soundPos) / 8, 1);
        float currentLevel = (float) (state.heatLevel / state.workingHeatLevel);
        float vol = (2 * currentLevel) / attenuation;
        if ((!state.pilotLit || state.heatLevel > PILOT_HEAT) && state.heatLevel > 0 && vol > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
            state.isSoundPlaying = ITSound.startSound(
                    () -> (!state.pilotLit || state.heatLevel > PILOT_HEAT) && state.heatLevel > 0,
                    ctx.isValid(),
                    soundPos,
                    ITSounds.boiler_liquid,
                    () -> {
                        LocalPlayer p = Minecraft.getInstance().player;
                        if (p == null) { return 0f; }
                        float a = (float) Math.max(p.distanceToSqr(soundPos) / 8, 1);
                        return (2 * (float) (state.heatLevel / state.workingHeatLevel)) / a;
                    },
                    () -> (float) (state.heatLevel / state.workingHeatLevel)
            );
        }
        if (state.pilotLit && state.heatLevel <= PILOT_HEAT && state.heatLevel > 0 && vol > 0.01f && !state.isPilotSoundPlaying.getAsBoolean()) {
            state.isPilotSoundPlaying = ITSound.startSound(
                    () -> state.pilotLit && state.heatLevel <= PILOT_HEAT && state.heatLevel > 0,
                    ctx.isValid(),
                    soundPos,
                    ITSounds.pilot,
                    () -> {
                        LocalPlayer p = Minecraft.getInstance().player;
                        if (p == null) { return 0f; }
                        float a = (float) Math.max(p.distanceToSqr(soundPos) / 8, 1);
                        return (2 * (float) (PILOT_HEAT / state.workingHeatLevel)) / a;
                    },
                    () -> (float) (PILOT_HEAT / state.workingHeatLevel)
            );
        }
        Level level = ctx.getLevel().getRawLevel();
        if (state.pilotLit) {
            BlockPos exhaustAbs = ctx.getLevel().toAbsolute(EXHAUST_POIS.getFirst());
            Vec3 flamePos = new Vec3(exhaustAbs.getX() + 0.5, exhaustAbs.getY() + 0.1, exhaustAbs.getZ() + 0.5);
            double velX = (level.random.nextFloat() * 0.0625 - 0.03125);
            double velY = 0.0625;
            double velZ = (level.random.nextFloat() * 0.0625 - 0.03125);
            level.addParticle(ParticleTypes.FLAME, flamePos.x, flamePos.y, flamePos.z, velX, velY, velZ);
        }
        boolean hasWater = state.boilerInput != null && state.boilerInput.get() != null && state.boilerInput.get().getFluidAmount() > 0;
        if (state.pilotLit && state.heatLevel > PILOT_HEAT && state.rsState.isEnabled(ctx) && hasWater) {
            BlockPos exhaustAbs = ctx.getLevel().toAbsolute(EXHAUST_POIS.getFirst());
            Vec3 smokePos = new Vec3(exhaustAbs.getX() + 0.5, exhaustAbs.getY() + 1.25, exhaustAbs.getZ() + 0.5);
            double velX = 0;
            double velY = 0.125;
            double velZ = 0;
            float r = 0.2F, g = 0.2F, b = 0.2F;
            level.addAlwaysVisibleParticle(new ColoredSmoke(r, g, b), smokePos.x, smokePos.y, smokePos.z, velX, velY, velZ);
        }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        Level level = ctx.getLevel().getRawLevel();
        boolean prevTanksDirty = state.tanksDirty;
        boolean prevInventoryDirty = state.inventoryDirty;
        double prevHeatLevel = state.heatLevel;
        boolean prevPilotLit = state.pilotLit;
        boolean wasActive = state.active;
        if (state.tanks.input1().getFluidAmount() <= 0) { state.pilotLit = false; }
        double delta = HEAT_LOSS_PER_TICK;
        boolean hasWater = state.boilerInput != null && state.boilerInput.get() != null && state.boilerInput.get().getFluidAmount() > 0;
        boolean fullMode = state.rsState.isEnabled(ctx) && hasWater;
        if (!state.pilotLit) {
            state.heatLevel = Math.max(state.heatLevel - delta, 0);
            state.burnRemaining = 0;
        } else {
            if (state.burnRemaining > 0) {
                state.burnRemaining--;
                if (state.lastFuel != null) {
                    if (fullMode) {
                        if (state.heatLevel < state.targetHeat) { state.heatLevel = Math.min(state.heatLevel + state.lastFuel.getHeatPerTick(), state.targetHeat); }
                        else { state.heatLevel = Math.max(state.heatLevel - delta, state.targetHeat); }
                    } else { state.heatLevel = Math.max(state.heatLevel - delta, PILOT_HEAT); }
                } else { state.burnRemaining = 0; }
            } else {
                state.lastFuel = null;
                if (state.tanks.input1().getFluidAmount() > 0) {
                    FluidStack dummy = state.tanks.input1().getFluid().copyWithAmount(Integer.MAX_VALUE);
                    state.lastFuel = state.recipeGetter.apply(level, dummy);
                }
                if (state.lastFuel != null) {
                    state.targetHeat = state.lastFuel.getTargetHeat();
                    state.workingHeatLevel = state.targetHeat;
                    FluidStack drained;
                    if (fullMode) {
                        int drainAmount = state.lastFuel.getInputAmount();
                        drained = state.tanks.input1().drain(drainAmount, FluidAction.EXECUTE);
                        if (drained.getAmount() == drainAmount) {
                            if (state.heatLevel < state.targetHeat) { state.heatLevel = Math.min(state.heatLevel + state.lastFuel.getHeatPerTick(), state.targetHeat); }
                            else { state.heatLevel = Math.max(state.heatLevel - delta, state.targetHeat); }
                        } else {
                            drained = state.tanks.input1().drain(1, FluidAction.EXECUTE);
                            if (drained.getAmount() >= 1) { state.heatLevel = Math.max(state.heatLevel - delta, PILOT_HEAT); }
                            else { state.pilotLit = false; state.heatLevel = Math.max(state.heatLevel - delta, 0); }
                        }
                    } else {
                        drained = state.tanks.input1().drain(1, FluidAction.EXECUTE);
                        if (drained.getAmount() >= 1) { state.heatLevel = Math.max(state.heatLevel - delta, PILOT_HEAT); }
                        else { state.pilotLit = false; state.heatLevel = Math.max(state.heatLevel - delta, 0); }
                    }
                    state.pilotLit = true;
                } else {
                    state.pilotLit = false;
                    state.heatLevel = Math.max(state.heatLevel - delta, 0);
                    state.workingHeatLevel = DEFAULT_WORKING_HEAT_LEVEL;
                }
            }
        }
        tryEmptyContainer(state.tanks.input1(), state.inventory);
        state.active = state.pilotLit && fullMode && state.heatLevel >= state.workingHeatLevel;
        boolean heatLevelChanged = prevHeatLevel != state.heatLevel;
        boolean pilotLitChanged = prevPilotLit != state.pilotLit;
        boolean activeChanged = wasActive != state.active;
        boolean tanksChanged = prevTanksDirty != state.tanksDirty;
        boolean inventoryChanged = prevInventoryDirty != state.inventoryDirty;
        boolean update = heatLevelChanged || pilotLitChanged || activeChanged || tanksChanged || inventoryChanged;
        if (update) { ctx.markMasterDirty(); ctx.requestMasterBESync(); }
    }

    private void tryEmptyContainer(ITMarkableFluidTank tank, ITSlotwiseItemHandler inv) {
        ItemStack filledContainer = inv.getStackInSlot(INPUT_FUEL_SLOT_FILLED);
        if (filledContainer.isEmpty()) { return; }
        FluidActionResult simResult = FluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.SIMULATE);
        if (!simResult.isSuccess()) { return; }
        ItemStack emptyContainer = simResult.getResult();
        ItemStack outputStack = inv.getStackInSlot(INPUT_FUEL_SLOT_EMPTY);
        if (!outputStack.isEmpty() && !ItemStack.isSameItemSameComponents(outputStack, emptyContainer)) { return; }
        if (outputStack.getCount() + emptyContainer.getCount() > emptyContainer.getMaxStackSize()) { return; }
        FluidActionResult execResult = FluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.EXECUTE);
        filledContainer.shrink(1);
        inv.setStackInSlot(INPUT_FUEL_SLOT_FILLED, filledContainer);
        if (outputStack.isEmpty()) { inv.setStackInSlot(INPUT_FUEL_SLOT_EMPTY, execResult.getResult()); }
        else { outputStack.grow(execResult.getResult().getCount()); }
    }

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.register(Capabilities.FluidHandler.BLOCK, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (INPUT_FLUID_POIS.contains(localPos) && (side == null || side == INPUT_FLUID_FACING)) { return state.fluidInputHandler; }
            return null;
        });
        register.register(HeatCapabilities.HEAT_PROVIDER, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (HEAT_OUTPUT_POIS.contains(localPos) && (side == null || side == HEAT_OUTPUT_FACING)) { return state.heatProvider; }
            return null;
        });
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { ITMultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return BoilerLiquidShape.GETTER; }

    public static class State implements IMultiblockState, ITIDisplayContext {
        public final BiFunction<Level, FluidStack, BoilerLiquidRecipe> recipeGetter = ITCachedRecipe.cached(BoilerLiquidRecipe::findRecipe);
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final BoilerTank tanks;
        public final IFluidHandler fluidInputHandler;
        public final IHeatProvider heatProvider;
        public final Supplier<IHeatConsumer> boilerInput;
        public ITSlotwiseItemHandler inventory;
        public double heatLevel = 0;
        public int burnRemaining = 0;
        public BoilerLiquidRecipe lastFuel;
        public double targetHeat = DEFAULT_WORKING_HEAT_LEVEL;
        public double workingHeatLevel = DEFAULT_WORKING_HEAT_LEVEL;
        public boolean pilotLit = false;
        public boolean active = false;
        public BooleanSupplier isSoundPlaying = () -> false;
        public BooleanSupplier isPilotSoundPlaying = () -> false;
        public boolean tanksDirty = false;
        public boolean inventoryDirty = false;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); this.tanksDirty = true; this.inventoryDirty = true; };
            tanks = new BoilerTank(() -> { onChanged.run(); this.tanksDirty = true; });
            inventory = new ITSlotwiseItemHandler(
                    List.of(
                            ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            ITSlotwiseItemHandler.IOConstraint.OUTPUT
                    ),
                    () -> { onChanged.run(); this.inventoryDirty = true; }
            );
            fluidInputHandler = new ITArrayFluidHandler(tanks.input1(), false, true, () -> { onChanged.run(); this.tanksDirty = true; });
            heatProvider = new HeatSourceImpl(this);
            MultiblockFace heatMBFace = new MultiblockFace(HEAT_OUTPUT_FACING, HEAT_OUTPUT_POIS.getFirst());
            CapabilityPosition opposingCP = CapabilityPosition.opposing(heatMBFace);
            MultiblockFace opposingMBFace = new MultiblockFace(opposingCP.side(), opposingCP.posInMultiblock());
            boilerInput = ctx.getCapabilityAt(HeatCapabilities.HEAT_CONSUMER, opposingMBFace);
        }

        public double getWorkingHeatLevel() { return workingHeatLevel; }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.putDouble("heatLevel", heatLevel);
            nbt.putInt("burnRemaining", burnRemaining);
            nbt.putBoolean("pilotLit", pilotLit);
            nbt.putDouble("targetHeat", targetHeat);
            nbt.put("inventory", inventory.serializeNBT(provider));
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            heatLevel = nbt.getDouble("heatLevel");
            burnRemaining = nbt.getInt("burnRemaining");
            pilotLit = nbt.getBoolean("pilotLit");
            targetHeat = nbt.getDouble("targetHeat");
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
            if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display"), provider); }
        }

        @Override public boolean isActive() { return active; }

        @Override public IItemHandlerModifiable getInventory() { return inventory; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input1()}; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("active", active);
            nbt.putDouble("heatLevel", heatLevel);
            nbt.putBoolean("pilotLit", pilotLit);
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.put("inventory", inventory.serializeNBT(provider));
            nbt.putDouble("workingHeatLevel", workingHeatLevel);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            active = nbt.getBoolean("active");
            heatLevel = nbt.getDouble("heatLevel");
            pilotLit = nbt.getBoolean("pilotLit");
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
            workingHeatLevel = nbt.getDouble("workingHeatLevel");
            tanksDirty = false;
            inventoryDirty = false;
        }
    }

    private record HeatSourceImpl(State state) implements IHeatProvider {
        @Override public double getHeatLevel() { return state.heatLevel; }
    }

    public record BoilerTank(ITMarkableFluidTank input1) {
        public BoilerTank(Runnable markDirty) { this(new ITMarkableFluidTank(ITServerConfig.boilerLiquidTankCapacity, v -> markDirty.run())); }

        public static BoilerTank makeClient() { return new BoilerTank(() -> {}); }

        public CompoundTag toNBT(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.put("input1", this.input1().writeToNBT(provider, new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag, HolderLookup.Provider provider) { this.input1().readFromNBT(provider, tag.getCompound("input1")); }

        @SuppressWarnings("unused")
        public int getCapacity() { return ITServerConfig.boilerLiquidTankCapacity; }
    }
}
