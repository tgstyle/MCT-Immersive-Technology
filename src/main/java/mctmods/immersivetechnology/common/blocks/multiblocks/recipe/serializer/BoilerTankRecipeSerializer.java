package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.serializer;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;

import com.google.gson.JsonObject;

import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerTankRecipe;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoilerTankRecipeSerializer extends IERecipeSerializer<BoilerTankRecipe> {
    @Override
    public ItemStack getIcon() { return ITMultiblockProvider.BOILER_TANK.iconStack(); }

    @Override
    public BoilerTankRecipe readFromJson(ResourceLocation recipeID, JsonObject json, ICondition.IContext iContext) {
        FluidTagInput input = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input"));
        FluidStack output = ApiUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "result"));
        int time = GsonHelper.getAsInt(json, "time", 1);
        return new BoilerTankRecipe(recipeID, input, output, time);
    }

    @Override
    public @Nullable BoilerTankRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        FluidTagInput input = FluidTagInput.read(buffer);
        FluidStack output = buffer.readFluidStack();
        int time = buffer.readInt();
        return new BoilerTankRecipe(recipeId, input, output, time);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, BoilerTankRecipe recipe) {
        recipe.input.write(buffer);
        buffer.writeFluidStack(recipe.output);
        buffer.writeInt(recipe.getTotalProcessTime());
    }
}
