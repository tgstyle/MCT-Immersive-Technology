package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.client.util.ClientUtils;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerLiquidRecipe;
import mctmods.immersivetechnology.core.CommonConfig;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.util.FluidContainers;
import mctmods.immersivetechnology.core.util.HeatUtils;
import mctmods.immersivetechnology.core.util.IDisplaySyncState;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import com.immersiveconvergence.api.capability.HeatCapabilities;
import com.immersiveconvergence.api.capability.IHeatConsumer;
import com.immersiveconvergence.api.capability.IHeatProvider;
import com.immersiveconvergence.api.client.MachineSound;
import com.immersiveconvergence.api.integration.DisplayLines;
import com.immersiveconvergence.api.multiblock.IDataReloadAware;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.util.ConstrainedItemHandler;
import com.immersiveconvergence.api.util.MarkableFluidTank;
import com.immersiveconvergence.api.util.MultiBlockInventoryUtils;
import com.immersiveconvergence.api.util.MultiTankFluidHandler;
import com.immersiveconvergence.api.util.RecipeCache;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class BoilerLiquidLogic implements IMultiblockLogic<BoilerLiquidLogic.State>, IServerTickableComponent<BoilerLiquidLogic.State>, IClientTickableComponent<BoilerLiquidLogic.State> {
    public static final int INPUT_FUEL_SLOT_FILLED = 0;
    public static final int INPUT_FUEL_SLOT_EMPTY = 1;
    public static BlockPos REDSTONE_POI;
    public static List<BlockPos> COMPARATOR_POSITIONS;
    public static List<BlockPos> IGNITION_POIS;
    public static List<BlockPos> INPUT_FLUID_POIS;
    public static List<BlockPos> HEAT_OUTPUT_POIS;
    public static BlockPos SOUND_POI;
    public static List<BlockPos> EXHAUST_POIS;
    private static RelativeBlockFace INPUT_FLUID_FACING;
    public static RelativeBlockFace HEAT_OUTPUT_FACING;
    public static RelativeBlockFace IGNITION_FACING;

    static { ITShapes.readPois("boiler_liquid", BoilerLiquidLogic::loadPois); }

    public static double defaultWorkingHeatLevel() { return CommonConfig.boilerDefaultWorkingHeat(); }

    public static double pilotHeat() { return ServerConfig.boilerLiquidPilotHeat; }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        if (Minecraft.getInstance().player == null) { return; }
        Vec3 soundPos = Vec3.atCenterOf(ctx.getLevel().toAbsolute(SOUND_POI));
        float vol = ClientUtils.attenuated(soundPos, 8, 2 * (float) (state.heatLevel / state.workingHeatLevel));
        if ((!state.pilotLit || state.heatLevel > pilotHeat()) && state.heatLevel > 0 && vol > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
            state.isSoundPlaying = MachineSound.startSound(() -> (!state.pilotLit || state.heatLevel > pilotHeat()) && state.heatLevel > 0, ctx.isValid(), soundPos, Sounds.boiler_liquid, () -> ClientUtils.attenuated(soundPos, 8, 2 * (float) (state.heatLevel / state.workingHeatLevel)), () -> (float) (state.heatLevel / state.workingHeatLevel));
        }
        if (state.pilotLit && state.heatLevel <= pilotHeat() && state.heatLevel > 0 && vol > 0.01f && !state.isPilotSoundPlaying.getAsBoolean()) {
            state.isPilotSoundPlaying = MachineSound.startSound(() -> state.pilotLit && state.heatLevel <= pilotHeat() && state.heatLevel > 0, ctx.isValid(), soundPos, Sounds.pilot, () -> ClientUtils.attenuated(soundPos, 8, 2 * (float) (pilotHeat() / state.workingHeatLevel)), () -> (float) (pilotHeat() / state.workingHeatLevel));
        }
        if (state.pilotLit) { ClientUtils.boilerExhaust(ctx.getLevel().getRawLevel(), ctx.getLevel().toAbsolute(EXHAUST_POIS.get(0)), state.heatLevel > pilotHeat() && state.rsState.isEnabled(ctx) && HeatUtils.hasWater(state.boilerInput)); }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        Level level = ctx.getLevel().getRawLevel();
        boolean prevTanksDirty = state.tanksDirty;
        boolean prevInventoryDirty = state.inventoryDirty;
        double prevHeatLevel = state.heatLevel;
        boolean prevPilotLit = state.pilotLit;
        boolean wasActive = state.active;
        MarkableFluidTank fuel = state.tanks.input1();
        if (fuel.getFluidAmount() <= 0) { state.pilotLit = false; }
        double delta = ServerConfig.boilerLiquidHeatLossPerTick;
        boolean fullMode = state.rsState.isEnabled(ctx) && HeatUtils.hasWater(state.boilerInput);
        if (state.pilotLit) {
            state.lastFuel = fuel.getFluidAmount() > 0 ? state.recipeGetter.apply(level, new FluidStack(fuel.getFluid(), Integer.MAX_VALUE)) : null;
            if (state.lastFuel != null) {
                state.targetHeat = state.lastFuel.getTargetHeat();
                state.workingHeatLevel = state.targetHeat;
                int drainAmount = state.lastFuel.input.getAmount();
                if (fullMode && fuel.drain(drainAmount, FluidAction.EXECUTE).getAmount() == drainAmount) {
                    if (state.heatLevel < state.targetHeat) { state.heatLevel = Math.min(state.heatLevel + state.lastFuel.getHeatPerTick(), state.targetHeat); }
                    else { state.heatLevel = Math.max(state.heatLevel - delta, state.targetHeat); }
                }
                else if (fuel.drain(1, FluidAction.EXECUTE).getAmount() >= 1) { state.heatLevel = Math.max(state.heatLevel - delta, pilotHeat()); }
                else { state.heatLevel = Math.max(state.heatLevel - delta, 0); }
            }
            else {
                state.pilotLit = false;
                state.heatLevel = Math.max(state.heatLevel - delta, 0);
                state.workingHeatLevel = defaultWorkingHeatLevel();
            }
        }
        else { state.heatLevel = Math.max(state.heatLevel - delta, 0); }
        FluidContainers.emptyBucket(fuel, state.inventory, INPUT_FUEL_SLOT_FILLED, INPUT_FUEL_SLOT_EMPTY);
        state.active = state.pilotLit && fullMode && state.heatLevel >= state.workingHeatLevel;
        int newComparatorValue = state.workingHeatLevel > 0 ? (int) Math.min(15, (15 * state.heatLevel) / state.workingHeatLevel) : 0;
        boolean comparatorChanged = newComparatorValue != state.lastComparatorValue;
        if (comparatorChanged) {
            for (BlockPos pos : COMPARATOR_POSITIONS) { ctx.setComparatorOutputFor(pos, newComparatorValue); }
            state.lastComparatorValue = newComparatorValue;
        }
        if (prevHeatLevel != state.heatLevel || prevPilotLit != state.pilotLit || wasActive != state.active || prevTanksDirty != state.tanksDirty || prevInventoryDirty != state.inventoryDirty || comparatorChanged) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        BlockPos localPos = position.posInMultiblock();
        RelativeBlockFace side = position.side();
        if (cap == ForgeCapabilities.FLUID_HANDLER && INPUT_FLUID_POIS.contains(localPos) && (side == null || side == INPUT_FLUID_FACING)) { return ctx.getState().inputFuelCap.cast(ctx); }
        if (cap == HeatCapabilities.HEAT_PROVIDER_CAPABILITY && HEAT_OUTPUT_POIS.contains(localPos) && (side == null || side == HEAT_OUTPUT_FACING)) { return ctx.getState().heatSourceCap.cast(ctx); }
        return LazyOptional.empty();
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { MultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get("boiler_liquid").getter; }

    public static class State implements IDisplaySyncState, IDataReloadAware {
        public final BiFunction<Level, FluidStack, BoilerLiquidRecipe> recipeGetter = RecipeCache.cached(BoilerLiquidRecipe::findRecipe);
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final BoilerTank tanks;
        public final StoredCapability<IFluidHandler> inputFuelCap;
        public final StoredCapability<IHeatProvider> heatSourceCap = new StoredCapability<>(() -> this.heatLevel);
        public CapabilityReference<IHeatConsumer> boilerInput;
        public final ConstrainedItemHandler inventory;
        public double heatLevel = 0;
        public int lastComparatorValue = -1;
        public BoilerLiquidRecipe lastFuel;
        public double targetHeat = defaultWorkingHeatLevel();
        public double workingHeatLevel = defaultWorkingHeatLevel();
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
            tanks = new BoilerTank(onChanged);
            inventory = new ConstrainedItemHandler(List.of(ConstrainedItemHandler.IOConstraint.FLUID_INPUT, ConstrainedItemHandler.IOConstraint.OUTPUT), onChanged);
            inputFuelCap = new StoredCapability<>(new MultiTankFluidHandler(tanks.input1(), false, true, onChanged));
            bindOutputs(ctx);
        }

        @Override public void onDataReload(IInitialMultiblockContext<?> context) {
            lastComparatorValue = -1;
            bindOutputs(context);
        }

        private void bindOutputs(IInitialMultiblockContext<?> context) { boilerInput = context.getCapabilityAt(HeatCapabilities.HEAT_CONSUMER_CAPABILITY, MultiblockPOIHelper.opposing(HEAT_OUTPUT_FACING, HEAT_OUTPUT_POIS.get(0))); }

        public double getWorkingHeatLevel() { return workingHeatLevel; }

        @Override public void writeSaveNBT(CompoundTag nbt) {
            rsState.writeSaveNBT(nbt);
            nbt.put("tanks", tanks.toNBT());
            nbt.putDouble("heatLevel", heatLevel);
            nbt.putBoolean("pilotLit", pilotLit);
            nbt.putDouble("targetHeat", targetHeat);
            nbt.put("inventory", inventory.serializeNBT());
        }

        @Override public void readSaveNBT(CompoundTag nbt) {
            rsState.readSaveNBT(nbt);
            tanks.readNBT(nbt.getCompound("tanks"));
            heatLevel = nbt.getDouble("heatLevel");
            pilotLit = nbt.getBoolean("pilotLit");
            targetHeat = nbt.getDouble("targetHeat");
            inventory.deserializeNBT(nbt.getCompound("inventory"));
            tanksDirty = false;
            inventoryDirty = false;
        }

        @Override public boolean isActive() { return active; }

        @Override public IItemHandlerModifiable getInventory() { return inventory; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input1()}; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.putDouble("heatLevel", heatLevel);
            nbt.putBoolean("pilotLit", pilotLit);
            nbt.put("tanks", tanks.toNBT());
            nbt.put("inventory", inventory.serializeNBT());
            nbt.putDouble("workingHeatLevel", workingHeatLevel);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt) {
            active = nbt.getBoolean("active");
            heatLevel = nbt.getDouble("heatLevel");
            pilotLit = nbt.getBoolean("pilotLit");
            tanks.readNBT(nbt.getCompound("tanks"));
            inventory.deserializeNBT(nbt.getCompound("inventory"));
            workingHeatLevel = nbt.getDouble("workingHeatLevel");
            tanksDirty = false;
            inventoryDirty = false;
        }

        @Override public void addDisplayLines(Level level, DisplayLines lines) {
            lines.temperature(heatLevel, workingHeatLevel);
            if (tanks.input1().getFluid().isEmpty()) { lines.fuelEmpty(); }
        }
    }

    public record BoilerTank(MarkableFluidTank input1) {
        public BoilerTank(Runnable markDirty) { this(new MarkableFluidTank(ServerConfig.boilerLiquidTankCapacity, v -> markDirty.run())); }

        public static BoilerTank makeClient() { return new BoilerTank(() -> {}); }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("input1", input1.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) { input1.readFromNBT(tag.getCompound("input1")); }
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        REDSTONE_POI = MultiblockPOIHelper.getPosList(pois, "redstone0").get(0);
        COMPARATOR_POSITIONS = MultiblockPOIHelper.getPosList(pois, "comparator0");
        IGNITION_POIS = MultiblockPOIHelper.getPosList(pois, "ignition0");
        INPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_input0");
        HEAT_OUTPUT_POIS = MultiblockPOIHelper.getPosList(pois, "heat_output0");
        SOUND_POI = MultiblockPOIHelper.getPosList(pois, "sound0").get(0);
        EXHAUST_POIS = MultiblockPOIHelper.getPosList(pois, "exhaust0");
        INPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_input0");
        HEAT_OUTPUT_FACING = MultiblockPOIHelper.getFacing(pois, "heat_output0");
        IGNITION_FACING = MultiblockPOIHelper.getFacing(pois, "ignition0");
    }
}
