package mctmods.immersivetechnology.client;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.api.ManualPageMultiblock;
import blusunrize.immersiveengineering.api.energy.wires.WireApi;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.IECustomStateMapper;
import blusunrize.immersiveengineering.client.models.obj.IEOBJLoader;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IIEMetaBlock;
import blusunrize.lib.manual.ManualPages;

import com.immersiveconvergence.api.client.ICSoundHandler;
import com.immersiveconvergence.api.client.split.SplitModelHandler;
import com.immersiveconvergence.api.particles.ParticleSettings;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.client.gui.*;
import mctmods.immersivetechnology.client.models.ModelConfigurableSides;
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
    public static final String CAT_POWER = "it_power";
    public static final String CAT_IT = "it";

    @Override public void preInit() {
        ClientUtils.mc().getFramebuffer().enableStencil();
        ParticleSettings.particleCollide = () -> ITConfig.Client.particles.collide;
        ModelLoaderRegistry.registerLoader(IEOBJLoader.instance);
        OBJLoader.INSTANCE.addDomain(ImmersiveTechnology.MODID);
        IEOBJLoader.instance.addDomain(ImmersiveTechnology.MODID);
        MinecraftForge.EVENT_BUS.register(this);
        ModelLoaderRegistry.registerLoader(new ModelConfigurableSides.Loader());
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock_alternator", () -> TileEntityITMultiblockPartAlternator.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock_boiler_tank", () -> TileEntityITMultiblockPartBoilerTank.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock1_boiler_liquid", () -> TileEntityITMultiblockPartBoilerLiquid.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock2_boiler_solid", () -> TileEntityITMultiblockPartBoilerSolid.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "stone_multiblock_cooling_tower", () -> TileEntityITMultiblockPartCoolingTower.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock_distiller", () -> TileEntityITMultiblockPartDistiller.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock_solar_tower", () -> TileEntityITMultiblockPartSolarTower.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock_steam_turbine", () -> TileEntityITMultiblockPartSteamTurbine.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock_steel_tank", () -> TileEntityITMultiblockPartSteelSheetmetalTank.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock1_electrolytic_crucible_battery", () -> TileEntityITMultiblockPartElectrolyticCrucibleBattery.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock1_gas_turbine", () -> TileEntityITMultiblockPartGasTurbine.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock1_heat_exchanger", () -> TileEntityITMultiblockPartHeatExchanger.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock1_high_pressure_steam_turbine", () -> TileEntityITMultiblockPartHighPressureSteamTurbine.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock1_melting_crucible", () -> TileEntityITMultiblockPartMeltingCrucible.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock1_radiator", () -> TileEntityITMultiblockPartRadiator.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "metal_multiblock1_solar_melter", () -> TileEntityITMultiblockPartSolarMelter.instance);
        SplitModelHandler.register(ImmersiveTechnology.MODID, "stone_multiblock_advanced_coke_oven", () -> TileEntityITMultiblockPartAdvancedCokeOven.instance);
    }

    @SubscribeEvent public void PlayerChangedDimensions(PlayerEvent.PlayerChangedDimensionEvent e) { ICSoundHandler.deleteAllSounds(); }

    @SubscribeEvent public void PlayerLeftSession(PlayerEvent.PlayerLoggedOutEvent e) { ICSoundHandler.deleteAllSounds(); }

    @SubscribeEvent public void PlayerDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent e) { ICSoundHandler.deleteAllSounds(); }

    @SubscribeEvent public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (!ITUtils.REMOVE_FROM_TICKING.isEmpty()) {
                World world = Minecraft.getMinecraft().world;
                if (world == null) { ITLogger.warn("ClientProxy has tried to access null world! This shouldn't normally happen..."); }
                else {
                    Set<TileEntity> forThisWorld = new HashSet<>();
                    for (TileEntity te : ITUtils.REMOVE_FROM_TICKING) {
                        if (te.getWorld() == world) { forThisWorld.add(te); }
                    }
                    if (!forThisWorld.isEmpty()) {
                        world.tickableTileEntities.removeAll(forThisWorld);
                        ITUtils.REMOVE_FROM_TICKING.removeAll(forThisWorld);
                    }
                }
            }
        }
    }

    @SuppressWarnings({"deprecation", "ConstantConditions"})
    @SubscribeEvent public static void registerModels(ModelRegistryEvent evt) {
        WireApi.registerConnectorForRender("conn_timer", new ResourceLocation("immersivetech:block/connector/connector_timer/connector_timer.obj.ie"), null);
        WireApi.registerConnectorForRender("conn_con_net", new ResourceLocation("immersivetech:block/connector/connectors_con_net.obj.ie"), null);
        for (Block block : ITContent.registeredITBlocks) {
            final ResourceLocation loc = Block.REGISTRY.getNameForObject(block);
            Item blockItem = Item.getItemFromBlock(block);
            if (block instanceof IIEMetaBlock) {
                IIEMetaBlock ieMetaBlock = (IIEMetaBlock)block;
                if (ieMetaBlock.useCustomStateMapper()) { ModelLoader.setCustomStateMapper(block, IECustomStateMapper.getStateMapper(ieMetaBlock)); }
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
        ManualHelper.addEntry("technologistsWrench", CAT_IT, new ManualPages.Crafting(ManualHelper.getManual(), "technologistsWrench0", new ItemStack(ITContent.itemFormationTool)));

        if (Multiblocks.enable.enable_advancedCokeOven) {
            ManualHelper.addEntry("advancedCokeOven", CAT_IT,
                    new ManualPageMultiblock(ManualHelper.getManual(), "advancedCokeOven0", TileEntityITMultiblockPartAdvancedCokeOven.instance),
                    new ManualPages.Text(ManualHelper.getManual(), "advancedCokeOven1"),
                    new ManualPages.Crafting(ManualHelper.getManual(), "advancedCokeOven2", new ItemStack(ITContent.blockMetalDevice, 1, BlockType_MetalDevice.ADVANCED_COKE_OVEN_BASEHEATER.getMeta())),
                    new ManualPages.Text(ManualHelper.getManual(), "advancedCokeOven3"));
        }
        if (Multiblocks.enable.enable_boiler) {
            ManualHelper.addEntry("boilerTank", CAT_POWER,
                    new ManualPageMultiblock(ManualHelper.getManual(), "boilerTank0", TileEntityITMultiblockPartBoilerTank.instance),
                    new ManualPages.Text(ManualHelper.getManual(), "boilerTank1"),
                    new ManualPages.Text(ManualHelper.getManual(), "boilerTank2"));
            ManualHelper.addEntry("boilerLiquid", CAT_POWER,
                    new ManualPageMultiblock(ManualHelper.getManual(), "boilerLiquid0", TileEntityITMultiblockPartBoilerLiquid.instance),
                    new ManualPages.Text(ManualHelper.getManual(), "boilerLiquid1"),
                    new ManualPages.Text(ManualHelper.getManual(), "boilerLiquid2"));
        }
        if (Multiblocks.enable.enable_boilerSolid) {
            ManualHelper.addEntry("boilerSolid", CAT_POWER,
                    new ManualPageMultiblock(ManualHelper.getManual(), "boilerSolid0", TileEntityITMultiblockPartBoilerSolid.instance),
                    new ManualPages.Text(ManualHelper.getManual(), "boilerSolid1"),
                    new ManualPages.Text(ManualHelper.getManual(), "boilerSolid2"));
        }
        if (Multiblocks.enable.enable_solarTower) {
            ManualHelper.addEntry("solarTower", CAT_POWER,
                    new ManualPageMultiblock(ManualHelper.getManual(), "solarTower0", TileEntityITMultiblockPartSolarTower.instance),
                    new ManualPages.Text(ManualHelper.getManual(), "solarTower1"),
                    new ManualPageMultiblock(ManualHelper.getManual(), "solarTower2", TileEntityITMultiblockPartSolarReflector.instance),
                    new ManualPages.Text(ManualHelper.getManual(), "solarTower2a"),
                    new ManualPages.Text(ManualHelper.getManual(), "solarTower3"),
                    new ManualPages.Text(ManualHelper.getManual(), "solarTower4"));
        }
        if (Multiblocks.enable.enable_heatExchanger) {
            ManualHelper.addEntry("heatExchanger", CAT_POWER,
                    new ManualPageMultiblock(ManualHelper.getManual(), "heatExchanger0", TileEntityITMultiblockPartHeatExchanger.instance),
                    new ManualPages.Text(ManualHelper.getManual(), "heatExchanger1"),
                    new ManualPages.Text(ManualHelper.getManual(), "heatExchanger2"));
        }
        if (Multiblocks.enable.enable_gasTurbine || Multiblocks.enable.enable_steamTurbine) {
            ManualHelper.addEntry("alternator", CAT_POWER,
                    new ManualPageMultiblock(ManualHelper.getManual(), "alternator0", TileEntityITMultiblockPartAlternator.instance),
                    new ManualPages.Text(ManualHelper.getManual(), "alternator1"),
                    new ManualPages.Image(ManualHelper.getManual(), "alternator2", "immersivetech:textures/misc/alternator.png;0;0;110;50"));
        }
        if (Multiblocks.enable.enable_steamTurbine) {
            ManualHelper.addEntry("steamTurbine", CAT_POWER,
                    new ManualPageMultiblock(ManualHelper.getManual(), "steamTurbine0", TileEntityITMultiblockPartSteamTurbine.instance),
                    new ManualPages.Text(ManualHelper.getManual(), "steamTurbine1"),
                    new ManualPages.Text(ManualHelper.getManual(), "steamTurbine2"),
                    new ManualPages.Text(ManualHelper.getManual(), "steamTurbine3"));
        }
        if (Multiblocks.enable.enable_highPressureSteamTurbine) {
            ManualHelper.addEntry("highPressureSteamTurbine", CAT_POWER,
                    new ManualPageMultiblock(ManualHelper.getManual(), "highPressureSteamTurbine0", TileEntityITMultiblockPartHighPressureSteamTurbine.instance),
                    new ManualPages.Text(ManualHelper.getManual(), "highPressureSteamTurbine1"),
                    new ManualPages.Text(ManualHelper.getManual(), "highPressureSteamTurbine2"),
                    new ManualPages.Text(ManualHelper.getManual(), "highPressureSteamTurbine3"));
        }
        if (Multiblocks.enable.enable_gasTurbine) {
            ManualHelper.addEntry("gasTurbine", CAT_POWER,
                    new ManualPageMultiblock(ManualHelper.getManual(), "gasTurbine0", TileEntityITMultiblockPartGasTurbine.instance),
                    new ManualPages.Text(ManualHelper.getManual(), "gasTurbine1"),
                    new ManualPages.Text(ManualHelper.getManual(), "gasTurbine2"),
                    new ManualPages.Text(ManualHelper.getManual(), "gasTurbine3"),
                    new ManualPages.Text(ManualHelper.getManual(), "gasTurbine4"));
        }
        if (Multiblocks.enable.enable_coolingTower) {
            ManualHelper.addEntry("coolingTower", CAT_POWER,
                    new ManualPageMultiblock(ManualHelper.getManual(), "coolingTower0", TileEntityITMultiblockPartCoolingTower.instance),
                    new ManualPages.Text(ManualHelper.getManual(), "coolingTower1"),
                    new ManualPages.Text(ManualHelper.getManual(), "coolingTower2"));
        }
        if (Multiblocks.enable.enable_radiator) {
            ManualHelper.addEntry("radiator", CAT_POWER,
                    new ManualPageMultiblock(ManualHelper.getManual(), "radiator0", TileEntityITMultiblockPartRadiator.instance),
                    new ManualPages.Text(ManualHelper.getManual(), "radiator1"),
                    new ManualPages.Text(ManualHelper.getManual(), "radiator2"));
        }
        ManualHelper.addEntry("controlBlocks", CAT_IT,
                new ManualPages.Crafting(ManualHelper.getManual(), "controlBlocks0", new ItemStack(ITContent.blockValve, 1, BlockType_Valve.STACK_LIMITER.getMeta())),
                new ManualPages.Text(ManualHelper.getManual(), "controlBlocks0a"),
                new ManualPages.Crafting(ManualHelper.getManual(), "controlBlocks1", new ItemStack(ITContent.blockValve, 1, BlockType_Valve.LOAD_CONTROLLER.getMeta())),
                new ManualPages.Text(ManualHelper.getManual(), "controlBlocks1a"),
                new ManualPages.Crafting(ManualHelper.getManual(), "controlBlocks2", new ItemStack(ITContent.blockValve, 1, BlockType_Valve.FLUID_VALVE.getMeta())),
                new ManualPages.Text(ManualHelper.getManual(), "controlBlocks2a"));
        ManualHelper.addEntry("redstone", CAT_IT,
                new ManualPages.Crafting(ManualHelper.getManual(), "redstone0", new ItemStack(ITContent.blockConnectors, 1, BlockType_Connectors.CONNECTORS_TIMER.getMeta())),
                new ManualPages.Text(ManualHelper.getManual(), "redstone1"));
        ManualHelper.addEntry("openBarrel", CAT_IT,
                new ManualPages.Crafting(ManualHelper.getManual(), "openBarrel0", new ItemStack(ITContent.blockMetalBarrel, 1, BlockType_MetalBarrel.BARREL_OPEN.getMeta())),
                new ManualPages.Text(ManualHelper.getManual(), "openBarrel1"));
        ManualHelper.addEntry("steelBarrel", CAT_IT,
                new ManualPages.Crafting(ManualHelper.getManual(), "steelBarrel0", new ItemStack(ITContent.blockMetalBarrel, 2, BlockType_MetalBarrel.BARREL_STEEL.getMeta())));
        ManualHelper.addEntry("steelTank", CAT_IT,
                new ManualPageMultiblock(ManualHelper.getManual(), "steelTank0", TileEntityITMultiblockPartSteelSheetmetalTank.instance),
                new ManualPages.Text(ManualHelper.getManual(), "steelTank1"),
                new ManualPages.Text(ManualHelper.getManual(), "steelTank2"));
        if (Multiblocks.enable.enable_distiller) {
            ManualHelper.addEntry("distiller", CAT_IT,
                    new ManualPageMultiblock(ManualHelper.getManual(), "distiller0", TileEntityITMultiblockPartDistiller.instance),
                    new ManualPages.Text(ManualHelper.getManual(), "distiller1"),
                    new ManualPages.Text(ManualHelper.getManual(), "distiller2"));
        }
        if (Multiblocks.enable.enable_meltingCrucible) {
            ManualHelper.addEntry("meltingCrucible", CAT_IT,
                    new ManualPageMultiblock(ManualHelper.getManual(), "meltingCrucible0", TileEntityITMultiblockPartMeltingCrucible.instance),
                    new ManualPages.Text(ManualHelper.getManual(), "meltingCrucible1"),
                    new ManualPages.Text(ManualHelper.getManual(), "meltingCrucible2"));
        }
        if (Multiblocks.enable.enable_solarMelter) {
            ManualHelper.addEntry("solarMelter", CAT_IT,
                    new ManualPageMultiblock(ManualHelper.getManual(), "solarMelter0", TileEntityITMultiblockPartSolarMelter.instance),
                    new ManualPages.Text(ManualHelper.getManual(), "solarMelter1"),
                    new ManualPages.Text(ManualHelper.getManual(), "solarMelter2"),
                    new ManualPages.Text(ManualHelper.getManual(), "solarMelter2a"));
        }
        if (Multiblocks.enable.enable_electrolyticCrucibleBattery) {
            ManualHelper.addEntry("electrolyticCrucibleBattery", CAT_IT,
                    new ManualPageMultiblock(ManualHelper.getManual(), "electrolyticCrucibleBattery0", TileEntityITMultiblockPartElectrolyticCrucibleBattery.instance),
                    new ManualPages.Text(ManualHelper.getManual(), "electrolyticCrucibleBattery1"),
                    new ManualPages.Text(ManualHelper.getManual(), "electrolyticCrucibleBattery2"),
                    new ManualPages.Text(ManualHelper.getManual(), "electrolyticCrucibleBattery3"));
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
        IEApi.renderCacheClearers.add(ModelConfigurableSides.modelCache::clear);
    }

    @Override public void clearRenderCaches() {
        for (Runnable r : IEApi.renderCacheClearers) { r.run(); }
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
