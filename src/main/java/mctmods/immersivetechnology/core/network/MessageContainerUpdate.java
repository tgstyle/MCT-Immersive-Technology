package mctmods.immersivetechnology.core.network;

import mctmods.immersivetechnology.common.gui.helper.ContainerMenu;
import io.netty.buffer.ByteBuf;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record MessageContainerUpdate(int windowId, CompoundTag nbt) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MessageContainerUpdate> TYPE = new CustomPacketPayload.Type<>(Reference.rl("containerupdate"));

    public static final StreamCodec<ByteBuf, MessageContainerUpdate> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            MessageContainerUpdate::windowId,
            ByteBufCodecs.COMPOUND_TAG,
            MessageContainerUpdate::nbt,
            MessageContainerUpdate::new
    );

    @Override public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MessageContainerUpdate message, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        context.enqueueWork(() -> {
            player.resetLastActionTime();
            if (player.containerMenu.containerId == message.windowId()) {
                AbstractContainerMenu menu = player.containerMenu;
                if (menu instanceof ContainerMenu itMenu) {
                    itMenu.receiveMessageFromScreen(message.nbt());
                }
            }
        });
    }
}
