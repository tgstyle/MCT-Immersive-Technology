package ferro2000.immersivetech.common.util.network;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import ferro2000.immersivetech.common.blocks.stone.tileentities.TileEntityCokeOvenAdvanced;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
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

    public static class HandlerServer implements IMessageHandler<MessageRequestUpdate, IMessage> {
        @Override
        public IMessage onMessage(MessageRequestUpdate message, MessageContext ctx) {
            WorldServer world = ctx.getServerHandler().player.getServerWorld();
            world.addScheduledTask(() -> {
                if(world.isBlockLoaded(message.pos)) {
                    TileEntity tile = world.getTileEntity(message.pos);
                    if(tile instanceof TileEntityCokeOvenAdvanced)
                        ((TileEntityCokeOvenAdvanced)tile).updateRequested(ctx.getServerHandler().player);
                }
            });
            return null;
        }
    }

    @SideOnly(Side.CLIENT)
    public static class HandlerClient implements IMessageHandler<MessageRequestUpdate, IMessage> {
        @Override
        public IMessage onMessage(MessageRequestUpdate message, MessageContext ctx) {
            return null;
        }
    }
}
