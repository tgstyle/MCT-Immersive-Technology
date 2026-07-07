package mctmods.immersivetechnology.core.network;

import mctmods.immersivetechnology.core.util.TranslationKey;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ITOSDSyncBlock implements ITIMessage {
    private final String key;
    private final int distance;

    public ITOSDSyncBlock(String key, int distance) { this.key = key; this.distance = distance; }

    public ITOSDSyncBlock(FriendlyByteBuf buf) { this.key = buf.readUtf(); this.distance = buf.readInt(); }

    @Override public void toBytes(FriendlyByteBuf buf) { buf.writeUtf(key); buf.writeInt(distance); }

    @Override public void process(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            if (ctx.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                TranslationKey transKey = TranslationKey.valueOf(key);
                String actualKey = transKey.getLocation();
                Component msg;
                if (distance >= 0) { msg = Component.translatable(actualKey, distance); }
                else { msg = Component.translatable(actualKey); }
                Minecraft.getInstance().gui.getChat().addMessage(msg);
            }
        });
        ctx.setPacketHandled(true);
    }
}
