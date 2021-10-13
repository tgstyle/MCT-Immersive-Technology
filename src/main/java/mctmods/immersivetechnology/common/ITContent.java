package mctmods.immersivetechnology.common;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.crafting.*;
import mctmods.immersivetechnology.common.Config.ITConfig.Experimental;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.*;
import mctmods.immersivetechnology.common.Config.ITConfig.MechanicalEnergy;
import mctmods.immersivetechnology.common.blocks.BlockITBase;
import mctmods.immersivetechnology.common.blocks.BlockITFluid;
import mctmods.immersivetechnology.common.blocks.BlockITSlab;
import mctmods.immersivetechnology.common.blocks.BlockValve;
import mctmods.immersivetechnology.common.blocks.connectors.BlockConnectors;
import mctmods.immersivetechnology.common.blocks.connectors.tileentities.TileEntityTimer;
import mctmods.immersivetechnology.common.blocks.metal.*;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.*;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.*;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.conversion.*;
import mctmods.immersivetechnology.common.blocks.stone.BlockStoneDecoration;
import mctmods.immersivetechnology.common.blocks.stone.types.BlockType_StoneDecoration;
import mctmods.immersivetechnology.common.blocks.wooden.BlockWoodenCrate;
import mctmods.immersivetechnology.common.blocks.wooden.tileentities.TileEntityCrate;
import mctmods.immersivetechnology.common.fluid.FluidColored;
import mctmods.immersivetechnology.common.items.ItemITBase;
import mctmods.immersivetechnology.common.tileentities.TileEntityFluidValve;
import mctmods.immersivetechnology.common.tileentities.TileEntityITSlab;
import mctmods.immersivetechnology.common.tileentities.TileEntityLoadController;
import mctmods.immersivetechnology.common.tileentities.TileEntityStackLimiter;
import mctmods.immersivetechnology.common.util.ITLogger;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
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
import net.minecraftforge.oredict.OreIngredient;

import java.util.ArrayList;

@EventBusSubscriber(modid= ImmersiveTechnology.MODID)
public class ITContent {
	/*BLOCKS*/
	public static ArrayList<Block> registeredITBlocks = new ArrayList<>();

	/*MULTIBLOCKS*/
	public static BlockITBase<?> blockMetalMultiblock;
	public static BlockITBase<?> blockMetalMultiblock1;

	/*CONNECTORS*/
	public static BlockITBase<?> blockConnectors;

	/*METAL*/
	public static BlockITBase<?> blockMetalTrash;
	public static BlockITBase<?> blockMetalBarrel;
	public static BlockITBase<?> blockValve;
	public static BlockIEBase<?> blockMetalDevice0Dummy;
	public static BlockIEBase<?> blockMetalDevice1Dummy;

	/*STONE*/
	public static BlockITBase<?> blockStoneDecoration;
	public static BlockITBase<?> blockStoneDecorationSlab;

	/*WOODEN*/
	public static BlockITBase<?> blockWoodenCrate;

	/*FLUID BLOCKS*/
	public static BlockITFluid blockFluidDistWater;
	public static BlockITFluid blockFluidSteam;
	public static BlockITFluid blockFluidExhaustSteam;
	public static BlockITFluid blockFlueGas;
	public static BlockITFluid blockFluidHighPressureSteam;
	public static BlockITFluid blockFluidHotWater;
	public static BlockITFluid blockFluidMoltenSalt;
	public static BlockITFluid blockFluidMoltenSodium;
	public static BlockITFluid blockFluidSuperheatedMoltenSodium;
	public static BlockITFluid blockFluidChlorine;

	/*ITEMS*/
	public static ArrayList<Item> registeredITItems = new ArrayList<>();

	/*MATERIALS*/
	public static Item itemMaterial;

	/*FLUIDS*/
	public static Fluid fluidDistWater;
	public static Fluid fluidSteam;
	public static Fluid fluidExhaustSteam;
	public static Fluid fluidFlueGas;
	public static Fluid fluidHighPressureSteam;
	public static Fluid fluidHotWater;
	public static Fluid fluidMoltenSalt;
	public static Fluid fluidMoltenSodium;
	public static Fluid fluidSuperheatedMoltenSodium;
	public static Fluid fluidChlorine;

	public static ArrayList<Fluid> normallyPressurized = new ArrayList<>();

	public static void preInit() {
		/*MULTIBLOCKS*/
		blockMetalMultiblock = new BlockMetalMultiblock();
		blockMetalMultiblock1 = new blockMetalMultiblock1();

		/*CONNECTORS*/
		blockConnectors = new BlockConnectors();

		/*METAL*/
		blockMetalTrash = new BlockMetalTrash();
		blockMetalBarrel = new BlockMetalBarrel();
		blockValve = new BlockValve();

		/*STONE*/
		blockStoneDecoration = new BlockStoneDecoration();
		blockStoneDecorationSlab = (BlockITBase<?>) new BlockITSlab<>("stone_decoration_slab", Material.ROCK, PropertyEnum.create("type", BlockType_StoneDecoration.class)).setMetaExplosionResistance(BlockType_StoneDecoration.COKEBRICK_REINFORCED.getMeta(), 180).setHardness(2.0F).setResistance(10.0F);

		/*WOODEN*/
		blockWoodenCrate = new BlockWoodenCrate();

		/*FLUIDS*/
		fluidSteam = new FluidColored("steam", 0x3E444F, 1000, -100, 500, true);
		fluidExhaustSteam = new FluidColored("exhauststeam", 0xC1C1C5, 500, -100, 500, true);
		fluidDistWater = new FluidColored("distwater", 0x7079E0, 1000, 1000, false);
		fluidFlueGas = new FluidColored("fluegas", 0xFFFFFF, -100, 500, true);
		fluidHighPressureSteam = new FluidColored("highpressuresteam", 0x606978, 1500, -300, 500, true);
		fluidHotWater = new FluidColored("hot_spring_water", 0x0dffff, 350, 1000, 1000, false);
		fluidMoltenSalt = new FluidColored("moltensalt", 0xc4c6c7, 1100, 2170, 10000);
		fluidMoltenSodium = new FluidColored("moltensodium", 0xc2c2c2, 400, 927, 10000);
		fluidSuperheatedMoltenSodium = new FluidColored("superheatedmoltensodium", 0xaea0a2, 927, 1000, 10000);
		fluidChlorine = new FluidColored("chlorine", 0xc0e67b, 300, -100, 1000, false);

		/*FLUID BLOCKS*/
		blockFluidSteam = new BlockITFluid("fluidSteam", fluidSteam, Material.WATER);
		blockFluidExhaustSteam = new BlockITFluid("exhaustSteam", fluidExhaustSteam, Material.WATER);
		blockFluidDistWater = new BlockITFluid("fluidDistWater", fluidDistWater, Material.WATER);
		blockFlueGas = new BlockITFluid("fluidFlueGas", fluidFlueGas, Material.WATER);
		blockFluidHighPressureSteam = new BlockITFluid("fluidHighPressureSteam", fluidHighPressureSteam, Material.WATER);
		blockFluidHotWater = new BlockITFluid("fluidHotWater", fluidHotWater, Material.WATER);
		blockFluidMoltenSalt = new BlockITFluid("fluidMoltenSalt", fluidMoltenSalt, Material.LAVA);
		blockFluidMoltenSodium = new BlockITFluid("fluidMoltenSodium", fluidMoltenSodium, Material.LAVA);
		blockFluidSuperheatedMoltenSodium = new BlockITFluid("fluidSuperheatedMoltenSodium", fluidSuperheatedMoltenSodium, Material.LAVA);
		blockFluidChlorine = new BlockITFluid("fluidChlorine", fluidChlorine, Material.WATER);

		/*ITEMS*/
		itemMaterial = new ItemITBase("material", 64, "salt");
		
		/*MANUAL*/
		registerVariables();

		if(Experimental.replace_IE_pipes) {
			blockMetalDevice0Dummy = IEContent.blockMetalDevice0;
			IEContent.blockMetalDevice0.setCreativeTab(null);
			IEContent.blockMetalDevice0.setRegistryName("immersiveengineering", "metaldevice0dummy");
			IEContent.blockMetalDevice0.setUnlocalizedName("immersiveengineering.metaldevice0dummy");
			IEContent.registeredIEItems.remove(Item.getItemFromBlock(IEContent.blockMetalDevice0));
			IEContent.registeredIEBlocks.remove(IEContent.blockMetalDevice0);
			IEContent.blockMetalDevice0 = new BlockMetalDevice0();

			blockMetalDevice1Dummy = IEContent.blockMetalDevice1;
			IEContent.blockMetalDevice1.setCreativeTab(null);
			IEContent.blockMetalDevice1.setRegistryName("immersiveengineering", "metaldevice1dummy");
			IEContent.blockMetalDevice1.setUnlocalizedName("immersiveengineering.metaldevice1dummy");
			IEContent.registeredIEItems.remove(Item.getItemFromBlock(IEContent.blockMetalDevice1));
			IEContent.registeredIEBlocks.remove(IEContent.blockMetalDevice1);
			IEContent.blockMetalDevice1 = new BlockMetalDevice1();
			ITLogger.info("Replaced IE Pipes with IT Pipes");
		}
	}

	public static void init() {
		/*TILE ENTITIES*/
		registerTile(TileEntityITSlab.class);
		registerTile(TileEntityTimer.class);
		registerTile(TileEntityTrashItem.class);
		registerTile(TileEntityTrashFluid.class);
		registerTile(TileEntityTrashEnergy.class);
		registerTile(TileEntityBarrel.class);
		registerTile(TileEntityBarrelOpen.class);
		registerTile(TileEntityBarrelSteel.class);
		registerTile(TileEntityCrate.class);
		registerTile(TileEntityFluidValve.class);
		registerTile(TileEntityLoadController.class);
		registerTile(TileEntityStackLimiter.class);

		//MORE TEMPORARY STUFF
		registerTile(TileEntityAlternator.class);
		registerTile(TileEntitySteamTurbine.class);
		registerTile(TileEntityBoiler.class);
		registerTile(TileEntityDistiller.class);
		registerTile(TileEntitySolarTower.class);
		registerTile(TileEntitySolarReflector.class);
		registerTile(TileEntityHighPressureSteamTurbine.class);

		if(Multiblock.enable_boiler) {
			registerTile(TileEntityBoilerSlave.class);
			registerTile(TileEntityBoilerMaster.class);
			MultiblockHandler.registerMultiblock(MultiblockBoiler.instance);
		}
		if(Multiblock.enable_distiller) {
			registerTile(TileEntityDistillerSlave.class);
			registerTile(TileEntityDistillerMaster.class);
			MultiblockHandler.registerMultiblock(MultiblockDistiller.instance);
		}
		if(Multiblock.enable_solarTower) {
			registerTile(TileEntitySolarTowerSlave.class);
			registerTile(TileEntitySolarTowerMaster.class);
			MultiblockHandler.registerMultiblock(MultiblockSolarTower.instance);
			registerTile(TileEntitySolarReflectorSlave.class);
			registerTile(TileEntitySolarReflectorMaster.class);
			MultiblockHandler.registerMultiblock(MultiblockSolarReflector.instance);
		}
		if(Multiblock.enable_steamTurbine) {
			registerTile(TileEntityAlternatorSlave.class);
			registerTile(TileEntityAlternatorMaster.class);
			MultiblockHandler.registerMultiblock(MultiblockAlternator.instance);
			registerTile(TileEntitySteamTurbineSlave.class);
			registerTile(TileEntitySteamTurbineMaster.class);
			MultiblockHandler.registerMultiblock(MultiblockSteamTurbine.instance);
		}
		if(Multiblock.enable_coolingTower) {
			registerTile(TileEntityCoolingTowerSlave.class);
			registerTile(TileEntityCoolingTowerMaster.class);
			MultiblockHandler.registerMultiblock(MultiblockCoolingTower.instance);
		}
		if(Multiblock.enable_gasTurbine) {
			// Enable alternator if not enabled with steam turbine
			if(!Multiblock.enable_steamTurbine) {
				registerTile(TileEntityAlternatorSlave.class);
				registerTile(TileEntityAlternatorMaster.class);
				MultiblockHandler.registerMultiblock(MultiblockAlternator.instance);
			}
			registerTile(TileEntityGasTurbineSlave.class);
			registerTile(TileEntityGasTurbineMaster.class);
			MultiblockHandler.registerMultiblock(MultiblockGasTurbine.instance);
		}
		if(Multiblock.enable_heatExchanger) {
			registerTile(TileEntityHeatExchangerSlave.class);
			registerTile(TileEntityHeatExchangerMaster.class);
			MultiblockHandler.registerMultiblock(MultiblockHeatExchanger.instance);
		}
		if(Multiblock.enable_highPressureSteamTurbine) {
			// Enable alternator if not enabled with steam turbine
			if(!Multiblock.enable_steamTurbine || Multiblock.enable_gasTurbine) {
				registerTile(TileEntityAlternatorSlave.class);
				registerTile(TileEntityAlternatorMaster.class);
				MultiblockHandler.registerMultiblock(MultiblockAlternator.instance);
			}
			registerTile(TileEntityHighPressureSteamTurbineSlave.class);
			registerTile(TileEntityHighPressureSteamTurbineMaster.class);
			MultiblockHandler.registerMultiblock(MultiblockHighPressureSteamTurbine.instance);
		}
		if(Multiblock.enable_electrolyticCrucibleBattery) {
			registerTile(TileEntityElectrolyticCrucibleBatterySlave.class);
			registerTile(TileEntityElectrolyticCrucibleBatteryMaster.class);
			MultiblockHandler.registerMultiblock(MultiblockElectrolyticCrucibleBattery.instance);
		}
		if(Multiblock.enable_meltingCrucible) {
			registerTile(TileEntityMeltingCrucibleSlave.class);
			registerTile(TileEntityMeltingCrucibleMaster.class);
			MultiblockHandler.registerMultiblock(MultiblockMeltingCrucible.instance);
		}
		if(Multiblock.enable_radiator) {
			registerTile(TileEntityRadiatorSlave.class);
			registerTile(TileEntityRadiatorMaster.class);
			MultiblockHandler.registerMultiblock(MultiblockRadiator.instance);
		}
		if(Multiblock.enable_solarMelter) {
			registerTile(TileEntitySolarMelterSlave.class);
			registerTile(TileEntitySolarMelterMaster.class);
			MultiblockHandler.registerMultiblock(MultiblockSolarMelter.instance);
		}

		registerTile(TileEntitySteelSheetmetalTank.class);
		registerTile(TileEntitySteelSheetmetalTankSlave.class);
		registerTile(TileEntitySteelSheetmetalTankMaster.class);
		MultiblockHandler.registerMultiblock(MultiblockSteelSheetmetalTank.instance);
		if(Experimental.replace_IE_pipes) {
			normallyPressurized.add(FluidRegistry.getFluid("steam"));
			normallyPressurized.add(FluidRegistry.getFluid("fluegas"));
			normallyPressurized.add(FluidRegistry.getFluid("exhauststeam"));
			normallyPressurized.add(FluidRegistry.getFluid("highpressuresteam"));
			IEHijackedRegisterTile(TileEntityFluidPumpAlternative.class, "FluidPump");
			TileEntityFluidPipeAlternative.initCovers();
			IEHijackedRegisterTile(TileEntityFluidPipeAlternative.class, "FluidPipe");
		}
	}

	@SubscribeEvent
	public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
		/*RECIPES*/
		if(Multiblock.enable_boiler && Recipes.register_boiler_recipes) {
			BoilerRecipe.addRecipe(new FluidStack(FluidRegistry.getFluid("steam"), 450), new FluidStack(FluidRegistry.WATER, 250), 10);
			BoilerRecipe.addRecipe(new FluidStack(FluidRegistry.getFluid("steam"), 500), new FluidStack(FluidRegistry.getFluid("distwater"), 250), 10);
			BoilerRecipe.addFuel(new FluidStack(FluidRegistry.getFluid("biodiesel"), 10), 1, 10);
			if(FluidRegistry.getFluid("gasoline") != null) BoilerRecipe.addFuel(new FluidStack(FluidRegistry.getFluid("gasoline"), 50), 1, 10);
			if(FluidRegistry.getFluid("diesel") != null) BoilerRecipe.addFuel(new FluidStack(FluidRegistry.getFluid("diesel"), 7), 1, 10);
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
			DistillerRecipe.addRecipe(new FluidStack(FluidRegistry.getFluid("distwater"), 500), new FluidStack(FluidRegistry.WATER, 1000), distillerItem, 10000, 20, distillerChance);
		}
		if(Multiblock.enable_solarTower && Recipes.register_solarTower_recipes) {
			SolarTowerRecipe.addRecipe(new FluidStack(FluidRegistry.getFluid("steam"), 450), new FluidStack(FluidRegistry.WATER, 250), 20);
			SolarTowerRecipe.addRecipe(new FluidStack(FluidRegistry.getFluid("steam"), 500), new FluidStack(FluidRegistry.getFluid("distwater"), 250), 20);
			SolarTowerRecipe.addRecipe(new FluidStack(FluidRegistry.getFluid("superheatedmoltensodium"), 80), new FluidStack(FluidRegistry.getFluid("moltensodium"), 80), 20);
		}
		if(Multiblock.enable_steamTurbine && Recipes.register_steamTurbine_recipes) {
			SteamTurbineRecipe.addFuel(new FluidStack(FluidRegistry.getFluid("exhauststeam"), 100), new FluidStack(FluidRegistry.getFluid("steam"), 100), 1);
		}
		if(Multiblock.enable_gasTurbine && Recipes.register_gas_turbine_recipes) {
			GasTurbineRecipe.addFuel(new FluidStack(FluidRegistry.getFluid("fluegas"), 1000), new FluidStack(FluidRegistry.getFluid("biodiesel"), 160), 10);
			if(FluidRegistry.getFluid("gasoline") != null) GasTurbineRecipe.addFuel(new FluidStack(FluidRegistry.getFluid("fluegas"), 1000), new FluidStack(FluidRegistry.getFluid("gasoline"), 800), 10);
			if(FluidRegistry.getFluid("diesel") != null) GasTurbineRecipe.addFuel(new FluidStack(FluidRegistry.getFluid("fluegas"), 1000), new FluidStack(FluidRegistry.getFluid("diesel"), 114), 10);
			if(FluidRegistry.getFluid("kerosene") != null) GasTurbineRecipe.addFuel(new FluidStack(FluidRegistry.getFluid("fluegas"), 1000), new FluidStack(FluidRegistry.getFluid("kerosene"), 150), 10);
		}
		if(Multiblock.enable_coolingTower && Recipes.register_cooling_tower_recipes) {
			CoolingTowerRecipe.addRecipe(new FluidStack(FluidRegistry.getFluid("water"), 2925), new FluidStack(FluidRegistry.getFluid("water"), 2925), new FluidStack(FluidRegistry.getFluid("water"), 2925), new FluidStack(FluidRegistry.getFluid("hot_spring_water"), 8100), new FluidStack(FluidRegistry.getFluid("water"), 900), 3);
		}
		if(Multiblock.enable_heatExchanger && Recipes.register_heat_exchanger_recipes) {
			HeatExchangerRecipe.addRecipe(new FluidStack(FluidRegistry.getFluid("steam"), 450), null, new FluidStack(FluidRegistry.WATER, 250), new FluidStack(FluidRegistry.getFluid("fluegas"), 1000), 640, 10);
			HeatExchangerRecipe.addRecipe(new FluidStack(FluidRegistry.getFluid("steam"), 500), null, new FluidStack(FluidRegistry.getFluid("distwater"), 250), new FluidStack(FluidRegistry.getFluid("fluegas"), 1000), 640, 10);
			HeatExchangerRecipe.addRecipe(new FluidStack(FluidRegistry.getFluid("steam"), 450), new FluidStack(FluidRegistry.getFluid("moltensodium"), 80), new FluidStack(FluidRegistry.WATER, 250), new FluidStack(FluidRegistry.getFluid("superheatedmoltensodium"), 80), 640, 10);
			HeatExchangerRecipe.addRecipe(new FluidStack(FluidRegistry.getFluid("steam"), 500), new FluidStack(FluidRegistry.getFluid("moltensodium"), 80), new FluidStack(FluidRegistry.getFluid("distwater"), 250), new FluidStack(FluidRegistry.getFluid("superheatedmoltensodium"), 80), 640, 10);
			HeatExchangerRecipe.addRecipe(new FluidStack(FluidRegistry.getFluid("distwater"), 250),  new FluidStack(FluidRegistry.getFluid("hot_spring_water"), 4500), new FluidStack(FluidRegistry.getFluid("exhauststeam"), 500), new FluidStack(FluidRegistry.getFluid("water"), 4500), 160, 5);
		}
		if(Multiblock.enable_highPressureSteamTurbine && Recipes.register_highPressureSteamTurbine_recipes) {
			HighPressureSteamTurbineRecipe.addFuel(new FluidStack(FluidRegistry.getFluid("steam"), 100), new FluidStack(FluidRegistry.getFluid("highpressuresteam"), 100), 1);
		}
		if(Multiblock.enable_electrolyticCrucibleBattery && Recipes.register_electrolyticCrucibleBattery_recipes) {
			if(FluidRegistry.isFluidRegistered("hydrogen") && FluidRegistry.isFluidRegistered("oxygen")) ElectrolyticCrucibleBatteryRecipe.addRecipe(new FluidStack(FluidRegistry.getFluid("hydrogen"), 1000), new FluidStack(FluidRegistry.getFluid("oxygen"), 500), null, null, new FluidStack(FluidRegistry.getFluid("water"), 500), 512000, 250);
			else if(FluidRegistry.isFluidRegistered("liquidhydrogen") && FluidRegistry.isFluidRegistered("liquidoxygen")) ElectrolyticCrucibleBatteryRecipe.addRecipe(new FluidStack(FluidRegistry.getFluid("liquidhydrogen"), 1000), new FluidStack(FluidRegistry.getFluid("liquidoxygen"), 500), null, null, new FluidStack(FluidRegistry.getFluid("water"), 1000), 512000, 250);
			else if(FluidRegistry.isFluidRegistered("fluidhydrogen") && FluidRegistry.isFluidRegistered("fluidoxygen")) ElectrolyticCrucibleBatteryRecipe.addRecipe(new FluidStack(FluidRegistry.getFluid("fluidhydrogen"), 1000), new FluidStack(FluidRegistry.getFluid("fluidoxygen"), 500), null, null, new FluidStack(FluidRegistry.getFluid("water"), 500), 512000, 250);
			ElectrolyticCrucibleBatteryRecipe.addRecipe(new FluidStack(FluidRegistry.getFluid("chlorine"), 1000), new FluidStack(FluidRegistry.getFluid("moltensodium"), 1000), null, null, new FluidStack(FluidRegistry.getFluid("moltensalt"), 1000), 512000, 250);
		}
		if((Multiblock.enable_meltingCrucible && Recipes.register_meltingCrucible_recipes) || (Multiblock.enable_solarMelter && Recipes.register_meltingCrucible_recipes)) {
			MeltingCrucibleRecipe.addRecipe(new FluidStack(FluidRegistry.getFluid("lava"), 1000), new OreIngredient("cobblestone"), 40960, 80);
			MeltingCrucibleRecipe.addRecipe(new FluidStack(FluidRegistry.getFluid("moltensalt"), 100), new OreIngredient("dustSalt"), 4096, 8);
		}
		if(Multiblock.enable_radiator && Recipes.register_radiator_recipes) {
			RadiatorRecipe.addRecipe(new FluidStack(FluidRegistry.getFluid("distwater"), 250), new FluidStack(FluidRegistry.getFluid("exhauststeam"), 500), 80);
		}
	}

	@SuppressWarnings("deprecation")
	public static void registerTile(Class<? extends TileEntity> tile) {
		String tileEntity = tile.getSimpleName();
		tileEntity = tileEntity.substring(tileEntity.indexOf("TileEntity") + "TileEntity".length());
		GameRegistry.registerTileEntity(tile, ImmersiveTechnology.MODID + ":" + tileEntity);
	}

	@SuppressWarnings("deprecation")
	public static void IEHijackedRegisterTile(Class<? extends TileEntity> tile, String name) {
		GameRegistry.registerTileEntity(tile, "immersiveengineering:" + name);
		IEContent.registeredIETiles.add(tile);
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
		Config.manual_int.put("alternator_energyPerTickPerPort", (Alternator.alternator_energy_perTick / 6));
		Config.manual_int.put("alternator_energyStorage", Alternator.alternator_energy_capacitorSize);
		Config.manual_int.put("alternator_energyPerTick", Alternator.alternator_energy_perTick);
		Config.manual_double.put("boiler_cooldownTime", ((Boiler.boiler_heat_workingLevel / Boiler.boiler_progress_lossInTicks) / 20));
		Config.manual_int.put("solarTower_minRange", SolarReflector.solarReflector_minRange);
		Config.manual_int.put("solarTower_maxRange", SolarReflector.solarReflector_maxRange);
		Config.manual_int.put("steamTurbine_timeToMax", ((MechanicalEnergy.mechanicalEnergy_speed_max / SteamTurbine.steamTurbine_speed_gainPerTick) / 20));
		Config.manual_int.put("highPressureSteamTurbine_timeToMax", ((MechanicalEnergy.mechanicalEnergy_speed_max / HighPressureSteamTurbine.highPressureSteamTurbine_speed_gainPerTick) / 20));
		Config.manual_int.put("steelTank_tankSize", SteelTank.steelTank_tankSize);
	}

}