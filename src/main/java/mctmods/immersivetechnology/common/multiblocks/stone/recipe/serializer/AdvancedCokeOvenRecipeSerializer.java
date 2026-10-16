package mctmods.immersivetechnology.common.multiblocks.stone.recipe.serializer;

import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.immersiveconvergence.api.crafting.ICIngredientWithSize;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AdvancedCokeOvenRecipeSerializer extends ICRecipeSerializer<AdvancedCokeOvenRecipe> {
    @Override
    public ItemStack getIcon() { return MultiblockRegistry.ADVANCED_COKE_OVEN.iconStack(); }

    @Override
    public AdvancedCokeOvenRecipe readFromJson(ResourceLocation recipeId, JsonObject json, ICondition.IContext context) {
        ICIngredientWithSize input = ICIngredientWithSize.deserialize(json.get("input"));
        Lazy<ItemStack> itemOutput = readOutput(json.get("result"));
        int time = GsonHelper.getAsInt(json, "time");
        int creosoteOutput = GsonHelper.getAsInt(json, "creosote");
        return new AdvancedCokeOvenRecipe(recipeId, input, itemOutput, time, creosoteOutput);
    }

    @Nullable
    @Override
    public AdvancedCokeOvenRecipe fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer) {
        ICIngredientWithSize input = ICIngredientWithSize.read(buffer);
        Lazy<ItemStack> itemOutput = readLazyStack(buffer);
        int time = buffer.readInt();
        int creosoteOutput = buffer.readInt();
        return new AdvancedCokeOvenRecipe(recipeId, input, itemOutput, time, creosoteOutput);
    }

    @Override
    public void toNetwork(@Nonnull FriendlyByteBuf buffer, AdvancedCokeOvenRecipe recipe) {
        recipe.input.write(buffer);
        writeLazyStack(buffer, recipe.itemOutput);
        buffer.writeInt(recipe.getBaseTime());
        buffer.writeInt(recipe.creosoteOutput);
    }
}
