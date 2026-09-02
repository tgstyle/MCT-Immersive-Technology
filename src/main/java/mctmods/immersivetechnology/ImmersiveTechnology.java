package mctmods.immersivetechnology;

import mctmods.immersivetechnology.core.ClientConfig;
import mctmods.immersivetechnology.core.CommonConfig;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.network.PacketHandler;
import mctmods.immersivetechnology.core.proxy.ClientProxySupplier;
import mctmods.immersivetechnology.core.proxy.CommonProxy;
import mctmods.immersivetechnology.core.registration.ModFluids;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static mctmods.immersivetechnology.core.lib.Reference.MODID;
import com.immersiveconvergence.api.fluid.FluidEntry;
import com.immersiveconvergence.api.multiblock.ClearTank;
import mctmods.immersivetechnology.common.items.FormationTool;
import com.immersiveconvergence.api.block.BlockToolGates;
import mctmods.immersivetechnology.core.registration.ModTags;

@SuppressWarnings("unused")
@Mod(MODID)
public class ImmersiveTechnology {
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
        MachineTemplateMultiblock.templateModeGate = () -> ServerConfig.DISASSEMBLY_MODE.get() == ServerConfig.DisassemblyMode.TEMPLATE_BLOCKS;
        ClearTank.additionalTool = stack -> stack.getItem() instanceof FormationTool;
        BlockToolGates.isFormationTool = stack -> stack.is(ModTags.formationTools);
        BlockToolGates.isScrewdriver = stack -> stack.is(ModTags.screwdrivers);
        BlockToolGates.descFlavour = Reference.DESC_FLAVOUR;
        BlockToolGates.descInfo = Reference.DESC_INFO;
        FluidEntry.registerDispenserBehavior(ModFluids.ALL_ENTRIES);
    }
}
