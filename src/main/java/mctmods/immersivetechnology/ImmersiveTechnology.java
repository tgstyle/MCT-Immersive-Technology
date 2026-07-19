package mctmods.immersivetechnology;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import mctmods.immersivetechnology.common.multiblocks.helper.QueueProcessor;
import mctmods.immersivetechnology.common.multiblocks.helper.ModTemplateMultiblock;
import mctmods.immersivetechnology.core.integration.top.OneProbeHelper;
import mctmods.immersivetechnology.core.network.MessageContainerData;
import mctmods.immersivetechnology.core.network.MessageContainerUpdate;
import mctmods.immersivetechnology.core.network.PacketHandler;
import mctmods.immersivetechnology.core.util.loot.LootFunctions;
import mctmods.immersivetechnology.core.ClientConfig;
import mctmods.immersivetechnology.core.CommonConfig;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.proxy.ClientProxySupplier;
import mctmods.immersivetechnology.core.proxy.CommonProxy;
import mctmods.immersivetechnology.core.registration.ModFluids;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.MinecraftForge;
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

import static mctmods.immersivetechnology.common.fluids.ModFluid.BUCKET_DISPENSE_BEHAVIOR;
import static mctmods.immersivetechnology.core.lib.Reference.MODID;

@SuppressWarnings("unused")
@Mod(MODID)
public class ImmersiveTechnology {
    public static final CommonProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxySupplier::get, () -> CommonProxy::new);

    public ImmersiveTechnology(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        Reference.IT_LOGGER.info("IT Starting");
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::enqueueIMC);
        Reference.IT_LOGGER.info("Starting Proxy Mod Construction");
        CommonProxy.modConstruction(modEventBus);
        LootFunctions.init(modEventBus);
        Reference.IT_LOGGER.info("Initializing Packet Handler");
        PacketHandler.initialize();
        Reference.IT_LOGGER.info("Initializing Mixins and adding Mixin Configuration");
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.immersivetechnology.json");
        context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        context.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(ImmersiveTechnology.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        Reference.IT_LOGGER.info("HELLO FROM COMMON SETUP");
        for (ModFluids.FluidEntry entry : ModFluids.ALL_ENTRIES) {
            DispenserBlock.registerBehavior(entry.getBucket(), BUCKET_DISPENSE_BEHAVIOR);
        }
        PacketHandler.registerMessage(MessageContainerUpdate.class, MessageContainerUpdate::new);
        PacketHandler.registerMessage(MessageContainerData.class, MessageContainerData::new);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        if (ModList.get().isLoaded("theoneprobe")) {
            InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> (Function<mcjty.theoneprobe.api.ITheOneProbe, Void>) top -> {
                OneProbeHelper.register(top);
                return null;
            });
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            List<QueueProcessor> copy = new ArrayList<>(ModTemplateMultiblock.pendingQueues);
            copy.forEach(QueueProcessor::tick);
            ModTemplateMultiblock.pendingQueues.removeIf(QueueProcessor::isEmpty);
        }
    }

    @SubscribeEvent public void onServerStarting(ServerStartingEvent event) {
        Reference.IT_LOGGER.info("HELLO FROM SERVER STARTING");
    }
}
