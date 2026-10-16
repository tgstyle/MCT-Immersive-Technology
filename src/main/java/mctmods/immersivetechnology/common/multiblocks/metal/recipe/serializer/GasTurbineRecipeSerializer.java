package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.crafting.ICFluidTagInput;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.GasTurbineRecipe;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GasTurbineRecipeSerializer extends ICRecipeSerializer<GasTurbineRecipe> {
    @Override public ItemStack getIcon() { return MultiblockRegistry.GAS_TURBINE.iconStack(); }

    @Override public GasTurbineRecipe readFromJson(ResourceLocation recipeId, JsonObject json, ICondition.IContext iContext) {
        ICFluidTagInput input = ICFluidTagInput.deserialize(json.getAsJsonObject("input"));
        FluidStack fluidOutput = ICUtils.optionalFluidStack(json, "output");
        int time = GsonHelper.getAsInt(json, "time");
        float torque = json.has("torque") ? GsonHelper.getAsFloat(json, "torque") : 1.0f;
        return new GasTurbineRecipe(recipeId, input, fluidOutput, time, torque);
    }

    @Override @Nullable public GasTurbineRecipe fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer) {
        ICFluidTagInput input = ICFluidTagInput.read(buffer);
        boolean hasOutput = buffer.readBoolean();
        FluidStack fluidOutput = hasOutput ? buffer.readFluidStack() : null;
        int time = buffer.readInt();
        float torque = buffer.readFloat();
        return new GasTurbineRecipe(recipeId, input, fluidOutput, time, torque);
    }

    @Override public void toNetwork(@Nonnull FriendlyByteBuf buffer, GasTurbineRecipe recipe) {
        recipe.input.write(buffer);
        boolean hasOutput = recipe.fluidOutput != null;
        buffer.writeBoolean(hasOutput);
        if (hasOutput) buffer.writeFluidStack(recipe.fluidOutput);
        buffer.writeInt(recipe.getBaseTime());
        buffer.writeFloat(recipe.torque);
    }
}
