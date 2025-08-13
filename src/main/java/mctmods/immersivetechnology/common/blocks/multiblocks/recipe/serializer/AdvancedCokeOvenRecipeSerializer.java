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
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class AdvancedCokeOvenRecipeSerializer extends IERecipeSerializer<AdvancedCokeOvenRecipe> {
    @Override
    public ItemStack getIcon() { return ITMultiblockProvider.ADVANCED_COKE_OVEN.iconStack(); }

    @Override
    public AdvancedCokeOvenRecipe readFromJson(ResourceLocation recipeId, JsonObject json, ICondition.IContext context) {
        IngredientWithSize input = IngredientWithSize.deserialize(json.get("input"));
        Lazy<ItemStack> itemOutput = readOutput(json.get("result"));
        int time = GsonHelper.getAsInt(json, "time");
        int creosoteOutput = GsonHelper.getAsInt(json, "creosote");
        return new AdvancedCokeOvenRecipe(recipeId, input, itemOutput, time, creosoteOutput);
    }

    @Nullable
    @Override
    public AdvancedCokeOvenRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        IngredientWithSize input = IngredientWithSize.read(buffer);
        Lazy<ItemStack> itemOutput = readLazyStack(buffer);
        int time = buffer.readInt();
        int creosoteOutput = buffer.readInt();
        return new AdvancedCokeOvenRecipe(recipeId, input, itemOutput, time, creosoteOutput);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, AdvancedCokeOvenRecipe recipe) {
        recipe.input.write(buffer);
        writeLazyStack(buffer, recipe.itemOutput);
        buffer.writeInt(recipe.time);
        buffer.writeInt(recipe.creosoteOutput);
    }
}
