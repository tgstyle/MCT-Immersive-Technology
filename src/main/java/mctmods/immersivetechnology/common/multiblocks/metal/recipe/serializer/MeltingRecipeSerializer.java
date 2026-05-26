package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.MeltingRecipe;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MeltingRecipeSerializer extends IERecipeSerializer<MeltingRecipe> {
    @Override public ItemStack getIcon() { return ITMultiblockProvider.SOLAR_MELTER.iconStack(); }

    @Override public MeltingRecipe readFromJson(ResourceLocation recipeId, JsonObject json, ICondition.IContext iContext) {
        FluidTagInput input = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input"));
        FluidStack fluidOutput = ApiUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "output"));
        int time = GsonHelper.getAsInt(json, "time");
        double requiredTemp = GsonHelper.getAsDouble(json, "requiredTemp");
        return new MeltingRecipe(recipeId, input, fluidOutput, time, requiredTemp);
    }

    @Override @Nullable public MeltingRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        FluidTagInput input = FluidTagInput.read(buffer);
        FluidStack fluidOutput = buffer.readFluidStack();
        int time = buffer.readInt();
        double requiredTemp = buffer.readDouble();
        return new MeltingRecipe(recipeId, input, fluidOutput, time, requiredTemp);
    }

    @Override public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull MeltingRecipe recipe) {
        recipe.input.write(buffer);
        buffer.writeFluidStack(recipe.fluidOutput);
        buffer.writeInt(recipe.getTotalProcessTime());
        buffer.writeDouble(recipe.requiredTemp);
    }
}
