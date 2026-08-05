package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerSolidLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerSolidRecipe;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoilerSolidRecipeSerializer extends IERecipeSerializer<BoilerSolidRecipe> {
    @Override public ItemStack getIcon() { return MultiblockRegistry.BOILER_SOLID.iconStack(); }

    @Override public BoilerSolidRecipe readFromJson(ResourceLocation recipeId, JsonObject json, ICondition.IContext context) {
        IngredientWithSize input = IngredientWithSize.deserialize(GsonHelper.getAsJsonObject(json, "input"));
        double heatPerTick = GsonHelper.getAsDouble(json, "heatPerTick");
        double targetHeat = GsonHelper.getAsDouble(json, "targetHeat", BoilerSolidLogic.defaultWorkingHeatLevel());
        return new BoilerSolidRecipe(recipeId, input, heatPerTick, targetHeat);
    }

    @Override @Nullable public BoilerSolidRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        IngredientWithSize input = IngredientWithSize.read(buffer);
        double heatPerTick = buffer.readDouble();
        double targetHeat = buffer.readDouble();
        return new BoilerSolidRecipe(recipeId, input, heatPerTick, targetHeat);
    }

    @Override public void toNetwork(@NotNull FriendlyByteBuf buffer, BoilerSolidRecipe recipe) {
        recipe.input.write(buffer);
        buffer.writeDouble(recipe.getHeatPerTick());
        buffer.writeDouble(recipe.getTargetHeat());
    }
}
