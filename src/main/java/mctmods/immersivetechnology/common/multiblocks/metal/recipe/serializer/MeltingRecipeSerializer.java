package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.crafting.ICFluidTagInput;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.MeltingRecipe;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MeltingRecipeSerializer extends ICRecipeSerializer<MeltingRecipe> {
    @Override public ItemStack getIcon() { return MultiblockRegistry.SOLAR_MELTER.iconStack(); }

    @Override public MeltingRecipe readFromJson(ResourceLocation recipeId, JsonObject json, ICondition.IContext iContext) {
        ICFluidTagInput input = ICFluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input"));
        FluidStack fluidOutput = ICUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "output"));
        int time = GsonHelper.getAsInt(json, "time");
        double requiredTemp = GsonHelper.getAsDouble(json, "requiredTemp");
        return new MeltingRecipe(recipeId, input, fluidOutput, time, requiredTemp);
    }

    @Override @Nullable public MeltingRecipe fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer) {
        ICFluidTagInput input = ICFluidTagInput.read(buffer);
        FluidStack fluidOutput = buffer.readFluidStack();
        int time = buffer.readInt();
        double requiredTemp = buffer.readDouble();
        return new MeltingRecipe(recipeId, input, fluidOutput, time, requiredTemp);
    }

    @Override public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull MeltingRecipe recipe) {
        recipe.input.write(buffer);
        buffer.writeFluidStack(recipe.fluidOutput);
        buffer.writeInt(recipe.getTotalProcessTime());
        buffer.writeDouble(recipe.requiredTemp);
    }
}
