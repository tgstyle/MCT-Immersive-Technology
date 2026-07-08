package mctmods.immersivetechnology.core.network;

import mctmods.immersivetechnology.common.gui.helper.ITContainerMenu;
import io.netty.buffer.ByteBuf;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record ITMessageContainerUpdate(int windowId, CompoundTag nbt) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ITMessageContainerUpdate> TYPE = new CustomPacketPayload.Type<>(ITLib.rl("containerupdate"));

    public static final StreamCodec<ByteBuf, ITMessageContainerUpdate> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ITMessageContainerUpdate::windowId,
            ByteBufCodecs.COMPOUND_TAG,
            ITMessageContainerUpdate::nbt,
            ITMessageContainerUpdate::new
    );

    @Override public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ITMessageContainerUpdate message, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        context.enqueueWork(() -> {
            player.resetLastActionTime();
            if (player.containerMenu.containerId == message.windowId()) {
                AbstractContainerMenu menu = player.containerMenu;
                if (menu instanceof ITContainerMenu itMenu) {
                    itMenu.receiveMessageFromScreen(message.nbt());
                }
            }
        });
    }
}
