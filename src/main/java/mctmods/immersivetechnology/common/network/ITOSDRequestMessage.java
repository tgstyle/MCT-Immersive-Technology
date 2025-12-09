package mctmods.immersivetechnology.common.network;

import java.util.function.Supplier;

import mctmods.immersivetechnology.common.blocks.metal.logic.OSDCommonBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveCommonBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public record ITOSDRequestMessage(BlockPos pos) implements ITMessage {
    public ITOSDRequestMessage(FriendlyByteBuf buf) { this(buf.readBlockPos()); }

    @Override public void toBytes(FriendlyByteBuf buf) { buf.writeBlockPos(pos); }

    @Override public void process(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            if (ctx.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                ServerPlayer player = ctx.getSender();
                if (player != null) {
                    Level level = player.level();
                    BlockEntity te = level.getBlockEntity(pos);
                    if (te instanceof OSDCommonBlockEntity trash) {
                        ITPacketHandler.sendToPlayer(player, new ITOSDSyncMessage(pos, trash.lastAcceptedAmount, 0, 0));
                    }
                    if (te instanceof ValveCommonBlockEntity valve) {
                        ITPacketHandler.sendToPlayer(player, new ITOSDSyncMessage(pos, valve.lastAcceptedAmount, valve.average, valve.packetAverage));
                    }
                }
            }
        });
    }
}
