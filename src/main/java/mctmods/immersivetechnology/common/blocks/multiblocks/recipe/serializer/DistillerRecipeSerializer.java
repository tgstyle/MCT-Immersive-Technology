package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.serializer;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.DistillerRecipe;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
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
    @Override
    public ItemStack getIcon() { return ITMultiblockProvider.DISTILLER.iconStack(); }

    @Override
    public DistillerRecipe readFromJson(ResourceLocation recipeID, JsonObject json, ICondition.IContext iContext) {
        int energy = GsonHelper.getAsInt(json, "energy");
        FluidTagInput input = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input"));
        FluidStack output = ApiUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "result"));
        int time = GsonHelper.getAsInt(json, "time");
        ItemStack itemOutput = ItemStack.EMPTY;
        float chance = 0.0f;
        if (json.has("item_output")) {
            itemOutput = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "item_output"));
            chance = GsonHelper.getAsFloat(json, "chance", 0.0f);
        }
        return new DistillerRecipe(recipeID, input, output, itemOutput, chance, time, energy);
    }

    @Override
    public @Nullable DistillerRecipe fromNetwork(@NotNull ResourceLocation recipeId, FriendlyByteBuf buffer) {
        int energy = buffer.readInt();
        FluidTagInput input = FluidTagInput.read(buffer);
        FluidStack output = buffer.readFluidStack();
        int time = buffer.readInt();
        boolean hasItem = buffer.readBoolean();
        ItemStack itemOutput = hasItem ? buffer.readItem() : ItemStack.EMPTY;
        float chance = hasItem ? buffer.readFloat() : 0.0f;
        return new DistillerRecipe(recipeId, input, output, itemOutput, chance, time, energy);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, DistillerRecipe recipe) {
        buffer.writeInt(recipe.getTotalProcessEnergy());
        recipe.water.write(buffer);
        buffer.writeFluidStack(recipe.fluidOutput);
        buffer.writeInt(recipe.getTotalProcessTime());
        boolean hasItem = !recipe.itemOutput.isEmpty();
        buffer.writeBoolean(hasItem);
        if (hasItem) {
            buffer.writeItem(recipe.itemOutput);
            buffer.writeFloat(recipe.chance);
        }
    }
}