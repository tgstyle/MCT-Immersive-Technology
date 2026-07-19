package mctmods.immersivetechnology.core.network;

import mctmods.immersivetechnology.core.lib.Reference;
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
public record OSDSyncBlock(String key, int distance) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OSDSyncBlock> TYPE = new CustomPacketPayload.Type<>(Reference.rl("osdsyncblock"));

    public static final StreamCodec<ByteBuf, OSDSyncBlock> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            OSDSyncBlock::key,
            ByteBufCodecs.VAR_INT,
            OSDSyncBlock::distance,
            OSDSyncBlock::new
    );

    @Override public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OSDSyncBlock message, IPayloadContext context) {
        context.enqueueWork(() -> {
            TranslationKey transKey = TranslationKey.valueOf(message.key());
            String actualKey = transKey.getLocation();
            Component msg;
            if (message.distance() >= 0) { msg = Component.translatable(actualKey, message.distance()); }else { msg = Component.translatable(actualKey); }
            Minecraft.getInstance().gui.getChat().addMessage(msg);
        });
    }
}
