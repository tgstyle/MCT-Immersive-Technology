package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.common.register.IEItems;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.AdvancedCokeOvenFuel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.conditions.ICondition;

import javax.annotation.Nullable;

public class AdvancedCokeOvenFuelSerializer extends IERecipeSerializer<AdvancedCokeOvenFuel>
{
    public AdvancedCokeOvenFuelSerializer()
    {

    }

    public ItemStack getIcon() {
        return new ItemStack(IEItems.Ingredients.COAL_COKE);
    }

    public AdvancedCokeOvenFuel readFromJson(ResourceLocation recipeId, JsonObject json, ICondition.IContext context) {
        Ingredient input = Ingredient.fromJson(json.getAsJsonObject("input"));
        int time = GsonHelper.getAsInt(json, "time", 1200);
        return new AdvancedCokeOvenFuel(recipeId, input, time);
    }

    @Nullable
    public AdvancedCokeOvenFuel fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        Ingredient input = Ingredient.fromNetwork(buffer);
        int time = buffer.readInt();
        return new AdvancedCokeOvenFuel(recipeId, input, time);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, AdvancedCokeOvenFuel recipe) {
        recipe.input.toNetwork(buffer);
        buffer.writeInt(recipe.burnTime);
    }

}
