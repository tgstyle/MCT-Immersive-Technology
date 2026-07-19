package mctmods.immersivetechnology.core.network;

import mctmods.immersivetechnology.common.blocks.connectors.logic.ConnectorTimerBlockEntity;
import mctmods.immersivetechnology.common.blocks.helper.BaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageTileSync implements IMessage {
    private final BlockPos pos;
    private final CompoundTag nbt;

    public MessageTileSync(BlockPos pos, CompoundTag nbt) {
        this.pos = pos;
        this.nbt = nbt;
    }

    public MessageTileSync(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.nbt = buf.readNbt();
    }

    @Override public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeNbt(this.nbt);
    }

    @SuppressWarnings("resource")
    @Override public void process(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ServerPlayer player = ctx.getSender();
        if (player != null) {
            ctx.enqueueWork(() -> {
                Level level = player.level();
                BlockEntity tile = level.getBlockEntity(this.pos);
                if (tile instanceof BaseBlockEntity itbe) {
                    itbe.receiveMessageFromClient(this.nbt);
                } else if (tile instanceof ConnectorTimerBlockEntity timer) {
                    timer.receiveMessageFromClient(this.nbt);
                }
            });
        }
    }
}
