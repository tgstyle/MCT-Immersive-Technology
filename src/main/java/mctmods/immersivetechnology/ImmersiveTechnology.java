package mctmods.immersivetechnology;

import mctmods.immersivetechnology.core.config.ClientConfig;
import mctmods.immersivetechnology.core.config.CommonConfig;
import mctmods.immersivetechnology.core.config.ServerConfig;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.network.PacketHandler;
import mctmods.immersivetechnology.core.proxy.ClientProxySupplier;
import mctmods.immersivetechnology.core.proxy.CommonProxy;
import mctmods.immersivetechnology.core.registration.ModFluids;
import mctmods.immersivetechnology.core.util.ToolGateHooks;
import static mctmods.immersivetechnology.core.lib.Reference.MODID;

import com.immersiveconvergence.api.fluid.FluidEntry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@SuppressWarnings("unused") @Mod(MODID) public class ImmersiveTechnology {
    public static final CommonProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxySupplier::get, () -> CommonProxy::new);

    public ImmersiveTechnology(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        Reference.IT_LOGGER.info("IT Starting");
        modEventBus.addListener(this::commonSetup);
        Reference.IT_LOGGER.info("Starting Proxy Mod Construction");
        CommonProxy.modConstruction(modEventBus);
        Reference.IT_LOGGER.info("Initializing Packet Handler");
        PacketHandler.initialize();
        context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        context.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        Reference.IT_LOGGER.info("HELLO FROM COMMON SETUP");
        ToolGateHooks.install();
        FluidEntry.registerDispenserBehavior(ModFluids.ALL_ENTRIES);
    }
}
