package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.config.ServerConfig;
import mctmods.immersivetechnology.core.registration.ModFluids;
import mctmods.immersivetechnology.core.util.IDisplaySyncState;

import com.immersiveconvergence.api.capability.ExhaustCapabilities;
import com.immersiveconvergence.api.capability.IExhaustReceiver;
import com.immersiveconvergence.api.multiblock.IFluidOutputPump;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.multiblock.blocks.component.IClientTickableComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.IMultiblockComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.IServerTickableComponent;
import com.immersiveconvergence.api.multiblock.blocks.env.IInitialMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.logic.IMultiblockLogic;
import com.immersiveconvergence.api.multiblock.blocks.util.CapabilityPosition;
import com.immersiveconvergence.api.multiblock.blocks.util.RelativeBlockFace;
import com.immersiveconvergence.api.multiblock.blocks.util.ShapeType;
import com.immersiveconvergence.api.util.MarkableFluidTank;
import com.immersiveconvergence.api.util.MultiTankFluidHandler;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public class Co2CollectorLogic implements IMultiblockLogic<Co2CollectorLogic.State>, IServerTickableComponent<Co2CollectorLogic.State>, IClientTickableComponent<Co2CollectorLogic.State>, IFluidOutputPump<Co2CollectorLogic.State> {
    private static final int VENT_TICKS = 20;

    public static BlockPos TURBINE_FACE;
    public static List<BlockPos> OUTPUT_POSITIONS;
    private static CapabilityPosition FLUE_OUT;
    private static CapabilityPosition CO2_OUT;
    private static List<RelativeBlockFace> OUTPUT_FACINGS;

    static { ITShapes.readPois("co2_collector", Co2CollectorLogic::loadPois); }

    @Override public List<BlockPos> getOutputPositions() { return OUTPUT_POSITIONS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(FLUE_OUT.side()); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return List.of(state.flue, state.co2); }

    @Override public List<RelativeBlockFace> getOutputFacings() { return OUTPUT_FACINGS; }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        if (!ctx.getLevel().shouldTickModulo(2)) { return; }
        State state = ctx.getState();
        vent(ctx, 0, state.ventFlue, state.flue);
        vent(ctx, 1, state.ventCo2, state.co2);
    }

    private void vent(IMultiblockContext<State> ctx, int index, boolean venting, MarkableFluidTank tank) {
        if (!venting || isOutputConnected(ctx, index)) { return; }
        Direction facing = ctx.getLevel().toAbsolute(OUTPUT_FACINGS.get(index));
        if (facing != null) { ImmersiveTechnology.proxy.exhaust(ctx.getLevel().getRawLevel(), ctx.getLevel().toAbsolute(OUTPUT_POSITIONS.get(index)), facing, 1.0f, tank.getFluid()); }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        pumpOutputs(ctx);
        ctx.getState().tickVents();
    }

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.register(Capabilities.FluidHandler.BLOCK, (state, position) -> {
            if (FLUE_OUT.equals(position)) { return state.flueHandler; }
            return CO2_OUT.equals(position) ? state.co2Handler : null;
        });
        register.register(ExhaustCapabilities.EXHAUST_RECEIVER, (state, position) -> TURBINE_FACE.equals(position.posInMultiblock()) && position.side() == RelativeBlockFace.FRONT ? state : null);
    }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get("co2_collector").getter; }

    public static class State implements IDisplaySyncState, IExhaustReceiver {
        public final MarkableFluidTank flue;
        public final MarkableFluidTank co2;
        public final IFluidHandler flueHandler;
        public final IFluidHandler co2Handler;
        private final Runnable onChanged;
        private int flueVent = 0;
        private int co2Vent = 0;
        public boolean ventFlue = false;
        public boolean ventCo2 = false;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            this.onChanged = () -> { markDirty.run(); sync.run(); };
            this.flue = new MarkableFluidTank(ServerConfig.co2CollectorFlueTankCapacity, v -> onChanged.run());
            this.co2 = new MarkableFluidTank(ServerConfig.co2CollectorCo2TankCapacity, v -> onChanged.run());
            this.flueHandler = new MultiTankFluidHandler(flue, true, false, onChanged);
            this.co2Handler = new MultiTankFluidHandler(co2, true, false, onChanged);
        }

        @Override public void acceptExhaust(@Nullable FluidStack flueIn, int co2In) {
            if (flueIn != null && !flueIn.isEmpty()) {
                FluidStack offered = flueIn.copy();
                if (flue.fill(offered, FluidAction.EXECUTE) < offered.getAmount()) { flueVent = VENT_TICKS; }
            }
            if (co2In > 0) {
                FluidStack offered = new FluidStack(ModFluids.CARBON_DIOXIDE.getStill(), co2In);
                if (co2.fill(offered, FluidAction.EXECUTE) < offered.getAmount()) { co2Vent = VENT_TICKS; }
            }
            refreshVents();
        }

        private void tickVents() {
            if (flueVent > 0) { flueVent--; }
            if (co2Vent > 0) { co2Vent--; }
            refreshVents();
        }

        private void refreshVents() {
            boolean flueNow = flueVent > 0;
            boolean co2Now = co2Vent > 0;
            if (flueNow == ventFlue && co2Now == ventCo2) { return; }
            ventFlue = flueNow;
            ventCo2 = co2Now;
            onChanged.run();
        }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.put("flueTank", flue.writeToNBT(provider, new CompoundTag()));
            nbt.put("co2Tank", co2.writeToNBT(provider, new CompoundTag()));
            nbt.putInt("flueVent", flueVent);
            nbt.putInt("co2Vent", co2Vent);
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            flue.readFromNBT(provider, nbt.getCompound("flueTank"));
            co2.readFromNBT(provider, nbt.getCompound("co2Tank"));
            flueVent = nbt.getInt("flueVent");
            co2Vent = nbt.getInt("co2Vent");
            ventFlue = flueVent > 0;
            ventCo2 = co2Vent > 0;
        }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{flue, co2}; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.put("flueTank", flue.writeToNBT(provider, new CompoundTag()));
            nbt.put("co2Tank", co2.writeToNBT(provider, new CompoundTag()));
            nbt.putBoolean("ventFlue", ventFlue);
            nbt.putBoolean("ventCo2", ventCo2);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            flue.readFromNBT(provider, nbt.getCompound("flueTank"));
            co2.readFromNBT(provider, nbt.getCompound("co2Tank"));
            ventFlue = nbt.getBoolean("ventFlue");
            ventCo2 = nbt.getBoolean("ventCo2");
        }
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        TURBINE_FACE = MultiblockPOIHelper.getPosList(pois, "turbine_face0").getFirst();
        FLUE_OUT = MultiblockPOIHelper.getCapabilityPosition(pois, "flue_output0");
        CO2_OUT = MultiblockPOIHelper.getCapabilityPosition(pois, "co2_output0");
        OUTPUT_POSITIONS = List.of(FLUE_OUT.posInMultiblock(), CO2_OUT.posInMultiblock());
        OUTPUT_FACINGS = List.of(Objects.requireNonNull(FLUE_OUT.side()), Objects.requireNonNull(CO2_OUT.side()));
    }
}
