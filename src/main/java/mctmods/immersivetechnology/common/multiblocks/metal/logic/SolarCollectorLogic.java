package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.util.EnergyPush;
import mctmods.immersivetechnology.core.util.IDisplaySyncState;
import mctmods.immersivetechnology.core.util.MirrorAnimation;
import mctmods.immersivetechnology.core.util.SunExposure;
import mctmods.immersivetechnology.core.util.SunTracking;

import com.immersiveconvergence.api.energy.ICEnergyStorage;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.multiblock.blocks.component.IClientTickableComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.IMultiblockComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.IServerTickableComponent;
import com.immersiveconvergence.api.multiblock.blocks.env.IInitialMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockLevel;
import com.immersiveconvergence.api.multiblock.blocks.logic.IMultiblockLogic;
import com.immersiveconvergence.api.multiblock.blocks.util.RelativeBlockFace;
import com.immersiveconvergence.api.multiblock.blocks.util.ShapeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class SolarCollectorLogic implements IMultiblockLogic<SolarCollectorLogic.State>, IServerTickableComponent<SolarCollectorLogic.State>, IClientTickableComponent<SolarCollectorLogic.State> {
    private static final Vec3 PANEL_PIVOT = new Vec3(1.5, 2.57, 1.5);
    private static final BlockPos LOWER_CORNER = BlockPos.ZERO;
    private static final BlockPos UPPER_CORNER = new BlockPos(2, 2, 2);
    private static List<BlockPos> ENERGY_POIS;
    private static RelativeBlockFace ENERGY_FACING;

    static { ITShapes.readPois("solar_collector", SolarCollectorLogic::loadPois); }

    public static int energyPerTick(int section) { return section * ServerConfig.solarCollectorEnergyPerSection; }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        Level level = ctx.getLevel().getRawLevel();
        Vec3 sun = SunTracking.sunVector(level);
        float rotation = SunTracking.supportRotation(ctx.getLevel().getOrientation().front(), 1, 0);
        ctx.getState().animation.track(SunTracking.isSunUp(level), rotation, SunTracking.dayArcTilt(sun));
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        IMultiblockLevel mlevel = ctx.getLevel();
        Level level = mlevel.getRawLevel();
        int section = level.isRaining() ? 0 : SunTracking.incidenceSection(level);
        if (section > 0 && mlevel.shouldTickModulo(20)) { state.shaded = SunExposure.shaded(level, mlevel.toAbsolute(PANEL_PIVOT), SunTracking.sunVector(level), BoundingBox.fromCorners(mlevel.toAbsolute(LOWER_CORNER), mlevel.toAbsolute(UPPER_CORNER))); }
        int generated = section > 0 && !state.shaded ? energyPerTick(section) : 0;
        int before = state.energy.getEnergyStored();
        if (generated > 0) { state.energy.setStoredEnergy(before + generated); }
        pushEnergy(state, ctx, level);
        boolean wasActive = state.active;
        state.active = generated > 0;
        if (wasActive != state.active) { ctx.markDirtyAndSync(); }
        else if (before != state.energy.getEnergyStored()) { ctx.markMasterDirty(); }
    }

    private static void pushEnergy(State state, IMultiblockContext<State> ctx, Level level) {
        int available = state.energy.extractEnergy(Integer.MAX_VALUE, true);
        if (available <= 0) { return; }
        List<IEnergyStorage> connected = new ArrayList<>();
        EnergyPush.addConnectedHandlers(connected, ctx, level, ENERGY_POIS, ENERGY_FACING);
        if (!connected.isEmpty()) { state.energy.extractEnergy(EnergyPush.distributeFluxProper(connected, available), false); }
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get("solar_collector").getter; }

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.register(Capabilities.EnergyStorage.BLOCK, (state, position) -> {
            RelativeBlockFace side = position.side();
            if (ENERGY_POIS.contains(position.posInMultiblock()) && (side == null || side == ENERGY_FACING)) { return state.energy; }
            return null;
        });
    }

    public static class State implements IDisplaySyncState {
        public final ICEnergyStorage energy;
        public final MirrorAnimation animation = new MirrorAnimation(MirrorAnimation.IDLE);
        public boolean active = false;
        private boolean shaded = false;

        public State() {
            int capacity = 4 * ServerConfig.solarCollectorEnergyPerSection * 20;
            this.energy = new ICEnergyStorage(capacity, 0, capacity);
        }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.put("energy", energy.serializeNBT(provider));
            nbt.putBoolean("active", active);
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            Tag stored = nbt.get("energy");
            if (stored != null) { energy.deserializeNBT(provider, stored); }
            active = nbt.getBoolean("active");
        }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) { nbt.putBoolean("active", active); }

        @Override public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) { active = nbt.getBoolean("active"); }

        @Override public boolean isActive() { return active; }

        @Override public IEnergyStorage getEnergyStorage() { return energy; }
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        ENERGY_POIS = MultiblockPOIHelper.getPosList(pois, "energy0");
        ENERGY_FACING = MultiblockPOIHelper.getFacing(pois, "energy0");
    }
}
