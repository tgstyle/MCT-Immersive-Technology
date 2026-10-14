package mctmods.immersivetechnology.common.data.generators;

import com.immersiveconvergence.api.ICMods;
import com.immersiveconvergence.api.crafting.ICIngredientWithSize;
import com.immersiveconvergence.api.crafting.builders.ICAlloyRecipeBuilder;
import com.immersiveconvergence.api.crafting.builders.ICArcFurnaceRecipeBuilder;
import com.immersiveconvergence.api.crafting.builders.ICCrusherRecipeBuilder;
import com.immersiveconvergence.api.crafting.builders.ICHammerCrushingRecipeBuilder;
import com.immersiveconvergence.api.crafting.builders.ICIEContent;
import com.immersiveconvergence.api.crafting.builders.ICJsonRecipes;
import com.immersiveconvergence.api.crafting.builders.ICMetalPressRecipeBuilder;
import com.immersiveconvergence.api.crafting.builders.ICMineralMixBuilder;
import com.immersiveconvergence.api.crafting.builders.ICMixerRecipeBuilder;
import com.immersiveconvergence.api.registration.ItemEntry;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.builder.*;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.builder.AdvancedCokeOvenRecipeBuilder;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.builder.CoolingTowerRecipeBuilder;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.ModBlocks;
import mctmods.immersivetechnology.core.registration.ModFluids;
import mctmods.immersivetechnology.core.registration.ModItems;
import mctmods.immersivetechnology.core.registration.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class Recipes extends RecipeProvider {

    private static final TagKey<Fluid> BIODIESEL = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "biodiesel"));
    private final HashMap<String, Integer> PATH_COUNT = new HashMap<>();
    private final ICJsonRecipes ieRecipes = new ICJsonRecipes();

    public Recipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override protected @Nonnull CompletableFuture<?> run(@Nonnull CachedOutput output, @Nonnull HolderLookup.Provider registries) {
        CompletableFuture<?> recipes = super.run(output, registries);
        return CompletableFuture.allOf(recipes, ieRecipes.save(output, recipePathProvider));
    }

    @Override protected void buildRecipes(@Nonnull RecipeOutput output) {
        itemRecipes(output);
        materialRecipes(output);
        nuclearRecipes(output);
        recipesAdvancedCokeOven(output);
        recipesBoilerLiquid(output);
        recipesBoilerSolid(output);
        recipesBoilerTank(output);
        recipesDistiller(output);
        recipesHeatExchanger(output);
        recipesMixer();
        recipesSolarTower(output);
        recipesRadiator(output);
        recipesMelting(output);
        recipesElectrolyticCrucibleBattery(output);
        recipesGasTurbine(output);
        recipesSteamTurbine(output);
        recipesStyleNOX(output);
        recipesCoolingTower(output);
    }

    private void itemRecipes(RecipeOutput output) {
        RecipeOutput ie = withIE(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.Stone.REINFORCED_COKE_BRICK.get().asItem(), 1).define('P', cTag("plates/steel")).define('C', ICIEContent.item(ICIEContent.COKEBRICK)).pattern("P").pattern("C").unlockedBy("has_steel_plate", has(cTag("plates/steel"))).save(ie, toResourceLocation("reinforced_coke_brick"));
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.Stone.REINFORCED_COKE_BRICK.get().asItem(), 1).define('S', ModBlocks.Stone.SLAB_REINFORCED_COKE_BRICK.get()).pattern("S").pattern("S").unlockedBy("has_slab_reinforced_coke_brick", has(ModBlocks.Stone.SLAB_REINFORCED_COKE_BRICK.get())).save(output, toResourceLocation("reinforced_coke_brick_slab_back"));
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.Stone.SLAB_REINFORCED_COKE_BRICK.get().asItem(), 6).define('B', ModBlocks.Stone.REINFORCED_COKE_BRICK.get()).pattern("BBB").unlockedBy("has_reinforced_coke_brick", has(ModBlocks.Stone.REINFORCED_COKE_BRICK.get())).save(output, toResourceLocation("reinforced_coke_brick_slab"));
        Item componentIron = ICIEContent.item(ICIEContent.COMPONENT_IRON);
        Item circuitBoard = ICIEContent.item(ICIEContent.CIRCUIT_BOARD);
        Item connectorRedstone = ICIEContent.item(ICIEContent.CONNECTOR_REDSTONE);
        Item slabSheetmetalIron = ICIEContent.item(ICIEContent.SLAB_SHEETMETAL_IRON);
        Item slabSheetmetalSteel = ICIEContent.item(ICIEContent.SLAB_SHEETMETAL_STEEL);
        Item furnaceHeater = ICIEContent.item(ICIEContent.FURNACE_HEATER);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.Metal.TECHNOLOGY_ENGINEERING.get().asItem(), 4).define('C', cTag("sheetmetals/copper")).define('S', Ingredient.of(ICIEContent.item(ICIEContent.COMPONENT_STEEL))).define('E', cTag("ingots/electrum")).pattern("CSC").pattern("SES").pattern("CSC").unlockedBy("has_copper_sheetmetal", has(cTag("sheetmetals/copper"))).save(ie, toResourceLocation("technology_engineering"));
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.Metal.BARREL_OPEN.get().asItem(), 1).define('S', Ingredient.of(slabSheetmetalIron)).define('B', Ingredient.of(ICIEContent.item(ICIEContent.SHEETMETAL_IRON))).pattern("S S").pattern("B B").pattern("BBB").unlockedBy("has_slab_sheetmetal_iron", has(slabSheetmetalIron)).save(ie, toResourceLocation("barrel_open"));
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.Metal.BARREL_STEEL.get().asItem(), 1).define('S', Ingredient.of(slabSheetmetalSteel)).define('B', Ingredient.of(ICIEContent.item(ICIEContent.SHEETMETAL_STEEL))).pattern("SSS").pattern("B B").pattern("BBB").unlockedBy("has_slab_sheetmetal_steel", has(slabSheetmetalSteel)).save(ie, toResourceLocation("barrel_steel"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.Metal.TRASH_ITEM.get().asItem(), 1).define('P', cTag("plates/iron")).define('C', Tags.Items.CHESTS).define('S', cTag("sheetmetals/iron")).pattern("PPP").pattern("PCP").pattern(" S ").unlockedBy("has_iron_plate", has(cTag("plates/iron"))).save(output, toResourceLocation("trash_item"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.Metal.TRASH_FLUID.get().asItem(), 1).define('P', cTag("plates/iron")).define('B', ICIEContent.item(ICIEContent.FLUID_PUMP)).define('S', cTag("sheetmetals/iron")).pattern("PPP").pattern("PBP").pattern(" S ").unlockedBy("has_iron_plate", has(cTag("plates/iron"))).save(ie, toResourceLocation("trash_fluid"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.Metal.TRASH_ENERGY.get().asItem(), 1).define('P', cTag("plates/iron")).define('C', ICIEContent.item(ICIEContent.COIL_HV)).define('S', cTag("sheetmetals/iron")).pattern("PPP").pattern("PCP").pattern(" S ").unlockedBy("has_iron_plate", has(cTag("plates/iron"))).save(ie, toResourceLocation("trash_energy"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.Metal.VALVE_FLUID.get().asItem(), 2).define('L', cTag("plates/iron")).define('R', connectorRedstone).define('P', ICIEContent.item(ICIEContent.FLUID_PIPE)).define('I', componentIron).define('C', circuitBoard).pattern("LRL").pattern("PIP").pattern("LCL").unlockedBy("has_iron_plate", has(cTag("plates/iron"))).save(ie, toResourceLocation("valve_fluid"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.Metal.VALVE_LIMITER.get().asItem(), 2).define('L', cTag("plates/iron")).define('R', connectorRedstone).define('P', ICIEContent.item(ICIEContent.CONVEYOR_BASIC)).define('I', componentIron).define('C', circuitBoard).pattern("LRL").pattern("PIP").pattern("LCL").unlockedBy("has_iron_plate", has(cTag("plates/iron"))).save(ie, toResourceLocation("valve_limiter"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.Metal.VALVE_LOAD.get().asItem(), 2).define('L', cTag("plates/iron")).define('R', connectorRedstone).define('P', ICIEContent.item(ICIEContent.CONNECTOR_HV)).define('I', componentIron).define('C', circuitBoard).pattern("LRL").pattern("PIP").pattern("LCL").unlockedBy("has_iron_plate", has(cTag("plates/iron"))).save(ie, toResourceLocation("valve_load"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.Metal.ADVANCED_COKE_OVEN_BASEHEATER.get().asItem(), 1).define('S', cTag("sheetmetals/iron")).define('H', furnaceHeater).define('R', Items.REDSTONE).pattern("SSS").pattern("HRH").pattern("SSS").unlockedBy("has_furnace_heater", has(furnaceHeater)).save(ie, toResourceLocation("advanced_coke_oven_baseheater"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.FORMATION_TOOL.get(), 1).define('I', cTag("ingots/iron")).define('E', cTag("ingots/electrum")).pattern(" I ").pattern(" EI").pattern("I  ").unlockedBy("has_iron_ingot", has(cTag("ingots/iron"))).save(output, toResourceLocation("formation_tool"));
    }

    private void recipesAdvancedCokeOven(RecipeOutput output) {
        AdvancedCokeOvenRecipeBuilder.builder(Items.COAL).addOutput(cTag("coal_coke"), 1).setCreosote(500).setTime(600).save(output, toResourceLocation("advanced_coke_oven/coke"));
        AdvancedCokeOvenRecipeBuilder.builder(Items.COAL_BLOCK).addOutput(cTag("storage_blocks/coal_coke"), 1).setCreosote(5000).setTime(5400).save(output, toResourceLocation("advanced_coke_oven/coke_block"));
        AdvancedCokeOvenRecipeBuilder.builder(ItemTags.LOGS, 1).addOutput(Items.CHARCOAL).setCreosote(250).setTime(600).save(output, toResourceLocation("advanced_coke_oven/charcoal"));
    }

    private void recipesBoilerLiquid(RecipeOutput output) {
        BoilerLiquidRecipeBuilder.builder().addInput(BIODIESEL, 10).setTime(10).setHeatPerTick(0.1).setTargetHeat(600.0).save(output, toResourceLocation("boiler_liquid/biodiesel"));
        BoilerLiquidRecipeBuilder.builder().addInput(FluidTags.create(ResourceLocation.fromNamespaceAndPath("forge", "gasoline")), 50).setTime(10).setHeatPerTick(0.1).setTargetHeat(600.0).save(output.withConditions(new ModLoadedCondition("immersivepetroleum")), toResourceLocation("boiler_liquid/gasoline"));
        BoilerLiquidRecipeBuilder.builder().addInput(FluidTags.create(ResourceLocation.fromNamespaceAndPath("forge", "diesel")), 14).setTime(10).setHeatPerTick(0.1).setTargetHeat(600.0).save(output.withConditions(new ModLoadedCondition("immersivepetroleum")), toResourceLocation("boiler_liquid/diesel"));
        BoilerLiquidRecipeBuilder.builder().addInput(FluidTags.create(ResourceLocation.fromNamespaceAndPath("forge", "kerosene")), 9).setTime(10).setHeatPerTick(0.1).setTargetHeat(600.0).save(output.withConditions(new ModLoadedCondition("immersivepetroleum")), toResourceLocation("boiler_liquid/kerosene"));
    }

    private void recipesBoilerSolid(RecipeOutput output) {
        BoilerSolidRecipeBuilder.builder().addInput(ItemTags.COALS, 1).setHeatPerTick(0.1).setTargetHeat(600.0).save(output, toResourceLocation("boiler_solid/coal"));
        BoilerSolidRecipeBuilder.builder().addInput(cTag("coal_coke"), 1).setHeatPerTick(0.1).setTargetHeat(600.0).save(output, toResourceLocation("boiler_solid/coal_coke"));
    }

    private void recipesBoilerTank(RecipeOutput output) {
        BoilerTankRecipeBuilder.builder().addInput(FluidTags.WATER, 250).addOutput(ModFluids.STEAM.getStill(), 450).setTime(10).setRequiredHeat(600.0).save(output, toResourceLocation("boiler_tank/water"));
        BoilerTankRecipeBuilder.builder().addInput(ModTags.fluidDistilledWater, 250).addOutput(ModFluids.STEAM.getStill(), 500).setTime(10).setRequiredHeat(600.0).save(output, toResourceLocation("boiler_tank/distilled_water"));
    }

    private void recipesDistiller(RecipeOutput output) {
        ItemStack salt = new ItemStack(ModItems.SALT.get(), 1);
        DistillerRecipeBuilder.builder().addInput(FluidTags.WATER, 1000).addOutput(new FluidStack(ModFluids.DISTILLED_WATER.getStill(), 500)).setTime(20).setEnergy(10000).addItemOutput(salt, 0.5f).save(output, toResourceLocation("distiller/water"));
        DistillerRecipeBuilder.builder().addInput(ModTags.fluidCrudeNitrate, 1000).addOutput(new FluidStack(ModFluids.NITRIC_ACID.getStill(), 500)).setTime(40).setEnergy(20000).addItemOutput(salt, 0.5f).save(output, toResourceLocation("distiller/crude_nitrate"));
    }

    private void recipesHeatExchanger(RecipeOutput output) {
        HeatExchangerRecipeBuilder.builder().addInput0(FluidTags.WATER, 250).addInput1(ModTags.fluidFlueGas, 1000).addOutput0(new FluidStack(ModFluids.STEAM.getStill(), 450)).setEnergy(640).setTime(10).save(output, toResourceLocation("heat_exchanger/water_fluegas"));
        HeatExchangerRecipeBuilder.builder().addInput0(ModTags.fluidDistilledWater, 250).addInput1(ModTags.fluidFlueGas, 1000).addOutput0(new FluidStack(ModFluids.STEAM.getStill(), 500)).setEnergy(640).setTime(10).save(output, toResourceLocation("heat_exchanger/distwater_fluegas"));
        HeatExchangerRecipeBuilder.builder().addInput0(FluidTags.WATER, 250).addInput1(ModTags.fluidMoltenSalt, 80).addOutput0(new FluidStack(ModFluids.STEAM.getStill(), 450)).addOutput1(new FluidStack(ModFluids.HEATED_SALT.getStill(), 80)).setEnergy(640).setTime(10).save(output, toResourceLocation("heat_exchanger/water_moltensalt"));
        HeatExchangerRecipeBuilder.builder().addInput0(ModTags.fluidDistilledWater, 250).addInput1(ModTags.fluidMoltenSalt, 80).addOutput0(new FluidStack(ModFluids.STEAM.getStill(), 500)).addOutput1(new FluidStack(ModFluids.HEATED_SALT.getStill(), 80)).setEnergy(640).setTime(10).save(output, toResourceLocation("heat_exchanger/distwater_moltensalt"));
        HeatExchangerRecipeBuilder.builder().addInput0(ModTags.fluidExhaustSteam, 500).addInput1(FluidTags.WATER, 4500).addOutput0(new FluidStack(ModFluids.DISTILLED_WATER.getStill(), 250)).addOutput1(new FluidStack(ModFluids.HOT_WATER.getStill(), 4500)).setEnergy(160).setTime(5).save(output, toResourceLocation("heat_exchanger/exhauststeam_water"));
    }

    private void recipesMixer() {
        ICMixerRecipeBuilder.builder().output(ModFluids.SALT_SLURRY.getStill(), 1000).fluidInput(FluidTags.WATER, 1000).input(ModTags.saltForge, 4).setEnergy(3200).build(ieRecipes, toResourceLocation("mixer/salt_slurry"));
        ICMixerRecipeBuilder.builder().output(ModFluids.GRAVEL_SLURRY.getStill(), 1000).fluidInput(FluidTags.WATER, 1000).input(Tags.Items.GRAVELS, 4).setEnergy(3200).build(ieRecipes, toResourceLocation("mixer/gravel_slurry"));
        ICMixerRecipeBuilder.builder().output(ModFluids.SULFURIC_ACID.getStill(), 1000).fluidInput(FluidTags.WATER, 1000).input(cTag("dusts/sulfur"), 4).setEnergy(3200).build(ieRecipes, toResourceLocation("mixer/sulfuric_acid"));
        ICMixerRecipeBuilder.builder().output(ModFluids.CRUDE_NITRATE.getStill(), 1000).fluidInput(ModTags.fluidSulfuricAcid, 1000).input(cTag("dusts/saltpeter"), 4).setEnergy(3200).build(ieRecipes, toResourceLocation("mixer/crude_nitrate"));
    }

    private void recipesSolarTower(RecipeOutput output) {
        SolarTowerRecipeBuilder.builder().addInput(FluidTags.WATER, 250).addOutput(new FluidStack(ModFluids.STEAM.getStill(), 450)).setTime(10).setRequiredTemp(600.0).save(output, toResourceLocation("solar_tower/water"));
        SolarTowerRecipeBuilder.builder().addInput(ModTags.fluidDistilledWater, 250).addOutput(new FluidStack(ModFluids.STEAM.getStill(), 500)).setTime(10).setRequiredTemp(600.0).save(output, toResourceLocation("solar_tower/distilled_water"));
        SolarTowerRecipeBuilder.builder().addInput(ModTags.fluidSaltSlurry, 1000).addOutput(new FluidStack(ModFluids.HEATED_SALT.getStill(), 500)).setTime(10).setRequiredTemp(400.0).save(output, toResourceLocation("solar_tower/salt_slurry"));
        SolarTowerRecipeBuilder.builder().addInput(ModTags.fluidGravelSlurry, 1000).addOutput(new FluidStack(ModFluids.HEATED_GRAVEL.getStill(), 500)).setTime(10).setRequiredTemp(400.0).save(output, toResourceLocation("solar_tower/gravel_slurry"));
    }

    private void recipesRadiator(RecipeOutput output) {
        RadiatorRecipeBuilder.builder().addInput(ModTags.fluidExhaustSteam, 500).addOutput(new FluidStack(ModFluids.DISTILLED_WATER.getStill(), 250)).setTime(80).save(output, toResourceLocation("radiator/exhaust_steam"));
        RadiatorRecipeBuilder.builder().addInput(ModTags.fluidHotWater, 1000).addOutput(new FluidStack(Fluids.WATER, 800)).setTime(20).save(output, toResourceLocation("radiator/hot_water"));
    }

    private void recipesMelting(RecipeOutput output) {
        MeltingRecipeBuilder.builder().addInput(ModTags.fluidHeatedSaltSlurry, 1000).addOutput(new FluidStack(ModFluids.MOLTEN_SALT.getStill(), 500)).setTime(20).setRequiredTemp(1000.0).save(output, toResourceLocation("melting/heated_salt"));
        MeltingRecipeBuilder.builder().addInput(ModTags.fluidHeatedGravelSlurry, 1000).addOutput(new FluidStack(Fluids.LAVA, 500)).setTime(20).setRequiredTemp(1000.0).save(output, toResourceLocation("melting/heated_gravel_slurry"));
    }

    private void recipesElectrolyticCrucibleBattery(RecipeOutput output) {
        ElectrolyticCrucibleBatteryRecipeBuilder.builder().addInput(ModTags.fluidSaltSlurry, 1000).addOutput0(new FluidStack(ModFluids.CHLORINE.getStill(), 1000)).addOutput1(new FluidStack(ModFluids.HYDROGEN.getStill(), 3000)).addOutput2(new FluidStack(ModFluids.HEATED_SALT.getStill(), 700)).addItemOutput(ItemStack.EMPTY).setEnergy(614400).setTime(300).save(output, toResourceLocation("electrolytic_crucible_battery/hydrogen"));
    }

    private void recipesGasTurbine(RecipeOutput output) {
        GasTurbineRecipeBuilder.builder().addInput(BIODIESEL, 160).addOutput(ModFluids.FLUE_GAS.getStill(), 1000).setTime(10).setTorque(1.0f).save(output, toResourceLocation("gas_turbine/biodiesel"));
        GasTurbineRecipeBuilder.builder().addInput(ModTags.fluidHydrogen, 100).addOutput(ModFluids.FLUE_GAS.getStill(), 1000).setTime(10).setTorque(1.0f).save(output, toResourceLocation("gas_turbine/hydrogen"));
        GasTurbineRecipeBuilder.builder().addInput(FluidTags.create(ResourceLocation.fromNamespaceAndPath("forge", "gasoline")), 800).addOutput(ModFluids.FLUE_GAS.getStill(), 1000).setTime(10).setTorque(1.0f).save(output.withConditions(new ModLoadedCondition("immersivepetroleum")), toResourceLocation("gas_turbine/gasoline"));
        GasTurbineRecipeBuilder.builder().addInput(FluidTags.create(ResourceLocation.fromNamespaceAndPath("forge", "diesel")), 114).addOutput(ModFluids.FLUE_GAS.getStill(), 1000).setTime(10).setTorque(1.0f).save(output.withConditions(new ModLoadedCondition("immersivepetroleum")), toResourceLocation("gas_turbine/diesel"));
        GasTurbineRecipeBuilder.builder().addInput(FluidTags.create(ResourceLocation.fromNamespaceAndPath("forge", "kerosene")), 150).addOutput(ModFluids.FLUE_GAS.getStill(), 1000).setTime(10).setTorque(1.0f).save(output.withConditions(new ModLoadedCondition("immersivepetroleum")), toResourceLocation("gas_turbine/kerosene"));
    }

    private void recipesSteamTurbine(RecipeOutput output) {
        SteamTurbineRecipeBuilder.builder().addInput(ModTags.fluidSteam, 100).addOutput(ModFluids.EXHAUST_STEAM.getStill(), 100).setTime(1).setTorque(1.0f).save(output, toResourceLocation("steam_turbine/steam"));
    }

    private void recipesStyleNOX(RecipeOutput output) {
        StyleNOXRecipeBuilder.builder()
                .addInput(new ICIngredientWithSize(Ingredient.of(ModItems.FUEL_ELEMENT.get()), 8))
                .addOutput(new ItemStack(ModItems.SPENT_ELEMENT.get(), 8))
                .addWater(ModTags.fluidDistilledWater, 200)
                .addCO2(ModTags.fluidCarbonDioxide, 10)
                .addSteamOutput(new FluidStack(ModFluids.STEAM.getStill(), 400))
                .setTime(72000)
                .setEnergy(800000)
                .save(output, toResourceLocation("stylenox/uranium"));
    }

    private void recipesCoolingTower(RecipeOutput output) {
        CoolingTowerRecipeBuilder.builder().addInput(ModTags.fluidHotWater, 8100).addInput(FluidTags.WATER, 900).addOutput(Fluids.WATER, 2925).addOutput(Fluids.WATER, 2925).addOutput(Fluids.WATER, 2925).setTime(3).save(output, toResourceLocation("cooling_tower/hot_water"));
    }

    private void materialRecipes(RecipeOutput output) {
        oreMetal(output, "chromium", ModItems.INGOT_CHROMIUM, ModItems.DUST_CHROMIUM, ModItems.RAW_CHROMIUM, ModBlocks.Metal.STORAGE_CHROMIUM, ModBlocks.Metal.ORE_CHROMIUM, ModBlocks.Metal.DEEPSLATE_ORE_CHROMIUM);
        oreMetal(output, "magnesium", ModItems.INGOT_MAGNESIUM, ModItems.DUST_MAGNESIUM, ModItems.RAW_MAGNESIUM, ModBlocks.Metal.STORAGE_MAGNESIUM, ModBlocks.Metal.ORE_MAGNESIUM, ModBlocks.Metal.DEEPSLATE_ORE_MAGNESIUM);
        alloyMetal(output, "stainless_steel", ModItems.INGOT_STAINLESS_STEEL, ModItems.DUST_STAINLESS_STEEL);
        alloyMetal(output, "magnox", ModItems.INGOT_MAGNOX, ModItems.DUST_MAGNOX);
        ICMineralMixBuilder.builder().dimensionOverworld().addOverworldSpoils().ore(Blocks.CALCITE, 0.6f).ore(Blocks.DRIPSTONE_BLOCK, 0.2f).ore(cTag("ores/magnesium"), 0.1f).ore(cTag("ores/chromium"), 0.1f).weight(5).failchance(0.2f).build(ieRecipes, toResourceLocation("mineral/dolomite"));

        ICArcFurnaceRecipeBuilder.builder().input(cTag("ingots/steel"), 6).additive(cTag("ingots/chromium"), 2).additive(cTag("ingots/nickel")).output(cTag("ingots/stainless_steel"), 9).setTime(100).setEnergy(51200).build(ieRecipes, toResourceLocation("arcfurnace/alloy_stainless_steel"));
        ICAlloyRecipeBuilder.builder().input(cTag("ingots/magnesium"), 3).input(cTag("ingots/aluminum"), 1).output(cTag("ingots/magnox"), 4).build(ieRecipes, toResourceLocation("alloysmelter/magnox"));
        ICArcFurnaceRecipeBuilder.builder().input(cTag("ingots/magnesium"), 3).additive(cTag("ingots/aluminum")).output(cTag("ingots/magnox"), 4).setTime(100).setEnergy(51200).build(ieRecipes, toResourceLocation("arcfurnace/alloy_magnox"));

        add3x3Conversion(ModItems.INGOT_STAINLESS_STEEL.get(), ModItems.NUGGET_STAINLESS_STEEL.get(), cTag("nuggets/stainless_steel"), output);
        plateAndRod(output, "stainless_steel", ModItems.INGOT_STAINLESS_STEEL, ModItems.PLATE_STAINLESS_STEEL, true);
        plateAndRod(output, "magnox", ModItems.INGOT_MAGNOX, ModItems.PLATE_MAGNOX, false);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.Metal.SHEETMETAL_STAINLESS_STEEL.get().asItem(), 4).define('p', cTag("plates/stainless_steel")).pattern(" p ").pattern("p p").pattern(" p ").unlockedBy("has_plate_stainless_steel", has(ModItems.PLATE_STAINLESS_STEEL.get())).save(output, toResourceLocation("sheetmetal_stainless_steel"));
    }

    private void nuclearRecipes(RecipeOutput output) {
        add3x3Conversion(ModBlocks.Metal.GRAPHITE_MODERATOR.get(), ICIEContent.item(ICIEContent.INGOT_HOP_GRAPHITE), cTag("ingots/hop_graphite"), withIE(output));
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.Metal.REACTOR_CLADDING.get().asItem(), 1).define('p', cTag("plates/stainless_steel")).pattern("ppp").pattern("ppp").pattern("ppp").unlockedBy("has_plate_stainless_steel", has(ModItems.PLATE_STAINLESS_STEEL.get())).save(output, toResourceLocation("reactor_cladding"));
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.Metal.NUCLEAR_ENGINEERING.get().asItem(), 4).define('T', ModBlocks.Metal.TECHNOLOGY_ENGINEERING.get()).define('U', cTag("ingots/uranium")).define('P', cTag("plates/stainless_steel")).pattern("PTP").pattern("UTU").pattern("PTP").unlockedBy("has_technology_engineering", has(ModBlocks.Metal.TECHNOLOGY_ENGINEERING.get())).save(output, toResourceLocation("nuclear_engineering"));

        ICMetalPressRecipeBuilder.builder().input(cTag("ingots/uranium")).mold(ICIEContent.MOLD_ROD).output(ModItems.PELLET_URANIUM.get(), 2).setEnergy(2400).build(ieRecipes, toResourceLocation("metalpress/pellet_uranium"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FUEL_ELEMENT.get(), 1).define('x', ModItems.PELLET_URANIUM.get()).define('y', cTag("plates/magnox")).pattern("xyx").pattern("x x").pattern("xyx").unlockedBy("has_pellet_uranium", has(ModItems.PELLET_URANIUM.get())).save(output, toResourceLocation("fuel_element"));
        ICCrusherRecipeBuilder.builder().input(ModItems.SPENT_ELEMENT.get()).output(cTag("dusts/spent_fuel"), 3).setEnergy(6000).build(ieRecipes, toResourceLocation("crusher/spent_element"));
    }

    private void oreMetal(RecipeOutput output, String name, ItemEntry<?> ingot, ItemEntry<?> dust, ItemEntry<?> raw, ModBlocks.BlockEntry<?> storage, ModBlocks.BlockEntry<?> ore, ModBlocks.BlockEntry<?> deepslateOre) {
        TagKey<Item> ingotTag = cTag("ingots/" + name);
        TagKey<Item> dustTag = cTag("dusts/" + name);
        TagKey<Item> rawTag = cTag("raw_materials/" + name);
        TagKey<Item> oreTag = cTag("ores/" + name);
        add3x3Conversion(storage.get(), ingot.get(), ingotTag, output);
        addSmeltingBlasting(ore.get(), ingot.get(), 0.7f, output, "ingot_" + name + "_from_ore");
        addSmeltingBlasting(deepslateOre.get(), ingot.get(), 0.7f, output, "ingot_" + name + "_from_deepslate_ore");
        addSmeltingBlasting(raw.get(), ingot.get(), 0.7f, output, "ingot_" + name + "_from_raw");
        addSmeltingBlasting(dust.get(), ingot.get(), 0f, output, "ingot_" + name + "_from_dust");
        ICHammerCrushingRecipeBuilder.builder(oreTag, dustTag).build(ieRecipes, toResourceLocation("crafting/hammercrushing_" + name));
        ICHammerCrushingRecipeBuilder.builder(rawTag, dustTag).build(ieRecipes, toResourceLocation("crafting/raw_hammercrushing_" + name));
        ICCrusherRecipeBuilder.builder().input(oreTag).output(dustTag, 2).setEnergy(6000).build(ieRecipes, toResourceLocation("crusher/ore_" + name));
        ICCrusherRecipeBuilder.builder().input(rawTag).output(dustTag, 1).addSecondary(dustTag, 1/3f).setEnergy(6000).build(ieRecipes, toResourceLocation("crusher/raw_ore_" + name));
        ICArcFurnaceRecipeBuilder.builder().input(oreTag).output(ingotTag, 2).slag(cTag("slag"), 1).setTime(200).setEnergy(102400).build(ieRecipes, toResourceLocation("arcfurnace/ore_" + name));
        ICArcFurnaceRecipeBuilder.builder().input(rawTag).output(ingotTag, 1).secondary(ingotTag, 0.5f).setTime(100).setEnergy(25600).build(ieRecipes, toResourceLocation("arcfurnace/raw_ore_" + name));
        crushAndSmeltIngot(name);
    }

    private void alloyMetal(RecipeOutput output, String name, ItemEntry<?> ingot, ItemEntry<?> dust) {
        addSmeltingBlasting(dust.get(), ingot.get(), 0f, output, "ingot_" + name + "_from_dust");
        crushAndSmeltIngot(name);
    }

    private void crushAndSmeltIngot(String name) {
        ICCrusherRecipeBuilder.builder().input(cTag("ingots/" + name)).output(cTag("dusts/" + name), 1).setEnergy(3000).build(ieRecipes, toResourceLocation("crusher/ingot_" + name));
        ICArcFurnaceRecipeBuilder.builder().input(cTag("dusts/" + name)).output(cTag("ingots/" + name), 1).setTime(100).setEnergy(51200).build(ieRecipes, toResourceLocation("arcfurnace/dust_" + name));
    }

    private void plateAndRod(RecipeOutput output, String name, ItemEntry<?> ingot, ItemEntry<?> plate, boolean hasRod) {
        TagKey<Item> ingotTag = cTag("ingots/" + name);
        ICMetalPressRecipeBuilder.builder().input(ingotTag).mold(ICIEContent.MOLD_PLATE).output(cTag("plates/" + name), 1).setEnergy(2400).build(ieRecipes, toResourceLocation("metalpress/plate_" + name));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, plate.get()).requires(ingotTag).requires(ICIEContent.HAMMERS).unlockedBy("has_ingot_" + name, has(ingot.get())).save(withIE(output), toResourceLocation("plate_" + name + "_hammering"));
        if (hasRod) { ICMetalPressRecipeBuilder.builder().input(ingotTag).mold(ICIEContent.MOLD_ROD).output(cTag("rods/" + name), 2).setEnergy(2400).build(ieRecipes, toResourceLocation("metalpress/rod_" + name)); }
    }

    private void add3x3Conversion(ItemLike bigItem, ItemLike smallItem, TagKey<Item> smallTag, RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, bigItem).define('s', smallTag).define('i', smallItem).pattern("sss").pattern("sis").pattern("sss").unlockedBy("has_" + toPath(smallItem), has(smallItem)).save(output, toResourceLocation(toPath(smallItem) + "_to_" + toPath(bigItem)));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, smallItem, 9).requires(bigItem).unlockedBy("has_" + toPath(bigItem), has(bigItem)).save(output, toResourceLocation(toPath(bigItem) + "_to_" + toPath(smallItem)));
    }

    private void addSmeltingBlasting(ItemLike input, ItemLike result, float xp, RecipeOutput output, String path) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.MISC, result, xp, 200).unlockedBy("has_" + toPath(input), has(input)).save(output, toResourceLocation("smelting/" + path));
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(input), RecipeCategory.MISC, result, xp, 100).unlockedBy("has_" + toPath(input), has(input)).save(output, toResourceLocation("smelting/" + path + "_from_blasting"));
    }

    private static TagKey<Item> cTag(String path) { return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", path)); }


    private static RecipeOutput withIE(RecipeOutput output) { return output.withConditions(new ModLoadedCondition(ICMods.IMMERSIVE_ENGINEERING)); }

    private static String toPath(ItemLike item) { return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath(); }

    private ResourceLocation toResourceLocation(String resourceLocation) {
        if (!resourceLocation.contains("/")) { resourceLocation = "crafting/" + resourceLocation; }
        if (PATH_COUNT.containsKey(resourceLocation)) {
            int count = PATH_COUNT.get(resourceLocation) + 1;
            PATH_COUNT.put(resourceLocation, count);
            return Reference.rl(resourceLocation + count);
        }
        PATH_COUNT.put(resourceLocation, 1);
        return Reference.rl(resourceLocation);
    }
}
