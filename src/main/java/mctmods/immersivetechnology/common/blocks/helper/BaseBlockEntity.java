package mctmods.immersivetechnology.common.blocks.helper;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Objects;

@SuppressWarnings({"unused","deprecation"})
public abstract class BaseBlockEntity extends BlockEntity implements BlockInterfaces.IBlockStateProvider, IModelOffsetProvider {
    @Nullable private BlockState overrideBlockState = null;
    private final EnumMap<Direction, Integer> redstoneBySide = new EnumMap<>(Direction.class);

    public BaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    @Override public void load(@NotNull CompoundTag nbtIn) {
        super.load(nbtIn);
        readCustomNBT(nbtIn, false);
    }

    public abstract void readCustomNBT(CompoundTag nbt, boolean descPacket);

    @Override protected void saveAdditional(@NotNull CompoundTag nbt) {
        super.saveAdditional(nbt);
        writeCustomNBT(nbt, false);
    }

    public abstract void writeCustomNBT(CompoundTag nbt, boolean descPacket);

    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, be -> {
            CompoundTag nbtTagCompound = new CompoundTag();
            writeCustomNBT(nbtTagCompound, true);
            return nbtTagCompound;
        });
    }

    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag nonNullTag = pkt.getTag() != null ? pkt.getTag() : new CompoundTag();
        readCustomNBT(nonNullTag, true);
    }

    @Override public void handleUpdateTag(CompoundTag tag) { readCustomNBT(tag, true); }

    @Override @NotNull public CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        writeCustomNBT(nbt, true);
        return nbt;
    }

    public void receiveMessageFromClient(CompoundTag message) { }

    public void receiveMessageFromServer(CompoundTag message) { }

    @Override public boolean triggerEvent(int id, int type) {
        if (id == 0 || id == 255) { markContainingBlockForUpdate(null); return true; }
        else if (id == 254) {
            assert level != null;
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, 3);
            return true;
        }
        return super.triggerEvent(id, type);
    }

    public void markContainingBlockForUpdate(@Nullable BlockState newState) { if (level != null) { markBlockForUpdate(getBlockPos(), newState); } }

    public void markBlockForUpdate(BlockPos pos, @Nullable BlockState newState) {
        assert level != null;
        BlockState state = level.getBlockState(pos);
        if (newState == null) { newState = state; }
        level.sendBlockUpdated(pos, state, newState, 3);
        level.updateNeighborsAt(pos, newState.getBlock());
    }

    @Override public void setRemoved() {
        if (!isUnloaded) { setRemovedIE(); }
        super.setRemoved();
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); }

    private boolean isUnloaded = false;

    @Override public void onLoad() {
        super.onLoad();
        isUnloaded = false;
    }

    @Override public void onChunkUnloaded() {
        super.onChunkUnloaded();
        isUnloaded = true;
    }

    public void setRemovedIE() { }

    @Nonnull public Level getLevelNonnull() { return Objects.requireNonNull(super.getLevel()); }

    public void onEntityCollision(Level world, Entity entity) { }

    public void setOverrideState(@Nullable BlockState state) { overrideBlockState = state; }

    @Override @NotNull public BlockState getBlockState() { if (overrideBlockState != null) { return overrideBlockState; } else { return super.getBlockState(); } }

    @Override public void setBlockState(@NotNull BlockState newState) {
        BlockState old = getBlockState();
        super.setBlockState(newState);
        if (getType().isValid(old) && !getType().isValid(newState)) { setOverrideState(old); }
        else if (getType().isValid(newState)) { setOverrideState(null); }
    }

    @Override public void setState(@NotNull BlockState state) { if (getLevelNonnull().getBlockState(worldPosition) == getState()) { getLevelNonnull().setBlockAndUpdate(worldPosition, state); } }

    @Override @NotNull public BlockState getState() { return getBlockState(); }

    protected void markChunkDirty() { if (level != null && level.hasChunk(worldPosition.getX() >> 4, worldPosition.getZ() >> 4)) { level.getChunkAt(worldPosition).setUnsaved(true); } }

    @Override public void setLevel(@NotNull Level world) {
        super.setLevel(world);
        redstoneBySide.clear();
    }

    @Override @NotNull public ModelData getModelData() {
        BlockPos offset = getModelOffset(getState(), Vec3i.ZERO);
        if (offset != null) { return ModelData.builder().with(ModProperties.Model.SUBMODEL_OFFSET, offset).build(); }
        return ModelData.EMPTY;
    }

    @Override
    public BlockPos getModelOffset(BlockState state, Vec3i size) {
        if (!(this instanceof BlockInterfaces.IGeneralMultiblock multi)) { return BlockPos.ZERO; }
        BlockInterfaces.IGeneralMultiblock m = multi.master();
        if (!(m instanceof BlockEntity masterTile)) { return BlockPos.ZERO; }
        return getBlockPos().subtract(masterTile.getBlockPos());
    }

    @Override public void setChanged() {
        if (level != null) {
            markChunkDirty();
            BlockState state = getBlockState();
            if (state.hasAnalogOutputSignal()) { level.updateNeighbourForOutputSignal(worldPosition, state.getBlock()); }
        }
    }

    protected void onNeighborBlockChange(BlockPos otherPos) {
        BlockPos delta = otherPos.subtract(worldPosition);
        Direction side = Direction.getNearest(delta.getX(), delta.getY(), delta.getZ());
        Preconditions.checkNotNull(side);
        updateRSForSide(side);
    }

    private void updateRSForSide(Direction side) {
        int rsStrength = getLevelNonnull().getSignal(worldPosition.relative(side), side);
        if (rsStrength == 0 && this instanceof BlockInterfaces.IRedstoneOutput && ((BlockInterfaces.IRedstoneOutput) this).canConnectRedstone(side)) {
            assert level != null;
            BlockState state = level.getBlockState(worldPosition.relative(side));
            if (state.getBlock() == Blocks.REDSTONE_WIRE && state.getValue(RedStoneWireBlock.POWER) > rsStrength) { rsStrength = state.getValue(RedStoneWireBlock.POWER); }
        }
        redstoneBySide.put(side, rsStrength);
    }

    protected int getRSInput(Direction from) {
        assert level != null;
        if (level.isClientSide || !redstoneBySide.containsKey(from)) { updateRSForSide(from); }
        return redstoneBySide.get(from);
    }

    protected boolean isRSPowered() {
        for (Direction d : Direction.values()) { if (getRSInput(d) > 0) { return true; } }
        return false;
    }
}
