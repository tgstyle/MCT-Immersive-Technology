package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.DistillerRecipe;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DistillerRecipeSerializer extends IERecipeSerializer<DistillerRecipe> {
    @Override public ItemStack getIcon() { return MultiblockRegistry.DISTILLER.iconStack(); }

    @Override public DistillerRecipe readFromJson(ResourceLocation recipeID, JsonObject json, ICondition.IContext iContext) {
        FluidTagInput input = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input"));
        FluidStack fluidOutput = ApiUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "result"));
        ItemStack itemOutput = ItemStack.EMPTY;
        float chance = 0.0f;
        if (json.has("item_output")) {
            JsonObject itemJson = GsonHelper.getAsJsonObject(json, "item_output");
            itemOutput = ShapedRecipe.itemStackFromJson(itemJson);
            if (itemJson.has("chance")) chance = GsonHelper.getAsFloat(itemJson, "chance", 0.0f);
        }
        int time = GsonHelper.getAsInt(json, "time");
        int energy = GsonHelper.getAsInt(json, "energy");
        return new DistillerRecipe(recipeID, input, fluidOutput, itemOutput, chance, time, energy);
    }

    @Override @Nullable public DistillerRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        FluidTagInput input = FluidTagInput.read(buffer);
        FluidStack fluidOutput = buffer.readFluidStack();
        boolean hasItem = buffer.readBoolean();
        ItemStack itemOutput = hasItem ? buffer.readItem() : ItemStack.EMPTY;
        float chance = hasItem ? buffer.readFloat() : 0.0f;
        int time = buffer.readInt();
        int energy = buffer.readInt();
        return new DistillerRecipe(recipeId, input, fluidOutput, itemOutput, chance, time, energy);
    }

    @Override public void toNetwork(@NotNull FriendlyByteBuf buffer, DistillerRecipe recipe) {
        recipe.input.write(buffer);
        buffer.writeFluidStack(recipe.fluidOutput);
        boolean hasItem = !recipe.itemOutput.isEmpty();
        buffer.writeBoolean(hasItem);
        if (hasItem) {
            buffer.writeItem(recipe.itemOutput);
            buffer.writeFloat(recipe.chance);
        }
        buffer.writeInt(recipe.getTotalProcessTime());
        buffer.writeInt(recipe.getTotalProcessEnergy());
    }
}
