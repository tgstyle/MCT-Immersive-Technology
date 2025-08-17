package mctmods.immersivetechnology.common.network;

import mctmods.immersivetechnology.common.blocks.metal.TrashCommonBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ITOSDSyncMessage implements ITMessage {
    private final BlockPos pos;
    private final long amount;

    public ITOSDSyncMessage(BlockPos pos, long amount) { this.pos = pos; this.amount = amount; }

    public ITOSDSyncMessage(FriendlyByteBuf buf) { this.pos = buf.readBlockPos(); this.amount = buf.readLong(); }

    @Override
    public void toBytes(FriendlyByteBuf buf) { buf.writeBlockPos(pos); buf.writeLong(amount); }

    @Override
    public void process(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            if (ctx.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                if (Minecraft.getInstance().level != null) {
                    BlockEntity te = Minecraft.getInstance().level.getBlockEntity(pos);
                    if (te instanceof TrashCommonBlockEntity trash) { trash.lastAcceptedAmount = amount; }
                }
            }
        });
    }
}
