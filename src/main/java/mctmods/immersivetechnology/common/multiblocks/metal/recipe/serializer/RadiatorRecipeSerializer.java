package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.crafting.ICFluidTagInput;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.RadiatorRecipe;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RadiatorRecipeSerializer extends ICRecipeSerializer<RadiatorRecipe> {
    @Override public net.minecraft.world.item.ItemStack getIcon() { return MultiblockRegistry.RADIATOR.iconStack(); }

    @Override public RadiatorRecipe readFromJson(ResourceLocation recipeID, JsonObject json, ICondition.IContext iContext) {
        ICFluidTagInput input = ICFluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input"));
        FluidStack output = ICUtils.fluidStack(json, "output");
        int time = GsonHelper.getAsInt(json, "time");
        return new RadiatorRecipe(recipeID, output, input, time);
    }

    @Override @Nullable public RadiatorRecipe fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer) {
        ICFluidTagInput input = ICFluidTagInput.read(buffer);
        FluidStack output = buffer.readFluidStack();
        int time = buffer.readInt();
        return new RadiatorRecipe(recipeId, output, input, time);
    }

    @Override public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull RadiatorRecipe recipe) {
        recipe.input.write(buffer);
        buffer.writeFluidStack(recipe.fluidOutput);
        buffer.writeInt(recipe.getBaseTime());
    }
}
