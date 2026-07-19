package mctmods.immersivetechnology.core.network;

import com.mojang.datafixers.util.Pair;
import mctmods.immersivetechnology.common.gui.helper.GenericDataSerializers;
import mctmods.immersivetechnology.common.gui.helper.GenericDataSerializers.DataPair;
import mctmods.immersivetechnology.common.gui.helper.ContainerMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record MessageContainerData(List<Pair<Integer, DataPair<?>>> synced) implements IMessage {
    public MessageContainerData(FriendlyByteBuf buf) { this(readSynced(buf)); }

    private static List<Pair<Integer, DataPair<?>>> readSynced(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<Pair<Integer, DataPair<?>>> synced = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int index = buf.readVarInt();
            DataPair<?> dataPair = GenericDataSerializers.read(buf);
            synced.add(Pair.of(index, dataPair));
        }
        return synced;
    }

    @Override public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(synced.size());
        for (Pair<Integer, DataPair<?>> pair : synced) {
            buf.writeVarInt(pair.getFirst());
            pair.getSecond().write(buf);
        }
    }

    @Override public void process(Supplier<Context> context) {
        context.get().enqueueWork(() -> {
            assert Minecraft.getInstance().player != null;
            AbstractContainerMenu currentContainer = Minecraft.getInstance().player.containerMenu;
            if (currentContainer instanceof ContainerMenu itContainer) { itContainer.receiveSync(synced); }
        });
    }
}
