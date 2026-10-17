package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.client.util.ClientUtils;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.ClientConfig;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.util.IDisplaySyncState;
import mctmods.immersivetechnology.core.util.MirrorAnimation;
import mctmods.immersivetechnology.core.util.SunTracking;
import mctmods.immersivetechnology.core.util.solarregistry.SolarRegistry;

import com.immersiveconvergence.api.multiblock.blocks.component.IClientTickableComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.IServerTickableComponent;
import com.immersiveconvergence.api.multiblock.blocks.env.IInitialMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.logic.IMultiblockLogic;
import com.immersiveconvergence.api.multiblock.blocks.util.MultiblockOrientation;
import com.immersiveconvergence.api.multiblock.blocks.util.ShapeType;
import com.immersiveconvergence.api.multiblock.blockimpl.InitialMultiblockContext;
import com.immersiveconvergence.api.client.MachineSound;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
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
        MirrorAnimation anim = state.animation;
        if (!specialRender) {
            if (state.isMirrorTaken) {
                anim.supportRotation = state.computeTargetSupportRotation();
                anim.tilt = state.computeTargetMirrorTilt();
            }
            else {
                anim.supportRotation = 0;
                anim.tilt = 0;
            }
            anim.stop();
            anim.phase = state.isMirrorTaken ? MirrorAnimation.TRACK : MirrorAnimation.IDLE;
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
        if (!state.isMirrorTaken && anim.phase != targetPhase) {
            int oldPhase = anim.phase;
            anim.phase = targetPhase;
            if (anim.phase == -2 && oldPhase != -3) {
                state.entryDanceTick = gameTime;
                state.entry_supportRotation = anim.supportRotation;
                state.entry_mirrorTilt = anim.tilt;
                anim.baseRotation = SunTracking.wrap360(state.entry_supportRotation);
                state.danceSoundStarted = false;
                state.prevDancePhase = 0;
            }
            else if (anim.phase == MirrorAnimation.IDLE) { anim.baseRotation = SunTracking.wrap360(anim.supportRotation); }
        }
        if (state.danceStartTick != state.prevDanceStartTick) {
            state.prevDanceStartTick = state.danceStartTick;
            state.danceSoundId++;
        }
        if (state.isMirrorTaken != state.prev_isMirrorTaken || !state.towerCollectorPosition.equals(state.prev_towerCollectorPosition)) {
            if (state.isMirrorTaken) { state.tweenToTower(); }
            else if (targetPhase == MirrorAnimation.IDLE) { anim.startTween(0, -anim.tilt, MirrorAnimation.TO_IDLE); }
            state.prev_isMirrorTaken = state.isMirrorTaken;
            state.prev_towerCollectorPosition = state.towerCollectorPosition;
        }
        if (anim.tweening()) {
            anim.stepTween();
            if (!anim.tweening()) {
                if (anim.phase == MirrorAnimation.TO_IDLE && state.isMirrorTaken) { state.tweenToTower(); }
                else if (anim.phase == MirrorAnimation.ROTATE) { anim.startTween(0, state.computeTargetMirrorTilt() - anim.tilt, MirrorAnimation.TILT); }
                else if (anim.phase == MirrorAnimation.TILT) { anim.phase = MirrorAnimation.TRACK; }
            }
        }
        else if (anim.phase == -5 || anim.phase == MirrorAnimation.IDLE) { anim.spin(); }
        else if (anim.phase == -2 || anim.phase == -3) {
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
            else if (anim.phase == -3) { fade *= Mth.clamp(1 - (currentDancePhase - danceDuration), 0, 1); }
            double baseBeatSin = Math.sin(currentDancePhase * baseFreq);
            double doubleBeat = Math.sin(currentDancePhase * 4.18);
            double tripleBeat = Math.sin(currentDancePhase * 6.27);
            double swayCos = Math.cos(currentDancePhase * 1.05);
            double rotSway = Math.sin(currentDancePhase * 0.52) * 60 * fade;
            double rotTwist = Math.cos(currentDancePhase * 2.09) * 30 * fade;
            double rotFlair = tripleBeat * 10 * fade;
            float target_mirrorTilt = (float) ((baseBeatSin * 20 + doubleBeat * 10 + tripleBeat * 15 + swayCos * 25) * fade);
            float target_supportRotation = (float) (anim.baseRotation + rotSway + rotTwist + rotFlair);
            float entryCurrent = state.entryDanceTick < 0 ? currentDancePhase : (gameTime - state.entryDanceTick) * 0.05f;
            float localFade = Mth.clamp(entryCurrent / 3f, 0, 1);
            anim.tilt = state.entry_mirrorTilt * (1 - localFade) + target_mirrorTilt * localFade;
            anim.supportRotation = state.entry_supportRotation * (1 - localFade) + target_supportRotation * localFade;
            anim.tilt = Mth.clamp(anim.tilt, -50, 50);
            if (currentDancePhase >= 0) { anim.baseRotation = SunTracking.wrap360(anim.baseRotation + (float) (0.5 + Math.abs(baseBeatSin) * 1.5 * fade)); }
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
                    state.isDanceSoundPlaying = MachineSound.startSound(() -> (anim.phase == -2 || anim.phase == -3) &&state.danceSoundId == thisId, ctx.isValid(), soundPos, Sounds.dance, isLoop, () -> danceVolume(state, soundPos, isLoop), () -> 1f);
                    state.danceSoundStarted = true;
                }
            }
        }
        if (state.isMirrorTaken && anim.phase == MirrorAnimation.TRACK) { spawnBeamParticle(ctx, state); }
    }

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
            if (state.animation.phase == -3) { f *= Mth.clamp(1 - (cdp - dd), 0, 1); }
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
            ctx.markDirtyAndSync();
        }
        if (state.loadTicks > 20 && state.reAttachOnLoad && !level.isClientSide) {
            state.reAttachOnLoad = false;
            if (level.isLoaded(state.towerCollectorPosition)) {
                if (SolarReceiverLogic.stateAt(level, state.towerCollectorPosition) instanceof SolarReceiverLogic.ReceiverState<?> receiver && receiver.registered) { state.setTowerCollectorPosition(state.towerCollectorPosition); }
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
        if (changed || update) { ctx.markDirtyAndSync(); }
        if (!state.isMirrorTaken) { SolarRegistry.updateDance(level); }
    }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get("solar_reflector").getter; }

    @Override public State createInitialState(IInitialMultiblockContext<State> context) { return new State(context); }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) {
        Level level = state.levelSupplier.get();
        if (level != null && !level.isClientSide) { SolarRegistry.unregisterReflector(level, state.poiPos); }
    }

    public static class State implements IDisplaySyncState {
        public boolean isMirrorTaken;
        private BlockPos towerCollectorPosition;
        public final MirrorAnimation animation = new MirrorAnimation(MirrorAnimation.ROTATE);
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
            this.isMirrorTaken = false;
            this.prev_isMirrorTaken = false;
            this.prev_towerCollectorPosition = this.towerCollectorPosition;
        }

        private void tweenToTower() { animation.startTween(SunTracking.shortestTurn(animation.supportRotation, computeTargetSupportRotation()), 0, MirrorAnimation.ROTATE); }

        private float computeTargetSupportRotation() { return SunTracking.supportRotation(facing, towerCollectorPosition.getX() - poiPos.getX(), towerCollectorPosition.getZ() - poiPos.getZ()); }

        private float computeTargetMirrorTilt() {
            int dx = towerCollectorPosition.getX() - poiPos.getX();
            int dy = towerCollectorPosition.getY() - poiPos.getY();
            int dz = towerCollectorPosition.getZ() - poiPos.getZ();
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            double dist = Math.sqrt(horizontal * horizontal + dy * dy);
            if (dist == 0) { return 0; }
            return SunTracking.tilt(dx / dist, -1 - dy / dist, dz / dist);
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

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("isMirrorTaken", isMirrorTaken);
            nbt.putLong("towerCollectorPosition", towerCollectorPosition.asLong());
            nbt.putBoolean("active", active);
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            isMirrorTaken = nbt.getBoolean("isMirrorTaken");
            towerCollectorPosition = BlockPos.of(nbt.getLong("towerCollectorPosition"));
            active = nbt.getBoolean("active");
            reAttachOnLoad = nbt.getBoolean("isMirrorTaken");
            initialized = false;
            Level level = levelSupplier.get();
            if (level != null && !level.isClientSide) { SolarRegistry.registerReflector(level, poiPos); }
        }

        @Override public boolean isActive() { return active; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("isMirrorTaken", isMirrorTaken);
            nbt.putLong("towerCollectorPosition", towerCollectorPosition.asLong());
            nbt.putLong("danceStartTick", danceStartTick);
            nbt.putLong("pendingTick", pendingTick);
            nbt.putInt("globalAnimationPhase", globalAnimationPhase);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            isMirrorTaken = nbt.getBoolean("isMirrorTaken");
            towerCollectorPosition = BlockPos.of(nbt.getLong("towerCollectorPosition"));
            danceStartTick = nbt.getLong("danceStartTick");
            pendingTick = nbt.getLong("pendingTick");
            globalAnimationPhase = nbt.getInt("globalAnimationPhase");
        }
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        DANCE_SOUND_POI = MultiblockPOIHelper.getPosList(pois, "sound0").getFirst();
        LINK_POI = MultiblockPOIHelper.getPosList(pois, "link0").getFirst();
        SUN_POI = MultiblockPOIHelper.getPosList(pois, "sun0").getFirst();
        BEAM_POI = MultiblockPOIHelper.getPosList(pois, "beam0").getFirst();
    }
}
