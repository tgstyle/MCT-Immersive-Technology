package mctmods.immersivetechnology.common.util.network;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageRequestUpdate implements IMessage {

	BlockPos pos;

	public MessageRequestUpdate(TileEntityIEBase tile) {
		this.pos = tile.getPos();
	}

	public MessageRequestUpdate() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(pos.getX()).writeInt(pos.getY()).writeInt(pos.getZ());
	}

	@SideOnly(Side.CLIENT)
	public static class HandlerClient implements IMessageHandler<MessageRequestUpdate, IMessage> {
		@Override
		public IMessage onMessage(MessageRequestUpdate message, MessageContext ctx) {
			return null;
		}
	}

}