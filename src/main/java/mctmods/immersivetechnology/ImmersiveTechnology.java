package mctmods.immersivetechnology;

import mctmods.immersivetechnology.core.config.ClientConfig;
import mctmods.immersivetechnology.core.config.CommonConfig;
import mctmods.immersivetechnology.core.config.ServerConfig;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.proxy.ClientProxySupplier;
import mctmods.immersivetechnology.core.proxy.CommonProxy;
import mctmods.immersivetechnology.core.registration.BlockEntities;
import mctmods.immersivetechnology.core.registration.ModFluids;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import mctmods.immersivetechnology.core.util.ModBusHooks;
import mctmods.immersivetechnology.core.util.ToolGateHooks;
import static mctmods.immersivetechnology.core.lib.Reference.MODID;

import com.immersiveconvergence.api.fluid.FluidEntry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@SuppressWarnings("unused") @Mod(MODID) public class ImmersiveTechnology {
    public static final CommonProxy proxy = ClientProxySupplier.forDist();

    public ImmersiveTechnology(ModContainer container, Dist dist, IEventBus modBus) {
        Reference.IT_LOGGER.info("IT Starting");
        ModBusHooks.install(modBus);
        modBus.addListener(this::commonSetup);
        modBus.addListener(RegisterCapabilitiesEvent.class, this::registerCapabilities);
        Reference.IT_LOGGER.info("Starting Proxy Mod Construction");
        CommonProxy.modConstruction(modBus);
        Reference.IT_LOGGER.info("Initializing Packet Handler");
        container.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        NeoForge.EVENT_BUS.register(ImmersiveTechnology.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        Reference.IT_LOGGER.info("HELLO FROM COMMON SETUP");
        ToolGateHooks.install();
        FluidEntry.registerDispenserBehavior(ModFluids.ALL_ENTRIES);
    }


    @SubscribeEvent public static void onServerStarted(ServerStartedEvent event) {
        Reference.IT_LOGGER.info("HELLO FROM SERVER STARTING");
        MultiblockRegistry.init();
    }

    private void registerCapabilities(final RegisterCapabilitiesEvent event) {
        FluidEntry.registerBucketCapabilities(event, ModFluids.ALL_ENTRIES);
        BlockEntities.registerCapabilities(event);
    }
}
