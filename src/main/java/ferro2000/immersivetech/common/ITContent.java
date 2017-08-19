package ferro2000.immersivetech.common;

import java.util.ArrayList;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import ferro2000.immersivetech.ImmersiveTech;
import ferro2000.immersivetech.api.craftings.DistillerRecipes;
import ferro2000.immersivetech.api.craftings.SolarTowerRecipes;
import ferro2000.immersivetech.api.energy.SteamHandler;
import ferro2000.immersivetech.common.blocks.BlockITBase;
import ferro2000.immersivetech.common.blocks.BlockITFluid;
import ferro2000.immersivetech.common.blocks.metal.BlockMetalMultiblock;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockDistiller;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockSolarReflector;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockSolarTower;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockSteamTurbine;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityDistiller;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntitySolarReflector;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntitySolarTower;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntitySteamTurbine;
import ferro2000.immersivetech.common.items.ItemITBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ITContent {
	
	/*BLOCKS*/
	public static ArrayList<Block> registeredITBlocks = new ArrayList();
	/*MULTIBLOCKS*/
	public static BlockITBase blockMetalMultiblock;
	/*FLUID BLOCKS*/
	public static BlockITFluid blockFluidDistWater;
	public static BlockITFluid blockFluidSteam;
	
	/*ITEMS*/
	public static ArrayList<Item> registeredITItems = new ArrayList();
	/*MATERIALS*/
	public static Item itemMaterial;
	
	/*FLUIDS*/
	public static Fluid fluidDistWater;
	public static Fluid fluidSteam;
	
	public static void preInit(){
		
		/*BLOCKS*/
		/*MULTIBLOCKS*/
		blockMetalMultiblock = new BlockMetalMultiblock();
		
		/*FLUIDS*/
		fluidDistWater = new Fluid("distwater", new ResourceLocation("immersivetech:blocks/fluid/distWater_still"), new ResourceLocation("immersivetech:blocks/fluid/distWater_flow")).setDensity(1000).setViscosity(1000);
		if(!FluidRegistry.registerFluid(fluidDistWater))
			fluidDistWater = FluidRegistry.getFluid("distwater");
		FluidRegistry.addBucketForFluid(fluidDistWater);
		fluidSteam = new Fluid("steam", new ResourceLocation("immersivetech:blocks/fluid/steam_still"), new ResourceLocation("immersivetech:blocks/fluid/steam_flow")).setDensity(-100).setViscosity(500).setGaseous(true);
		if(!FluidRegistry.registerFluid(fluidSteam))
			fluidSteam = FluidRegistry.getFluid("steam");
		FluidRegistry.addBucketForFluid(fluidSteam);
		
		/*FLUID BLOCKS*/
		blockFluidDistWater = new BlockITFluid("fluidDistWater", fluidDistWater, Material.WATER);
		blockFluidSteam = new BlockITFluid("fluidSteam", fluidSteam, Material.WATER);
		
		/*ITEMS*/
		/*MATERIALS*/
		itemMaterial = new ItemITBase("material", 64, "salt");
		
	}
	
	public static int distWaterWater = 500;
	
	public static int steamWater = 500;
	public static int steamDistWater = 750;
	
	public static int steamBurnTime = 250;
	
	public static void init(){
		
		/*TILE ENTITIES*/
		
		/*BLOCKS*/
		/*MULTIBLOCKS*/
		registerTile(TileEntityDistiller.class);
		registerTile(TileEntitySolarTower.class);
		registerTile(TileEntitySolarReflector.class);
		registerTile(TileEntitySteamTurbine.class);
		
		MultiblockHandler.registerMultiblock(MultiblockDistiller.instance);
		MultiblockHandler.registerMultiblock(MultiblockSolarTower.instance);
		MultiblockHandler.registerMultiblock(MultiblockSolarReflector.instance);
		MultiblockHandler.registerMultiblock(MultiblockSteamTurbine.instance);
		
		/*RECIPES*/
		/*MULTIBLOCKS*/
		DistillerRecipes.addRecipe(new FluidStack(fluidDistWater, distWaterWater/10), new FluidStack(FluidRegistry.WATER, 100), new ItemStack(itemMaterial, 1, 0), 50, 1, 0.0025F);
		
		SolarTowerRecipes.addRecipe(new FluidStack(fluidSteam, steamWater/5), new FluidStack(FluidRegistry.WATER, 200), 10);
		SolarTowerRecipes.addRecipe(new FluidStack(fluidSteam, steamDistWater/5), new FluidStack(fluidDistWater, 200), 10);
		
		/*HANDLER*/
		/*STEAM*/
		SteamHandler.registerSteam(fluidSteam, steamBurnTime);
		SteamHandler.registerSteam(FluidRegistry.getFluid("steam"), steamBurnTime);
		
		SteamHandler.registerDrillSteam(fluidSteam);
		SteamHandler.registerDrillSteam(FluidRegistry.getFluid("steam"));
		
	}
	
	public static void registerTile(Class<? extends TileEntity> tile){
	    String s = tile.getSimpleName();
	    s = s.substring(s.indexOf("TileEntity") + "TileEntity".length());
	    GameRegistry.registerTileEntity(tile, ImmersiveTech.MODID + ":" + s);
	}

}
