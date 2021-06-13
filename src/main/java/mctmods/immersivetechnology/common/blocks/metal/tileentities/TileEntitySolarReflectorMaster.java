package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class TileEntitySolarReflectorMaster extends TileEntitySolarReflectorSlave {

	private BlockPos towerCollectorPosition = new BlockPos(this.getPos());

	@Override
	public void update() {
		super.update();
	}

	@Override
	public boolean isDummy() {
		return false;
	}

	@Override
	public TileEntitySolarReflectorMaster master() {
		master = this;
		return this;
	}

	public double getSolarCollectorStrength() {
		int numClear = 0;
		for (int l = -1; l < 2; l++) {
			for (int w = -1; w < 2; w++) {
				BlockPos pos = this.getPos().offset(EnumFacing.NORTH, l).offset(EnumFacing.EAST, w).add(0, 1, 0);
				if (world.canBlockSeeSky(pos)) numClear++;
			}
		}

		return numClear/9.0;
	}

	public boolean setTowerCollectorPosition(BlockPos position) {
		if (towerCollectorPosition.equals(getPos())) {
			towerCollectorPosition = position;
			notifyNearbyClients();
			return true;
		} else if (towerCollectorPosition.equals(position)) {
			return true;
		}
		return false;
	}

	public void notifyNearbyClients() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setIntArray("towerCollectorPosition", new int[]{towerCollectorPosition.getX(), towerCollectorPosition.getY(), towerCollectorPosition.getZ()});
		BlockPos center = getPos();
		ImmersiveTechnology.packetHandler.sendToAllAround(new MessageTileSync(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 40));
	}

	public float[] getAnimationRotations() {
		int xdiff = getPos().getX() - towerCollectorPosition.getX();
		int ydiff = getPos().getY() - towerCollectorPosition.getY();
		int zdiff = getPos().getZ() - towerCollectorPosition.getZ();
		double xzdiff = Math.sqrt(xdiff * xdiff + zdiff * zdiff);

		return new float[]{0, (float)((Math.PI/2) - Math.atan2(ydiff, xzdiff)), 0};
	}

	@Override
	public void receiveMessageFromServer(NBTTagCompound message) {
		super.receiveMessageFromServer(message);
		towerCollectorPosition = new BlockPos(message.getIntArray("towerCollectorPosition")[0], message.getIntArray("towerCollectorPosition")[1], message.getIntArray("towerCollectorPosition")[2]);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
		towerCollectorPosition = new BlockPos(nbt.getIntArray("towerCollectorPosition")[0], nbt.getIntArray("towerCollectorPosition")[1], nbt.getIntArray("towerCollectorPosition")[2]);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
		nbt.setIntArray("towerCollectorPosition", new int[]{towerCollectorPosition.getX(), towerCollectorPosition.getY(), towerCollectorPosition.getZ()});
	}
}