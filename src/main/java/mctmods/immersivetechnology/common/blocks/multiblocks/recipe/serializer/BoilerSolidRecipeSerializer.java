package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerSolidRecipe;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoilerSolidRecipeSerializer extends IERecipeSerializer<BoilerSolidRecipe> {
    @Override
    public ItemStack getIcon() { return ITMultiblockProvider.BOILER_SOLID.iconStack(); }

    @Override
    public BoilerSolidRecipe readFromJson(ResourceLocation recipeId, JsonObject json, ICondition.IContext context) {
        IngredientWithSize input = IngredientWithSize.deserialize(GsonHelper.getAsJsonObject(json, "input"));
        double heatPerTick = GsonHelper.getAsDouble(json, "heatPerTick");
        return new BoilerSolidRecipe(recipeId, input, heatPerTick);
    }

    @Override
    public @Nullable BoilerSolidRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        IngredientWithSize input = IngredientWithSize.read(buffer);
        double heatPerTick = buffer.readDouble();
        return new BoilerSolidRecipe(recipeId, input, heatPerTick);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, BoilerSolidRecipe recipe) {
        recipe.input.write(buffer);
        buffer.writeDouble(recipe.getHeatPerTick());
    }
}
