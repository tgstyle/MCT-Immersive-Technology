package ferro2000.immersivetech.common;

import java.util.ArrayList;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.common.IEContent;

import ferro2000.immersivetech.ImmersiveTech;
import ferro2000.immersivetech.api.crafting.BoilerRecipe;
import ferro2000.immersivetech.api.crafting.DistillerRecipe;
import ferro2000.immersivetech.api.crafting.SolarTowerRecipe;
import ferro2000.immersivetech.api.crafting.SteamTurbineRecipe;
import ferro2000.immersivetech.common.Config.ITConfig;
import ferro2000.immersivetech.common.Config.ITConfig.Machines;
import ferro2000.immersivetech.common.blocks.BlockITBase;
import ferro2000.immersivetech.common.blocks.BlockITFluid;
import ferro2000.immersivetech.common.blocks.connectors.BlockConnectors;
import ferro2000.immersivetech.common.blocks.connectors.tileentities.TileEntityConnectorNet;
import ferro2000.immersivetech.common.blocks.connectors.tileentities.TileEntityTimer;
import ferro2000.immersivetech.common.blocks.metal.BlockMetalDevice;
import ferro2000.immersivetech.common.blocks.metal.BlockMetalMultiblock;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockAlternator;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockBoiler;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockDistiller;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockSolarReflector;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockSolarTower;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockSteamTurbine;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityAlternator;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityBoiler;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityCokeOvenPreheater;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityDistiller;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntitySolarReflector;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntitySolarTower;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntitySteamTurbine;
import ferro2000.immersivetech.common.blocks.stone.BlockStoneDecoration;
import ferro2000.immersivetech.common.blocks.stone.BlockStoneMultiblock;
import ferro2000.immersivetech.common.blocks.stone.multiblocks.MultiblockCokeOvenAdvanced;
import ferro2000.immersivetech.common.blocks.stone.tileentities.TileEntityCokeOvenAdvanced;
import ferro2000.immersivetech.common.items.ItemITBase;
import ferro2000.immersivetech.common.util.ITLogger;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

@Mod.EventBusSubscriber(modid=ImmersiveTech.MODID)
public class ITContent {
	/*BLOCKS*/
	public static ArrayList<Block> registeredITBlocks = new ArrayList<Block>();

	/*MULTIBLOCKS*/
	public static BlockITBase<?> blockMetalMultiblock;
	public static BlockITBase<?> blockStoneMultiblock;

	/*CONNECTORS*/
	public static BlockITBase<?> blockConnectors;

	/*METAL*/
	public static BlockITBase<?> blockMetalDevice;

	/*STONE*/
	public static BlockITBase<?> blockStoneDecoration;

	/*FLUID BLOCKS*/
	public static BlockITFluid blockFluidDistWater;
	public static BlockITFluid blockFluidSteam;

	/*ITEMS*/
	public static ArrayList<Item> registeredITItems = new ArrayList<Item>();

	/*MATERIALS*/
	public static Item itemMaterial;

	/*FLUIDS*/
	public static Fluid fluidDistWater;
	public static Fluid fluidSteam;

	public static void preInit() {
		/*MULTIBLOCKS*/
		blockMetalMultiblock = new BlockMetalMultiblock();
		blockStoneMultiblock = new BlockStoneMultiblock();

		/*CONNECTORS*/
		blockConnectors = new BlockConnectors();

		/*METAL*/
		blockMetalDevice = new BlockMetalDevice();

		/*STONE*/
		blockStoneDecoration = new BlockStoneDecoration();

		/*FLUIDS*/
		fluidDistWater = new Fluid("dist_water", new ResourceLocation("immersivetech:blocks/fluid/dist_water_still"), new ResourceLocation("immersivetech:blocks/fluid/dist_water_flow")).setDensity(1000).setViscosity(1000);
		if(!FluidRegistry.registerFluid(fluidDistWater)) {
			fluidDistWater = FluidRegistry.getFluid("distwater");
		}
		FluidRegistry.addBucketForFluid(fluidDistWater);

		fluidSteam = new Fluid("steam", new ResourceLocation("immersivetech:blocks/fluid/steam_still"), new ResourceLocation("immersivetech:blocks/fluid/steam_flow")).setDensity(-100).setViscosity(500).setGaseous(true);
		if(!FluidRegistry.registerFluid(fluidSteam)) {
			fluidSteam = FluidRegistry.getFluid("steam");
		}
		FluidRegistry.addBucketForFluid(fluidSteam);

		/*FLUID BLOCKS*/
		blockFluidDistWater = new BlockITFluid("fluidDistWater", fluidDistWater, Material.WATER);
		blockFluidSteam = new BlockITFluid("fluidSteam", fluidSteam, Material.WATER);

		/*ITEMS*/
		itemMaterial = new ItemITBase("material", 64, "salt");
		
		/*TILE ENTITIES*/
		registerTile(TileEntityTimer.class);
		registerTile(TileEntityConnectorNet.class);
	}

	public static void init() {
		/*MULTIBLOCKS*/
		if(Machines.enable_distiller) {
			registerTile(TileEntityDistiller.class);
			MultiblockHandler.registerMultiblock(MultiblockDistiller.instance);
		}

		if(Machines.enable_alternator) {
			registerTile(TileEntityAlternator.class);
			MultiblockHandler.registerMultiblock(MultiblockAlternator.instance);
		}

		if(Machines.enable_boiler) {
			registerTile(TileEntityBoiler.class);
			MultiblockHandler.registerMultiblock(MultiblockBoiler.instance);
		}

		if(Machines.enable_cokeOvenAdvanced) {
			registerTile(TileEntityCokeOvenAdvanced.class);
			MultiblockHandler.registerMultiblock(MultiblockCokeOvenAdvanced.instance);
			registerTile(TileEntityCokeOvenPreheater.class);
		}

		if(Machines.enable_solarReflector) {
			registerTile(TileEntitySolarReflector.class);
			MultiblockHandler.registerMultiblock(MultiblockSolarReflector.instance);
		}

		if(Machines.enable_solarTower) {
			registerTile(TileEntitySolarTower.class);
			MultiblockHandler.registerMultiblock(MultiblockSolarTower.instance);
		}

		if(Machines.enable_steamTurbine) {
			registerTile(TileEntitySteamTurbine.class);
			MultiblockHandler.registerMultiblock(MultiblockSteamTurbine.instance);
		}
	}

	@SubscribeEvent
	public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
		/*RECIPES*/
		if(Machines.enable_boiler && Machines.register_boiler_recipes) {
			BoilerRecipe.addRecipe(new FluidStack(fluidSteam, 50), new FluidStack(FluidRegistry.WATER, 100), 1);
			BoilerRecipe.addRecipe(new FluidStack(fluidSteam, 75), new FluidStack(fluidDistWater, 100), 1);
			BoilerRecipe.addFuel(new FluidStack(IEContent.fluidBiodiesel, 5), 1, 10);
		}

		if(Machines.enable_distiller && Machines.register_distiller_recipes) {
			ResourceLocation distillerItemName = new ResourceLocation(ITConfig.Machines.distiller_outputItem);
			int distillerItemMeta = ITConfig.Machines.distiller_outputItemMeta;
			float distillerChance = ITConfig.Machines.distiller_outputItemChance;
			if(!ForgeRegistries.ITEMS.containsKey(distillerItemName)) {
				ITLogger.error("Item for Salt is invalid, setting default. ", distillerItemName);
				distillerItemName = itemMaterial.getRegistryName();
				distillerItemMeta = 0;
			}
			ItemStack distillerItem = new ItemStack(ForgeRegistries.ITEMS.getValue(distillerItemName), 1, distillerItemMeta);
			DistillerRecipe.addRecipe(new FluidStack(fluidDistWater, 100), new FluidStack(FluidRegistry.WATER, 200), distillerItem, 1000, 2, distillerChance);
		}

		if(Machines.enable_solarTower && Machines.register_solarTower_recipes) {
			SolarTowerRecipe.addRecipe(new FluidStack(fluidSteam, 50), new FluidStack(FluidRegistry.WATER, 100), 1);
			SolarTowerRecipe.addRecipe(new FluidStack(fluidSteam, 75), new FluidStack(fluidDistWater, 100), 1);
		}

		if(Machines.enable_steamTurbine && Machines.register_steamTurbine_recipes) {
			SteamTurbineRecipe.addFuel(new FluidStack(FluidRegistry.WATER, 10), new FluidStack(fluidSteam, 100), 1);
		}
	}

	@SuppressWarnings("deprecation")
	public static void registerTile(Class<? extends TileEntity> tile) {
		String s = tile.getSimpleName();
		s = s.substring(s.indexOf("TileEntity") + "TileEntity".length());
		GameRegistry.registerTileEntity(tile, ImmersiveTech.MODID + ":" + s);
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		for(Block block : registeredITBlocks) event.getRegistry().register(block.setRegistryName(createRegistryName(block.getUnlocalizedName())));
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		for(Item item : registeredITItems) event.getRegistry().register(item.setRegistryName(createRegistryName(item.getUnlocalizedName())));
		registerOres();
	}

	private static ResourceLocation createRegistryName(String unlocalized) {
		unlocalized = unlocalized.substring(unlocalized.indexOf("immersive"));
		unlocalized = unlocalized.replaceFirst("\\.", ":");
		return new ResourceLocation(unlocalized);
	}
	
	public static void registerOres() {
		/*ORE DICTIONARY*/
		OreDictionary.registerOre("dustSalt", itemMaterial);
		OreDictionary.registerOre("itemSalt", itemMaterial);
		OreDictionary.registerOre("foodSalt", itemMaterial);
	}

}