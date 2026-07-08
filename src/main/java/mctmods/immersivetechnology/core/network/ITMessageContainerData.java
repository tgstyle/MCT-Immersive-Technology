package mctmods.immersivetechnology.core.network;

import com.mojang.datafixers.util.Pair;
import mctmods.immersivetechnology.common.gui.helper.ITGenericDataSerializers;
import mctmods.immersivetechnology.common.gui.helper.ITGenericDataSerializers.DataPair;
import mctmods.immersivetechnology.common.gui.helper.ITContainerMenu;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public record ITMessageContainerData(List<Pair<Integer, DataPair<?>>> synced) implements CustomPacketPayload {

    public ITMessageContainerData(RegistryFriendlyByteBuf buf) { this(readSynced(buf)); }

    private static List<Pair<Integer, DataPair<?>>> readSynced(RegistryFriendlyByteBuf buf) {
        int size = buf.readInt();
        List<Pair<Integer, DataPair<?>>> synced = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int index = buf.readVarInt();
            DataPair<?> dataPair = ITGenericDataSerializers.read(buf);
            synced.add(Pair.of(index, dataPair));
        }
        return synced;
    }

    public static final CustomPacketPayload.Type<ITMessageContainerData> TYPE = new CustomPacketPayload.Type<>(ITLib.rl("containerdata"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ITMessageContainerData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> data.toBytes(buf),
            ITMessageContainerData::new
    );

    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeInt(synced.size());
        for (Pair<Integer, DataPair<?>> pair : synced) {
            buf.writeVarInt(pair.getFirst());
            pair.getSecond().write(buf);
        }
    }

    @Override public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ITMessageContainerData message, IPayloadContext context) {
        context.enqueueWork(() -> {
            assert Minecraft.getInstance().player != null;
            AbstractContainerMenu currentContainer = Minecraft.getInstance().player.containerMenu;
            if (currentContainer instanceof ITContainerMenu itContainer) { itContainer.receiveSync(message.synced()); }
        });
    }
}
