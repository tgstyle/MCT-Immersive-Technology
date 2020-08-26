package mctmods.immersivetechnology.common.util.network;

import io.netty.buffer.ByteBuf;
import mctmods.immersivetechnology.ImmersiveTechnology;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BinaryMessageTileSync implements IMessage {
    BlockPos pos;
    ByteBuf buffer;

    public static void sendToAllTracking(World world, BlockPos pos, ByteBuf buf) {
        ImmersiveTechnology.packetHandler.sendToAllTracking(new BinaryMessageTileSync(pos, buf), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 0));
    }

    public BinaryMessageTileSync(BlockPos tile, ByteBuf buffer) {
        this.pos = tile;
        this.buffer = buffer;
    }

    public BinaryMessageTileSync() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        this.buffer = buf.readBytes(buf.readableBytes());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX()).writeInt(pos.getY()).writeInt(pos.getZ());
        buf.writeBytes(buffer);
    }

    public static class HandlerServer implements IMessageHandler<BinaryMessageTileSync, IMessage> {
        @Override
        public IMessage onMessage(BinaryMessageTileSync message, MessageContext ctx) {
            WorldServer world = ctx.getServerHandler().player.getServerWorld();
            world.addScheduledTask(() -> {
                if(world.isBlockLoaded(message.pos)) {
                    TileEntity tile = world.getTileEntity(message.pos);
                    if(tile instanceof IBinaryMessageReceiver)
                        ((IBinaryMessageReceiver)tile).receiveMessageFromClient(message.buffer);
                }
            });
            return null;
        }
    }

    @SideOnly(Side.CLIENT)
    public static class HandlerClient implements IMessageHandler<BinaryMessageTileSync, IMessage>	{
        @Override
        public IMessage onMessage(BinaryMessageTileSync message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                World world = Minecraft.getMinecraft().world;
                if(world!=null) {
                    TileEntity tile = world.getTileEntity(message.pos);
                    if(tile instanceof IBinaryMessageReceiver)
                        ((IBinaryMessageReceiver)tile).receiveMessageFromServer(message.buffer);
                }
            });
            return null;
        }
    }

}