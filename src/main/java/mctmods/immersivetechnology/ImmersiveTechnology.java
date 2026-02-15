package mctmods.immersivetechnology;

import java.util.function.Function;

import mctmods.immersivetechnology.api.HeatCapabilities;
import mctmods.immersivetechnology.api.MechanicalCapabilities;
import mctmods.immersivetechnology.api.capability.IHeatConsumer;
import mctmods.immersivetechnology.api.capability.IHeatProvider;
import mctmods.immersivetechnology.api.capability.IMechanicalEnergyConsumer;
import mctmods.immersivetechnology.api.capability.IMechanicalEnergyProvider;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.helper.ITQueueProcessor;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import static mctmods.immersivetechnology.common.fluids.ITFluid.BUCKET_DISPENSE_BEHAVIOR;
import static mctmods.immersivetechnology.core.lib.ITLib.MODID;

@SuppressWarnings("unused")
@Mod(MODID)
public class ImmersiveTechnology {
    public static final CommonProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxySupplier::get, () -> CommonProxy::new);

    public ImmersiveTechnology(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ITLib.IT_LOGGER.info("IT Starting");
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::enqueueIMC);
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
        MinecraftForge.EVENT_BUS.register(ImmersiveTechnology.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ITLib.IT_LOGGER.info("HELLO FROM COMMON SETUP");
        for (ITFluids.FluidEntry entry : ITFluids.ALL_ENTRIES) {
            DispenserBlock.registerBehavior(entry.getBucket(), BUCKET_DISPENSE_BEHAVIOR);
        }
        ITPacketHandler.registerMessage(ITMessageContainerUpdate.class, ITMessageContainerUpdate::new);
        ITPacketHandler.registerMessage(ITMessageContainerData.class, ITMessageContainerData::new);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        if (ModList.get().isLoaded("theoneprobe")) {
            InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> (Function<mcjty.theoneprobe.api.ITheOneProbe, Void>) top -> {
                OneProbeHelper.register(top);
                return null;
            });
        }
    }

    private static final CapabilityToken<IHeatProvider> HEAT_PROVIDER_TOKEN = new CapabilityToken<>() {};
    private static final CapabilityToken<IHeatConsumer> HEAT_CONSUMER_TOKEN = new CapabilityToken<>() {};
    private static final CapabilityToken<IMechanicalEnergyProvider> MECHANICAL_PROVIDER_TOKEN = new CapabilityToken<>() {};
    private static final CapabilityToken<IMechanicalEnergyConsumer> MECHANICAL_CONSUMER_TOKEN = new CapabilityToken<>() {};

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IHeatProvider.class);
        event.register(IHeatConsumer.class);
        event.register(IMechanicalEnergyProvider.class);
        event.register(IMechanicalEnergyConsumer.class);
        HeatCapabilities.HEAT_PROVIDER_CAPABILITY = CapabilityManager.get(HEAT_PROVIDER_TOKEN);
        HeatCapabilities.HEAT_CONSUMER_CAPABILITY = CapabilityManager.get(HEAT_CONSUMER_TOKEN);
        MechanicalCapabilities.MECHANICAL_PROVIDER_CAPABILITY = CapabilityManager.get(MECHANICAL_PROVIDER_TOKEN);
        MechanicalCapabilities.MECHANICAL_CONSUMER_CAPABILITY = CapabilityManager.get(MECHANICAL_CONSUMER_TOKEN);
    }

    @SubscribeEvent public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ITTemplateMultiblock.pendingQueues.forEach(ITQueueProcessor::tick);
            ITTemplateMultiblock.pendingQueues.removeIf(ITQueueProcessor::isEmpty);
        }
    }

    @SubscribeEvent public void onServerStarting(ServerStartingEvent event) {
        ITLib.IT_LOGGER.info("HELLO FROM SERVER STARTING");
    }
}
