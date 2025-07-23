package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.serializer;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.DistillerRecipe;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class DistillerRecipeSerializer extends IERecipeSerializer<DistillerRecipe>
{
    @Override
    public ItemStack getIcon() {
        return ITMultiblockProvider.DISTILLER.iconStack();
    }

    @Override
    public DistillerRecipe readFromJson(ResourceLocation recipeID, JsonObject json, ICondition.IContext iContext)
    {
        int energy = GsonHelper.getAsInt(json, "energy");
        FluidTagInput input = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input"));
        FluidStack output = ApiUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "result"));
        int time = GsonHelper.getAsInt(json, "time");
        DistillerRecipe recipe = new DistillerRecipe(recipeID, input, output, time, energy);
        return recipe;
    }

    @Override
    public @Nullable DistillerRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
    {
        int energy = buffer.readInt();
        FluidTagInput input = FluidTagInput.read(buffer);
        FluidStack output = buffer.readFluidStack();
        int time = buffer.readInt();
        return new DistillerRecipe(recipeId, input, output, time, energy);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, DistillerRecipe recipe)
    {
        buffer.writeInt(recipe.getTotalProcessEnergy());
        buffer.writeFluidStack(recipe.fluidOutput);
        recipe.water.write(buffer);
        buffer.writeInt(recipe.getTotalProcessTime());
    }
}
