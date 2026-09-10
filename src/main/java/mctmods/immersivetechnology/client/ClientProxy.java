package mctmods.immersivetechnology.client;

import com.immersiveconvergence.api.ICMods;
import com.immersiveconvergence.common.event.ICTickingRegistry;
import com.immersiveconvergence.api.client.ICClientUtils;
import com.immersiveconvergence.api.ICIntegration;
import com.immersiveconvergence.api.client.ICModels;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces;
import com.immersiveconvergence.api.manual.ICManual;
import com.immersiveconvergence.api.client.ICSoundHandler;
import com.immersiveconvergence.api.client.split.MultiblockTextureHandler;
import com.immersiveconvergence.api.client.split.SplitModelHandler;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.IGuiTile;
import com.immersiveconvergence.api.particles.ParticleSettings;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.client.gui.*;
import mctmods.immersivetechnology.client.models.ModelConfigurableSides;
import mctmods.immersivetechnology.common.util.compat.jei.ITMultiblockIngredients;
import mctmods.immersivetechnology.client.render.fluid.TileRenderBarrelOpen;
import mctmods.immersivetechnology.client.render.multiblock.*;
import mctmods.immersivetechnology.client.render.multiblock.withanimation.TileRenderHighPressureSteamTurbine;
import mctmods.immersivetechnology.client.render.animation.TileRenderSolarReflector;
import mctmods.immersivetechnology.client.render.multiblock.withanimation.TileRenderSteamTurbine;
import mctmods.immersivetechnology.client.render.multiblock.withanimation.TileRendererGasTurbine;
import mctmods.immersivetechnology.client.render.fluid.TileRenderSteelSheetmetalTank;
import mctmods.immersivetechnology.client.render.animation.TileRenderAdvancedCokeOvenBaseheater;
import mctmods.immersivetechnology.common.CommonProxy;
import mctmods.immersivetechnology.common.Config.ITConfig;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.BlockITFluid;
import mctmods.immersivetechnology.common.blocks.BlockValve.BlockType_Valve;
import mctmods.immersivetechnology.common.blocks.connectors.tileentities.TileEntityTimer;
import mctmods.immersivetechnology.common.blocks.connectors.types.BlockType_Connectors;
import mctmods.immersivetechnology.common.blocks.wooden.tileentities.TileEntityCrate;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityBarrelOpen;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityFluidValve;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityLoadController;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityStackLimiter;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityTrashItem;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityAdvancedCokeOvenBaseheater;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalBarrel;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalDevice;
import mctmods.immersivetechnology.common.items.ItemITBase;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.*;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartAlternator;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartBoilerLiquid;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartBoilerSolid;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartBoilerTank;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentitiesmultiblockpart.TileEntityITMultiblockPartCoolingTower;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartDistiller;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartElectrolyticCrucibleBattery;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartGasTurbine;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartHeatExchanger;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartHighPressureSteamTurbine;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartMeltingCrucible;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartRadiator;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSolarMelter;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSolarReflector;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSolarTower;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSteamTurbine;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSteelSheetmetalTank;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityAdvancedCokeOvenMaster;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentitiesmultiblockpart.TileEntityITMultiblockPartAdvancedCokeOven;
import mctmods.immersivetechnology.common.util.ITLogger;
import mctmods.immersivetechnology.common.util.ITUtils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Mod.EventBusSubscriber(modid = ImmersiveTechnology.MODID, value = Side.CLIENT)
public class ClientProxy extends CommonProxy {
    public static final String CAT_IT = "it";
    public static final String CAT_POWER = "it_power";
    public static final String CAT_PROCESSING = "it_processing";

    @Override public void preInit() {
        ICClientUtils.mc().getFramebuffer().enableStencil();
        ParticleSettings.particleCollide = () -> ITConfig.Client.particles.collide;
        ICModels.registerOBJLoader();
        OBJLoader.INSTANCE.addDomain(ImmersiveTechnology.MODID);
        ICModels.addOBJDomain(ImmersiveTechnology.MODID);
        MultiblockTextureHandler.register(ImmersiveTechnology.MODID);
        MinecraftForge.EVENT_BUS.register(this);
        ModelLoaderRegistry.registerLoader(new ModelConfigurableSides.Loader());
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock_alternator", () -> TileEntityITMultiblockPartAlternator.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock_boiler_tank", true, () -> TileEntityITMultiblockPartBoilerTank.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock1_boiler_liquid", true, () -> TileEntityITMultiblockPartBoilerLiquid.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock2_boiler_solid", true, () -> TileEntityITMultiblockPartBoilerSolid.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "stone_multiblock_cooling_tower", () -> TileEntityITMultiblockPartCoolingTower.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock_distiller", true, () -> TileEntityITMultiblockPartDistiller.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock_solar_tower", true, () -> TileEntityITMultiblockPartSolarTower.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock_steam_turbine", true, () -> TileEntityITMultiblockPartSteamTurbine.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock_steel_tank", () -> TileEntityITMultiblockPartSteelSheetmetalTank.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock1_electrolytic_crucible_battery", true, () -> TileEntityITMultiblockPartElectrolyticCrucibleBattery.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock1_gas_turbine", true, () -> TileEntityITMultiblockPartGasTurbine.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock1_heat_exchanger", true, () -> TileEntityITMultiblockPartHeatExchanger.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock1_high_pressure_steam_turbine", true, () -> TileEntityITMultiblockPartHighPressureSteamTurbine.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock1_melting_crucible", true, () -> TileEntityITMultiblockPartMeltingCrucible.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock1_radiator", () -> TileEntityITMultiblockPartRadiator.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock1_solar_melter", true, () -> TileEntityITMultiblockPartSolarMelter.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "stone_multiblock_advanced_coke_oven", () -> TileEntityITMultiblockPartAdvancedCokeOven.instance);
    }

    @SubscribeEvent public void PlayerChangedDimensions(PlayerEvent.PlayerChangedDimensionEvent e) { ICSoundHandler.deleteAllSounds(); }

    @SubscribeEvent public void PlayerLeftSession(PlayerEvent.PlayerLoggedOutEvent e) { ICSoundHandler.deleteAllSounds(); }

    @SubscribeEvent public void PlayerDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent e) { ICSoundHandler.deleteAllSounds(); }

    @SubscribeEvent public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) { ICTickingRegistry.drain(Minecraft.getMinecraft().world); }
    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent public static void registerModels(ModelRegistryEvent evt) {
        ICModels.registerConnectorForRender("conn_timer", new ResourceLocation("immersivetech:block/connector/connector_timer/connector_timer.obj.ie"));
        ICModels.registerConnectorForRender("conn_con_net", new ResourceLocation("immersivetech:block/connector/connectors_con_net.obj.ie"));
        ICModels.registerConnectorForRender("valve_load", new ResourceLocation("immersivetech:block/metal/valve_load/valve_load.obj.ie"));
        for (Block block : ITContent.registeredITBlocks) {
            final ResourceLocation loc = Block.REGISTRY.getNameForObject(block);
            Item blockItem = Item.getItemFromBlock(block);
            if (block instanceof ICBlockInterfaces.IMetaBlock) {
                ICBlockInterfaces.IMetaBlock ieMetaBlock = (ICBlockInterfaces.IMetaBlock)block;
                if (ieMetaBlock.useCustomStateMapper()) { ModelLoader.setCustomStateMapper(block, ICModels.customStateMapper(block)); }
                ModelLoader.setCustomMeshDefinition(blockItem, stack -> new ModelResourceLocation(loc, "inventory"));
                for (int meta = 0; meta < ieMetaBlock.getMetaEnums().length; meta++) {
                    String location = loc.toString();
                    String prop = ieMetaBlock.appendPropertiesToState() ? ("inventory," + ieMetaBlock.getMetaProperty().getName() + "=" + ieMetaBlock.getMetaEnums()[meta].toString().toLowerCase(Locale.US)) : null;
                    if (ieMetaBlock.useCustomStateMapper()) {
                        String custom = ieMetaBlock.getCustomStateMapping(meta, true);
                        location += "_" + custom;
                    }
                    try { ModelLoader.setCustomModelResourceLocation(blockItem, meta, new ModelResourceLocation(location, prop)); }
                    catch (NullPointerException npe) { throw new RuntimeException("WELP! apparently " + ieMetaBlock + " lacks an item!", npe); }
                }
            }
            else if (block instanceof BlockITFluid) { mapFluidState(block, ((BlockITFluid)block).getFluid()); }
            else { ModelLoader.setCustomModelResourceLocation(blockItem, 0, new ModelResourceLocation(loc, "inventory")); }
        }
        for (Item item : ITContent.registeredITItems) {
            if (item instanceof ItemBlock) { continue; }
            if (item instanceof ItemITBase) {
                ItemITBase ipMetaItem = (ItemITBase)item;
                if (ipMetaItem.registerSubModels && ipMetaItem.getSubNames() != null && ipMetaItem.getSubNames().length > 0) {
                    for (int meta = 0; meta < ipMetaItem.getSubNames().length; meta++) {
                        ResourceLocation loc = new ResourceLocation(ImmersiveTechnology.MODID, ipMetaItem.itemName + "/" + ipMetaItem.getSubNames()[meta]);
                        ModelBakery.registerItemVariants(ipMetaItem, loc);
                        ModelLoader.setCustomModelResourceLocation(ipMetaItem, meta, new ModelResourceLocation(loc, "inventory"));
                    }
                }
                else {
                    final ResourceLocation loc = new ResourceLocation(ImmersiveTechnology.MODID, ipMetaItem.itemName);
                    ModelBakery.registerItemVariants(ipMetaItem, loc);
                    ModelLoader.setCustomMeshDefinition(ipMetaItem, stack -> new ModelResourceLocation(loc, "inventory"));
                }
            }
            else {
                final ResourceLocation loc = Item.REGISTRY.getNameForObject(item);
                ModelBakery.registerItemVariants(item, loc);
                ModelLoader.setCustomMeshDefinition(item, stack -> {
                    assert loc != null;
                    return new ModelResourceLocation(loc, "inventory");
                });
            }
        }
    }

    @Override
    public void init() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAdvancedCokeOvenBaseheater.class, new TileRenderAdvancedCokeOvenBaseheater());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBarrelOpen.class, new TileRenderBarrelOpen());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGasTurbineMaster.class, new TileRendererGasTurbine());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHighPressureSteamTurbineMaster.class, new TileRenderHighPressureSteamTurbine());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySolarMelterMaster.class, new TileRenderSolarMelter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySolarReflectorMaster.class, new TileRenderSolarReflector());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySteamTurbineMaster.class, new TileRenderSteamTurbine());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySteelSheetmetalTankMaster.class, new TileRenderSteelSheetmetalTank());

    }

    @Override public void postInit() {
        ITMultiblockIngredients.init();
        ICManual.addEntry("intro", CAT_IT,
                ICManual.text("intro0"));
        ICManual.addEntry("barrelCreative", CAT_IT,
                ICManual.text("barrelCreative0"),
                ICManual.text("barrelCreative1"));
        ICManual.addEntry("fluidPipes", CAT_IT,
                ICManual.text("fluidPipes3"),
                ICManual.text("fluidPipes4"));
        ICManual.addEntry("technologistsWrench", CAT_IT, ICManual.crafting("technologistsWrench0", new ItemStack(ITContent.itemFormationTool)));

        if (Multiblocks.enable.enable_advancedCokeOven) {
            ICManual.addEntry("advancedCokeOven", CAT_PROCESSING,
                    ICManual.multiblock("advancedCokeOven0", TileEntityITMultiblockPartAdvancedCokeOven.instance),
                    ICManual.text("advancedCokeOven1"),
                    ICManual.crafting("advancedCokeOven2", new ItemStack(ITContent.blockMetalDevice, 1, BlockType_MetalDevice.ADVANCED_COKE_OVEN_BASEHEATER.getMeta())),
                    ICManual.text("advancedCokeOven3"));
        }
        if (Multiblocks.enable.enable_boiler) {
            ICManual.addEntry("boilerTank", CAT_POWER,
                    ICManual.multiblock("boilerTank0", TileEntityITMultiblockPartBoilerTank.instance),
                    ICManual.text("boilerTank1"),
                    ICManual.text("boilerTank2"));
            ICManual.addEntry("boilerLiquid", CAT_POWER,
                    ICManual.multiblock("boilerLiquid0", TileEntityITMultiblockPartBoilerLiquid.instance),
                    ICManual.text("boilerLiquid1"),
                    ICManual.text("boilerLiquid2"));
        }
        if (Multiblocks.enable.enable_boilerSolid) {
            ICManual.addEntry("boilerSolid", CAT_POWER,
                    ICManual.multiblock("boilerSolid0", TileEntityITMultiblockPartBoilerSolid.instance),
                    ICManual.text("boilerSolid1"),
                    ICManual.text("boilerSolid2"));
        }
        if (Multiblocks.enable.enable_solarTower) {
            ICManual.addEntry("solarTower", CAT_POWER,
                    ICManual.multiblock("solarTower0", TileEntityITMultiblockPartSolarTower.instance),
                    ICManual.text("solarTower1"),
                    ICManual.text("solarTower2"));
            ICManual.addEntry("solarReflector", CAT_POWER,
                    ICManual.multiblock("solarReflector0", TileEntityITMultiblockPartSolarReflector.instance),
                    ICManual.text("solarReflector1"),
                    ICManual.text("solarReflector2"),
                    ICManual.text("solarReflector3"));
        }
        if (Multiblocks.enable.enable_heatExchanger) {
            ICManual.addEntry("heatExchanger", CAT_POWER,
                    ICManual.multiblock("heatExchanger0", TileEntityITMultiblockPartHeatExchanger.instance),
                    ICManual.text("heatExchanger1"),
                    ICManual.text("heatExchanger2"));
        }
        if (Multiblocks.enable.enable_gasTurbine || Multiblocks.enable.enable_steamTurbine) {
            ICManual.addEntry("alternator", CAT_POWER,
                    ICManual.multiblock("alternator0", TileEntityITMultiblockPartAlternator.instance),
                    ICManual.text("alternator1"),
                    ICManual.image("alternator2", "immersivetech:textures/misc/alternator.png;0;0;110;50"));
        }
        if (Multiblocks.enable.enable_steamTurbine) {
            ICManual.addEntry("steamTurbine", CAT_POWER,
                    ICManual.multiblock("steamTurbine0", TileEntityITMultiblockPartSteamTurbine.instance),
                    ICManual.text("steamTurbine1"),
                    ICManual.text("steamTurbine2"),
                    ICManual.text("steamTurbine3"));
        }
        if (Multiblocks.enable.enable_highPressureSteamTurbine) {
            ICManual.addEntry("highPressureSteamTurbine", CAT_POWER,
                    ICManual.multiblock("highPressureSteamTurbine0", TileEntityITMultiblockPartHighPressureSteamTurbine.instance),
                    ICManual.text("highPressureSteamTurbine1"),
                    ICManual.text("highPressureSteamTurbine2"),
                    ICManual.text("highPressureSteamTurbine3"));
        }
        if (Multiblocks.enable.enable_gasTurbine) {
            ICManual.addEntry("gasTurbine", CAT_POWER,
                    ICManual.multiblock("gasTurbine0", TileEntityITMultiblockPartGasTurbine.instance),
                    ICManual.text("gasTurbine1"),
                    ICManual.text("gasTurbine2"),
                    ICManual.text("gasTurbine3"),
                    ICManual.text("gasTurbine4"));
        }
        if (Multiblocks.enable.enable_coolingTower) {
            ICManual.addEntry("coolingTower", CAT_POWER,
                    ICManual.multiblock("coolingTower0", TileEntityITMultiblockPartCoolingTower.instance),
                    ICManual.text("coolingTower1"),
                    ICManual.text("coolingTower2"));
        }
        if (Multiblocks.enable.enable_radiator) {
            ICManual.addEntry("radiator", CAT_POWER,
                    ICManual.multiblock("radiator0", TileEntityITMultiblockPartRadiator.instance),
                    ICManual.text("radiator1"),
                    ICManual.text("radiator2"));
        }
        if (ICMods.immersiveEngineering()) {
        ICManual.addEntry("controlBlocks", CAT_IT,
                ICManual.crafting("controlBlocks0", new ItemStack(ITContent.blockValve, 1, BlockType_Valve.STACK_LIMITER.getMeta())),
                ICManual.text("controlBlocks0a"),
                ICManual.crafting("controlBlocks1", new ItemStack(ITContent.blockValve, 1, BlockType_Valve.LOAD_CONTROLLER.getMeta())),
                ICManual.text("controlBlocks1a"),
                ICManual.crafting("controlBlocks2", new ItemStack(ITContent.blockValve, 1, BlockType_Valve.FLUID_VALVE.getMeta())),
                ICManual.text("controlBlocks2a"));
        ICManual.addEntry("redstone", CAT_IT,
                ICManual.crafting("redstone0", new ItemStack(ITContent.blockConnectors, 1, BlockType_Connectors.CONNECTORS_TIMER.getMeta())),
                ICManual.text("redstone1"));
        }
        ICManual.addEntry("openBarrel", CAT_IT,
                ICManual.crafting("openBarrel0", new ItemStack(ITContent.blockMetalBarrel, 1, BlockType_MetalBarrel.BARREL_OPEN.getMeta())),
                ICManual.text("openBarrel1"));
        ICManual.addEntry("steelBarrel", CAT_IT,
                ICManual.crafting("steelBarrel0", new ItemStack(ITContent.blockMetalBarrel, 2, BlockType_MetalBarrel.BARREL_STEEL.getMeta())));
        ICManual.addEntry("steelTank", CAT_PROCESSING,
                ICManual.multiblock("steelTank0", TileEntityITMultiblockPartSteelSheetmetalTank.instance),
                ICManual.text("steelTank1"),
                ICManual.text("steelTank2"));
        if (Multiblocks.enable.enable_distiller) {
            ICManual.addEntry("distiller", CAT_PROCESSING,
                    ICManual.multiblock("distiller0", TileEntityITMultiblockPartDistiller.instance),
                    ICManual.text("distiller1"),
                    ICManual.text("distiller2"));
        }
        if (Multiblocks.enable.enable_meltingCrucible) {
            ICManual.addEntry("meltingCrucible", CAT_PROCESSING,
                    ICManual.multiblock("meltingCrucible0", TileEntityITMultiblockPartMeltingCrucible.instance),
                    ICManual.text("meltingCrucible1"),
                    ICManual.text("meltingCrucible2"));
        }
        if (Multiblocks.enable.enable_solarMelter) {
            ICManual.addEntry("solarMelter", CAT_PROCESSING,
                    ICManual.multiblock("solarMelter0", TileEntityITMultiblockPartSolarMelter.instance),
                    ICManual.text("solarMelter1"),
                    ICManual.text("solarMelter2"),
                    ICManual.text("solarMelter2a"));
        }
        if (Multiblocks.enable.enable_electrolyticCrucibleBattery) {
            ICManual.addEntry("electrolyticCrucibleBattery", CAT_PROCESSING,
                    ICManual.multiblock("electrolyticCrucibleBattery0", TileEntityITMultiblockPartElectrolyticCrucibleBattery.instance),
                    ICManual.text("electrolyticCrucibleBattery1"),
                    ICManual.text("electrolyticCrucibleBattery2"),
                    ICManual.text("electrolyticCrucibleBattery3"));
        }
    }

    private static void mapFluidState(Block block, Fluid fluid) {
        Item item = Item.getItemFromBlock(block);
        FluidStateMapper mapper = new FluidStateMapper(fluid);
        if (item != Items.AIR) {
            ModelLoader.registerItemVariants(item);
            ModelLoader.setCustomMeshDefinition(item, mapper);
        }
        ModelLoader.setCustomStateMapper(block, mapper);
    }

    static class FluidStateMapper extends StateMapperBase implements ItemMeshDefinition {
        public final ModelResourceLocation location;

        public FluidStateMapper(Fluid fluid) { this.location = new ModelResourceLocation(ImmersiveTechnology.MODID + ":fluid_block", fluid.getName()); }

        @Nonnull @Override protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) { return location; }

        @Nonnull @Override public ModelResourceLocation getModelLocation(@Nonnull ItemStack stack) { return location; }
    }

    static {
        ICIntegration.addRenderCacheClearer(ModelConfigurableSides.modelCache::clear);
    }

    @Override public void clearRenderCaches() {
        ICIntegration.clearRenderCaches();
    }

    @Override public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (tile instanceof IGuiTile) {
            if (ID == ITGUI.GUIID_Advanced_coke_oven && tile instanceof TileEntityAdvancedCokeOvenMaster) { return new GuiAdvancedCokeOven(player.inventory, (TileEntityAdvancedCokeOvenMaster)tile); }
            if (ID == ITGUI.GUIID_Boiler_Tank && tile instanceof TileEntityBoilerTankMaster) { return new GuiBoilerTank(player.inventory, (TileEntityBoilerTankMaster)tile); }
            if (ID == ITGUI.GUIID_Boiler_Liquid && tile instanceof TileEntityBoilerLiquidMaster) { return new GuiBoilerLiquid(player.inventory, (TileEntityBoilerLiquidMaster)tile); }
            if (ID == ITGUI.GUIID_Boiler_Solid && tile instanceof TileEntityBoilerSolidMaster) { return new GuiBoilerSolid(player.inventory, (TileEntityBoilerSolidMaster)tile); }
            if (ID == ITGUI.GUIID_Crate && tile instanceof TileEntityCrate) { return new GuiCrate(player.inventory, (TileEntityCrate)tile); }
            if (ID == ITGUI.GUIID_Distiller && tile instanceof TileEntityDistillerMaster) { return new GuiDistiller(player.inventory, (TileEntityDistillerMaster)tile); }
            if (ID == ITGUI.GUIID_Fluid_Valve && tile instanceof TileEntityFluidValve) { return new GuiFluidValve((TileEntityFluidValve)tile); }
            if (ID == ITGUI.GUIID_Load_Controller && tile instanceof TileEntityLoadController) { return new GuiLoadController((TileEntityLoadController)tile); }
            if (ID == ITGUI.GUIID_Melting_Crucible && tile instanceof TileEntityMeltingCrucibleMaster) { return new GuiMeltingCrucible(player.inventory, (TileEntityMeltingCrucibleMaster)tile); }
            if (ID == ITGUI.GUIID_Solar_Melter && tile instanceof TileEntitySolarMelterMaster) { return new GuiSolarMelter(player.inventory, (TileEntitySolarMelterMaster)tile); }
            if (ID == ITGUI.GUIID_Solar_Tower && tile instanceof TileEntitySolarTowerMaster) { return new GuiSolarTower(player.inventory, (TileEntitySolarTowerMaster)tile); }
            if (ID == ITGUI.GUIID_Stack_Limiter && tile instanceof TileEntityStackLimiter) { return new GuiStackLimiter((TileEntityStackLimiter)tile); }
            if (ID == ITGUI.GUIID_Timer && tile instanceof TileEntityTimer) { return new GuiTimer(player.inventory, (TileEntityTimer)tile); }
            if (ID == ITGUI.GUIID_Trash_Item && tile instanceof TileEntityTrashItem) { return new GuiTrashItem(player.inventory, (TileEntityTrashItem)tile); }
        }
        return null;
    }
}
