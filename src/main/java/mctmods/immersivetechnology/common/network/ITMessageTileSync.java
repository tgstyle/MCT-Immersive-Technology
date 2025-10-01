package mctmods.immersivetechnology.common.network;

import mctmods.immersivetechnology.common.blocks.metal.logic.ValveCommonBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ITMessageTileSync implements ITMessage {
    private final BlockPos pos;
    private final CompoundTag nbt;

    public ITMessageTileSync(BlockPos pos, CompoundTag nbt) {
        this.pos = pos;
        this.nbt = nbt;
    }

    public ITMessageTileSync(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.nbt = buf.readNbt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeNbt(this.nbt);
    }

    @Override
    public void process(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ServerPlayer player = ctx.getSender();
        if (player != null) {
            ctx.enqueueWork(() -> {
                Level level = player.level();
                BlockEntity tile = level.getBlockEntity(this.pos);
                if (tile instanceof ValveCommonBlockEntity valve) {
                    valve.receiveMessageFromClient(this.nbt);
                }
            });
        }
    }
}
