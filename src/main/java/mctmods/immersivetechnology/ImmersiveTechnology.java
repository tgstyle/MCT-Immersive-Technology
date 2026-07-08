package mctmods.immersivetechnology;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import mctmods.immersivetechnology.common.multiblocks.helper.ITQueueProcessor;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.core.ITClientConfig;
import mctmods.immersivetechnology.core.ITCommonConfig;
import mctmods.immersivetechnology.core.ITServerConfig;
import mctmods.immersivetechnology.core.integration.top.OneProbeHelper;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.proxy.ClientProxySupplier;
import mctmods.immersivetechnology.core.proxy.CommonProxy;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import mctmods.immersivetechnology.core.registration.ITFluids;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import mctmods.immersivetechnology.core.util.loot.ITLootFunctions;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;

import static mctmods.immersivetechnology.common.fluids.ITFluid.BUCKET_DISPENSE_BEHAVIOR;
import static mctmods.immersivetechnology.core.lib.ITLib.MODID;

@SuppressWarnings("unused")
@Mod(MODID)
public class ImmersiveTechnology {
    public static final CommonProxy proxy;

    static {
        if (FMLLoader.getDist().isClient()) { proxy = ClientProxySupplier.get(); }
        else { proxy = new CommonProxy(); }
    }

    public ImmersiveTechnology(ModContainer container, Dist dist, IEventBus modBus) {
        ITLib.IT_LOGGER.info("IT Starting");
        ITLib.MOD_BUS = modBus;
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::enqueueIMC);
        modBus.addListener(RegisterCapabilitiesEvent.class, this::registerCapabilities);
        ITLib.IT_LOGGER.info("Starting Proxy Mod Construction");
        CommonProxy.modConstruction(modBus);
        ITLootFunctions.init(modBus);
        ITLib.IT_LOGGER.info("Initializing Packet Handler");
        container.registerConfig(ModConfig.Type.COMMON, ITCommonConfig.SPEC);
        container.registerConfig(ModConfig.Type.SERVER, ITServerConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, ITClientConfig.SPEC);
        NeoForge.EVENT_BUS.register(ImmersiveTechnology.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ITLib.IT_LOGGER.info("HELLO FROM COMMON SETUP");
        for (ITFluids.FluidEntry entry : ITFluids.ALL_ENTRIES) { DispenserBlock.registerBehavior(entry.getBucket(), BUCKET_DISPENSE_BEHAVIOR); }
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        if (net.neoforged.fml.ModList.get().isLoaded("theoneprobe")) {
            InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> (Function<mcjty.theoneprobe.api.ITheOneProbe, Void>) top -> {
                OneProbeHelper.register(top);
                return null;
            });
        }
    }

    @SubscribeEvent public static void onServerTick(ServerTickEvent.Post event) {
        List<ITQueueProcessor> copy = new ArrayList<>(ITTemplateMultiblock.pendingQueues);
        copy.forEach(ITQueueProcessor::tick);
        ITTemplateMultiblock.pendingQueues.removeIf(ITQueueProcessor::isEmpty);
    }

    @SubscribeEvent public static void onServerStarted(ServerStartedEvent event) {
        ITLib.IT_LOGGER.info("HELLO FROM SERVER STARTING");
        ITMultiblockProvider.init();
    }

    private void registerCapabilities(final RegisterCapabilitiesEvent event) {
        for (ITFluids.FluidEntry entry : ITFluids.ALL_ENTRIES) { event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidBucketWrapper(stack), entry.getBucket()); }
        ITBlockEntities.registerCapabilities(event);
    }
}
