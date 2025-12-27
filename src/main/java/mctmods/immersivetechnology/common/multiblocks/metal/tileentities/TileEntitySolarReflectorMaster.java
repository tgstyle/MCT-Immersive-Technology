package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSolarReflector;
import mctmods.immersivetechnology.common.util.multiblock.PoICache;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import mctmods.immersivetechnology.common.util.network.IBinaryMessageReceiver;
import mctmods.immersivetechnology.common.util.solarregistry.SolarRegistry;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class TileEntitySolarReflectorMaster extends TileEntitySolarReflectorSlave implements IBinaryMessageReceiver {
    private boolean isMirrorTaken = false;
    private BlockPos towerCollectorPosition;
    private float[] animationRotations = new float[2];
    private boolean initialized = false;
    private boolean reAttachOnLoad = false;
    private PoICache link0;

    private BlockPos getTowerCollectorPosition() { return towerCollectorPosition != null ? towerCollectorPosition : getPos(); }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntitySolarReflectorMaster master() { return this; }

    @Override public void disassemble() {
        super.disassemble();
        InitializePoIs();
        SolarRegistry.unregisterReflector(world, getBlockPosForPos(link0.position));
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
            SolarRegistry.notifyTaken(world, getPos(), true);
            calculateAnimationRotations();
            notifyNearbyClients();
            this.markDirty();
        }
        return getTowerCollectorPosition().equals(position);
    }

    public void detachTower(BlockPos position) {
        if (!getTowerCollectorPosition().equals(position)) return;
        isMirrorTaken = false;
        towerCollectorPosition = getPos();
        SolarRegistry.notifyTaken(world, getPos(), false);
        calculateAnimationRotations();
        notifyNearbyClients();
        this.markDirty();
    }

    public void notifyNearbyClients() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeBoolean(isMirrorTaken);
        buf.writeInt(getTowerCollectorPosition().getX());
        buf.writeInt(getTowerCollectorPosition().getY());
        buf.writeInt(getTowerCollectorPosition().getZ());
        buf.writeFloat(animationRotations[0]);
        buf.writeFloat(animationRotations[1]);
        BinaryMessageTileSync.sendToAllTracking(world, getPos(), buf);
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
    public void receiveMessageFromServer(ByteBuf message) {
        isMirrorTaken = message.readBoolean();
        int x = message.readInt();
        int y = message.readInt();
        int z = message.readInt();
        towerCollectorPosition = new BlockPos(x, y, z);
        animationRotations[0] = message.readFloat();
        animationRotations[1] = message.readFloat();
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) { }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        isMirrorTaken = nbt.getBoolean("isMirrorTaken");
        towerCollectorPosition = null;
        if (nbt.hasKey("towerCollectorPosition")) {
            int[] posArr = nbt.getIntArray("towerCollectorPosition");
            if (posArr.length == 3) towerCollectorPosition = new BlockPos(posArr[0], posArr[1], posArr[2]);
        }
        animationRotations[0] = nbt.getFloat("rotation0");
        animationRotations[1] = nbt.getFloat("rotation1");
        initialized = nbt.getBoolean("initialized");
        reAttachOnLoad = nbt.getBoolean("reAttachOnLoad");
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setBoolean("isMirrorTaken", isMirrorTaken);
        nbt.setIntArray("towerCollectorPosition", new int[]{getTowerCollectorPosition().getX(), getTowerCollectorPosition().getY(), getTowerCollectorPosition().getZ()});
        nbt.setFloat("rotation0", animationRotations[0]);
        nbt.setFloat("rotation1", animationRotations[1]);
        nbt.setBoolean("initialized", initialized);
        nbt.setBoolean("reAttachOnLoad", reAttachOnLoad);
    }

    @Override public void update() {
        super.update();
        if (!initialized && formed && !world.isRemote) {
            InitializePoIs();
            SolarRegistry.registerReflector(world, getBlockPosForPos(link0.position));
            initialized = true;
        }
        if (reAttachOnLoad && !world.isRemote) {
            reAttachOnLoad = false;
            if (world.isBlockLoaded(towerCollectorPosition)) {
                TileEntity tile = world.getTileEntity(towerCollectorPosition);
                boolean isValid = tile instanceof TileEntitySolarTowerMaster || tile instanceof TileEntitySolarMelterMaster;
                if (isValid) {
                    setTowerCollectorPosition(towerCollectorPosition);
                } else {
                    isMirrorTaken = false;
                    towerCollectorPosition = getPos();
                    SolarRegistry.notifyTaken(world, getPos(), false);
                    calculateAnimationRotations();
                    notifyNearbyClients();
                    markDirty();
                }
            }
        }
    }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartSolarReflector.instance.pointsOfInterest) {
            if (poi.name.equals("link0")) {
                link0 = new PoICache(facing, poi, mirrored);
                break;
            }
        }
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }
}
