package mctmods.immersivetechnology.core.network;

import mctmods.immersivetechnology.common.blocks.metal.logic.OSDCommonBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveCommonBlockEntity;
import io.netty.buffer.ByteBuf;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record OSDRequestMessage(BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OSDRequestMessage> TYPE = new CustomPacketPayload.Type<>(Reference.rl("osdrequest"));

    public static final StreamCodec<ByteBuf, OSDRequestMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            OSDRequestMessage::pos,
            OSDRequestMessage::new
    );

    @Override public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OSDRequestMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            @SuppressWarnings("resource")
            ServerLevel level = player.serverLevel();
            BlockEntity te = level.getBlockEntity(message.pos());
            if (te instanceof OSDCommonBlockEntity trash) {
                PacketHandler.sendToPlayer(player, new OSDSyncMessage(message.pos(), trash.lastAcceptedAmount, 0, 0));
            }
            if (te instanceof ValveCommonBlockEntity valve) {
                PacketHandler.sendToPlayer(player, new OSDSyncMessage(message.pos(), valve.lastAcceptedAmount, valve.average, valve.packetAverage));
            }
        });
    }
}
