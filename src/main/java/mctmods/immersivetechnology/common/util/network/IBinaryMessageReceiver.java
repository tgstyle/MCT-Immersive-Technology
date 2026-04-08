package mctmods.immersivetechnology.common.util.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

public interface IBinaryMessageReceiver {
    void receiveMessageFromServer(ByteBuf message);
    void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player);
}
