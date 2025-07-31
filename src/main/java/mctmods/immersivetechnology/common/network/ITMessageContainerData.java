package mctmods.immersivetechnology.common.network;

import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers.DataPair;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent.Context;
import mctmods.immersivetechnology.common.blocks.gui.helper.ITContainerMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ITMessageContainerData implements ITINetMessage {
    private final List<Pair<Integer, DataPair<?>>> synced;

    public ITMessageContainerData(List<Pair<Integer, DataPair<?>>> synced) { this.synced = synced; }

    public ITMessageContainerData(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.synced = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int index = buf.readVarInt();
            DataPair<?> dataPair = GenericDataSerializers.read(buf);
            this.synced.add(Pair.of(index, dataPair));
        }
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(synced.size());
        for (Pair<Integer, DataPair<?>> pair : synced) {
            buf.writeVarInt(pair.getFirst());
            pair.getSecond().write(buf);
        }
    }

    @Override
    public void process(Supplier<Context> context) {
        context.get().enqueueWork(() -> {
            assert Minecraft.getInstance().player != null;
            AbstractContainerMenu currentContainer = Minecraft.getInstance().player.containerMenu;
            if (currentContainer instanceof ITContainerMenu itContainer) { itContainer.receiveSync(synced); }
        });
    }
}
