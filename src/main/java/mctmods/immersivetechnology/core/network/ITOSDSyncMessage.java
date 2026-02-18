package mctmods.immersivetechnology.core.network;

import mctmods.immersivetechnology.common.blocks.metal.logic.OSDCommonBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveCommonBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ITOSDSyncMessage(BlockPos pos, long lastAccepted, long average, int packetAverage) implements ITMessage {
    public ITOSDSyncMessage(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readLong(), buf.readLong(), buf.readInt());
    }

    @Override public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeLong(lastAccepted);
        buf.writeLong(average);
        buf.writeInt(packetAverage);
    }

    @Override public void process(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            if (ctx.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                if (Minecraft.getInstance().level != null) {
                    BlockEntity te = Minecraft.getInstance().level.getBlockEntity(pos);
                    if (te instanceof OSDCommonBlockEntity osd) {
                        osd.lastAcceptedAmount = lastAccepted;
                    }
                    if (te instanceof ValveCommonBlockEntity valve) {
                        valve.lastAcceptedAmount = lastAccepted;
                        valve.average = average;
                        valve.packetAverage = packetAverage;
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
