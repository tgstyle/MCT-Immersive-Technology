package mctmods.immersivetechnology.core.network;

import mctmods.immersivetechnology.core.lib.ITLib;
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
@EventBusSubscriber(modid = ITLib.MODID)
public class ITPacketHandler {

    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToClient(
                ITMessageContainerUpdate.TYPE,
                ITMessageContainerUpdate.STREAM_CODEC,
                ITMessageContainerUpdate::handle
        );

        registrar.playToClient(
                ITMessageContainerData.TYPE,
                ITMessageContainerData.STREAM_CODEC,
                ITMessageContainerData::handle
        );

        registrar.playToServer(
                ITMessageTileSync.TYPE,
                ITMessageTileSync.STREAM_CODEC,
                ITMessageTileSync::handle
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
