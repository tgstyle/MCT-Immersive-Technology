package mctmods.immersivetechnology.common.util.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

public interface IBinaryMessageReceiver {

    default void receiveMessageFromClient(ByteBuf buf, EntityPlayerMP player) {}

    default void receiveMessageFromServer(ByteBuf buf) {}
}
