package mctmods.immersivetechnology.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl.InitialMultiblockContext;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.SolarReflectorShape;
import mctmods.immersivetechnology.common.util.multiblock.MultiblockData;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.common.util.solarregistry.SolarRegistry;
import mctmods.immersivetechnology.core.ITClientConfig;
import mctmods.immersivetechnology.core.lib.ITSound;
import mctmods.immersivetechnology.core.registration.ITSounds;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.SolarReflectorLogic.State;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SolarReflectorLogic implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<SolarReflectorLogic.State> {
    private static final float BASE_FREQ = 2.09f;
    public static final float DANCE_DURATION = 63f;

    private static final MultiblockData DATA = SolarReflectorShape.DATA;

    private static PoIJSONSchema findPOI(String name) {
        for (PoIJSONSchema poi : DATA.pointsOfInterest) { if (poi.name.equals(name)) { return poi; } }
        throw new RuntimeException("Missing POI: " + name);
    }

    public static final BlockPos DANCE_SOUND_POI = new BlockPos(findPOI("sound").x, findPOI("sound").y, findPOI("sound").z);
    public static final BlockPos LINK_POI = new BlockPos(findPOI("link").x, findPOI("link").y, findPOI("link").z);
    private static final BlockPos SUN_POI = new BlockPos(findPOI("sun").x, findPOI("sun").y, findPOI("sun").z);
    public static final BlockPos BEAM_POI = new BlockPos(findPOI("beam").x, findPOI("beam").y, findPOI("beam").z);

    @Override
    public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        state.formedTicks++;
        boolean isDisabled = ITClientConfig.disableReflectorDance.get();
        boolean isLoop = ITClientConfig.loopReflectorDance.get();
        int targetPhase = isDisabled ? -4 : isLoop ? -2 : state.globalAnimationPhase;
        long gameTime = ctx.getLevel().getRawLevel().getGameTime();
        if (!state.isMirrorTaken && state.animationPhase != targetPhase) {
            int oldPhase = state.animationPhase;
            state.animationPhase = targetPhase;
            if (state.animationPhase == -2 && oldPhase != -3) {
                state.entryDanceTick = state.danceStartTick;
                state.entry_supportRotation = state.animation_supportRotation;
                state.entry_mirrorTilt = state.animation_mirrorTilt;
                state.baseRotation = (state.entry_supportRotation % 360 + 360) % 360;
                state.danceSoundStarted = false;
            } else if (state.animationPhase == -4) { state.baseRotation = (state.animation_supportRotation % 360 + 360) % 360; }
        }
        if (state.danceStartTick != state.prevDanceStartTick) {
            state.prevDanceStartTick = state.danceStartTick;
            state.danceSoundId++;
        }
        if (state.isMirrorTaken != state.prev_isMirrorTaken || !state.towerCollectorPosition.equals(state.prev_towerCollectorPosition)) {
            boolean taken = state.isMirrorTaken;
            BlockPos position = state.towerCollectorPosition;
            if (taken) {
                float targetRot = state.computeTargetSupportRotation();
                float delta = targetRot - state.animation_supportRotation;
                delta = (delta + 540) % 360 - 180;
                state.start_supportRotation = state.animation_supportRotation;
                state.delta_supportRotation = delta;
                state.start_mirrorTilt = state.animation_mirrorTilt;
                state.delta_mirrorTilt = 0;
                state.animation_maxTicks = 60;
                state.animationTicks = 60;
                state.animationPhase = 0;
            } else {
                if (targetPhase == -4) {
                    float deltaRot = 0;
                    float deltaTilt = 0 - state.animation_mirrorTilt;
                    state.start_supportRotation = state.animation_supportRotation;
                    state.delta_supportRotation = deltaRot;
                    state.start_mirrorTilt = state.animation_mirrorTilt;
                    state.delta_mirrorTilt = deltaTilt;
                    state.animation_maxTicks = 60;
                    state.animationTicks = 60;
                    state.animationPhase = -1;
                }
            }
            state.prev_isMirrorTaken = taken;
            state.prev_towerCollectorPosition = position;
        }
        if (state.animationTicks > 0) {
            float prog = (state.animation_maxTicks - state.animationTicks) / (float)state.animation_maxTicks;
            float eased = Mth.cos(prog * Mth.PI) * -0.5f + 0.5f;
            state.animation_supportRotation = state.start_supportRotation + state.delta_supportRotation * eased;
            state.animation_mirrorTilt = state.start_mirrorTilt + state.delta_mirrorTilt * eased;
            state.animationTicks--;
            if (state.animationTicks == 0) {
                if (state.animationPhase == -1) {
                    if (state.isMirrorTaken) {
                        float targetRot = state.computeTargetSupportRotation();
                        float delta = targetRot - state.animation_supportRotation;
                        delta = (delta + 540) % 360 - 180;
                        state.start_supportRotation = state.animation_supportRotation;
                        state.delta_supportRotation = delta;
                        state.start_mirrorTilt = state.animation_mirrorTilt;
                        state.delta_mirrorTilt = 0;
                        state.animation_maxTicks = 60;
                        state.animationTicks = 60;
                        state.animationPhase = 0;
                    } else {
                        state.animationPhase = -4;
                        state.baseRotation = (state.animation_supportRotation % 360 + 360) % 360;
                    }
                } else if (state.animationPhase == 0) {
                    float targetTilt = state.computeTargetMirrorTilt();
                    float deltaTilt = targetTilt - state.animation_mirrorTilt;
                    state.start_supportRotation = state.animation_supportRotation;
                    state.delta_supportRotation = 0;
                    state.start_mirrorTilt = state.animation_mirrorTilt;
                    state.delta_mirrorTilt = deltaTilt;
                    state.animation_maxTicks = 60;
                    state.animationTicks = 60;
                    state.animationPhase = 1;
                } else if (state.animationPhase == 1) { state.animationPhase = 2; }
            }
        } else if (state.animationPhase == -2 || state.animationPhase == -3) {
            float currentDancePhase = (gameTime - state.danceStartTick) * 0.05f;
            if (isLoop) {
                currentDancePhase = ((currentDancePhase % DANCE_DURATION) + DANCE_DURATION) % DANCE_DURATION;
                if (currentDancePhase < state.prevDancePhase - 0.01f) { state.danceSoundId++; }
            }
            state.prevDancePhase = currentDancePhase;
            float fade = Mth.clamp(currentDancePhase / 3f, 0, 1);
            if (state.animationPhase == -3) { fade *= Mth.clamp(1 - (currentDancePhase - DANCE_DURATION), 0, 1); }
            if (currentDancePhase < 0) {
                state.baseRotation += 0.5f;
                state.baseRotation = (state.baseRotation % 360 + 360) % 360;
                state.entry_supportRotation = state.baseRotation;
                state.entry_mirrorTilt += (0 - state.entry_mirrorTilt) * 0.1f;
                fade = 0;
            }
            double baseBeatSin = Math.sin(currentDancePhase * BASE_FREQ);
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
            if (currentDancePhase >= 0) {
                state.baseRotation += (float) (0.5 + Math.abs(baseBeatSin) * 1.5 * fade);
                state.baseRotation = (state.baseRotation % 360 + 360) % 360;
            }
            if (!isLoop && currentDancePhase >= DANCE_DURATION) { state.animationPhase = -3; }
            if (currentDancePhase >= DANCE_DURATION + 1) { state.animationPhase = -4; }
            if (!state.isDanceSoundPlaying.getAsBoolean() && !state.danceSoundStarted) {
                long elapsed = gameTime - state.danceStartTick;
                if (elapsed >= 0 && elapsed < 180) {
                    final Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(DANCE_SOUND_POI.getX() + 0.5, DANCE_SOUND_POI.getY() + 0.5, DANCE_SOUND_POI.getZ() + 0.5));
                    state.danceSoundId++;
                    int thisId = state.danceSoundId;
                    state.isDanceSoundPlaying = ITSound.startSound(
                            () -> (state.animationPhase == -2 || state.animationPhase == -3) && state.danceSoundId == thisId, ctx.isValid(), soundPos, ITSounds.dance, isLoop,
                            () -> {
                                LocalPlayer player = Minecraft.getInstance().player;
                                if (player == null) { return 0f; }
                                float attenuation = (float) Math.max(player.distanceToSqr(soundPos) / 32, 1);
                                long gt = 0;
                                if (Minecraft.getInstance().level != null) { gt = Minecraft.getInstance().level.getGameTime(); }
                                float cdp = (gt - state.danceStartTick) * 0.05f;
                                if (cdp < 0) { return 0f; }
                                if (isLoop) { cdp = ((cdp % DANCE_DURATION) + DANCE_DURATION) % DANCE_DURATION; }
                                else { if (cdp > DANCE_DURATION + 1f) { return 0f; } }
                                float f = Mth.clamp(cdp / 3f, 0, 1);
                                float baseVol = 0.05f + f * 0.45f;
                                if (!isLoop && state.animationPhase == -3) { baseVol *= Mth.clamp(1 - (cdp - DANCE_DURATION), 0, 1); }
                                return baseVol / attenuation;
                            },
                            () -> 1f
                    );
                    state.danceSoundStarted = true;
                }
            }
        } else if (state.animationPhase == -4) {
            state.baseRotation += 0.5f;
            state.baseRotation = (state.baseRotation % 360 + 360) % 360;
            state.animation_supportRotation = state.baseRotation;
            state.animation_mirrorTilt = 0;
        }
        if (state.isMirrorTaken && state.animationPhase == 2 && state.getSolarCollectorStrength() > 0) {
            Level level = ctx.getLevel().getRawLevel();
            if (level.random.nextFloat() < 0.04f) {
                Vec3 start = ctx.getLevel().toAbsolute(Vec3.atCenterOf(BEAM_POI));
                Vec3 end = Vec3.atCenterOf(state.getTowerCollectorPosition());
                Vec3 diff = end.subtract(start);
                double dist = diff.length();
                Vec3 dir = diff.normalize();
                double rdist = level.random.nextDouble() * dist * 0.9;
                Vec3 pos = start.add(dir.scale(rdist));
                double speed = 0.08 + level.random.nextDouble() * 0.05;
                Vec3 vel = dir.scale(speed);
                Vec3 perp1 = dir.cross(new Vec3(0, 1, 0)).normalize().scale(level.random.nextGaussian() * 0.005);
                Vec3 perp2 = dir.cross(perp1).normalize().scale(level.random.nextGaussian() * 0.005);
                vel = vel.add(perp1).add(perp2);
                level.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
            }
        }
    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        Level level = ctx.getLevel().getRawLevel();
        boolean update = false;
        if (!state.initialized && !level.isClientSide) {
            state.initialized = true;
            SolarRegistry.registerReflector(level, state.poiPos);
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
        boolean isActive = state.isMirrorTaken && state.getSolarCollectorStrength() > 0;
        if (state.active != isActive) { state.active = isActive; update = true; }
        long oldDanceStartTick = state.danceStartTick;
        int oldGlobalAnimationPhase = state.globalAnimationPhase;
        if (!state.isMirrorTaken) {
            BlockPos root = SolarRegistry.findRoot(level, state.poiPos);
            if (root != null) {
                SolarRegistry.GroupData gd = SolarRegistry.getGroupData(level, root);
                if (gd != null) {
                    state.danceStartTick = gd.danceStartTick;
                    state.globalAnimationPhase = gd.animationPhase;
                } else {
                    state.danceStartTick = -1;
                    state.globalAnimationPhase = -4;
                }
            }
        } else {
            state.danceStartTick = -1;
            state.globalAnimationPhase = -4;
        }
        boolean changed = state.danceStartTick != oldDanceStartTick || state.globalAnimationPhase != oldGlobalAnimationPhase;
        if (changed || update) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
        if (!state.isMirrorTaken) { SolarRegistry.updateDance(level); }
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) { return LazyOptional.empty(); }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return SolarReflectorShape.GETTER; }

    @Override
    public State createInitialState(IInitialMultiblockContext<State> context) { return new State(context); }

    @Override
    public InteractionResult click(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) { return InteractionResult.PASS; }

    @Override
    public void dropExtraItems(State state, Consumer<ItemStack> drop) {
        Level level = state.levelSupplier.get();
        if (level != null && !level.isClientSide) { SolarRegistry.unregisterReflector(level, state.poiPos); }
    }

    public static class State implements IMultiblockState {
        public boolean isMirrorTaken;
        private BlockPos towerCollectorPosition;
        public float animation_supportRotation;
        public float animation_mirrorTilt;
        public int animationTicks = 0;
        public int animationPhase = 0;
        public float baseRotation;
        private final Direction facing;
        public final BlockPos poiPos;
        public final BlockPos sunPos;
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
        private transient int globalAnimationPhase = -4;
        private transient long entryDanceTick = -1;
        private transient float entry_supportRotation;
        private transient float entry_mirrorTilt;
        private transient float prevDancePhase = 0;
        private transient long prevDanceStartTick = -1;
        private transient boolean danceSoundStarted = false;

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
            Level level = levelSupplier.get();
            if (level != null && !level.isClientSide) { SolarRegistry.registerReflector(level, poiPos); }
        }

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
            float elevDeg = (float) Math.toDegrees(Math.atan(Math.abs(dy) / horizontal));
            return 90f - elevDeg;
        }

        public BlockPos getTowerCollectorPosition() { return towerCollectorPosition; }

        public double getSolarCollectorStrength() {
            Level level = levelSupplier.get();
            if (level == null) { return 0; }
            int numClear = 0;
            Direction right = facing.getClockWise();
            Direction back = facing.getOpposite();
            BlockPos baseCheck = sunPos.above();
            for (int l = -1; l < 2; l++) for (int w = -1; w < 2; w++) {
                BlockPos checkPos = baseCheck.relative(back, l).relative(right, w);
                if (level.canSeeSky(checkPos)) { numClear++; }
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

        @Override
        public void writeSaveNBT(CompoundTag nbt) {
            nbt.putBoolean("isMirrorTaken", isMirrorTaken);
            nbt.putLong("towerCollectorPosition", towerCollectorPosition.asLong());
            nbt.putBoolean("initialized", initialized);
            nbt.putBoolean("active", active);
        }

        @Override
        public void readSaveNBT(CompoundTag nbt) {
            isMirrorTaken = nbt.getBoolean("isMirrorTaken");
            towerCollectorPosition = BlockPos.of(nbt.getLong("towerCollectorPosition"));
            initialized = nbt.getBoolean("initialized");
            active = nbt.getBoolean("active");
            Level level = levelSupplier.get();
            if (level != null && !level.isClientSide) { SolarRegistry.registerReflector(level, poiPos); }
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt) {
            writeSaveNBT(nbt);
            nbt.putLong("danceStartTick", danceStartTick);
            nbt.putInt("globalAnimationPhase", globalAnimationPhase);
        }

        @Override
        public void readSyncNBT(CompoundTag nbt) {
            readSaveNBT(nbt);
            danceStartTick = nbt.getLong("danceStartTick");
            globalAnimationPhase = nbt.getInt("globalAnimationPhase");
        }
    }
}
