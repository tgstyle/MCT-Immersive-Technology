package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.crafting.ICFluidTagInput;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;

import com.google.gson.JsonObject;

import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerTankLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerTankRecipe;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BoilerTankRecipeSerializer extends ICRecipeSerializer<BoilerTankRecipe> {
    @Override public ItemStack getIcon() { return MultiblockRegistry.BOILER_TANK.iconStack(); }

    @Override public BoilerTankRecipe readFromJson(ResourceLocation recipeID, JsonObject json, ICondition.IContext iContext) {
        ICFluidTagInput input = ICFluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input"));
        FluidStack output = ICUtils.fluidStack(json, "result");
        int time = GsonHelper.getAsInt(json, "time", 1);
        double requiredHeat = GsonHelper.getAsDouble(json, "requiredHeat", BoilerTankLogic.defaultWorkingHeatLevel());
        return new BoilerTankRecipe(recipeID, input, output, time, requiredHeat);
    }

    @Override @Nullable public BoilerTankRecipe fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer) {
        ICFluidTagInput input = ICFluidTagInput.read(buffer);
        FluidStack output = buffer.readFluidStack();
        int time = buffer.readInt();
        double requiredHeat = buffer.readDouble();
        return new BoilerTankRecipe(recipeId, input, output, time, requiredHeat);
    }

    @Override public void toNetwork(@Nonnull FriendlyByteBuf buffer, BoilerTankRecipe recipe) {
        recipe.input.write(buffer);
        buffer.writeFluidStack(recipe.output);
        buffer.writeInt(recipe.getBaseTime());
        buffer.writeDouble(recipe.requiredHeat);
    }
}
