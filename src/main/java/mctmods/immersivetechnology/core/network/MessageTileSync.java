package mctmods.immersivetechnology.core.network;

import mctmods.immersivetechnology.common.blocks.helper.BaseBlockEntity;
import io.netty.buffer.ByteBuf;
import mctmods.immersivetechnology.core.lib.Reference;
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
public record MessageTileSync(BlockPos pos, CompoundTag nbt) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MessageTileSync> TYPE = new CustomPacketPayload.Type<>(Reference.rl("tilesync"));

    public static final StreamCodec<ByteBuf, MessageTileSync> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            MessageTileSync::pos,
            ByteBufCodecs.COMPOUND_TAG,
            MessageTileSync::nbt,
            MessageTileSync::new
    );

    @Override public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MessageTileSync message, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        context.enqueueWork(() -> {
            Level level = player.level();
            BlockEntity tile = level.getBlockEntity(message.pos());
            if (tile instanceof BaseBlockEntity itbe) {
                itbe.receiveMessageFromClient(message.nbt());
            }
        });
    }
}
