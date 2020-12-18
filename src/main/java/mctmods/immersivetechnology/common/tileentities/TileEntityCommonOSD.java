package mctmods.immersivetechnology.common.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mctmods.immersivetechnology.common.Config;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import mctmods.immersivetechnology.common.util.network.IBinaryMessageReceiver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.RayTraceResult;

public abstract class TileEntityCommonOSD extends TileEntityIEBase implements ITickable, IBlockOverlayText, IBinaryMessageReceiver {

	public long acceptedAmount = 0;
	public long lastAcceptedAmount = 0;
	public int secondCounter = 0;

	public void efficientMarkDirty() {//!!!!!!! only use it within update() function !!!!!!!
		world.getChunkFromBlockCoords(this.getPos()).markDirty();
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {

	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {

	}

	@Override
	public void update() {
		if(world.isRemote) {
			if (requestCooldown > 0) requestCooldown--;
			return;
		}
		if(++secondCounter < 20) return;
		lastAcceptedAmount = acceptedAmount;
		acceptedAmount = 0;
		secondCounter = 0;
	}

	abstract public TranslationKey text();

	public int requestCooldown = 0;

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer) {
		if (requestCooldown == 0) {
			ByteBuf message = Unpooled.copyBoolean(true);
			BinaryMessageTileSync.sendToServer(getPos(), message);
			requestCooldown = 20;
		}
		return new String[]{ text().format(Config.ITConfig.Experimental.per_tick_trash_cans? ((float)lastAcceptedAmount)/20 : lastAcceptedAmount) };
	}

	@Override
	public void receiveMessageFromClient(ByteBuf buf, EntityPlayerMP player) {
		ByteBuf message = Unpooled.copyLong(lastAcceptedAmount);
		BinaryMessageTileSync.sendToPlayer(player, getPos(), message);
	}

	@Override
	public void receiveMessageFromServer(ByteBuf buf) {
		lastAcceptedAmount = buf.readLong();
	}

	@Override
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop) {
		return false;
	}

}