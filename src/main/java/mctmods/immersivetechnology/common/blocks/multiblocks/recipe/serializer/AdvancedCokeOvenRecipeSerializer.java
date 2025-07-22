package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;

public class AdvancedCokeOvenRecipeSerializer extends IERecipeSerializer<AdvancedCokeOvenRecipe>
{
    @Override
    public ItemStack getIcon()
    {
        return ITMultiblockProvider.ADV_COKE_OVEN.iconStack();
    }

    @Override
    public AdvancedCokeOvenRecipe readFromJson(ResourceLocation recipeId, JsonObject json, ICondition.IContext context)
    {
        Lazy<ItemStack> output = readOutput(json.get("result"));
        IngredientWithSize input = IngredientWithSize.deserialize(json.get("input"));
        int time = GsonHelper.getAsInt(json, "time");
        int oil = GsonHelper.getAsInt(json, "creosote");
        return new AdvancedCokeOvenRecipe(recipeId, output, input, time, oil);
    }

    @Nullable
    @Override
    public AdvancedCokeOvenRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
    {
        Lazy<ItemStack> output = readLazyStack(buffer);
        IngredientWithSize input = IngredientWithSize.read(buffer);
        int time = buffer.readInt();
        int oil = buffer.readInt();
        return new AdvancedCokeOvenRecipe(recipeId, output, input, time, oil);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, AdvancedCokeOvenRecipe recipe)
    {
        writeLazyStack(buffer, recipe.output);
        recipe.input.write(buffer);
        buffer.writeInt(recipe.time);
        buffer.writeInt(recipe.creosoteOutput);
    }
}
