package mctmods.immersivetechnology.common.util.network;

import io.netty.buffer.ByteBuf;

public interface IBinaryMessageReceiver {

    default void receiveMessageFromClient(ByteBuf buf) {};
    default void receiveMessageFromServer(ByteBuf buf) {};
}
