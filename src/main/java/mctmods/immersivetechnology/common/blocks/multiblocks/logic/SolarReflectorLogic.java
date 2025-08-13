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
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.FullblockShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import java.util.function.Function;
import java.util.function.Supplier;

public class SolarReflectorLogic implements IMultiblockLogic<SolarReflectorLogic.State>, IServerTickableComponent<SolarReflectorLogic.State>, IClientTickableComponent<SolarReflectorLogic.State> {
    @Override
    public State createInitialState(IInitialMultiblockContext<State> context) { return new State(context); }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return FullblockShape.GETTER; }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) { return LazyOptional.empty(); }

    @Override
    public InteractionResult click(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) { return InteractionResult.PASS; }

    @Override
    public void tickServer(IMultiblockContext<State> ctx) { }

    @Override
    public void tickClient(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        if (state.animationTicks > 0) {
            state.animation_supportRotation += state.animation_supportRotationStep;
            state.animation_mirrorTilt += state.animation_mirrorTiltStep;
            state.animationTicks--;
            if (state.animationTicks == 0) {
                state.animation_supportRotationStep = 0;
                state.animation_mirrorTiltStep = 0;
            }
        }
        if (Math.abs(state.animation_supportRotationStep) < 0.001f) state.animation_supportRotationStep = 0;
        if (Math.abs(state.animation_mirrorTiltStep) < 0.001f) state.animation_mirrorTiltStep = 0;
    }

    public static class State implements IMultiblockState {
        private boolean isMirrorTaken = false;
        private BlockPos towerCollectorPosition;
        public float animation_supportRotation = 0;
        public float animation_supportRotationStep = 0;
        public float animation_mirrorTilt = 0;
        public float animation_mirrorTiltStep = 0;
        public int animationTicks = 0;
        private final Direction facing;
        private final BlockPos pos;
        private final Supplier<Level> levelSupplier;
        private final Runnable markDirty;
        private final Runnable sync;

        public State(IInitialMultiblockContext<State> context) {
            InitialMultiblockContext<State> initialContext = (InitialMultiblockContext<State>)context;
            MultiblockOrientation orientation = initialContext.orientation();
            this.facing = orientation.front();
            this.pos = initialContext.masterBE().getBlockPos();
            this.towerCollectorPosition = this.pos;
            this.levelSupplier = context.levelSupplier();
            this.markDirty = context.getMarkDirtyRunnable();
            this.sync = context.getSyncRunnable();
            calculateAnimationRotations();
        }

        public double getSolarCollectorStrength() {
            Level level = levelSupplier.get();
            if (level == null) return 0;
            int numClear = 0;
            Direction right = facing.getClockWise();
            Direction back = facing.getOpposite();
            for (int l = -1; l < 2; l++) {
                for (int w = -1; w < 2; w++) {
                    BlockPos checkPos = pos.relative(Direction.UP, 1).relative(back, l).relative(right, w);
                    if (level.canSeeSky(checkPos)) numClear++;
                }
            }
            return numClear / 9.0;
        }

        public boolean setTowerCollectorPosition(BlockPos position) {
            if (!isMirrorTaken) {
                towerCollectorPosition = position;
                isMirrorTaken = true;
                calculateAnimationRotations();
                markDirty.run();
                sync.run();
            }
            return towerCollectorPosition.equals(position);
        }

        public void detachTower(BlockPos position) {
            if (towerCollectorPosition.equals(position)) {
                isMirrorTaken = false;
                towerCollectorPosition = pos;
                calculateAnimationRotations();
                markDirty.run();
                sync.run();
            }
        }

        private void calculateAnimationRotations() {
            int xdiff = pos.getX() - towerCollectorPosition.getX();
            int ydiff = pos.getY() - towerCollectorPosition.getY();
            int zdiff = pos.getZ() - towerCollectorPosition.getZ();
            double xzdiff = Math.sqrt(xdiff * xdiff + zdiff * zdiff);

            float targetSupportRotation = (float)(Math.atan2(xdiff, zdiff) * 180 / Math.PI) + 90 * (facing.get2DDataValue() + ((facing.getStepX() == 0) ? 0 : 2));
            float targetMirrorTilt = (float) (Math.abs(Math.atan2(ydiff, xzdiff) * 180 / Math.PI) - 90);

            animation_supportRotationStep = (targetSupportRotation - animation_supportRotation) / 9f;
            animation_mirrorTiltStep = (targetMirrorTilt - animation_mirrorTilt) / 9f;
            animationTicks = 9;
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt) {
            nbt.putBoolean("isMirrorTaken", isMirrorTaken);
            nbt.putLong("towerCollectorPosition", towerCollectorPosition.asLong());
            nbt.putFloat("animation_supportRotation", animation_supportRotation);
            nbt.putFloat("animation_supportRotationStep", animation_supportRotationStep);
            nbt.putFloat("animation_mirrorTilt", animation_mirrorTilt);
            nbt.putFloat("animation_mirrorTiltStep", animation_mirrorTiltStep);
            nbt.putInt("animationTicks", animationTicks);
        }

        @Override
        public void readSaveNBT(CompoundTag nbt) {
            isMirrorTaken = nbt.getBoolean("isMirrorTaken");
            towerCollectorPosition = BlockPos.of(nbt.getLong("towerCollectorPosition"));
            animation_supportRotation = nbt.getFloat("animation_supportRotation");
            animation_supportRotationStep = nbt.getFloat("animation_supportRotationStep");
            animation_mirrorTilt = nbt.getFloat("animation_mirrorTilt");
            animation_mirrorTiltStep = nbt.getFloat("animation_mirrorTiltStep");
            animationTicks = nbt.getInt("animationTicks");
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt) {
            writeSaveNBT(nbt);
        }

        @Override
        public void readSyncNBT(CompoundTag nbt) {
            readSaveNBT(nbt);
        }
    }
}
