package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.network.IBinaryMessageReceiver;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import javax.annotation.Nonnull;

public class TileEntitySolarReflectorMaster extends TileEntitySolarReflectorSlave implements IBinaryMessageReceiver {
    private boolean isMirrorTaken = false;
    private BlockPos towerCollectorPosition;
    private float[] animationRotations = new float[2];

    private BlockPos getTowerCollectorPosition() { return towerCollectorPosition != null ? towerCollectorPosition : getPos(); }

    @Override public void update() { super.update(); }

    @Override public boolean isDummy() { return false; }

    @Override
    public TileEntitySolarReflectorMaster master() {
        master = this;
        return this;
    }

    public double getSolarCollectorStrength() {
        int numClear = 0;
        for (int l = -1; l < 2; l++) for (int w = -1; w < 2; w++) {
            BlockPos pos = this.getPos().offset(EnumFacing.NORTH, l).offset(EnumFacing.EAST, w).add(0, 1, 0);
            if (world.canBlockSeeSky(pos)) numClear++;
        }
        return numClear / 9.0;
    }

    public boolean setTowerCollectorPosition(BlockPos position) {
        if (!isMirrorTaken) {
            towerCollectorPosition = position;
            isMirrorTaken = true;
            calculateAnimationRotations();
            notifyNearbyClients();
            this.markDirty();
        }
        return getTowerCollectorPosition().equals(position);
    }

    public void detachTower(BlockPos position) { if (!getTowerCollectorPosition().equals(position)) return; isMirrorTaken = false; towerCollectorPosition = getPos(); calculateAnimationRotations(); notifyNearbyClients(); this.markDirty(); }

    public void notifyNearbyClients() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("isMirrorTaken", isMirrorTaken);
        tag.setIntArray("towerCollectorPosition", new int[]{getTowerCollectorPosition().getX(), getTowerCollectorPosition().getY(), getTowerCollectorPosition().getZ()});
        tag.setFloat("rotation0", animationRotations[0]);
        tag.setFloat("rotation1", animationRotations[1]);
        ImmersiveTechnology.packetHandler.sendToAllAround(new MessageTileSync(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 40));
    }

    public float[] getAnimationRotations() { return animationRotations; }

    private void calculateAnimationRotations() {
        BlockPos target = getTowerCollectorPosition();
        int xdiff = getPos().getX() - target.getX();
        int ydiff = getPos().getY() - target.getY();
        int zdiff = getPos().getZ() - target.getZ();
        double xzdiff = Math.sqrt(xdiff * xdiff + zdiff * zdiff);
        animationRotations = new float[]{(float)(Math.atan2(xdiff, zdiff) * 180 / Math.PI) + 90 * (getFacing().getHorizontalIndex() + ((getFacing().getXOffset() == 0) ? 0 : 2)), (float) (Math.abs(Math.atan2(ydiff, xzdiff) * 180 / Math.PI) - 90)};
    }

    @Override
    public void receiveMessageFromServer(@Nonnull NBTTagCompound message) {
        isMirrorTaken = message.getBoolean("isMirrorTaken");
        animationRotations = new float[]{message.getFloat("rotation0"), message.getFloat("rotation1")};
    }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        isMirrorTaken = nbt.getBoolean("isMirrorTaken");
        towerCollectorPosition = null;
        if (nbt.hasKey("towerCollectorPosition")) {
            int[] posArr = nbt.getIntArray("towerCollectorPosition");
            if (posArr.length == 3) towerCollectorPosition = new BlockPos(posArr[0], posArr[1], posArr[2]);
        }
        animationRotations[0] = nbt.getFloat("rotation0");
        animationRotations[1] = nbt.getFloat("rotation1");
    }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setBoolean("isMirrorTaken", isMirrorTaken);
        nbt.setIntArray("towerCollectorPosition", new int[]{getTowerCollectorPosition().getX(), getTowerCollectorPosition().getY(), getTowerCollectorPosition().getZ()});
        nbt.setFloat("rotation0", animationRotations[0]);
        nbt.setFloat("rotation1", animationRotations[1]);
    }
}
