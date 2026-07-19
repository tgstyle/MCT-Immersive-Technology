package mctmods.immersivetechnology;

import java.util.ArrayList;
import java.util.List;

import mctmods.immersivetechnology.common.multiblocks.helper.QueueProcessor;
import mctmods.immersivetechnology.common.multiblocks.helper.ModTemplateMultiblock;
import mctmods.immersivetechnology.core.ClientConfig;
import mctmods.immersivetechnology.core.CommonConfig;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.integration.top.OneProbeIMCHelper;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.proxy.ClientProxySupplier;
import mctmods.immersivetechnology.core.proxy.CommonProxy;
import mctmods.immersivetechnology.core.registration.BlockEntities;
import mctmods.immersivetechnology.core.registration.ModFluids;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import mctmods.immersivetechnology.core.util.loot.LootFunctions;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
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

import static mctmods.immersivetechnology.common.fluids.ModFluid.BUCKET_DISPENSE_BEHAVIOR;
import static mctmods.immersivetechnology.core.lib.Reference.MODID;

@SuppressWarnings("unused")
@Mod(MODID)
public class ImmersiveTechnology {
    public static final CommonProxy proxy;

    static {
        if (FMLLoader.getDist().isClient()) { proxy = ClientProxySupplier.get(); }
        else { proxy = new CommonProxy(); }
    }

    public ImmersiveTechnology(ModContainer container, Dist dist, IEventBus modBus) {
        Reference.IT_LOGGER.info("IT Starting");
        Reference.MOD_BUS = modBus;
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::enqueueIMC);
        modBus.addListener(RegisterCapabilitiesEvent.class, this::registerCapabilities);
        Reference.IT_LOGGER.info("Starting Proxy Mod Construction");
        CommonProxy.modConstruction(modBus);
        LootFunctions.init(modBus);
        Reference.IT_LOGGER.info("Initializing Packet Handler");
        container.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        NeoForge.EVENT_BUS.register(ImmersiveTechnology.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        Reference.IT_LOGGER.info("HELLO FROM COMMON SETUP");
        for (ModFluids.FluidEntry entry : ModFluids.ALL_ENTRIES) { DispenserBlock.registerBehavior(entry.getBucket(), BUCKET_DISPENSE_BEHAVIOR); }
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        if (net.neoforged.fml.ModList.get().isLoaded("theoneprobe")) { OneProbeIMCHelper.register(); }
    }

    @SubscribeEvent public static void onServerTick(ServerTickEvent.Post event) {
        List<QueueProcessor> copy = new ArrayList<>(ModTemplateMultiblock.pendingQueues);
        copy.forEach(QueueProcessor::tick);
        ModTemplateMultiblock.pendingQueues.removeIf(QueueProcessor::isEmpty);
    }

    @SubscribeEvent public static void onServerStarted(ServerStartedEvent event) {
        Reference.IT_LOGGER.info("HELLO FROM SERVER STARTING");
        MultiblockRegistry.init();
    }

    private void registerCapabilities(final RegisterCapabilitiesEvent event) {
        for (ModFluids.FluidEntry entry : ModFluids.ALL_ENTRIES) { event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidBucketWrapper(stack), entry.getBucket()); }
        BlockEntities.registerCapabilities(event);
    }
}
