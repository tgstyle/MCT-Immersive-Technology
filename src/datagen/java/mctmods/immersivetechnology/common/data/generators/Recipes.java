package mctmods.immersivetechnology.common.data.generators;

import com.immersiveconvergence.api.crafting.ICFluidTagInput;
import com.immersiveconvergence.api.crafting.builders.ICIEConditional;
import com.immersiveconvergence.api.crafting.builders.ICIEContent;
import com.immersiveconvergence.api.crafting.builders.ICMixerRecipeBuilder;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.builder.*;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.builder.AdvancedCokeOvenRecipeBuilder;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.builder.CoolingTowerRecipeBuilder;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.ModBlocks;
import mctmods.immersivetechnology.core.registration.ModFluids;
import mctmods.immersivetechnology.core.registration.ModItems;
import mctmods.immersivetechnology.core.registration.ModTags;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.function.Consumer;

public class Recipes extends RecipeProvider {
    private final HashMap<String, Integer> PATH_COUNT = new HashMap<>();

    public Recipes(PackOutput pOutput) { super(pOutput); }

    @Override protected void buildRecipes(@Nonnull Consumer<FinishedRecipe> consumer) {
        multiblockRecipes();
        itemRecipes(consumer);
        recipesAdvancedCokeOven(consumer);
        recipesBoilerLiquid(consumer);
        recipesBoilerSolid(consumer);
        recipesBoilerTank(consumer);
        recipesCoolingTower(consumer);
        recipesDistiller(consumer);
        recipesElectrolyticCrucibleBattery(consumer);
        recipesHeatExchanger(consumer);
        recipesMelting(consumer);
        recipesMixer(consumer);
        recipesRadiator(consumer);
        recipesSolarTower(consumer);
        recipesTurbine(consumer);
    }

    private void multiblockRecipes() { Reference.IT_LOGGER.info("Starting Multiblock Recipe Registration"); }

    private void itemRecipes(@Nonnull Consumer<FinishedRecipe> consumer) {
        Consumer<FinishedRecipe> ie = ICIEConditional.wrap(consumer);
        Item componentIron = ICIEContent.item(ICIEContent.COMPONENT_IRON);
        Item circuitBoard = ICIEContent.item(ICIEContent.CIRCUIT_BOARD);
        Item connectorRedstone = ICIEContent.item(ICIEContent.CONNECTOR_REDSTONE);
        Item slabSheetmetalIron = ICIEContent.item(ICIEContent.SLAB_SHEETMETAL_IRON);
        Item slabSheetmetalSteel = ICIEContent.item(ICIEContent.SLAB_SHEETMETAL_STEEL);
        Item furnaceHeater = ICIEContent.item(ICIEContent.FURNACE_HEATER);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.Stone.REINFORCED_COKE_BRICK.get().asItem(), 1).define('P', forgeTag("plates/steel")).define('C', ICIEContent.item(ICIEContent.COKEBRICK)).pattern("P").pattern("C").unlockedBy("has_steel_plate", has(forgeTag("plates/steel"))).save(ie, toResourceLocation("reinforced_coke_brick"));
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.Stone.REINFORCED_COKE_BRICK.get().asItem(), 1).define('S', ModBlocks.Stone.SLAB_REINFORCED_COKE_BRICK.get()).pattern("S").pattern("S").unlockedBy("has_slab_reinforced_coke_brick", has(ModBlocks.Stone.SLAB_REINFORCED_COKE_BRICK.get())).save(consumer, toResourceLocation("reinforced_coke_brick_slab_back"));
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.Stone.SLAB_REINFORCED_COKE_BRICK.get().asItem(), 6).define('B', ModBlocks.Stone.REINFORCED_COKE_BRICK.get()).pattern("BBB").unlockedBy("has_reinforced_coke_brick", has(ModBlocks.Stone.REINFORCED_COKE_BRICK.get())).save(consumer, toResourceLocation("reinforced_coke_brick_slab"));
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.Metal.TECHNOLOGY_ENGINEERING.get().asItem(), 4).define('C', forgeTag("sheetmetals/copper")).define('S', Ingredient.of(ICIEContent.item(ICIEContent.COMPONENT_STEEL))).define('E', forgeTag("ingots/electrum")).pattern("CSC").pattern("SES").pattern("CSC").unlockedBy("has_copper_sheetmetal", has(forgeTag("sheetmetals/copper"))).save(ie, toResourceLocation("technology_engineering"));

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.Metal.BARREL_OPEN.get().asItem(), 1).define('S', Ingredient.of(slabSheetmetalIron)).define('B', Ingredient.of(ICIEContent.item(ICIEContent.SHEETMETAL_IRON))).pattern("S S").pattern("B B").pattern("BBB").unlockedBy("has_slab_sheetmetal_iron", has(slabSheetmetalIron)).save(ie, toResourceLocation("barrel_open"));
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.Metal.BARREL_STEEL.get().asItem(), 1).define('S', Ingredient.of(slabSheetmetalSteel)).define('B', Ingredient.of(ICIEContent.item(ICIEContent.SHEETMETAL_STEEL))).pattern("SSS").pattern("B B").pattern("BBB").unlockedBy("has_slab_sheetmetal_steel", has(slabSheetmetalSteel)).save(ie, toResourceLocation("barrel_steel"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.Metal.TRASH_ITEM.get().asItem(), 1).define('P', forgeTag("plates/iron")).define('C', Tags.Items.CHESTS_WOODEN).define('S', forgeTag("sheetmetals/iron")).pattern("PPP").pattern("PCP").pattern(" S ").unlockedBy("has_iron_plate", has(forgeTag("plates/iron"))).save(consumer, toResourceLocation("trash_item"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.Metal.TRASH_FLUID.get().asItem(), 1).define('P', forgeTag("plates/iron")).define('B', ICIEContent.item(ICIEContent.FLUID_PUMP)).define('S', forgeTag("sheetmetals/iron")).pattern("PPP").pattern("PBP").pattern(" S ").unlockedBy("has_iron_plate", has(forgeTag("plates/iron"))).save(ie, toResourceLocation("trash_fluid"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.Metal.TRASH_ENERGY.get().asItem(), 1).define('P', forgeTag("plates/iron")).define('C', ICIEContent.item(ICIEContent.COIL_HV)).define('S', forgeTag("sheetmetals/iron")).pattern("PPP").pattern("PCP").pattern(" S ").unlockedBy("has_iron_plate", has(forgeTag("plates/iron"))).save(ie, toResourceLocation("trash_energy"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.Connector.CONNECTOR_TIMER.get().asItem(), 1).define('G', Tags.Items.GLASS_PANES).define('C', connectorRedstone).define('P', ICIEContent.item(ICIEContent.LOGIC_CIRCUIT)).define('Q', Items.QUARTZ).pattern("GCG").pattern("PPP").pattern("GQG").unlockedBy("has_redstone_connector", has(connectorRedstone)).save(ie, toResourceLocation("connector_timer"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.Metal.VALVE_FLUID.get().asItem(), 2).define('L', forgeTag("plates/iron")).define('R', connectorRedstone).define('P', ICIEContent.item(ICIEContent.FLUID_PIPE)).define('I', componentIron).define('C', circuitBoard).pattern("LRL").pattern("PIP").pattern("LCL").unlockedBy("has_iron_plate", has(forgeTag("plates/iron"))).save(ie, toResourceLocation("valve_fluid"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.Metal.VALVE_LIMITER.get().asItem(), 2).define('L', forgeTag("plates/iron")).define('R', connectorRedstone).define('P', ICIEContent.item(ICIEContent.CONVEYOR_BASIC)).define('I', componentIron).define('C', circuitBoard).pattern("LRL").pattern("PIP").pattern("LCL").unlockedBy("has_iron_plate", has(forgeTag("plates/iron"))).save(ie, toResourceLocation("valve_limiter"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.Metal.VALVE_LOAD.get().asItem(), 2).define('L', forgeTag("plates/iron")).define('R', connectorRedstone).define('P', ICIEContent.item(ICIEContent.CONNECTOR_HV)).define('I', componentIron).define('C', circuitBoard).pattern("LRL").pattern("PIP").pattern("LCL").unlockedBy("has_iron_plate", has(forgeTag("plates/iron"))).save(ie, toResourceLocation("valve_load"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.Metal.ADVANCED_COKE_OVEN_BASEHEATER.get(), 1).define('S', forgeTag("sheetmetals/iron")).define('H', furnaceHeater).define('R', Items.REDSTONE).pattern("SSS").pattern("HRH").pattern("SSS").unlockedBy("has_furnace_heater", has(furnaceHeater)).save(ie, toResourceLocation("advanced_coke_oven_baseheater"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.FORMATION_TOOL.get(), 1).define('I', forgeTag("ingots/iron")).define('E', forgeTag("ingots/electrum")).pattern(" I ").pattern(" EI").pattern("I  ").unlockedBy("has_iron_ingot", has(forgeTag("ingots/iron"))).save(consumer, toResourceLocation("formation_tool"));
    }

    private void recipesAdvancedCokeOven(@Nonnull Consumer<FinishedRecipe> out) {
        AdvancedCokeOvenRecipeBuilder.builder(Items.COAL).addOutput(forgeTag("coal_coke"), 1).setCreosote(500).setTime(600).build(out, toResourceLocation("advanced_coke_oven/coke"));
        AdvancedCokeOvenRecipeBuilder.builder(Items.COAL_BLOCK).addOutput(forgeTag("storage_blocks/coal_coke"), 1).setCreosote(5000).setTime(5400).build(out, toResourceLocation("advanced_coke_oven/coke_block"));
        AdvancedCokeOvenRecipeBuilder.builder(ItemTags.LOGS, 1).addOutput(Items.CHARCOAL).setCreosote(250).setTime(600).build(out, toResourceLocation("advanced_coke_oven/charcoal"));
    }

    private void recipesBoilerLiquid(@Nonnull Consumer<FinishedRecipe> out) {
        BoilerLiquidRecipeBuilder.builder().addInput(forgeFluidTag("biodiesel"), 10).setTime(10).setHeatPerTick(0.1).setTargetHeat(600.0).build(out, toResourceLocation("boiler_liquid/biodiesel"));

        var gasolineBuilder = BoilerLiquidRecipeBuilder.builder().addInput(FluidTags.create(ResourceLocation.fromNamespaceAndPath("forge", "gasoline")), 50).setTime(10).setHeatPerTick(0.1).setTargetHeat(600.0);
        ResourceLocation gasolineId = toResourceLocation("boiler_liquid/gasoline");
        ConditionalRecipe.builder().addCondition(modLoaded("immersivepetroleum")).addRecipe(inner -> gasolineBuilder.build(inner, gasolineId)).build(out, gasolineId);
        var dieselBuilder = BoilerLiquidRecipeBuilder.builder().addInput(FluidTags.create(ResourceLocation.fromNamespaceAndPath("forge", "diesel")), 14).setTime(10).setHeatPerTick(0.1).setTargetHeat(600.0);
        ResourceLocation dieselId = toResourceLocation("boiler_liquid/diesel");
        ConditionalRecipe.builder().addCondition(modLoaded("immersivepetroleum")).addRecipe(inner -> dieselBuilder.build(inner, dieselId)).build(out, dieselId);
        var keroseneBuilder = BoilerLiquidRecipeBuilder.builder().addInput(FluidTags.create(ResourceLocation.fromNamespaceAndPath("forge", "kerosene")), 9).setTime(10).setHeatPerTick(0.1).setTargetHeat(600.0);
        ResourceLocation keroseneId = toResourceLocation("boiler_liquid/kerosene");
        ConditionalRecipe.builder().addCondition(modLoaded("immersivepetroleum")).addRecipe(inner -> keroseneBuilder.build(inner, keroseneId)).build(out, keroseneId);
    }

    private void recipesBoilerSolid(@Nonnull Consumer<FinishedRecipe> out) {
        BoilerSolidRecipeBuilder.builder().addInput(ItemTags.COALS, 1).setHeatPerTick(0.1).setTargetHeat(600.0).build(out, toResourceLocation("boiler_solid/coal"));
        BoilerSolidRecipeBuilder.builder().addInput(forgeTag("coal_coke"), 1).setHeatPerTick(0.1).setTargetHeat(600.0).build(out, toResourceLocation("boiler_solid/coal_coke"));
    }

    private void recipesBoilerTank(@Nonnull Consumer<FinishedRecipe> out) {
        BoilerTankRecipeBuilder.builder(FluidTags.WATER, 250).addOutput(ModFluids.STEAM.getStill(), 450).setTime(10).setRequiredHeat(600.0).build(out, toResourceLocation("boiler_tank/water"));
        BoilerTankRecipeBuilder.builder(ModTags.fluidDistilledWater, 250).addOutput(ModFluids.STEAM.getStill(), 500).setTime(10).setRequiredHeat(600.0).build(out, toResourceLocation("boiler_tank/distilled_water"));
    }

    private void recipesCoolingTower(@Nonnull Consumer<FinishedRecipe> out) {
        CoolingTowerRecipeBuilder.builder().addInput(ModTags.fluidHotWater, 8100).addInput(FluidTags.WATER, 900).addOutput(Fluids.WATER, 2925).addOutput(Fluids.WATER, 2925).addOutput(Fluids.WATER, 2925).setTime(3).build(out, toResourceLocation("cooling_tower/hot_water"));
    }

    private void recipesDistiller(@Nonnull Consumer<FinishedRecipe> out) {
        ItemStack salt = new ItemStack(ModItems.SALT.get(), 1);
        DistillerRecipeBuilder.builder(new ICFluidTagInput(FluidTags.WATER, 1000), new FluidStack(ModFluids.DISTILLED_WATER.getStill(), 500), 20, 10000).addItemOutput(salt, 0.5f).build(out, toResourceLocation("distiller/water"));
    }

    private void recipesElectrolyticCrucibleBattery(@Nonnull Consumer<FinishedRecipe> out) {
        ElectrolyticCrucibleBatteryRecipeBuilder.builder(new ICFluidTagInput(ModTags.fluidSaltSlurry, 1000), new FluidStack(ModFluids.CHLORINE.getStill(), 1000), 614400, 300).addFluidOutput1(new FluidStack(ModFluids.HYDROGEN.getStill(), 3000)).addFluidOutput2(new FluidStack(ModFluids.HEATED_SALT.getStill(), 700)).build(out, toResourceLocation("electrolytic_crucible_battery/hydrogen"));
    }

    private void recipesHeatExchanger(@Nonnull Consumer<FinishedRecipe> out) {
        HeatExchangerRecipeBuilder.builder(new ICFluidTagInput(FluidTags.WATER, 250), new ICFluidTagInput(ModTags.fluidFlueGas, 1000), new FluidStack(ModFluids.STEAM.getStill(), 450), null, 640, 10).build(out, toResourceLocation("heat_exchanger/water_fluegas"));
        HeatExchangerRecipeBuilder.builder(new ICFluidTagInput(ModTags.fluidDistilledWater, 250), new ICFluidTagInput(ModTags.fluidFlueGas, 1000), new FluidStack(ModFluids.STEAM.getStill(), 500), null, 640, 10).build(out, toResourceLocation("heat_exchanger/distwater_fluegas"));
        HeatExchangerRecipeBuilder.builder(new ICFluidTagInput(FluidTags.WATER, 250), new ICFluidTagInput(ModTags.fluidMoltenSalt, 80), new FluidStack(ModFluids.STEAM.getStill(), 450), new FluidStack(ModFluids.HEATED_SALT.getStill(), 80), 640, 10).build(out, toResourceLocation("heat_exchanger/water_moltensalt"));
        HeatExchangerRecipeBuilder.builder(new ICFluidTagInput(ModTags.fluidDistilledWater, 250), new ICFluidTagInput(ModTags.fluidMoltenSalt, 80), new FluidStack(ModFluids.STEAM.getStill(), 500), new FluidStack(ModFluids.HEATED_SALT.getStill(), 80), 640, 10).build(out, toResourceLocation("heat_exchanger/distwater_moltensalt"));
        HeatExchangerRecipeBuilder.builder(new ICFluidTagInput(ModTags.fluidExhaustSteam, 500), new ICFluidTagInput(FluidTags.WATER, 4500), new FluidStack(ModFluids.DISTILLED_WATER.getStill(), 250), new FluidStack(ModFluids.HOT_WATER.getStill(), 4500), 160, 5).build(out, toResourceLocation("heat_exchanger/exhauststeam_water"));
    }

    private void recipesMelting(@Nonnull Consumer<FinishedRecipe> out) {
        MeltingRecipeBuilder.builder().addInput(ModTags.fluidHeatedSaltSlurry, 1000).addOutput(ModFluids.MOLTEN_SALT.getStill(), 500).setTime(20).setRequiredTemp(1000.0).build(out, toResourceLocation("melting/heated_salt"));
        MeltingRecipeBuilder.builder().addInput(ModTags.fluidHeatedGravelSlurry, 1000).addOutput(Fluids.LAVA, 500).setTime(20).setRequiredTemp(1000.0).build(out, toResourceLocation("melting/heated_gravel_slurry"));
    }

    private void recipesMixer(@Nonnull Consumer<FinishedRecipe> out) {
        ICMixerRecipeBuilder.builder().output(ModFluids.SALT_SLURRY.getStill(), 1000).fluidInput(FluidTags.WATER, 1000).input(ModTags.saltForge, 4).energy(3200).build(out, toResourceLocation("mixer/salt_slurry"));
        ICMixerRecipeBuilder.builder().output(ModFluids.GRAVEL_SLURRY.getStill(), 1000).fluidInput(FluidTags.WATER, 1000).input(Tags.Items.GRAVEL, 4).energy(3200).build(out, toResourceLocation("mixer/gravel_slurry"));
    }

    private void recipesRadiator(@Nonnull Consumer<FinishedRecipe> out) {
        RadiatorRecipeBuilder.builder().addInput(ModTags.fluidExhaustSteam, 500).addOutput(ModFluids.DISTILLED_WATER.getStill(), 250).setTime(80).build(out, toResourceLocation("radiator/exhaust_steam"));
        RadiatorRecipeBuilder.builder().addInput(ModTags.fluidHotWater, 1000).addOutput(new FluidStack(Fluids.WATER, 800)).setTime(20).build(out, toResourceLocation("radiator/hot_water"));
    }

    private void recipesSolarTower(@Nonnull Consumer<FinishedRecipe> out) {
        SolarTowerRecipeBuilder.builder().addInput(FluidTags.WATER, 250).addOutput(ModFluids.STEAM.getStill(), 450).setTime(10).setRequiredTemp(600.0).build(out, toResourceLocation("solar_tower/water"));
        SolarTowerRecipeBuilder.builder().addInput(ModTags.fluidDistilledWater, 250).addOutput(ModFluids.STEAM.getStill(), 500).setTime(10).setRequiredTemp(600.0).build(out, toResourceLocation("solar_tower/distilled_water"));
        SolarTowerRecipeBuilder.builder().addInput(ModTags.fluidSaltSlurry, 1000).addOutput(ModFluids.HEATED_SALT.getStill(), 500).setTime(10).setRequiredTemp(400.0).build(out, toResourceLocation("solar_tower/salt_slurry"));
        SolarTowerRecipeBuilder.builder().addInput(ModTags.fluidGravelSlurry, 1000).addOutput(ModFluids.HEATED_GRAVEL.getStill(), 500).setTime(10).setRequiredTemp(400.0).build(out, toResourceLocation("solar_tower/gravel_slurry"));
    }

    private void recipesTurbine(@Nonnull Consumer<FinishedRecipe> out) {
        SteamTurbineRecipeBuilder.builder().addInput(ModTags.fluidSteam, 100).addOutput(ModFluids.EXHAUST_STEAM.getStill(), 100).setTime(1).setTorque(1.0f).build(out, toResourceLocation("steam_turbine/steam"));
        SteamTurbineRecipeBuilder.builder().addInput(ModTags.fluidSteamForge, 100).addOutput(ModFluids.EXHAUST_STEAM.getStill(), 100).setTime(1).setTorque(1.0f).build(out, toResourceLocation("steam_turbine/steam_forge"));

        GasTurbineRecipeBuilder.builder().addInput(forgeFluidTag("biodiesel"), 160).addOutput(ModFluids.FLUE_GAS.getStill(), 1000).setTime(10).setTorque(1.0f).build(out, toResourceLocation("gas_turbine/biodiesel"));
        GasTurbineRecipeBuilder.builder().addInput(ModTags.fluidHydrogen, 100).addOutput(ModFluids.FLUE_GAS.getStill(), 1000).setTime(10).setTorque(1.0f).build(out, toResourceLocation("gas_turbine/hydrogen"));

        var gasolineBuilder = GasTurbineRecipeBuilder.builder().addInput(FluidTags.create(ResourceLocation.fromNamespaceAndPath("forge", "gasoline")), 800).addOutput(ModFluids.FLUE_GAS.getStill(), 1000).setTime(10).setTorque(1.0f);
        ResourceLocation gasolineId = toResourceLocation("gas_turbine/gasoline");
        ConditionalRecipe.builder().addCondition(modLoaded("immersivepetroleum")).addRecipe(inner -> gasolineBuilder.build(inner, gasolineId)).build(out, gasolineId);
        var dieselBuilder = GasTurbineRecipeBuilder.builder().addInput(FluidTags.create(ResourceLocation.fromNamespaceAndPath("forge", "diesel")), 114).addOutput(ModFluids.FLUE_GAS.getStill(), 1000).setTime(10).setTorque(1.0f);
        ResourceLocation dieselId = toResourceLocation("gas_turbine/diesel");
        ConditionalRecipe.builder().addCondition(modLoaded("immersivepetroleum")).addRecipe(inner -> dieselBuilder.build(inner, dieselId)).build(out, dieselId);
        var keroseneBuilder = GasTurbineRecipeBuilder.builder().addInput(FluidTags.create(ResourceLocation.fromNamespaceAndPath("forge", "kerosene")), 150).addOutput(ModFluids.FLUE_GAS.getStill(), 1000).setTime(10).setTorque(1.0f);
        ResourceLocation keroseneId = toResourceLocation("gas_turbine/kerosene");
        ConditionalRecipe.builder().addCondition(modLoaded("immersivepetroleum")).addRecipe(inner -> keroseneBuilder.build(inner, keroseneId)).build(out, keroseneId);
    }

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

    private static TagKey<Item> forgeTag(String path) { return ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", path)); }

    @SuppressWarnings("SameParameterValue")
    private static TagKey<Fluid> forgeFluidTag(String path) { return FluidTags.create(ResourceLocation.fromNamespaceAndPath("forge", path)); }

    @SuppressWarnings("SameParameterValue")
    private static ICondition modLoaded(String modId) { return new ModLoadedCondition(modId); }
}
