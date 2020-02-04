package ferro2000.immersivetech.common;

import java.util.ArrayList;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;

import ferro2000.immersivetech.ImmersiveTech;
import ferro2000.immersivetech.api.crafting.BoilerRecipe;
import ferro2000.immersivetech.api.crafting.DistillerRecipe;
import ferro2000.immersivetech.api.crafting.SolarTowerRecipe;
import ferro2000.immersivetech.api.crafting.SteamTurbineRecipe;
import ferro2000.immersivetech.common.Config.ITConfig.*;
import ferro2000.immersivetech.common.Config.ITConfig.Machines.*;
import ferro2000.immersivetech.common.blocks.BlockITBase;
import ferro2000.immersivetech.common.blocks.BlockITFluid;
import ferro2000.immersivetech.common.blocks.connectors.BlockConnectors;
import ferro2000.immersivetech.common.blocks.connectors.tileentities.TileEntityTimer;
import ferro2000.immersivetech.common.blocks.metal.BlockMetalBarrel;
import ferro2000.immersivetech.common.blocks.metal.BlockMetalDevice;
import ferro2000.immersivetech.common.blocks.metal.BlockMetalMultiblock;
import ferro2000.immersivetech.common.blocks.metal.BlockMetalTrash;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockAlternator;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockBoiler;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockDistiller;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockSolarReflector;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockSolarTower;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockSteamTurbine;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityAlternator;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityBarrel;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityBoiler;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityCokeOvenPreheater;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityDistiller;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntitySolarReflector;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntitySolarTower;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntitySteamTurbine;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityTrashEnergy;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityTrashFluid;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityTrashItem;
import ferro2000.immersivetech.common.blocks.stone.BlockStoneDecoration;
import ferro2000.immersivetech.common.blocks.stone.BlockStoneMultiblock;
import ferro2000.immersivetech.common.blocks.stone.multiblocks.MultiblockCokeOvenAdvanced;
import ferro2000.immersivetech.common.blocks.stone.tileentities.TileEntityCokeOvenAdvanced;
import ferro2000.immersivetech.common.fluid.FluidColored;
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
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

@EventBusSubscriber(modid=ImmersiveTech.MODID)
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
	public static BlockITBase<?> blockMetalTrash;
	public static BlockITBase<?> blockMetalBarrel;

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
		blockMetalTrash = new BlockMetalTrash();
		blockMetalBarrel = new BlockMetalBarrel();

		/*STONE*/
		blockStoneDecoration = new BlockStoneDecoration();

		/*FLUIDS*/
		fluidSteam = new FluidColored("steam", 0xA1B8FF, -100, 500, true);
		fluidDistWater = new FluidColored("dist_water", 0x7091FF, 1000, 1000, false);

		/*FLUID BLOCKS*/
		blockFluidSteam = new BlockITFluid("fluidSteam", fluidSteam, Material.WATER);
		blockFluidDistWater = new BlockITFluid("fluidDistWater", fluidDistWater, Material.WATER);

		/*ITEMS*/
		itemMaterial = new ItemITBase("material", 64, "salt");
	}

	public static void init() {
		/*TILE ENTITIES*/
		registerTile(TileEntityTimer.class);
		registerTile(TileEntityTrashItem.class);
		registerTile(TileEntityTrashFluid.class);
		registerTile(TileEntityTrashEnergy.class);
		registerTile(TileEntityBarrel.class);
		
		/*MULTIBLOCK TILE ENTITIES*/
		if(Multiblock.enable_distiller) {
			registerTile(TileEntityDistiller.class);
			MultiblockHandler.registerMultiblock(MultiblockDistiller.instance);
		}
		if(Multiblock.enable_alternator) {
			registerTile(TileEntityAlternator.class);
			MultiblockHandler.registerMultiblock(MultiblockAlternator.instance);
		}
		if(Multiblock.enable_boiler) {
			registerTile(TileEntityBoiler.class);
			MultiblockHandler.registerMultiblock(MultiblockBoiler.instance);
		}
		if(Multiblock.enable_advancedCokeOven) {
			registerTile(TileEntityCokeOvenAdvanced.class);
			MultiblockHandler.registerMultiblock(MultiblockCokeOvenAdvanced.instance);
			registerTile(TileEntityCokeOvenPreheater.class);
		}
		if(Multiblock.enable_solarReflector) {
			registerTile(TileEntitySolarReflector.class);
			MultiblockHandler.registerMultiblock(MultiblockSolarReflector.instance);
		}
		if(Multiblock.enable_solarTower) {
			registerTile(TileEntitySolarTower.class);
			MultiblockHandler.registerMultiblock(MultiblockSolarTower.instance);
		}
		if(Multiblock.enable_steamTurbine) {
			registerTile(TileEntitySteamTurbine.class);
			MultiblockHandler.registerMultiblock(MultiblockSteamTurbine.instance);
		}
	}

	@SubscribeEvent
	public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
		/*RECIPES*/
		if(Multiblock.enable_boiler && Recipes.register_boiler_recipes) {
			BoilerRecipe.addRecipe(new FluidStack(fluidSteam, 1000), new FluidStack(FluidRegistry.WATER, 2000), 40);
			BoilerRecipe.addRecipe(new FluidStack(fluidSteam, 1500), new FluidStack(fluidDistWater, 2000), 40);
			BoilerRecipe.addFuel(new FluidStack(IEContent.fluidBiodiesel, 5), 1, 10);
		}
		if(Multiblock.enable_distiller && Recipes.register_distiller_recipes) {
			ResourceLocation distillerItemName = new ResourceLocation(Distiller.distiller_output_item);
			int distillerItemMeta = Distiller.distiller_output_itemMeta;
			float distillerChance = Distiller.distiller_output_itemChance;
			if(!ForgeRegistries.ITEMS.containsKey(distillerItemName)) {
				ITLogger.error("Item for Salt is invalid, setting default - ", distillerItemName);
				distillerItemName = itemMaterial.getRegistryName();
				distillerItemMeta = 0;
			}
			ItemStack distillerItem = new ItemStack(ForgeRegistries.ITEMS.getValue(distillerItemName), 1, distillerItemMeta);
			DistillerRecipe.addRecipe(new FluidStack(fluidDistWater, 500), new FluidStack(FluidRegistry.WATER, 1000), distillerItem, 10000, 20, distillerChance);
		}
		if(Multiblock.enable_solarTower && Recipes.register_solarTower_recipes) {
			SolarTowerRecipe.addRecipe(new FluidStack(fluidSteam, 1000), new FluidStack(FluidRegistry.WATER, 2000), 80);
			SolarTowerRecipe.addRecipe(new FluidStack(fluidSteam, 1500), new FluidStack(fluidDistWater, 2000), 80);
		}
		if(Multiblock.enable_steamTurbine && Recipes.register_steamTurbine_recipes) {
			SteamTurbineRecipe.addFuel(new FluidStack(FluidRegistry.WATER, 10), new FluidStack(fluidSteam, 100), 1);
		}
	}

	@SuppressWarnings("deprecation")
	public static void registerTile(Class<? extends TileEntity> tile) {
		String tileEntity = tile.getSimpleName();
		tileEntity = tileEntity.substring(tileEntity.indexOf("TileEntity") + "TileEntity".length());
		GameRegistry.registerTileEntity(tile, ImmersiveTech.MODID + ":" + tileEntity);
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

	public static void registerVariables() {
		Config.manual_int.put("steamTurbine_timeToMax", ((MechanicalEnergy.mechanicalEnergy_speed_max / SteamTurbine.steamTurbine_speed_gainPerTick) / 20));
		Config.manual_int.put("solarTower_minRange", SolarReflector.solarReflector_minRange);
		Config.manual_int.put("solarTower_maxRange", SolarReflector.solarReflector_maxRange);
		Config.manual_double.put("boiler_cooldownTime", ((Boiler.boiler_heat_workingLevel / Boiler.boiler_progress_lossInTicks) / 20));
		Config.manual_int.put("alternator_energyPerTickPerPort", (Alternator.alternator_energy_perTick / 6));
		Config.manual_int.put("alternator_energyStorage", Alternator.alternator_energy_capacitorSize);
		Config.manual_int.put("alternator_energyPerTick", Alternator.alternator_energy_perTick);
		Config.manual_int.put("cokeOvenPreheater_consumption", CokeOvenPreheater.cokeOvenPreheater_energy_consumption);
	}

}