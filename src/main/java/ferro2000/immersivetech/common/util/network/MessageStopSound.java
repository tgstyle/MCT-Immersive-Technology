package ferro2000.immersivetech.common.util.network;

import ferro2000.immersivetech.common.util.sound.ITSoundHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageStopSound implements IMessage {
    BlockPos pos;

    public MessageStopSound(BlockPos tile) {
        this.pos = tile;
    }

    public MessageStopSound() {
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
    public static class HandlerClient implements IMessageHandler<MessageStopSound, IMessage>	{
        @Override
        public IMessage onMessage(MessageStopSound message, MessageContext ctx) {
            ITSoundHandler.StopSound(message.pos);
            return null;
        }
    }

    @SideOnly(Side.SERVER)
    public static class HandlerServer implements IMessageHandler<MessageStopSound, IMessage>	{
        @Override
        public IMessage onMessage(MessageStopSound message, MessageContext ctx) { return null; }
    }

}
