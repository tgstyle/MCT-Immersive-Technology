package mctmods.immersivetechnology.core.network;

import mctmods.immersivetechnology.common.blocks.helper.ITBaseBlockEntity;
import io.netty.buffer.ByteBuf;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record ITMessageTileSync(BlockPos pos, CompoundTag nbt) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ITMessageTileSync> TYPE = new CustomPacketPayload.Type<>(ITLib.rl("tilesync"));

    public static final StreamCodec<ByteBuf, ITMessageTileSync> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ITMessageTileSync::pos,
            ByteBufCodecs.COMPOUND_TAG,
            ITMessageTileSync::nbt,
            ITMessageTileSync::new
    );

    @Override public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ITMessageTileSync message, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        context.enqueueWork(() -> {
            Level level = player.level();
            BlockEntity tile = level.getBlockEntity(message.pos());
            if (tile instanceof ITBaseBlockEntity itbe) {
                itbe.receiveMessageFromClient(message.nbt());
            }
        });
    }
}
