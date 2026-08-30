package mctmods.immersivetechnology.common.util;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.crafting.BoilerRecipe;
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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.io.FilenameUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public class ITRecipeLoader {
    private static final JsonContext CONTEXT = new JsonContext(ImmersiveTechnology.MODID);
    private static final JsonParser PARSER = new JsonParser();

    static {
        CraftingHelper.register(new ResourceLocation(ImmersiveTechnology.MODID, "fluid_exists"), (IConditionFactory) (context, json) -> {
            String fluid = JsonUtils.getString(json, "fluid");
            return () -> FluidRegistry.isFluidRegistered(fluid);
        });
    }

    public static void loadRecipes() {
        Map<String, JsonObject> files = new TreeMap<>();
        ModContainer mod = Loader.instance().getIndexedModList().get(ImmersiveTechnology.MODID);
        CraftingHelper.findFiles(mod, "assets/" + ImmersiveTechnology.MODID + "/recipes_multiblocks", root -> true, (root, file) -> {
            readFile(files, root, file);
            return true;
        }, true, true);
        Path overrides = Loader.instance().getConfigDir().toPath().resolve(ImmersiveTechnology.MODID).resolve("recipes_multiblocks");
        try {
            Files.createDirectories(overrides);
            try (Stream<Path> stream = Files.walk(overrides)) { stream.filter(Files::isRegularFile).forEach(file -> readFile(files, overrides, file)); }
        }
        catch (IOException e) { ITLogger.error("Failed to read recipe overrides - " + e.getMessage()); }
        int loaded = 0;
        for (Map.Entry<String, JsonObject> entry : files.entrySet()) {
            if (parse(entry.getKey(), entry.getValue())) { loaded++; }
        }
        ITLogger.info("Loaded " + loaded + " multiblock recipes from " + files.size() + " files");
    }

    private static void readFile(Map<String, JsonObject> files, Path root, Path file) {
        String relative = root.relativize(file).toString();
        if (!"json".equals(FilenameUtils.getExtension(file.toString())) || relative.startsWith("_")) { return; }
        String name = FilenameUtils.removeExtension(relative).replaceAll("\\\\", "/");
        try (BufferedReader reader = Files.newBufferedReader(file)) { files.put(name, PARSER.parse(reader).getAsJsonObject()); }
        catch (JsonParseException | IllegalStateException | IOException e) { ITLogger.error("Failed to read recipe " + name + " - " + e.getMessage()); }
    }

    private static boolean parse(String name, JsonObject json) {
        try {
            if (json.has("conditions") && !CraftingHelper.processConditions(JsonUtils.getJsonArray(json, "conditions"), CONTEXT)) { return false; }
            if (JsonUtils.getString(json, "type").equals(ImmersiveTechnology.MODID + ":conditional")) {
                for (JsonElement element : JsonUtils.getJsonArray(json, "recipes")) {
                    JsonObject entry = element.getAsJsonObject();
                    if (!entry.has("conditions") || CraftingHelper.processConditions(JsonUtils.getJsonArray(entry, "conditions"), CONTEXT)) { return register(name, JsonUtils.getJsonObject(entry, "recipe")); }
                }
                return false;
            }
            return register(name, json);
        }
        catch (JsonParseException | IllegalStateException | IllegalArgumentException e) {
            ITLogger.error("Failed to load recipe " + name + " - " + e.getMessage());
            return false;
        }
    }

    private static boolean register(String name, JsonObject json) {
        String type = JsonUtils.getString(json, "type");
        switch (type) {
            case "immersivetech:boiler":
                BoilerRecipe.addRecipe(getFluidStack(json, "result"), getFluidStack(json, "input"), JsonUtils.getInt(json, "time"));
                break;
            case "immersivetech:boiler_fuel":
                BoilerRecipe.addFuel(getFluidStack(json, "input"), JsonUtils.getInt(json, "time"), json.get("heat").getAsDouble());
                break;
            case "immersivetech:distiller": {
                ItemStack itemOutput = ItemStack.EMPTY;
                float chance = 0;
                if (json.has("item_output")) {
                    JsonObject item = JsonUtils.getJsonObject(json, "item_output");
                    itemOutput = CraftingHelper.getItemStack(item, CONTEXT);
                    chance = item.get("chance").getAsFloat();
                }
                DistillerRecipe.addRecipe(getFluidStack(json, "result"), getFluidStack(json, "input"), itemOutput, JsonUtils.getInt(json, "energy"), JsonUtils.getInt(json, "time"), chance);
                break;
            }
            case "immersivetech:solar_tower":
                SolarTowerRecipe.addRecipe(getFluidStack(json, "output"), getFluidStack(json, "input"), JsonUtils.getInt(json, "time"));
                break;
            case "immersivetech:steam_turbine":
                SteamTurbineRecipe.addFuel(getFluidStack(json, "output"), getFluidStack(json, "input"), JsonUtils.getInt(json, "time"));
                break;
            case "immersivetech:high_pressure_steam_turbine":
                HighPressureSteamTurbineRecipe.addFuel(getFluidStack(json, "output"), getFluidStack(json, "input"), JsonUtils.getInt(json, "time"));
                break;
            case "immersivetech:gas_turbine":
                GasTurbineRecipe.addFuel(getFluidStack(json, "output"), getFluidStack(json, "input"), JsonUtils.getInt(json, "time"));
                break;
            case "immersivetech:cooling_tower":
                CoolingTowerRecipe.addRecipe(getFluidStack(json, "output0"), getFluidStack(json, "output1"), getFluidStack(json, "output2"), getFluidStack(json, "input0"), getFluidStack(json, "input1"), JsonUtils.getInt(json, "time"));
                break;
            case "immersivetech:heat_exchanger":
                HeatExchangerRecipe.addRecipe(getFluidStack(json, "output0"), optionalFluidStack(json, "output1"), getFluidStack(json, "input0"), getFluidStack(json, "input1"), JsonUtils.getInt(json, "energy"), JsonUtils.getInt(json, "time"));
                break;
            case "immersivetech:electrolytic_crucible_battery": {
                ItemStack itemOutput = json.has("item_output") ? CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "item_output"), CONTEXT) : null;
                ElectrolyticCrucibleBatteryRecipe.addRecipe(getFluidStack(json, "result0"), optionalFluidStack(json, "result1"), optionalFluidStack(json, "result2"), itemOutput, getFluidStack(json, "input"), JsonUtils.getInt(json, "energy"), JsonUtils.getInt(json, "time"));
                break;
            }
            case "immersivetech:melting_crucible":
                MeltingCrucibleRecipe.addRecipe(getFluidStack(json, "result"), CraftingHelper.getIngredient(json.get("input"), CONTEXT), JsonUtils.getInt(json, "time"));
                break;
            case "immersivetech:radiator":
                RadiatorRecipe.addRecipe(getFluidStack(json, "output"), getFluidStack(json, "input"), JsonUtils.getInt(json, "time"));
                break;
            default:
                ITLogger.error("Unknown recipe type " + type + " in " + name);
                return false;
        }
        return true;
    }

    private static FluidStack getFluidStack(JsonObject json, String key) {
        JsonObject object = JsonUtils.getJsonObject(json, key);
        String name = JsonUtils.getString(object, "fluid");
        if (!FluidRegistry.isFluidRegistered(name)) { throw new JsonSyntaxException("Unknown fluid " + name); }
        return new FluidStack(FluidRegistry.getFluid(name), JsonUtils.getInt(object, "amount"));
    }

    private static FluidStack optionalFluidStack(JsonObject json, String key) {
        if (!json.has(key)) { return null; }
        return getFluidStack(json, key);
    }
}
