package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.GasTurbineRecipe;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class GasTurbineRecipeSerializer extends IERecipeSerializer<GasTurbineRecipe> {
    @Override
    public ItemStack getIcon() { return ITMultiblockProvider.GAS_TURBINE.iconStack(); }

    @Override
    public GasTurbineRecipe readFromJson(ResourceLocation recipeId, JsonObject json, ICondition.IContext iContext) {
        FluidTagInput input = FluidTagInput.deserialize(json.getAsJsonObject("input"));
        FluidStack fluidOutput = null;
        if (json.has("output")) fluidOutput = ApiUtils.jsonDeserializeFluidStack(json.getAsJsonObject("output"));
        int time = GsonHelper.getAsInt(json, "time");
        return new GasTurbineRecipe(recipeId, input, fluidOutput, time);
    }

    @Nullable
    @Override
    public GasTurbineRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        FluidTagInput input = FluidTagInput.read(buffer);
        boolean hasOutput = buffer.readBoolean();
        FluidStack fluidOutput = hasOutput ? buffer.readFluidStack() : null;
        int time = buffer.readInt();
        return new GasTurbineRecipe(recipeId, input, fluidOutput, time);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, GasTurbineRecipe recipe) {
        recipe.input.write(buffer);
        boolean hasOutput = recipe.fluidOutput != null;
        buffer.writeBoolean(hasOutput);
        if (hasOutput) buffer.writeFluidStack(recipe.fluidOutput);
        buffer.writeInt(recipe.getTotalProcessTime());
    }
}
