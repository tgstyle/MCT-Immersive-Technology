package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public abstract class TileEntityGenericTrash extends TileEntityIEBase implements ITickable, IBlockOverlayText, IBlockBounds {

	public EnumFacing facing = EnumFacing.NORTH;

	public long acceptedAmount;
	public long lastAcceptedAmount;
	public int secondCounter;
	public int minuteCounter;
	public long average;
	public long lastAverage;
	public int packets;
	public int packetAverage;
	public int lastPacketAverage;

	public long[] averages = new long[60];
	public long[] packetTotals = new long[60];

	public void efficientMarkDirty() {//!!!!!!! only use it within update() function !!!!!!!
		world.getChunkFromBlockCoords(this.getPos()).markDirty();
	}

	public void calculateAverages() {
		long sum = 0;
		for(long avg : averages) sum += avg;
		average = sum / 60;
		sum = 0;
		for(long avg : packetTotals) sum += avg;
		packetAverage = (int)sum;
	}

	@Override
	public void update() {
		if(world.isRemote) return;
		efficientMarkDirty();
		if(++secondCounter < 20) return;
		if(average == 0 && acceptedAmount > 0) {//pre-populate averages to avoid slow build up
			for(int i = 0; i < 60; i++) averages[i] = acceptedAmount;
			packetTotals[minuteCounter] = packets;
			calculateAverages();
		}
		if(averages[minuteCounter] != acceptedAmount || packetTotals[minuteCounter] != packets) {
			averages[minuteCounter] = acceptedAmount;
			packetTotals[minuteCounter] = packets;
			calculateAverages();
		}
		if(lastAverage != average || lastPacketAverage != packetAverage) notifyNearbyClients();
		lastAcceptedAmount = acceptedAmount;
		acceptedAmount = 0;
		packets = 0;
		secondCounter = 0;
		if(++minuteCounter == 60) {
			lastPacketAverage = packetAverage;
			lastAverage = average;
			minuteCounter = 0;
		}
	}

	abstract public String unit();
	abstract public String unitPerSecond();

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer) {
		return player.isSneaking()?
				new String[]{
						ITUtils.Translate(".osd.general.trashed", false, true) +
								average / 20 + ITUtils.Translate(unit(), true),
						ITUtils.Translate(".osd.general.inpackets", false, true) +
								packetAverage + ITUtils.Translate(".osd.general.packetslastminute", true)}
				: new String[]{ acceptedAmount + ITUtils.Translate(unitPerSecond(), true) };
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		lastAcceptedAmount = acceptedAmount = nbt.getLong("acceptedAmount");
		secondCounter = nbt.getInteger("secondCounter");
		long avg = nbt.getLong("averages");
		for(int i = 0; i < 60; i++) averages[i] = avg;
		calculateAverages();
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		nbt.setLong("acceptedAmount", acceptedAmount);
		nbt.setInteger("secondCounter", secondCounter);
		calculateAverages();
		nbt.setLong("averages", average);
	}

	@Override
	public void receiveMessageFromServer(NBTTagCompound message) {
		packetAverage = message.getInteger("packets");
		average = message.getLong("average");
		acceptedAmount = message.getLong("acceptedAmount");
	}

	public void notifyNearbyClients() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("packets", Math.max(packets, packetAverage));
		tag.setLong("average", average);
		tag.setLong("acceptedAmount", acceptedAmount);
		BlockPos center = getPos();
		ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageTileSync(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
	}

	@Override
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop) {
		return false;
	}

	@Override
	public float[] getBlockBounds()	{
		return new float[]{facing.getAxis() == Axis.X ? 0 : .125f, 0, facing.getAxis() == Axis.Z ? .125f : .125f, facing.getAxis() == Axis.X ? 1 : .875f, 1, facing.getAxis() == Axis.Z ? .875f : .875f};
	}

}