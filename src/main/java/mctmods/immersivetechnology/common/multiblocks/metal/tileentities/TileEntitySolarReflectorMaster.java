package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.ImmersiveConvergence;
import com.immersiveconvergence.api.multiblock.PoICache;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.network.BinaryTileSyncMessage;
import com.immersiveconvergence.api.network.IBinaryMessageReceiver;
import com.immersiveconvergence.api.particles.BeamParticles;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSolarReflector;
import mctmods.immersivetechnology.common.util.solarregistry.SolarRegistry;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class TileEntitySolarReflectorMaster extends TileEntitySolarReflectorSlave implements IBinaryMessageReceiver {

    private float[] animationRotations = new float[2];

    boolean isMirrorTaken = false;
    private boolean initialized = false;
    private boolean needsPoIInit = false;

    private PoICache link0;
    private PoICache beam0;
    private BlockPos collectorPosition0;

    public BlockPos getCollectorPosition() { return collectorPosition0 != null ? collectorPosition0 : getPos(); }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        isMirrorTaken = nbt.getBoolean("isMirrorTaken");
        collectorPosition0 = null;
        if (nbt.hasKey("collectorPosition0")) {
            int[] posArr = nbt.getIntArray("collectorPosition0");
            if (posArr.length == 3) collectorPosition0 = new BlockPos(posArr[0], posArr[1], posArr[2]);
        }
        animationRotations[0] = nbt.getFloat("rotation0");
        animationRotations[1] = nbt.getFloat("rotation1");
        initialized = nbt.getBoolean("initialized");
        if (formed && !descPacket) needsPoIInit = true;
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setBoolean("isMirrorTaken", isMirrorTaken);
        nbt.setIntArray("collectorPosition0", new int[] {getCollectorPosition().getX(), getCollectorPosition().getY(), getCollectorPosition().getZ()});
        nbt.setFloat("rotation0", animationRotations[0]);
        nbt.setFloat("rotation1", animationRotations[1]);
        nbt.setBoolean("initialized", initialized);
    }

    @Override public void disassemble() {
        if (!world.isRemote) {
            InitializePoIs();
            SolarRegistry.unregisterReflector(world, getBlockPosForPos(link0.position));
            detachTower();
        }
        super.disassemble();
    }

    public void notifyNearbyClients() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeBoolean(isMirrorTaken);
        buf.writeInt(getCollectorPosition().getX());
        buf.writeInt(getCollectorPosition().getY());
        buf.writeInt(getCollectorPosition().getZ());
        buf.writeFloat(animationRotations[0]);
        buf.writeFloat(animationRotations[1]);
        NetworkRegistry.TargetPoint tp = new NetworkRegistry.TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 64);
        ImmersiveConvergence.packetHandler.sendToAllAround(new BinaryTileSyncMessage(getPos(), buf), tp);
    }

    @Override public void receiveMessageFromServer(ByteBuf message) {
        isMirrorTaken = message.readBoolean();
        int x = message.readInt();
        int y = message.readInt();
        int z = message.readInt();
        collectorPosition0 = new BlockPos(x, y, z);
        animationRotations[0] = message.readFloat();
        animationRotations[1] = message.readFloat();
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {}

    @Override public void update() {
        super.update();
        if (!formed) return;
        if (world.isRemote) {
            spawnBeamParticles();
            return;
        }
        if (needsPoIInit || link0 == null) {
            InitializePoIs();
            needsPoIInit = false;
        }
        if (!initialized) {
            SolarRegistry.registerReflector(world, getBlockPosForPos(link0.position));
            initialized = true;
        }
        if (isMirrorTaken) {
            if (collectorPosition0 == null) detachTower();
            else if (world.isBlockLoaded(collectorPosition0)) {
                TileEntity te = world.getTileEntity(collectorPosition0);
                boolean valid = false;
                if (te != null && !te.isInvalid()) {
                    if (te instanceof TileEntitySolarMelterSlave) {
                        TileEntitySolarMelterMaster m = ((TileEntitySolarMelterSlave)te).master();
                        valid = m != null && m.formed;
                    } else if (te instanceof TileEntitySolarTowerSlave) {
                        TileEntitySolarTowerMaster m = ((TileEntitySolarTowerSlave)te).master();
                        valid = m != null && m.formed;
                    }
                }
                if (!valid) detachTower();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void spawnBeamParticles() {
        if (!isMirrorTaken || collectorPosition0 == null) { return; }
        if (beam0 == null) { InitializePoIs(); }
        if (beam0 == null || getSolarCollectorStrength() <= 0) { return; }
        BlockPos origin = getBlockPosForPos(beam0.position);
        BeamParticles.spawnAlongBeam(world,
                new Vec3d(origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5),
                new Vec3d(collectorPosition0.getX() + 0.5, collectorPosition0.getY() + 0.5, collectorPosition0.getZ() + 0.5),
                world.rand);
    }

    private void calculateAnimationRotations() {
        BlockPos target = getCollectorPosition();
        int xdiff = getPos().getX() - target.getX();
        int ydiff = getPos().getY() - target.getY();
        int zdiff = getPos().getZ() - target.getZ();
        double xzdiff = Math.sqrt(xdiff * xdiff + zdiff * zdiff);
        animationRotations = new float[] {
                (float)(Math.atan2(xdiff, zdiff) * 180 / Math.PI) + 90 * (getFacing().getHorizontalIndex() + ((getFacing().getXOffset() == 0) ? 0 : 2)),
                (float)(Math.abs(Math.atan2(ydiff, xzdiff) * 180 / Math.PI) - 90)
        };
    }

    public boolean setTowerCollectorPosition(BlockPos position) {
        collectorPosition0 = position;
        if (!isMirrorTaken) {
            isMirrorTaken = true;
            SolarRegistry.notifyTaken(world, getPos(), true);
        }
        calculateAnimationRotations();
        notifyNearbyClients();
        efficientMarkDirty();
        return true;
    }

    public void detachTower() {
        if (!isMirrorTaken) return;
        BlockPos oldCollector = getCollectorPosition();
        isMirrorTaken = false;
        collectorPosition0 = getPos();
        SolarRegistry.notifyTaken(world, getPos(), false);
        calculateAnimationRotations();
        notifyNearbyClients();
        efficientMarkDirty();
        if (oldCollector != null && !oldCollector.equals(getPos()) && world.isBlockLoaded(oldCollector)) {
            TileEntity te = world.getTileEntity(oldCollector);
            if (te instanceof TileEntitySolarMelterSlave) {
                TileEntitySolarMelterMaster m = ((TileEntitySolarMelterSlave)te).master();
                if (m != null) m.forceReflectorCheck();
            } else if (te instanceof TileEntitySolarTowerSlave) {
                TileEntitySolarTowerMaster m = ((TileEntitySolarTowerSlave)te).master();
                if (m != null) m.forceReflectorCheck();
            }
        }
    }

    public double getSolarCollectorStrength() {
        int numClear = 0;
        for (int l = -1; l < 2; l++) {
            for (int w = -1; w < 2; w++) {
                BlockPos pos = getPos().offset(EnumFacing.NORTH, l).offset(EnumFacing.EAST, w).up();
                if (world.canBlockSeeSky(pos)) numClear++;
            }
        }
        return numClear / 9.0;
    }

    public float[] getAnimationRotations() { return animationRotations; }

    private void InitializePoIs() {
        link0 = null;
        beam0 = null;
        for (PoIJSONSchema poi : TileEntityITMultiblockPartSolarReflector.instance.pointsOfInterest) {
            if (poi.name.equals("link0")) { link0 = new PoICache(facing, poi, mirrored); }
            else if (poi.name.equals("beam0")) { beam0 = new PoICache(facing, poi, mirrored); }
        }
    }

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntitySolarReflectorMaster master() { return this; }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }
}
