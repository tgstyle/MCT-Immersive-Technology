package mctmods.immersivetechnology.common.network;

import mctmods.immersivetechnology.common.blocks.metal.CreativeBarrelBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.TrashCommonBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ITOSDRequestMessage implements ITMessage {
    private final BlockPos pos;

    public ITOSDRequestMessage(BlockPos pos) { this.pos = pos; }

    public ITOSDRequestMessage(FriendlyByteBuf buf) { this.pos = buf.readBlockPos(); }

    @Override
    public void toBytes(FriendlyByteBuf buf) { buf.writeBlockPos(pos); }

    @Override
    public void process(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            if (ctx.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                ServerPlayer player = ctx.getSender();
                if (player != null) {
                    Level level = player.level();
                    BlockEntity te = level.getBlockEntity(pos);
                    if (te instanceof TrashCommonBlockEntity trash) { ITPacketHandler.sendToPlayer(player, new ITOSDSyncMessage(pos, trash.lastAcceptedAmount)); }
                    if (te instanceof CreativeBarrelBlockEntity barrel) { ITPacketHandler.sendToPlayer(player, new ITOSDSyncMessage(pos, barrel.lastOutputAmount)); }
                }
            }
        });
    }
}
