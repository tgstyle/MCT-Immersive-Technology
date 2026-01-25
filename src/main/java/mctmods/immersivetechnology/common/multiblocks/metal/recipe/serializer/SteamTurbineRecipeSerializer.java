package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.SteamTurbineRecipe;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class SteamTurbineRecipeSerializer extends IERecipeSerializer<SteamTurbineRecipe> {
    @Override public ItemStack getIcon() { return ITMultiblockProvider.STEAM_TURBINE.iconStack(); }

    @Override public SteamTurbineRecipe readFromJson(ResourceLocation recipeId, JsonObject json, ICondition.IContext iContext) {
        FluidTagInput input = FluidTagInput.deserialize(json.getAsJsonObject("input"));
        FluidStack fluidOutput = null;
        if (json.has("output")) fluidOutput = ApiUtils.jsonDeserializeFluidStack(json.getAsJsonObject("output"));
        int time = GsonHelper.getAsInt(json, "time");
        float torque = json.has("torque") ? GsonHelper.getAsFloat(json, "torque") : 1.0f;
        return new SteamTurbineRecipe(recipeId, input, fluidOutput, time, torque);
    }

    @Override @Nullable public SteamTurbineRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        FluidTagInput input = FluidTagInput.read(buffer);
        boolean hasOutput = buffer.readBoolean();
        FluidStack fluidOutput = hasOutput ? buffer.readFluidStack() : null;
        int time = buffer.readInt();
        float torque = buffer.readFloat();
        return new SteamTurbineRecipe(recipeId, input, fluidOutput, time, torque);
    }

    @Override public void toNetwork(@NotNull FriendlyByteBuf buffer, SteamTurbineRecipe recipe) {
        recipe.input.write(buffer);
        boolean hasOutput = recipe.fluidOutput != null;
        buffer.writeBoolean(hasOutput);
        if (hasOutput) buffer.writeFluidStack(recipe.fluidOutput);
        buffer.writeInt(recipe.getTotalProcessTime());
        buffer.writeFloat(recipe.torque);
    }
}
