package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.client.util.ClientUtils;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.ClientConfig;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.util.IDisplaySyncState;
import mctmods.immersivetechnology.core.util.solarregistry.SolarRegistry;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl.InitialMultiblockContext;
import com.immersiveconvergence.api.client.MachineSound;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SolarReflectorLogic implements IMultiblockLogic<SolarReflectorLogic.State>, IServerTickableComponent<SolarReflectorLogic.State>, IClientTickableComponent<SolarReflectorLogic.State> {
    public static BlockPos DANCE_SOUND_POI;
    public static BlockPos LINK_POI;
    public static BlockPos SUN_POI;
    public static BlockPos BEAM_POI;

    static { ITShapes.readPois("solar_reflector", SolarReflectorLogic::loadPois); }

    public static float getDanceDuration() { return (float) ClientConfig.solarReflectorDanceDuration; }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        state.formedTicks++;
        boolean specialRender = ClientConfig.doSpecialRenderSolarReflector;
        if (!specialRender) {
            if (state.isMirrorTaken) {
                state.animation_supportRotation = state.computeTargetSupportRotation();
                state.animation_mirrorTilt = state.computeTargetMirrorTilt();
            }
            else {
                state.animation_supportRotation = 0;
                state.animation_mirrorTilt = 0;
            }
            state.animationTicks = 0;
            state.animationPhase = state.isMirrorTaken ? 2 : -4;
            if (state.isMirrorTaken) { spawnBeamParticle(ctx, state); }
            return;
        }
        float baseFreq = (float) ClientConfig.solarReflectorBaseFrequency;
        float danceDuration = (float) ClientConfig.solarReflectorDanceDuration;
        boolean isDisabled = ClientConfig.disableReflectorDance;
        boolean isLoop = ClientConfig.loopReflectorDance;
        long gameTime = ctx.getLevel().getRawLevel().getGameTime();
        int targetPhase;
        if (state.isMirrorTaken) { targetPhase = -4; }
        else {
            if (isDisabled) { targetPhase = -4; }
            else if (state.pendingTick > 0 && gameTime < state.pendingTick) { targetPhase = -5; }
            else if (state.danceStartTick > 0) {
                if (isLoop) { targetPhase = -2; }
                else { targetPhase = state.globalAnimationPhase; }
            }
            else { targetPhase = -4; }
        }
        if (!state.isMirrorTaken && state.animationPhase != targetPhase) {
            int oldPhase = state.animationPhase;
            state.animationPhase = targetPhase;
            if (state.animationPhase == -2 && oldPhase != -3) {
                state.entryDanceTick = gameTime;
                state.entry_supportRotation = state.animation_supportRotation;
                state.entry_mirrorTilt = state.animation_mirrorTilt;
                state.baseRotation = wrap360(state.entry_supportRotation);
                state.danceSoundStarted = false;
                state.prevDancePhase = 0;
            }
            else if (state.animationPhase == -4) { state.baseRotation = wrap360(state.animation_supportRotation); }
        }
        if (state.danceStartTick != state.prevDanceStartTick) {
            state.prevDanceStartTick = state.danceStartTick;
            state.danceSoundId++;
        }
        if (state.isMirrorTaken != state.prev_isMirrorTaken || !state.towerCollectorPosition.equals(state.prev_towerCollectorPosition)) {
            if (state.isMirrorTaken) { state.tweenToTower(); }
            else if (targetPhase == -4) { state.startTween(0, 0 - state.animation_mirrorTilt, -1); }
            state.prev_isMirrorTaken = state.isMirrorTaken;
            state.prev_towerCollectorPosition = state.towerCollectorPosition;
        }
        if (state.animationTicks > 0) {
            float prog = (state.animation_maxTicks - state.animationTicks) / (float)state.animation_maxTicks;
            float eased = Mth.cos(prog * Mth.PI) * -0.5f + 0.5f;
            state.animation_supportRotation = state.start_supportRotation + state.delta_supportRotation * eased;
            state.animation_mirrorTilt = state.start_mirrorTilt + state.delta_mirrorTilt * eased;
            state.animationTicks--;
            if (state.animationTicks == 0) {
                if (state.animationPhase == -1 && state.isMirrorTaken) { state.tweenToTower(); }
                else if (state.animationPhase == 0) { state.startTween(0, state.computeTargetMirrorTilt() - state.animation_mirrorTilt, 1); }
                else if (state.animationPhase == 1) { state.animationPhase = 2; }
            }
        }
        else if (state.animationPhase == -5 || state.animationPhase == -4) {
            state.baseRotation = wrap360(state.baseRotation + 0.5f);
            state.animation_supportRotation = state.baseRotation;
            state.animation_mirrorTilt = 0;
        }
        else if (state.animationPhase == -2 || state.animationPhase == -3) {
            float currentDancePhase = (gameTime - state.danceStartTick) * 0.05f;
            if (isLoop) {
                currentDancePhase = ((currentDancePhase % danceDuration) + danceDuration) % danceDuration;
                if (currentDancePhase < state.prevDancePhase - 0.01f) {
                    state.danceSoundId++;
                    state.danceSoundStarted = false;
                }
            }
            state.prevDancePhase = currentDancePhase;
            float fade = Mth.clamp(currentDancePhase / 3f, 0, 1);
            if (isLoop) { fade = Math.min(fade, Mth.clamp((danceDuration - currentDancePhase) / 3f, 0, 1)); }
            else if (state.animationPhase == -3) { fade *= Mth.clamp(1 - (currentDancePhase - danceDuration), 0, 1); }
            double baseBeatSin = Math.sin(currentDancePhase * baseFreq);
            double doubleBeat = Math.sin(currentDancePhase * 4.18);
            double tripleBeat = Math.sin(currentDancePhase * 6.27);
            double swayCos = Math.cos(currentDancePhase * 1.05);
            double rotSway = Math.sin(currentDancePhase * 0.52) * 60 * fade;
            double rotTwist = Math.cos(currentDancePhase * 2.09) * 30 * fade;
            double rotFlair = tripleBeat * 10 * fade;
            float target_mirrorTilt = (float) ((baseBeatSin * 20 + doubleBeat * 10 + tripleBeat * 15 + swayCos * 25) * fade);
            float target_supportRotation = (float) (state.baseRotation + rotSway + rotTwist + rotFlair);
            float entryCurrent = state.entryDanceTick < 0 ? currentDancePhase : (gameTime - state.entryDanceTick) * 0.05f;
            float localFade = Mth.clamp(entryCurrent / 3f, 0, 1);
            state.animation_mirrorTilt = state.entry_mirrorTilt * (1 - localFade) + target_mirrorTilt * localFade;
            state.animation_supportRotation = state.entry_supportRotation * (1 - localFade) + target_supportRotation * localFade;
            state.animation_mirrorTilt = Mth.clamp(state.animation_mirrorTilt, -50, 50);
            if (currentDancePhase >= 0) { state.baseRotation = wrap360(state.baseRotation + (float) (0.5 + Math.abs(baseBeatSin) * 1.5 * fade)); }
            if (!state.isDanceSoundPlaying.getAsBoolean() && !state.danceSoundStarted) {
                long durationTicks = Math.round(danceDuration / 0.05f);
                long elapsed = gameTime - state.danceStartTick;
                if (isLoop) {
                    elapsed %= durationTicks;
                    if (elapsed < 0) { elapsed += durationTicks; }
                }
                if (elapsed >= 0 && elapsed < 180) {
                    Vec3 soundPos = ctx.getLevel().toAbsolute(Vec3.atCenterOf(DANCE_SOUND_POI));
                    int thisId = ++state.danceSoundId;
                    state.isDanceSoundPlaying = MachineSound.startSound(() -> (state.animationPhase == -2 || state.animationPhase == -3) && state.danceSoundId == thisId, ctx.isValid(), soundPos, Sounds.dance, isLoop, () -> danceVolume(state, soundPos, isLoop), () -> 1f);
                    state.danceSoundStarted = true;
                }
            }
        }
        if (state.isMirrorTaken && state.animationPhase == 2) { spawnBeamParticle(ctx, state); }
    }

    private static float wrap360(float degrees) { return (degrees % 360 + 360) % 360; }

    private static float danceVolume(State state, Vec3 soundPos, boolean isLoop) {
        long gt = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0;
        float dd = (float) ClientConfig.solarReflectorDanceDuration;
        float cdp = (gt - state.danceStartTick) * 0.05f;
        if (cdp < 0) { return 0f; }
        float f;
        if (isLoop) {
            cdp = ((cdp % dd) + dd) % dd;
            f = Math.min(Mth.clamp(cdp / 3f, 0, 1), Mth.clamp((dd - cdp) / 3f, 0, 1));
        }
        else {
            if (cdp > dd + 1f) { return 0f; }
            f = Mth.clamp(cdp / 3f, 0, 1);
            if (state.animationPhase == -3) { f *= Mth.clamp(1 - (cdp - dd), 0, 1); }
        }
        return ClientUtils.attenuated(soundPos, 32, 0.05f + f * 0.45f);
    }

    private static void spawnBeamParticle(IMultiblockContext<State> ctx, State state) {
        if (state.getSolarCollectorStrength() <= 0) { return; }
        Level level = ctx.getLevel().getRawLevel();
        if (level.random.nextFloat() >= 0.04f) { return; }
        Vec3 start = ctx.getLevel().toAbsolute(Vec3.atCenterOf(BEAM_POI));
        Vec3 diff = Vec3.atCenterOf(state.getTowerCollectorPosition()).subtract(start);
        double dist = diff.length();
        if (dist * dist > 64 * 64) { return; }
        Vec3 dir = diff.normalize();
        Vec3 pos = start.add(dir.scale(level.random.nextDouble() * dist * 0.9));
        Vec3 vel = dir.scale(0.08 + level.random.nextDouble() * 0.05);
        Vec3 perp1 = dir.cross(new Vec3(0, 1, 0)).normalize().scale(level.random.nextGaussian() * 0.005);
        Vec3 perp2 = dir.cross(perp1).normalize().scale(level.random.nextGaussian() * 0.005);
        vel = vel.add(perp1).add(perp2);
        level.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        Level level = ctx.getLevel().getRawLevel();
        boolean update = false;
        state.loadTicks++;
        if (state.loadTicks > 10 && !state.initialized && !level.isClientSide) {
            state.initialized = true;
            SolarRegistry.registerReflector(level, state.poiPos);
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
        if (state.loadTicks > 20 && state.reAttachOnLoad && !level.isClientSide) {
            state.reAttachOnLoad = false;
            if (level.isLoaded(state.towerCollectorPosition)) {
                if (SolarCollectorLogic.stateAt(level, state.towerCollectorPosition) instanceof SolarCollectorLogic.CollectorState<?> collector && collector.registered) { state.setTowerCollectorPosition(state.towerCollectorPosition); }
                else {
                    state.isMirrorTaken = false;
                    state.towerCollectorPosition = state.poiPos;
                    SolarRegistry.notifyTaken(level, state.poiPos, false);
                }
                update = true;
            }
        }
        boolean isActive = state.isMirrorTaken && state.getSolarCollectorStrength() > 0;
        if (state.active != isActive) {
            state.active = isActive;
            update = true;
        }
        long oldDanceStartTick = state.danceStartTick;
        long oldPendingTick = state.pendingTick;
        int oldGlobalAnimationPhase = state.globalAnimationPhase;
        BlockPos root = state.isMirrorTaken ? null : SolarRegistry.findRoot(level, state.poiPos);
        SolarRegistry.GroupData gd = root != null ? SolarRegistry.getGroupData(level, root) : null;
        if (gd != null) {
            state.danceStartTick = gd.danceStartTick;
            state.pendingTick = gd.pendingTick;
            state.globalAnimationPhase = gd.animationPhase;
        }
        else if (state.isMirrorTaken || root != null) {
            state.danceStartTick = -1;
            state.pendingTick = -1;
            state.globalAnimationPhase = -4;
        }
        boolean changed = state.danceStartTick != oldDanceStartTick || state.pendingTick != oldPendingTick || state.globalAnimationPhase != oldGlobalAnimationPhase;
        if (changed || update) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
        if (!state.isMirrorTaken) { SolarRegistry.updateDance(level); }
    }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) { return LazyOptional.empty(); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get("solar_reflector").getter; }

    @Override public State createInitialState(IInitialMultiblockContext<State> context) { return new State(context); }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) {
        Level level = state.levelSupplier.get();
        if (level != null && !level.isClientSide) { SolarRegistry.unregisterReflector(level, state.poiPos); }
    }

    public static class State implements IDisplaySyncState {
        public boolean isMirrorTaken;
        private BlockPos towerCollectorPosition;
        public float animation_supportRotation;
        public float animation_mirrorTilt;
        public int animationTicks = 0;
        public int animationPhase = 0;
        public float baseRotation;
        private final Direction facing;
        public final BlockPos poiPos;
        public BlockPos sunPos;
        private final Supplier<Level> levelSupplier;
        private final Runnable markDirty;
        private final Runnable sync;
        public boolean initialized = false;
        public boolean active = false;
        public BooleanSupplier isDanceSoundPlaying = () -> false;
        private transient int danceSoundId = 0;
        public transient int formedTicks = 0;
        private transient float start_supportRotation;
        private transient float delta_supportRotation;
        private transient float start_mirrorTilt;
        private transient float delta_mirrorTilt;
        private transient int animation_maxTicks;
        private transient boolean prev_isMirrorTaken;
        private transient BlockPos prev_towerCollectorPosition;
        private transient long danceStartTick = -1;
        private transient long pendingTick = -1L;
        private transient int globalAnimationPhase = -4;
        private transient long entryDanceTick = -1;
        private transient float entry_supportRotation;
        private transient float entry_mirrorTilt;
        private transient float prevDancePhase = 0;
        private transient long prevDanceStartTick = -1;
        private transient boolean danceSoundStarted = false;
        private int loadTicks = 0;
        private boolean reAttachOnLoad = false;

        public State(IInitialMultiblockContext<State> context) {
            InitialMultiblockContext<State> initialContext = (InitialMultiblockContext<State>) context;
            MultiblockOrientation orientation = initialContext.orientation();
            this.facing = orientation.front();
            BlockPos masterOffset = initialContext.masterOffset();
            BlockPos masterPos = initialContext.masterBE().getBlockPos();
            BlockPos origin = masterPos.subtract(orientation.getAbsoluteOffset(masterOffset));
            this.poiPos = origin.offset(orientation.getAbsoluteOffset(LINK_POI));
            this.sunPos = origin.offset(orientation.getAbsoluteOffset(SUN_POI));
            this.towerCollectorPosition = this.poiPos;
            this.levelSupplier = context.levelSupplier();
            this.markDirty = context.getMarkDirtyRunnable();
            this.sync = context.getSyncRunnable();
            this.animation_supportRotation = 0;
            this.animation_mirrorTilt = 0;
            this.isMirrorTaken = false;
            this.baseRotation = 0;
            this.prev_isMirrorTaken = false;
            this.prev_towerCollectorPosition = this.towerCollectorPosition;
        }

        private void startTween(float deltaRotation, float deltaTilt, int phase) {
            start_supportRotation = animation_supportRotation;
            delta_supportRotation = deltaRotation;
            start_mirrorTilt = animation_mirrorTilt;
            delta_mirrorTilt = deltaTilt;
            animation_maxTicks = 60;
            animationTicks = 60;
            animationPhase = phase;
        }

        private void tweenToTower() { startTween((computeTargetSupportRotation() - animation_supportRotation + 540) % 360 - 180, 0, 0); }

        private float computeTargetSupportRotation() {
            int dx = towerCollectorPosition.getX() - poiPos.getX();
            int dz = towerCollectorPosition.getZ() - poiPos.getZ();
            float absoluteYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90);
            absoluteYaw = (absoluteYaw + 360) % 360;
            float mbRotation = facing.get2DDataValue() * 90f;
            float target = mbRotation - absoluteYaw + 180f;
            return (target + 720) % 360;
        }

        private float computeTargetMirrorTilt() {
            int dx = towerCollectorPosition.getX() - poiPos.getX();
            int dy = towerCollectorPosition.getY() - poiPos.getY();
            int dz = towerCollectorPosition.getZ() - poiPos.getZ();
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            double dist = Math.sqrt(horizontal * horizontal + dy * dy);
            if (dist == 0) { return 0; }
            double diffX = dx / dist;
            double diffY = -1 - dy / dist;
            double diffZ = dz / dist;
            double len = Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);
            if (len == 0) { return 0; }
            return (float) (90 - Math.toDegrees(Math.asin(Math.abs(diffY / len))));
        }

        public BlockPos getTowerCollectorPosition() { return towerCollectorPosition; }

        public double getSolarCollectorStrength() {
            Level level = levelSupplier.get();
            if (level == null) { return 0; }
            int numClear = 0;
            Direction right = facing.getClockWise();
            Direction back = facing.getOpposite();
            BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
            int baseX = sunPos.getX(), baseY = sunPos.getY() + 1, baseZ = sunPos.getZ();
            for (int l = -1; l < 2; l++) {
                for (int w = -1; w < 2; w++) {
                    checkPos.set(baseX + back.getStepX() * l + right.getStepX() * w, baseY, baseZ + back.getStepZ() * l + right.getStepZ() * w);
                    if (level.canSeeSky(checkPos)) { numClear++; }
                }
            }
            return numClear / 9.0;
        }

        public boolean setTowerCollectorPosition(BlockPos position) {
            int dx = position.getX() - poiPos.getX();
            int dz = position.getZ() - poiPos.getZ();
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            if (horizontal == 0) { return false; }
            if (!isMirrorTaken) {
                towerCollectorPosition = position;
                isMirrorTaken = true;
                Level level = levelSupplier.get();
                if (level != null) { SolarRegistry.notifyTaken(level, poiPos, true); }
                markDirty.run();
                sync.run();
            }
            return towerCollectorPosition.equals(position);
        }

        public void detachTower(BlockPos position) {
            if (towerCollectorPosition.equals(position)) {
                isMirrorTaken = false;
                towerCollectorPosition = poiPos;
                Level level = levelSupplier.get();
                if (level != null) { SolarRegistry.notifyTaken(level, poiPos, false); }
                markDirty.run();
                sync.run();
            }
        }

        @Override public void writeSaveNBT(CompoundTag nbt) {
            nbt.putBoolean("isMirrorTaken", isMirrorTaken);
            nbt.putLong("towerCollectorPosition", towerCollectorPosition.asLong());
            nbt.putBoolean("active", active);
        }

        @Override public void readSaveNBT(CompoundTag nbt) {
            isMirrorTaken = nbt.getBoolean("isMirrorTaken");
            towerCollectorPosition = BlockPos.of(nbt.getLong("towerCollectorPosition"));
            active = nbt.getBoolean("active");
            reAttachOnLoad = nbt.getBoolean("isMirrorTaken");
            initialized = false;
            Level level = levelSupplier.get();
            if (level != null && !level.isClientSide) { SolarRegistry.registerReflector(level, poiPos); }
        }

        @Override public boolean isActive() { return active; }

        @Override public IItemHandlerModifiable getInventory() { return null; }

        @Override public IFluidTank[] getInternalTanks() { return null; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("isMirrorTaken", isMirrorTaken);
            nbt.putLong("towerCollectorPosition", towerCollectorPosition.asLong());
            nbt.putLong("danceStartTick", danceStartTick);
            nbt.putLong("pendingTick", pendingTick);
            nbt.putInt("globalAnimationPhase", globalAnimationPhase);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt) {
            isMirrorTaken = nbt.getBoolean("isMirrorTaken");
            towerCollectorPosition = BlockPos.of(nbt.getLong("towerCollectorPosition"));
            danceStartTick = nbt.getLong("danceStartTick");
            pendingTick = nbt.getLong("pendingTick");
            globalAnimationPhase = nbt.getInt("globalAnimationPhase");
        }
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        DANCE_SOUND_POI = MultiblockPOIHelper.getPosList(pois, "sound0").get(0);
        LINK_POI = MultiblockPOIHelper.getPosList(pois, "link0").get(0);
        SUN_POI = MultiblockPOIHelper.getPosList(pois, "sun0").get(0);
        BEAM_POI = MultiblockPOIHelper.getPosList(pois, "beam0").get(0);
    }
}
