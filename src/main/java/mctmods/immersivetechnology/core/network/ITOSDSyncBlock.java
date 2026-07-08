package mctmods.immersivetechnology.core.network;

import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.util.TranslationKey;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record ITOSDSyncBlock(String key, int distance) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ITOSDSyncBlock> TYPE = new CustomPacketPayload.Type<>(ITLib.rl("osdsyncblock"));

    public static final StreamCodec<ByteBuf, ITOSDSyncBlock> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ITOSDSyncBlock::key,
            ByteBufCodecs.VAR_INT,
            ITOSDSyncBlock::distance,
            ITOSDSyncBlock::new
    );

    @Override public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ITOSDSyncBlock message, IPayloadContext context) {
        context.enqueueWork(() -> {
            TranslationKey transKey = TranslationKey.valueOf(message.key());
            String actualKey = transKey.getLocation();
            Component msg;
            if (message.distance() >= 0) { msg = Component.translatable(actualKey, message.distance()); }else { msg = Component.translatable(actualKey); }
            Minecraft.getInstance().gui.getChat().addMessage(msg);
        });
    }
}
