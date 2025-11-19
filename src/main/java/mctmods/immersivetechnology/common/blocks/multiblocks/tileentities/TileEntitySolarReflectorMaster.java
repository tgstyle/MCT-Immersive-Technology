package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

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

    @Override
    public void update() { super.update(); }

    @Override
    public boolean isDummy() { return false; }

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
        return towerCollectorPosition.equals(position);
    }

    public void detachTower(BlockPos position) {
        if (!towerCollectorPosition.equals(position)) return;
        isMirrorTaken = false;
        towerCollectorPosition = this.getPos();
        calculateAnimationRotations();
        notifyNearbyClients();
        this.markDirty();
    }

    public void notifyNearbyClients() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("isMirrorTaken", isMirrorTaken);
        tag.setIntArray("towerCollectorPosition", new int[]{towerCollectorPosition.getX(), towerCollectorPosition.getY(), towerCollectorPosition.getZ()});
        tag.setFloat("rotation0", animationRotations[0]);
        tag.setFloat("rotation1", animationRotations[1]);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendToAllAround(new MessageTileSync(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 40));
    }

    public float[] getAnimationRotations() { return animationRotations; }

    private void calculateAnimationRotations() {
        int xdiff = getPos().getX() - towerCollectorPosition.getX();
        int ydiff = getPos().getY() - towerCollectorPosition.getY();
        int zdiff = getPos().getZ() - towerCollectorPosition.getZ();
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
        nbt.setIntArray("towerCollectorPosition", new int[]{towerCollectorPosition.getX(), towerCollectorPosition.getY(), towerCollectorPosition.getZ()});
        nbt.setFloat("rotation0", animationRotations[0]);
        nbt.setFloat("rotation1", animationRotations[1]);
    }
}
