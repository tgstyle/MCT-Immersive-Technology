package mctmods.immersivetechnology.common;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.common.Config;

import com.immersiveconvergence.api.multiblock.BlockMatcher;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.Config.ITConfig;
import mctmods.immersivetechnology.common.blocks.BlockITBase;
import mctmods.immersivetechnology.common.blocks.BlockITFluid;
import mctmods.immersivetechnology.common.blocks.BlockITSlab;
import mctmods.immersivetechnology.common.blocks.BlockValve;
import mctmods.immersivetechnology.common.blocks.connectors.BlockConnectors;
import mctmods.immersivetechnology.common.blocks.connectors.tileentities.TileEntityTimer;
import mctmods.immersivetechnology.common.blocks.metal.*;
import mctmods.immersivetechnology.common.blocks.metal.conveyors.*;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.*;
import mctmods.immersivetechnology.common.blocks.stone.BlockStoneDecoration;
import mctmods.immersivetechnology.common.blocks.stone.types.BlockType_StoneDecoration;
import mctmods.immersivetechnology.common.blocks.wooden.BlockWoodenCrate;
import mctmods.immersivetechnology.common.blocks.wooden.tileentities.TileEntityCrate;
import mctmods.immersivetechnology.common.fluid.FluidColored;
import mctmods.immersivetechnology.common.items.ItemFormationTool;
import mctmods.immersivetechnology.common.items.ItemITBase;
import mctmods.immersivetechnology.common.multiblocks.metal.BlockMetalMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.BlockMetalMultiblock1;
import mctmods.immersivetechnology.common.multiblocks.metal.BlockMetalMultiblock2;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.*;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.*;
import mctmods.immersivetechnology.common.multiblocks.stone.BlockStoneMultiblock;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityAdvancedCokeOvenMaster;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityAdvancedCokeOvenSlave;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityCoolingTowerMaster;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityCoolingTowerSlave;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentitiesmultiblockpart.TileEntityITMultiblockPartAdvancedCokeOven;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentitiesmultiblockpart.TileEntityITMultiblockPartCoolingTower;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITSlab;
import mctmods.immersivetechnology.common.util.ITLogger;
import mctmods.immersivetechnology.common.util.ITRecipeLoader;
import mctmods.immersivetechnology.core.MCTMixinConfig;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = ImmersiveTechnology.MODID)
public class ITContent {
    /*BLOCKS*/
    public static ArrayList<Block> registeredITBlocks = new ArrayList<>();

    /*MULTIBLOCKS*/
    public static BlockITBase<?> blockMetalMultiblock;
    public static BlockITBase<?> blockMetalMultiblock1;
    public static BlockITBase<?> blockMetalMultiblock2;
    public static BlockITBase<?> blockStoneMultiblock;

    /*CONNECTORS*/
    public static BlockITBase<?> blockConnectors;

    /*METAL*/
    public static BlockITBase<?> blockMetalDevice;
    public static BlockITBase<?> blockMetalTrash;
    public static BlockITBase<?> blockMetalBarrel;
    public static BlockITBase<?> blockValve;
    public static BlockITBase<?> blockMetalDecoration;

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
    public static Item itemFormationTool;

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

    public static Set<Fluid> normallyPressurized = new HashSet<>();

    public static TileEntityITMultiblockPartAdvancedCokeOven multiblockAdvancedCokeOven;
    public static TileEntityITMultiblockPartBoilerTank multiblockBoilerTank;
    public static TileEntityITMultiblockPartBoilerLiquid multiblockBoilerLiquid;
    public static TileEntityITMultiblockPartBoilerSolid multiblockBoilerSolid;
    public static TileEntityITMultiblockPartDistiller multiblockDistiller;
    public static TileEntityITMultiblockPartSolarTower multiblockSolarTower;
    public static TileEntityITMultiblockPartSolarReflector multiblockSolarReflector;
    public static TileEntityITMultiblockPartAlternator multiblockAlternator;
    public static TileEntityITMultiblockPartSteamTurbine multiblockSteamTurbine;
    public static TileEntityITMultiblockPartCoolingTower multiblockCoolingTower;
    public static TileEntityITMultiblockPartGasTurbine multiblockGasTurbine;
    public static TileEntityITMultiblockPartHeatExchanger multiblockHeatExchanger;
    public static TileEntityITMultiblockPartHighPressureSteamTurbine multiblockHighPressureSteamTurbine;
    public static TileEntityITMultiblockPartElectrolyticCrucibleBattery multiblockElectrolyticCrucibleBattery;
    public static TileEntityITMultiblockPartMeltingCrucible multiblockMeltingCrucible;
    public static TileEntityITMultiblockPartRadiator multiblockRadiator;
    public static TileEntityITMultiblockPartSolarMelter multiblockSolarMelter;
    public static TileEntityITMultiblockPartSteelSheetmetalTank multiblockSteelSheetmetalTank;

    public static void preInit() {
        /*CONVEYORS*/
        registerConveyors();

        /*MULTIBLOCKS*/
        blockMetalMultiblock = new BlockMetalMultiblock();
        blockMetalMultiblock1 = new BlockMetalMultiblock1();
        blockMetalMultiblock2 = new BlockMetalMultiblock2();
        blockStoneMultiblock = new BlockStoneMultiblock();

        /*CONNECTORS*/
        blockConnectors = new BlockConnectors();

        /*METAL*/
        blockMetalTrash = new BlockMetalTrash();
        blockMetalBarrel = new BlockMetalBarrel();
        blockValve = new BlockValve();
        if (ITConfig.Multiblocks.enable.enable_advancedCokeOven) { blockMetalDevice = new BlockMetalDevice(); }

        blockMetalDecoration = new BlockMetalDecoration();

        /*STONE*/
        blockStoneDecoration = new BlockStoneDecoration();
        blockStoneDecorationSlab = (BlockITBase<?>) new BlockITSlab<>("stone_decoration_slab", Material.ROCK, PropertyEnum.create("type", BlockType_StoneDecoration.class)).setMetaExplosionResistance(BlockType_StoneDecoration.COKEBRICK_REINFORCED.getMeta(), 180).setHardness(2.0F).setResistance(10.0F);

        /*WOODEN*/
        blockWoodenCrate = new BlockWoodenCrate();

        /*FLUIDS*/
        fluidSteam = FluidColored.register(new FluidColored("steam", 0x3E444F, 1000, -100, 500, true));
        fluidExhaustSteam = FluidColored.register(new FluidColored("exhauststeam", 0xC1C1C5, 500, -100, 500, true));
        fluidDistWater = FluidColored.register(new FluidColored("distwater", 0x7079E0, 1000, 1000, false));
        fluidFlueGas = FluidColored.register(new FluidColored("fluegas", 0xFFFFFF, -100, 500, true));
        fluidHighPressureSteam = FluidColored.register(new FluidColored("highpressuresteam", 0x606978, 1500, -300, 500, true));
        fluidHotWater = FluidColored.register(new FluidColored("hotwater", 0x0dffff, 350, 1000, 1000, false));
        fluidMoltenSalt = FluidColored.register(new FluidColored("moltensalt", 0xc4c6c7, 1100, 2170, 10000));
        fluidMoltenSodium = FluidColored.register(new FluidColored("moltensodium", 0xc2c2c2, 400, 927, 10000));
        fluidSuperheatedMoltenSodium = FluidColored.register(new FluidColored("superheatedmoltensodium", 0xaea0a2, 927, 1000, 10000));
        fluidChlorine = FluidColored.register(new FluidColored("chlorine", 0xc0e67b, 300, -100, 1000, false));

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
        itemFormationTool = new ItemFormationTool();

        /*MANUAL*/
        registerVariables();
    }

    public static void init() {
        BlockMatcher.addGenericOreNames("blockSteel", "blockSheetmetalSteel", "blockSheetmetalIron", "blockSheetmetalSilver", "slabSheetmetalSteel", "scaffoldingSteel", "fenceSteel", "blockNickel", "blockTungsten", "blockSheetmetalNickel", "blockSheetmetalTungsten", "blockFurnace");

        /*TILE ENTITIES*/
        registerTile(TileEntityITSlab.class);
        registerTile(TileEntityTimer.class);
        registerTile(TileEntityTrashItem.class);
        registerTile(TileEntityTrashFluid.class);
        registerTile(TileEntityTrashEnergy.class);
        registerTile(TileEntityBarrelCreative.class);
        registerTile(TileEntityBarrelOpen.class);
        registerTile(TileEntityBarrelSteel.class);
        registerTile(TileEntityCrate.class);
        registerTile(TileEntityFluidValve.class);
        registerTile(TileEntityLoadController.class);
        registerTile(TileEntityStackLimiter.class);

        if (ITConfig.Multiblocks.enable.enable_advancedCokeOven) {
            registerTile(TileEntityAdvancedCokeOvenSlave.class);
            registerTile(TileEntityAdvancedCokeOvenMaster.class);
            registerTile(TileEntityAdvancedCokeOvenBaseheater.class);
            MultiblockHandler.registerMultiblock(TileEntityITMultiblockPartAdvancedCokeOven.instance);
            multiblockAdvancedCokeOven = TileEntityITMultiblockPartAdvancedCokeOven.instance;
        }
        if (ITConfig.Multiblocks.enable.enable_boiler) {
            registerTileAlias(TileEntityBoilerTankSlave.class, "BoilerSlave");
            registerTileAlias(TileEntityBoilerTankMaster.class, "BoilerMaster");
            MultiblockHandler.registerMultiblock(TileEntityITMultiblockPartBoilerTank.instance);
            multiblockBoilerTank = TileEntityITMultiblockPartBoilerTank.instance;
            registerTile(TileEntityBoilerLiquidSlave.class);
            registerTile(TileEntityBoilerLiquidMaster.class);
            MultiblockHandler.registerMultiblock(TileEntityITMultiblockPartBoilerLiquid.instance);
            multiblockBoilerLiquid = TileEntityITMultiblockPartBoilerLiquid.instance;
        }
        if (ITConfig.Multiblocks.enable.enable_boilerSolid) {
            registerTile(TileEntityBoilerSolidSlave.class);
            registerTile(TileEntityBoilerSolidMaster.class);
            MultiblockHandler.registerMultiblock(TileEntityITMultiblockPartBoilerSolid.instance);
            multiblockBoilerSolid = TileEntityITMultiblockPartBoilerSolid.instance;
        }
        if (ITConfig.Multiblocks.enable.enable_distiller) {
            registerTile(TileEntityDistillerSlave.class);
            registerTile(TileEntityDistillerMaster.class);
            MultiblockHandler.registerMultiblock(TileEntityITMultiblockPartDistiller.instance);
            multiblockDistiller = TileEntityITMultiblockPartDistiller.instance;
        }
        if (ITConfig.Multiblocks.enable.enable_solarTower) {
            registerTile(TileEntitySolarTowerSlave.class);
            registerTile(TileEntitySolarTowerMaster.class);
            MultiblockHandler.registerMultiblock(TileEntityITMultiblockPartSolarTower.instance);
            multiblockSolarTower = TileEntityITMultiblockPartSolarTower.instance;
            registerTile(TileEntitySolarReflectorSlave.class);
            registerTile(TileEntitySolarReflectorMaster.class);
            MultiblockHandler.registerMultiblock(TileEntityITMultiblockPartSolarReflector.instance);
            multiblockSolarReflector = TileEntityITMultiblockPartSolarReflector.instance;
        }
        boolean alternatorRegistered = false;
        if (ITConfig.Multiblocks.enable.enable_steamTurbine) {
            registerTile(TileEntityAlternatorSlave.class);
            registerTile(TileEntityAlternatorMaster.class);
            MultiblockHandler.registerMultiblock(TileEntityITMultiblockPartAlternator.instance);
            multiblockAlternator = TileEntityITMultiblockPartAlternator.instance;
            alternatorRegistered = true;
            registerTile(TileEntitySteamTurbineSlave.class);
            registerTile(TileEntitySteamTurbineMaster.class);
            MultiblockHandler.registerMultiblock(TileEntityITMultiblockPartSteamTurbine.instance);
            multiblockSteamTurbine = TileEntityITMultiblockPartSteamTurbine.instance;
        }
        if (ITConfig.Multiblocks.enable.enable_coolingTower) {
            registerTile(TileEntityCoolingTowerSlave.class);
            registerTile(TileEntityCoolingTowerMaster.class);
            MultiblockHandler.registerMultiblock(TileEntityITMultiblockPartCoolingTower.instance);
            multiblockCoolingTower = TileEntityITMultiblockPartCoolingTower.instance;
        }
        if (ITConfig.Multiblocks.enable.enable_gasTurbine) {
            if (!alternatorRegistered) {
                registerTile(TileEntityAlternatorSlave.class);
                registerTile(TileEntityAlternatorMaster.class);
                MultiblockHandler.registerMultiblock(TileEntityITMultiblockPartAlternator.instance);
                multiblockAlternator = TileEntityITMultiblockPartAlternator.instance;
                alternatorRegistered = true;
            }
            registerTile(TileEntityGasTurbineSlave.class);
            registerTile(TileEntityGasTurbineMaster.class);
            MultiblockHandler.registerMultiblock(TileEntityITMultiblockPartGasTurbine.instance);
            multiblockGasTurbine = TileEntityITMultiblockPartGasTurbine.instance;
        }
        if (ITConfig.Multiblocks.enable.enable_heatExchanger) {
            registerTile(TileEntityHeatExchangerSlave.class);
            registerTile(TileEntityHeatExchangerMaster.class);
            MultiblockHandler.registerMultiblock(TileEntityITMultiblockPartHeatExchanger.instance);
            multiblockHeatExchanger = TileEntityITMultiblockPartHeatExchanger.instance;
        }
        if (ITConfig.Multiblocks.enable.enable_highPressureSteamTurbine) {
            if (!alternatorRegistered) {
                registerTile(TileEntityAlternatorSlave.class);
                registerTile(TileEntityAlternatorMaster.class);
                MultiblockHandler.registerMultiblock(TileEntityITMultiblockPartAlternator.instance);
                multiblockAlternator = TileEntityITMultiblockPartAlternator.instance;
            }
            registerTile(TileEntityHighPressureSteamTurbineSlave.class);
            registerTile(TileEntityHighPressureSteamTurbineMaster.class);
            MultiblockHandler.registerMultiblock(TileEntityITMultiblockPartHighPressureSteamTurbine.instance);
            multiblockHighPressureSteamTurbine = TileEntityITMultiblockPartHighPressureSteamTurbine.instance;
        }
        if (ITConfig.Multiblocks.enable.enable_electrolyticCrucibleBattery) {
            registerTile(TileEntityElectrolyticCrucibleBatterySlave.class);
            registerTile(TileEntityElectrolyticCrucibleBatteryMaster.class);
            MultiblockHandler.registerMultiblock(TileEntityITMultiblockPartElectrolyticCrucibleBattery.instance);
            multiblockElectrolyticCrucibleBattery = TileEntityITMultiblockPartElectrolyticCrucibleBattery.instance;
        }
        if (ITConfig.Multiblocks.enable.enable_meltingCrucible) {
            registerTile(TileEntityMeltingCrucibleSlave.class);
            registerTile(TileEntityMeltingCrucibleMaster.class);
            MultiblockHandler.registerMultiblock(TileEntityITMultiblockPartMeltingCrucible.instance);
            multiblockMeltingCrucible = TileEntityITMultiblockPartMeltingCrucible.instance;
        }
        if (ITConfig.Multiblocks.enable.enable_radiator) {
            registerTile(TileEntityRadiatorSlave.class);
            registerTile(TileEntityRadiatorMaster.class);
            MultiblockHandler.registerMultiblock(TileEntityITMultiblockPartRadiator.instance);
            multiblockRadiator = TileEntityITMultiblockPartRadiator.instance;
        }
        if (ITConfig.Multiblocks.enable.enable_solarMelter) {
            registerTile(TileEntitySolarMelterSlave.class);
            registerTile(TileEntitySolarMelterMaster.class);
            MultiblockHandler.registerMultiblock(TileEntityITMultiblockPartSolarMelter.instance);
            multiblockSolarMelter = TileEntityITMultiblockPartSolarMelter.instance;
        }

        registerTile(TileEntitySteelSheetmetalTankSlave.class);
        registerTile(TileEntitySteelSheetmetalTankMaster.class);
        MultiblockHandler.registerMultiblock(TileEntityITMultiblockPartSteelSheetmetalTank.instance);
        multiblockSteelSheetmetalTank = TileEntityITMultiblockPartSteelSheetmetalTank.instance;
        if (MCTMixinConfig.mixinSettings.replace_IE_pipes) {
            normallyPressurized.add(FluidRegistry.getFluid("water"));
            normallyPressurized.add(FluidRegistry.getFluid("steam"));
            normallyPressurized.add(FluidRegistry.getFluid("fluegas"));
            normallyPressurized.add(FluidRegistry.getFluid("exhauststeam"));
            normallyPressurized.add(FluidRegistry.getFluid("highpressuresteam"));
            TileEntityFluidPipeAlternative.initCovers();
            ITLogger.info("IT Pipes Override Active");
        }
    }

    @SubscribeEvent public static void registerRecipes(RegistryEvent.Register<IRecipe> event) { ITRecipeLoader.loadRecipes(); }

    @SuppressWarnings("deprecation")
    public static void registerTile(Class<? extends TileEntity> tile) {
        String tileEntity = tile.getSimpleName();
        tileEntity = tileEntity.substring(tileEntity.indexOf("TileEntity") + "TileEntity".length());
        GameRegistry.registerTileEntity(tile, ImmersiveTechnology.MODID + ":" + tileEntity);
    }

    @SuppressWarnings("deprecation")
    public static void registerTileAlias(Class<? extends TileEntity> tile, String alias) { GameRegistry.registerTileEntity(tile, ImmersiveTechnology.MODID + ":" + alias); }

    @SubscribeEvent public static void registerBlocks(RegistryEvent.Register<Block> event) {
        for (Block block : registeredITBlocks) { event.getRegistry().register(block.setRegistryName(createRegistryName(block.getTranslationKey()))); }
    }

    @SubscribeEvent public static void registerItems(RegistryEvent.Register<Item> event) {
        for (Item item : registeredITItems) { event.getRegistry().register(item.setRegistryName(createRegistryName(item.getTranslationKey()))); }
        registerOres();
    }

    private static ResourceLocation createRegistryName(String unlocalized) {
        unlocalized = unlocalized.substring(unlocalized.indexOf("immersive"));
        unlocalized = unlocalized.replaceFirst("\\.", ":");
        return new ResourceLocation(unlocalized);
    }

    public static void registerOres() {
        OreDictionary.registerOre("dustSalt", itemMaterial);
        OreDictionary.registerOre("itemSalt", itemMaterial);
        OreDictionary.registerOre("foodSalt", itemMaterial);
        OreDictionary.registerOre("blockFurnace", new ItemStack(Blocks.FURNACE, 1, OreDictionary.WILDCARD_VALUE));
    }

    public static void registerVariables() {
        Config.manual_int.put("cokeOvenBaseheater_consumption", ITConfig.Multiblocks.advancedCokeOvenBaseheater.advancedCokeOvenBaseheater_energy_consumption);
        Config.manual_int.put("alternator_energyPerTickPerPort", (ITConfig.Multiblocks.alternator.alternator_energy_perTick / 6));
        Config.manual_int.put("alternator_energyStorage", ITConfig.Multiblocks.alternator.alternator_energy_capacitorSize);
        Config.manual_int.put("alternator_energyPerTick", ITConfig.Multiblocks.alternator.alternator_energy_perTick);
        Config.manual_int.put("boilerTank_tankSize", ITConfig.Multiblocks.boilerTank.boilerTank_tankSize);
        Config.manual_int.put("solarTower_minRange", ITConfig.Multiblocks.solarReflector.solarReflector_minRange);
        Config.manual_int.put("solarTower_maxRange", ITConfig.Multiblocks.solarReflector.solarReflector_maxRange);
        Config.manual_int.put("steamTurbine_timeToMax", ((ITConfig.Multiblocks.mechanicalEnergy.mechanicalEnergy_speed_max / ITConfig.Multiblocks.steamTurbine.steamTurbine_speed_gainPerTick) / 20));
        Config.manual_int.put("highPressureSteamTurbine_timeToMax", ((ITConfig.Multiblocks.mechanicalEnergy.mechanicalEnergy_speed_max / ITConfig.Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_speed_gainPerTick) / 20));
        Config.manual_int.put("steelTank_tankSize", ITConfig.Multiblocks.steelTank.steelTank_tankSize);
    }

    @SuppressWarnings("unchecked")
    public static void registerConveyors() {
        if (!MCTMixinConfig.mixinSettings.replace_IE_conveyors) { return; }

        try {
            Field classRegistryField = ConveyorHandler.class.getDeclaredField("classRegistry");
            Field reverseClassRegistryField = ConveyorHandler.class.getDeclaredField("reverseClassRegistry");

            classRegistryField.setAccessible(true);
            reverseClassRegistryField.setAccessible(true);

            Map<ResourceLocation, Class<?>> classRegistry = (Map<ResourceLocation, Class<?>>) classRegistryField.get(null);
            Map<Class<?>, ResourceLocation> reverseClassRegistry = (Map<Class<?>, ResourceLocation>) reverseClassRegistryField.get(null);

            registerBelt(classRegistry, reverseClassRegistry, "conveyor",         ConveyorBasicAlternative.class);
            registerBelt(classRegistry, reverseClassRegistry, "uncontrolled",     ConveyorUncontrolledAlternative.class);
            registerBelt(classRegistry, reverseClassRegistry, "splitter",         ConveyorSplitAlternative.class);
            registerBelt(classRegistry, reverseClassRegistry, "covered",          ConveyorCoveredAlternative.class);
            registerBelt(classRegistry, reverseClassRegistry, "dropper",          ConveyorDropAlternative.class);
            registerBelt(classRegistry, reverseClassRegistry, "droppercovered",   ConveyorDropCoveredAlternative.class);
            registerBelt(classRegistry, reverseClassRegistry, "extract",          ConveyorExtractAlternative.class);
            registerBelt(classRegistry, reverseClassRegistry, "extractcovered",   ConveyorExtractCoveredAlternative.class);
            registerBelt(classRegistry, reverseClassRegistry, "vertical",         ConveyorVerticalAlternative.class);
            registerBelt(classRegistry, reverseClassRegistry, "verticalcovered",  ConveyorVerticalCoveredAlternative.class);

            if (net.minecraftforge.fml.common.FMLCommonHandler.instance().getSide().isClient()) {
                try {
                    blusunrize.immersiveengineering.client.models.ModelConveyor.modelCache.clear();
                    Field itemCacheField = blusunrize.immersiveengineering.client.models.ModelConveyor.class.getDeclaredField("itemModelCache");
                    itemCacheField.setAccessible(true);
                    ((java.util.HashMap<?, ?>) itemCacheField.get(null)).clear();
                }
                catch (Exception e) { ITLogger.error("Failed to clear ModelConveyor caches", e); }
            }
            ITLogger.info("IT Conveyor Override Active");
        }
        catch (Exception e) { ITLogger.error("Failed to register IT conveyor replacements!", e); }
    }

    private static void registerBelt(Map<ResourceLocation, Class<?>> classRegistry, Map<Class<?>, ResourceLocation> reverseClassRegistry, String path, Class<?> beltClass) {
        ResourceLocation rl = new ResourceLocation("immersiveengineering", path);
        classRegistry.put(rl, beltClass);
        reverseClassRegistry.put(beltClass, rl);
    }
}
