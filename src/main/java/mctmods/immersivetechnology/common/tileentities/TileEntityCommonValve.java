package mctmods.immersivetechnology.common.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.EnumSet;

public abstract class TileEntityCommonValve extends TileEntityIEBase implements IEBlockInterfaces.IDirectionalTile, ITickable,
		IEBlockInterfaces.IBlockOverlayText, IEBlockInterfaces.IPlayerInteraction, IEBlockInterfaces.IGuiTile {

	final TranslationKey overlayNormal;
	final TranslationKey overlaySneakingFirstLine;
	final TranslationKey overlaySneakingSecondLine;
	final int GuiID;

	public TileEntityCommonValve(TranslationKey overlayNormal, TranslationKey overlaySneakingFirstLine, TranslationKey overlaySneakingSecondLine, int GuiID) {
		this.overlayNormal = overlayNormal;
		this.overlaySneakingFirstLine = overlaySneakingFirstLine;
		this.overlaySneakingSecondLine = overlaySneakingSecondLine;
		this.GuiID = GuiID;
	}

	public EnumFacing facing = EnumFacing.NORTH;

	public int packetLimit = -1;
	public int timeLimit = -1;
	public int keepSize = -1;
	public byte redstoneMode = 0;

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
		if(lastAverage != average || lastPacketAverage != packetAverage) notifyNearbyClients(new NBTTagCompound());
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

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
		if(!world.isRemote && !Utils.isHammer(heldItem)) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("packetLimit", packetLimit);
			tag.setInteger("timeLimit", timeLimit);
			tag.setInteger("keepSize", keepSize);
			ImmersiveTechnology.packetHandler.sendTo(new MessageTileSync(this, tag), (EntityPlayerMP) player);
			return true;
		} else if(player.isSneaking() && Utils.isHammer(heldItem)) {
			if(++redstoneMode > 2) redstoneMode = 0;
			String translationKey;
			switch(redstoneMode) {
				case 1: translationKey = TranslationKey.OVERLAY_REDSTONE_NORMAL.location; break;
				case 2: translationKey = TranslationKey.OVERLAY_REDSTONE_INVERTED.location; break;
				default: translationKey = TranslationKey.OVERLAY_REDSTONE_OFF.location;
			}
			ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(translationKey));
			efficientMarkDirty();
			return true;
		}
		return false;
	}

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer) {
		return player.isSneaking()? new String[] { overlaySneakingFirstLine.format((double)average / 20), overlaySneakingSecondLine.format(packetAverage)} : new String[]{ overlayNormal.format(acceptedAmount) };
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		facing = EnumFacing.getFront(nbt.getByte("facing"));
		packetLimit = nbt.getInteger("packetLimit");
		timeLimit = nbt.getInteger("timeLimit");
		keepSize = nbt.getInteger("keepSize");
		redstoneMode = nbt.getByte("redstoneMode");
		if(Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) return;
		lastAcceptedAmount = acceptedAmount = nbt.getLong("acceptedAmount");
		secondCounter = nbt.getInteger("secondCounter");
		long avg = nbt.getLong("averages");
		for(int i = 0; i < 60; i++) averages[i] = avg;
		calculateAverages();
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		nbt.setByte("facing", (byte)facing.getIndex());
		nbt.setInteger("packetLimit", packetLimit);
		nbt.setInteger("timeLimit", timeLimit);
		nbt.setInteger("keepSize", keepSize);
		nbt.setByte("redstoneMode", redstoneMode);
		if(Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) return;
		nbt.setLong("acceptedAmount", acceptedAmount);
		nbt.setInteger("secondCounter", secondCounter);
		calculateAverages();
		nbt.setLong("averages", average);
	}

	@Override
	public boolean canOpenGui() {
		return true;
	}

	@Override
	public int getGuiID() {
		return GuiID;
	}

	@Nullable
	@Override
	public TileEntity getGuiMaster() {
		return this;
	}

	@SideOnly(Side.CLIENT)
	public abstract void showGui();

	@SideOnly(Side.CLIENT)
	@Override
	public void receiveMessageFromServer(NBTTagCompound message) {
		if(message.hasKey("packetLimit")) {
			packetLimit = message.getInteger("packetLimit");
			timeLimit = message.getInteger("timeLimit");
			keepSize = message.getInteger("keepSize");
			showGui();
		} else {
			packetAverage = message.getInteger("packets");
			average = message.getLong("average");
			acceptedAmount = message.getLong("acceptedAmount");
		}
	}

	@Override
	public void receiveMessageFromClient(NBTTagCompound message) {
		packetLimit = message.getInteger("packetLimit");
		timeLimit = message.getInteger("timeLimit");
		keepSize = message.getInteger("keepSize");
		efficientMarkDirty();
	}

	public void notifyNearbyClients(NBTTagCompound tag) {
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
	public EnumFacing getFacing() {
		return this.facing;
	}

	@Override
	public void setFacing(EnumFacing facing) {
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation() {
		return 0;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer) {
		return false;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity) {
		return !entity.isSneaking();
	}

	@Override
	public boolean canRotate(EnumFacing axis) {
		return true;
	}

	public int getRSPower() {
		int toReturn = 0;
		for(EnumFacing directions : EnumSet.complementOf(EnumSet.of(facing, facing.getOpposite()))) {
			toReturn = Math.max(world.getRedstonePower(pos.offset(directions,-1), directions), toReturn);
		}
		return toReturn;
	}

	public static int longToInt(long value) {
		return value > Integer.MAX_VALUE? Integer.MAX_VALUE : value < Integer.MIN_VALUE? Integer.MIN_VALUE : (int) value;
	}

}