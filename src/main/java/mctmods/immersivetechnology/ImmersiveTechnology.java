package mctmods.immersivetechnology;


import mctmods.immersivetechnology.core.ClientConfig;
import mctmods.immersivetechnology.core.CommonConfig;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.proxy.ClientProxySupplier;
import mctmods.immersivetechnology.core.proxy.CommonProxy;
import mctmods.immersivetechnology.core.registration.BlockEntities;
import mctmods.immersivetechnology.core.registration.ModFluids;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import static mctmods.immersivetechnology.core.lib.Reference.MODID;
import com.immersiveconvergence.api.fluid.FluidEntry;
import com.immersiveconvergence.api.multiblock.ClearTank;
import mctmods.immersivetechnology.common.items.FormationTool;
import com.immersiveconvergence.api.multiblock.MultiblockBuilder;
import com.immersiveconvergence.api.block.BlockToolGates;
import mctmods.immersivetechnology.core.registration.ModTags;

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
        MultiblockBuilder.modBus = modBus;
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
        MachineTemplateMultiblock.templateModeGate = () -> ServerConfig.DISASSEMBLY_MODE.get() == ServerConfig.DisassemblyMode.TEMPLATE_BLOCKS;
        ClearTank.additionalTool = stack -> stack.getItem() instanceof FormationTool;
        BlockToolGates.isFormationTool = stack -> stack.is(ModTags.formationTools);
        BlockToolGates.isScrewdriver = stack -> stack.is(ModTags.screwdrivers);
        BlockToolGates.descFlavour = Reference.DESC_FLAVOUR;
        BlockToolGates.descInfo = Reference.DESC_INFO;
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
