package mctmods.immersivetechnology.core.network;

import mctmods.immersivetechnology.common.blocks.metal.logic.OSDCommonBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveCommonBlockEntity;
import io.netty.buffer.ByteBuf;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record ITOSDRequestMessage(BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ITOSDRequestMessage> TYPE = new CustomPacketPayload.Type<>(ITLib.rl("osdrequest"));

    public static final StreamCodec<ByteBuf, ITOSDRequestMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ITOSDRequestMessage::pos,
            ITOSDRequestMessage::new
    );

    @Override public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ITOSDRequestMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            @SuppressWarnings("resource")
            ServerLevel level = player.serverLevel();
            BlockEntity te = level.getBlockEntity(message.pos());
            if (te instanceof OSDCommonBlockEntity trash) {
                ITPacketHandler.sendToPlayer(player, new ITOSDSyncMessage(message.pos(), trash.lastAcceptedAmount, 0, 0));
            }
            if (te instanceof ValveCommonBlockEntity valve) {
                ITPacketHandler.sendToPlayer(player, new ITOSDSyncMessage(message.pos(), valve.lastAcceptedAmount, valve.average, valve.packetAverage));
            }
        });
    }
}
