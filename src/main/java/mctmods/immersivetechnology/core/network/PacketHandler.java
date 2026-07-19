package mctmods.immersivetechnology.core.network;

import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Reference.MODID)
public class PacketHandler {

    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToClient(
                MessageContainerUpdate.TYPE,
                MessageContainerUpdate.STREAM_CODEC,
                MessageContainerUpdate::handle
        );

        registrar.playToClient(
                MessageContainerData.TYPE,
                MessageContainerData.STREAM_CODEC,
                MessageContainerData::handle
        );

        registrar.playToServer(
                MessageTileSync.TYPE,
                MessageTileSync.STREAM_CODEC,
                MessageTileSync::handle
        );
    }

    public static void sendToPlayer(Player player, CustomPacketPayload message) {
        if (player instanceof ServerPlayer serverPlayer && message != null) {
            PacketDistributor.sendToPlayer(serverPlayer, message);
        }
    }

    public static void sendToServer(CustomPacketPayload message) {
        if (message != null) {
            PacketDistributor.sendToServer(message);
        }
    }

    public static void sendToDimension(ServerLevel level, CustomPacketPayload message) {
        if (level != null && message != null) {
            PacketDistributor.sendToPlayersInDimension(level, message);
        }
    }

    public static void sendToAll(CustomPacketPayload message) {
        if (message != null) {
            PacketDistributor.sendToAllPlayers(message);
        }
    }
}
