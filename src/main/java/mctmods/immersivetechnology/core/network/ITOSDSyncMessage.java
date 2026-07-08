package mctmods.immersivetechnology.core.network;

import mctmods.immersivetechnology.common.blocks.metal.logic.OSDCommonBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveCommonBlockEntity;
import mctmods.immersivetechnology.core.lib.ITLib;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record ITOSDSyncMessage(BlockPos pos, long lastAccepted, long average, int packetAverage) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ITOSDSyncMessage> TYPE = new CustomPacketPayload.Type<>(ITLib.rl("osdsync"));

    public static final StreamCodec<ByteBuf, ITOSDSyncMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ITOSDSyncMessage::pos,
            ByteBufCodecs.VAR_LONG,
            ITOSDSyncMessage::lastAccepted,
            ByteBufCodecs.VAR_LONG,
            ITOSDSyncMessage::average,
            ByteBufCodecs.VAR_INT,
            ITOSDSyncMessage::packetAverage,
            ITOSDSyncMessage::new
    );

    @Override public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ITOSDSyncMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                BlockEntity te = Minecraft.getInstance().level.getBlockEntity(message.pos());
                if (te instanceof OSDCommonBlockEntity osd) { osd.lastAcceptedAmount = message.lastAccepted(); }
                if (te instanceof ValveCommonBlockEntity valve) {
                    valve.lastAcceptedAmount = message.lastAccepted();
                    valve.average = message.average();
                    valve.packetAverage = message.packetAverage();
                }
            }
        });
    }
}
