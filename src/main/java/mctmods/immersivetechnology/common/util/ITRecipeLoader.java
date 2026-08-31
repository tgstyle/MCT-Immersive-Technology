package mctmods.immersivetechnology.common.util;

import com.immersiveconvergence.api.crafting.MultiblockRecipeLoader;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.crafting.BoilerLiquidRecipe;
import mctmods.immersivetechnology.api.crafting.BoilerSolidRecipe;
import mctmods.immersivetechnology.api.crafting.BoilerTankRecipe;
import mctmods.immersivetechnology.api.crafting.CoolingTowerRecipe;
import mctmods.immersivetechnology.api.crafting.DistillerRecipe;
import mctmods.immersivetechnology.api.crafting.ElectrolyticCrucibleBatteryRecipe;
import mctmods.immersivetechnology.api.crafting.GasTurbineRecipe;
import mctmods.immersivetechnology.api.crafting.HeatExchangerRecipe;
import mctmods.immersivetechnology.api.crafting.HighPressureSteamTurbineRecipe;
import mctmods.immersivetechnology.api.crafting.MeltingCrucibleRecipe;
import mctmods.immersivetechnology.api.crafting.RadiatorRecipe;
import mctmods.immersivetechnology.api.crafting.SolarTowerRecipe;
import mctmods.immersivetechnology.api.crafting.SteamTurbineRecipe;

import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.CraftingHelper;

import static com.immersiveconvergence.api.crafting.MultiblockRecipeLoader.getFluidStack;
import static com.immersiveconvergence.api.crafting.MultiblockRecipeLoader.optionalFluidStack;

public class ITRecipeLoader {
    public static void loadRecipes() {
        MultiblockRecipeLoader.registerType("immersivetech:boiler_tank", (json, context) -> BoilerTankRecipe.addRecipe(getFluidStack(json, "result"), getFluidStack(json, "input"), JsonUtils.getInt(json, "time"), json.get("requiredHeat").getAsDouble()));
        MultiblockRecipeLoader.registerType("immersivetech:boiler_liquid", (json, context) -> BoilerLiquidRecipe.addFuel(getFluidStack(json, "input"), JsonUtils.getInt(json, "time"), json.get("heatPerTick").getAsDouble(), json.get("targetHeat").getAsDouble()));
        MultiblockRecipeLoader.registerType("immersivetech:boiler_solid", (json, context) -> {
            JsonObject input = JsonUtils.getJsonObject(json, "input");
            int count = JsonUtils.getInt(input, "count", 1);
            Object itemInput = input.has("ore") ? new IngredientStack(JsonUtils.getString(input, "ore"), count) : CraftingHelper.getItemStack(input, context);
            BoilerSolidRecipe.addFuel(itemInput, json.get("heatPerTick").getAsDouble(), json.get("targetHeat").getAsDouble());
        });
        MultiblockRecipeLoader.registerType("immersivetech:distiller", (json, context) -> {
            ItemStack itemOutput = ItemStack.EMPTY;
            float chance = 0;
            if (json.has("item_output")) {
                JsonObject item = JsonUtils.getJsonObject(json, "item_output");
                itemOutput = CraftingHelper.getItemStack(item, context);
                chance = item.get("chance").getAsFloat();
            }
            DistillerRecipe.addRecipe(getFluidStack(json, "result"), getFluidStack(json, "input"), itemOutput, JsonUtils.getInt(json, "energy"), JsonUtils.getInt(json, "time"), chance);
        });
        MultiblockRecipeLoader.registerType("immersivetech:solar_tower", (json, context) -> SolarTowerRecipe.addRecipe(getFluidStack(json, "output"), getFluidStack(json, "input"), JsonUtils.getInt(json, "time")));
        MultiblockRecipeLoader.registerType("immersivetech:steam_turbine", (json, context) -> SteamTurbineRecipe.addFuel(getFluidStack(json, "output"), getFluidStack(json, "input"), JsonUtils.getInt(json, "time")));
        MultiblockRecipeLoader.registerType("immersivetech:high_pressure_steam_turbine", (json, context) -> HighPressureSteamTurbineRecipe.addFuel(getFluidStack(json, "output"), getFluidStack(json, "input"), JsonUtils.getInt(json, "time")));
        MultiblockRecipeLoader.registerType("immersivetech:gas_turbine", (json, context) -> GasTurbineRecipe.addFuel(getFluidStack(json, "output"), getFluidStack(json, "input"), JsonUtils.getInt(json, "time")));
        MultiblockRecipeLoader.registerType("immersivetech:cooling_tower", (json, context) -> CoolingTowerRecipe.addRecipe(getFluidStack(json, "output0"), getFluidStack(json, "output1"), getFluidStack(json, "output2"), getFluidStack(json, "input0"), getFluidStack(json, "input1"), JsonUtils.getInt(json, "time")));
        MultiblockRecipeLoader.registerType("immersivetech:heat_exchanger", (json, context) -> HeatExchangerRecipe.addRecipe(getFluidStack(json, "output0"), optionalFluidStack(json, "output1"), getFluidStack(json, "input0"), getFluidStack(json, "input1"), JsonUtils.getInt(json, "energy"), JsonUtils.getInt(json, "time")));
        MultiblockRecipeLoader.registerType("immersivetech:electrolytic_crucible_battery", (json, context) -> {
            ItemStack itemOutput = json.has("item_output") ? CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "item_output"), context) : null;
            ElectrolyticCrucibleBatteryRecipe.addRecipe(getFluidStack(json, "result0"), optionalFluidStack(json, "result1"), optionalFluidStack(json, "result2"), itemOutput, getFluidStack(json, "input"), JsonUtils.getInt(json, "energy"), JsonUtils.getInt(json, "time"));
        });
        MultiblockRecipeLoader.registerType("immersivetech:melting_crucible", (json, context) -> MeltingCrucibleRecipe.addRecipe(getFluidStack(json, "result"), CraftingHelper.getIngredient(json.get("input"), context), JsonUtils.getInt(json, "time")));
        MultiblockRecipeLoader.registerType("immersivetech:radiator", (json, context) -> RadiatorRecipe.addRecipe(getFluidStack(json, "output"), getFluidStack(json, "input"), JsonUtils.getInt(json, "time")));
        MultiblockRecipeLoader.loadRecipes(ImmersiveTechnology.MODID, "recipes_multiblocks");
    }
}
