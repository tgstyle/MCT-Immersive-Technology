package mctmods.immersivetechnology.common.shared.tileentities;

import com.immersiveconvergence.ImmersiveConvergence;
import com.immersiveconvergence.api.network.BinaryTileSyncMessage;
import com.immersiveconvergence.api.network.IBinaryMessageReceiver;
import com.immersiveconvergence.api.network.TileSyncMessage;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.TranslationKey;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

public abstract class TileEntityCommonValve extends TileEntityIEBase implements IEBlockInterfaces.IDirectionalTile, ITickable, IEBlockInterfaces.IBlockOverlayText, IEBlockInterfaces.IPlayerInteraction, IEBlockInterfaces.IGuiTile, IBinaryMessageReceiver {

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

	public void efficientMarkDirty() {
		world.getChunk(getPos()).markDirty();
	}

	public void calculateAverages() {
		long sum = 0;
		for (long avg : averages) { sum += avg; }
		average = sum / 60;
		sum = 0;
		for (long total : packetTotals) { sum += total; }
		packetAverage = (int)sum;
	}

	@Override public void update() {
		if (world.isRemote) {
			if (requestCooldown > 0) { requestCooldown--; }
			return;
		}
		efficientMarkDirty();
		if (++secondCounter < 20) { return; }
		if (average == 0 && acceptedAmount > 0) {
			for (int i = 0; i < 60; i++) { averages[i] = acceptedAmount; }
			packetTotals[minuteCounter] = packets;
			calculateAverages();
		}
		if (averages[minuteCounter] != acceptedAmount || packetTotals[minuteCounter] != packets) {
			averages[minuteCounter] = acceptedAmount;
			packetTotals[minuteCounter] = packets;
			calculateAverages();
		}
		lastAcceptedAmount = acceptedAmount;
		acceptedAmount = 0;
		packets = 0;
		secondCounter = 0;
		if (++minuteCounter == 60) {
			lastPacketAverage = packetAverage;
			lastAverage = average;
			minuteCounter = 0;
		}
	}

	@Override public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
		if (!world.isRemote && !Utils.isHammer(heldItem)) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("packetLimit", packetLimit);
			tag.setInteger("timeLimit", timeLimit);
			tag.setInteger("keepSize", keepSize);
			ImmersiveConvergence.packetHandler.sendTo(new TileSyncMessage(this, tag), (EntityPlayerMP)player);
			return true;
		}
		else if (player.isSneaking() && Utils.isHammer(heldItem)) {
			if (++redstoneMode > 2) { redstoneMode = 0; }
			String translationKey;
			switch (redstoneMode) {
				case 1:
					translationKey = TranslationKey.OVERLAY_REDSTONE_NORMAL.location;
					break;
				case 2:
					translationKey = TranslationKey.OVERLAY_REDSTONE_INVERTED.location;
					break;
				default:
					translationKey = TranslationKey.OVERLAY_REDSTONE_OFF.location;
					break;
			}
			ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(translationKey));
			efficientMarkDirty();
			return true;
		}
		return false;
	}

	int requestCooldown = 0;

	@Override @Nonnull public String[] getOverlayText(@Nonnull EntityPlayer player, @Nonnull RayTraceResult mop, boolean hammer) {
		if (requestCooldown == 0) {
			ByteBuf message = Unpooled.copyBoolean(true);
			BinaryTileSyncMessage.sendToServer(getPos(), message);
			requestCooldown = 20;
		}
		if (player.isSneaking()) {
			return new String[]{
					overlaySneakingFirstLine.format((double)average / 20),
					overlaySneakingSecondLine.format(packetAverage)
			};
		}
		return new String[]{ overlayNormal.format(lastAcceptedAmount) };
	}

	@Override public void receiveMessageFromClient(ByteBuf buf, EntityPlayerMP player) {
		ByteBuf message = Unpooled.copyInt(Math.max(packets, packetAverage));
		message.writeLong(average);
		message.writeLong(lastAcceptedAmount);
		BinaryTileSyncMessage.sendToPlayer(player, getPos(), message);
	}

	@Override public void receiveMessageFromServer(ByteBuf buf) {
		packetAverage = buf.readInt();
		average = buf.readLong();
		lastAcceptedAmount = buf.readLong();
	}

	@Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
		facing = EnumFacing.byIndex(nbt.getByte("facing"));
		packetLimit = nbt.getInteger("packetLimit");
		timeLimit = nbt.getInteger("timeLimit");
		keepSize = nbt.getInteger("keepSize");
		redstoneMode = nbt.getByte("redstoneMode");
		if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) { return; }
		lastAcceptedAmount = acceptedAmount = nbt.getLong("acceptedAmount");
		secondCounter = nbt.getInteger("secondCounter");
		long avg = nbt.getLong("averages");
		for (int i = 0; i < 60; i++) { averages[i] = avg; }
		calculateAverages();
	}

	@Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
		nbt.setByte("facing", (byte)facing.getIndex());
		nbt.setInteger("packetLimit", packetLimit);
		nbt.setInteger("timeLimit", timeLimit);
		nbt.setInteger("keepSize", keepSize);
		nbt.setByte("redstoneMode", redstoneMode);
		if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) { return; }
		nbt.setLong("acceptedAmount", acceptedAmount);
		nbt.setInteger("secondCounter", secondCounter);
		calculateAverages();
		nbt.setLong("averages", average);
	}

	@Override public boolean canOpenGui() { return true; }

	@Override public int getGuiID() { return GuiID; }

	@Nullable @Override public TileEntity getGuiMaster() { return this; }

	@SideOnly(Side.CLIENT)
	public abstract void showGui();

	@SideOnly(Side.CLIENT)
	@Override public void receiveMessageFromServer(@Nonnull NBTTagCompound message) {
		if (message.hasKey("packetLimit")) {
			packetLimit = message.getInteger("packetLimit");
			timeLimit = message.getInteger("timeLimit");
			keepSize = message.getInteger("keepSize");
			showGui();
		}
	}

	@Override public void receiveMessageFromClient(@Nonnull NBTTagCompound message) {
		packetLimit = message.getInteger("packetLimit");
		timeLimit = message.getInteger("timeLimit");
		keepSize = message.getInteger("keepSize");
		efficientMarkDirty();
	}

	@Override public boolean useNixieFont(@Nonnull EntityPlayer player, @Nonnull RayTraceResult mop) { return false; }

	@Override @Nonnull public EnumFacing getFacing() { return facing; }

	@Override public void setFacing(@Nonnull EnumFacing facing) { this.facing = facing; }

	@Override public int getFacingLimitation() { return 0; }

	@Override public boolean mirrorFacingOnPlacement(@Nonnull EntityLivingBase placer) { return false; }

	@Override public boolean canHammerRotate(@Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull EntityLivingBase entity) { return !entity.isSneaking(); }

	@Override public boolean canRotate(@Nonnull EnumFacing axis) { return true; }

	public int getRSPower() {
		int power = 0;
		for (EnumFacing dir : EnumSet.complementOf(EnumSet.of(facing, facing.getOpposite()))) {
			power = Math.max(world.getRedstonePower(pos.offset(dir, -1), dir), power);
		}
		return power;
	}

	public static int longToInt(long value) {
		if (value > Integer.MAX_VALUE) { return Integer.MAX_VALUE; }
		if (value < Integer.MIN_VALUE) { return Integer.MIN_VALUE; }
		return (int)value;
	}
}
