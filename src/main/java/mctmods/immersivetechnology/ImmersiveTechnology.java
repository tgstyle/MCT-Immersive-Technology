package mctmods.immersivetechnology;

import mctmods.immersivetechnology.client.ITClientRenderHandler;
import mctmods.immersivetechnology.common.network.ITMessageContainerData;
import mctmods.immersivetechnology.common.network.ITMessageContainerUpdate;
import mctmods.immersivetechnology.common.network.ITPacketHandler;
import mctmods.immersivetechnology.core.ITClientConfig;
import mctmods.immersivetechnology.core.ITCommonConfig;
import mctmods.immersivetechnology.core.ITServerConfig;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.proxy.ClientProxy;
import mctmods.immersivetechnology.core.proxy.CommonProxy;
import mctmods.immersivetechnology.core.registration.ITContent;
import mctmods.immersivetechnology.core.registration.ITFluids;
import mctmods.immersivetechnology.core.registration.ITRecipeSerializers;
import mctmods.immersivetechnology.core.registration.ITRegistrationHolder;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import static mctmods.immersivetechnology.common.fluids.ITFluid.BUCKET_DISPENSE_BEHAVIOR;
import static mctmods.immersivetechnology.core.lib.ITLib.MODID;

@Mod(MODID)
public class ImmersiveTechnology {
    public static CommonProxy proxy = Util.make(() -> {
        if (FMLLoader.getDist().isClient()) return new ClientProxy();
        return new CommonProxy();
    });

    public ImmersiveTechnology(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ITLib.IT_LOGGER.info("IT Starting");
        modEventBus.addListener(this::commonSetup);
        ITRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);

        ITLib.IT_LOGGER.info("Adding ITRegistrationHolder Registries");
        ITRegistrationHolder.addRegistersToEventBus(modEventBus);

        ITLib.IT_LOGGER.info("Starting Proxy Mod Construction");
        CommonProxy.modConstruction(modEventBus);

        ITLib.IT_LOGGER.info("Initialzing Packet Handler");
        ITPacketHandler.initialize();

        ITLib.IT_LOGGER.info("Initialzing Mixins and adding Mixin Configuration");
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.immersivetechnology.json");

        context.registerConfig(ModConfig.Type.COMMON, ITCommonConfig.SPEC);
        context.registerConfig(ModConfig.Type.SERVER, ITServerConfig.SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, ITClientConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ITLib.IT_LOGGER.info("HELLO FROM COMMON SETUP");

        for (ITFluids.FluidEntry entry : ITFluids.ALL_ENTRIES) { DispenserBlock.registerBehavior(entry.getBucket(), BUCKET_DISPENSE_BEHAVIOR); }

        ITPacketHandler.registerMessage(ITMessageContainerUpdate.class, ITMessageContainerUpdate::new);
        ITPacketHandler.registerMessage(ITMessageContainerData.class, ITMessageContainerData::new);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) { ITLib.IT_LOGGER.info("HELLO from server starting"); }

    public static ResourceLocation rl(String path) { return ResourceLocation.fromNamespaceAndPath(MODID, path); }

    public static ResourceLocation makeTextureLocation(String name) { return rl("textures/gui/" + name + ".png"); }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            ITLib.IT_LOGGER.info("HELLO FROM CLIENT SETUP");
            ITLib.IT_LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

            ITClientRenderHandler.init(event);
            ITContent.initializeManualEntries();
            ITContent.registerContainersAndScreens();
        }
    }
}
