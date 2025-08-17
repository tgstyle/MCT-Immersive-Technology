package mctmods.immersivetechnology.common.network;

import mctmods.immersivetechnology.common.blocks.gui.helper.ITContainerMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ITMessageContainerUpdate implements ITMessage {
    private final int windowId;
    private final CompoundTag nbt;

    public ITMessageContainerUpdate(FriendlyByteBuf buf) {
        this.windowId = buf.readByte();
        this.nbt = buf.readNbt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByte(this.windowId);
        buf.writeNbt(this.nbt);
    }

    @Override
    public void process(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ServerPlayer player = ctx.getSender();
        if (player != null) {
            ctx.enqueueWork(() -> {
                player.resetLastActionTime();
                if (player.containerMenu.containerId == this.windowId) {
                    AbstractContainerMenu menu = player.containerMenu;
                    if (menu instanceof ITContainerMenu itMenu) { itMenu.receiveMessageFromScreen(this.nbt); }
                }
            });
        }
    }
}
