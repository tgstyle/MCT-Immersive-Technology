package mctmods.immersivetechnology.core.network;

import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nonnull;
import java.util.function.Function;
import com.immersiveconvergence.api.network.MessageTileSync;
import com.immersiveconvergence.api.network.INetworkMessage;

@SuppressWarnings("unused")
public class PacketHandler {
    public static final String NET_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder.named(Reference.rl("main")).networkProtocolVersion(() -> NET_VERSION).serverAcceptedVersions(NET_VERSION::equals).clientAcceptedVersions(NET_VERSION::equals).simpleChannel();

    public static void initialize() {
        registerMessage(MessageContainerUpdate.class, MessageContainerUpdate::new);
        registerMessage(MessageContainerData.class, MessageContainerData::new);
        registerMessage(OSDRequestMessage.class, OSDRequestMessage::new);
        registerMessage(OSDSyncMessage.class, OSDSyncMessage::new);
        registerMessage(OSDSyncBlock.class, OSDSyncBlock::new);
        registerMessage(MessageTileSync.class, MessageTileSync::new);
    }

    private static int id = 0;

    public static <T extends INetworkMessage> void registerMessage(Class<T> type, Function<FriendlyByteBuf, T> decoder) { INSTANCE.registerMessage(id++, type, INetworkMessage::toBytes, decoder, (t, ctx) -> { t.process(ctx); ctx.get().setPacketHandled(true); }); }

    public static <MSG> void sendToPlayer(Player player, @Nonnull MSG message) { if (player instanceof ServerPlayer serverPlayer) INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), message); }

    public static <MSG> void sendToServer(MSG message) { if (message == null) return; INSTANCE.send(PacketDistributor.SERVER.noArg(), message); }

    public static <MSG> void sendToDimension(ResourceKey<Level> dim, MSG message) { if (message == null) return; INSTANCE.send(PacketDistributor.DIMENSION.with(() -> dim), message); }

    public static <MSG> void sendAll(MSG message) { if (message == null) return; INSTANCE.send(PacketDistributor.ALL.noArg(), message); }
}
