package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.serializer;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerRecipe;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoilerRecipeSerializer extends IERecipeSerializer<BoilerRecipe> {
    @Override
    public ItemStack getIcon() {
        return ITMultiblockProvider.BOILER.iconStack();
    }

    @Override
    public BoilerRecipe readFromJson(ResourceLocation recipeID, JsonObject json, ICondition.IContext iContext) {
        FluidStack output = ApiUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "result"));
        int time = GsonHelper.getAsInt(json, "time", 1);
        FluidTagInput input0 = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input"));
        return new BoilerRecipe(recipeID, output, input0, time);
    }

    @Override
    public @Nullable BoilerRecipe fromNetwork(@NotNull ResourceLocation recipeId, FriendlyByteBuf buffer) {
        FluidStack output = buffer.readFluidStack();
        FluidTagInput input0 = FluidTagInput.read(buffer);
        int time = buffer.readInt();
        return new BoilerRecipe(recipeId, output, input0, time);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, BoilerRecipe recipe) {
        buffer.writeFluidStack(recipe.output);
        recipe.water.write(buffer);
        buffer.writeInt(recipe.getTotalProcessTime());
    }
}