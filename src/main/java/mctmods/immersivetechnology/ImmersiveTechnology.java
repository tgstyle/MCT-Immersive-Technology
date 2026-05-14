package mctmods.immersivetechnology;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import mctmods.immersivetechnology.common.multiblocks.helper.ITQueueProcessor;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.core.integration.top.OneProbeHelper;
import mctmods.immersivetechnology.core.network.ITMessageContainerData;
import mctmods.immersivetechnology.core.network.ITMessageContainerUpdate;
import mctmods.immersivetechnology.core.network.ITPacketHandler;
import mctmods.immersivetechnology.core.util.loot.ITLootFunctions;
import mctmods.immersivetechnology.core.ITClientConfig;
import mctmods.immersivetechnology.core.ITCommonConfig;
import mctmods.immersivetechnology.core.ITServerConfig;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.proxy.ClientProxySupplier;
import mctmods.immersivetechnology.core.proxy.CommonProxy;
import mctmods.immersivetechnology.core.registration.ITFluids;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import static mctmods.immersivetechnology.common.fluids.ITFluid.BUCKET_DISPENSE_BEHAVIOR;
import static mctmods.immersivetechnology.core.lib.ITLib.MODID;

@SuppressWarnings("unused")
@Mod(MODID)
public class ImmersiveTechnology {
    public static final CommonProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxySupplier::get, () -> CommonProxy::new);

    public ImmersiveTechnology(ModLoadingContext context) {
        IEventBus modEventBus = context.getActiveContainer().getEventBus();
        ITLib.IT_LOGGER.info("IT Starting");
        modEventBus.addListener(this::commonSetup);
        ITLib.IT_LOGGER.info("Starting Proxy Mod Construction");
        CommonProxy.modConstruction(modEventBus);
        ITLootFunctions.init(modEventBus);
        ITLib.IT_LOGGER.info("Initializing Packet Handler");
        ITPacketHandler.initialize();
        ITLib.IT_LOGGER.info("Initializing Mixins and adding Mixin Configuration");
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.immersivetechnology.json");
        context.registerConfig(ModConfig.Type.COMMON, ITCommonConfig.SPEC);
        context.registerConfig(ModConfig.Type.SERVER, ITServerConfig.SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, ITClientConfig.SPEC);
        NeoForge.EVENT_BUS.register(ImmersiveTechnology.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ITLib.IT_LOGGER.info("HELLO FROM COMMON SETUP");
        for (ITFluids.FluidEntry entry : ITFluids.ALL_ENTRIES) {
            DispenserBlock.registerBehavior(entry.getBucket(), BUCKET_DISPENSE_BEHAVIOR);
        }
        ITPacketHandler.registerMessage(ITMessageContainerUpdate.class, ITMessageContainerUpdate::new);
        ITPacketHandler.registerMessage(ITMessageContainerData.class, ITMessageContainerData::new);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            List<ITQueueProcessor> copy = new ArrayList<>(ITTemplateMultiblock.pendingQueues);
            copy.forEach(ITQueueProcessor::tick);
            ITTemplateMultiblock.pendingQueues.removeIf(ITQueueProcessor::isEmpty);
        }
    }

    @SubscribeEvent public void onServerStarting(ServerStartingEvent event) {
        ITLib.IT_LOGGER.info("HELLO FROM SERVER STARTING");
    }
}
