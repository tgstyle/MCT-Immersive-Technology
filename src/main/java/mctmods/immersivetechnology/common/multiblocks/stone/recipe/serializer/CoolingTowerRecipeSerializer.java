package mctmods.immersivetechnology.common.multiblocks.stone.recipe.serializer;

import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.crafting.ICFluidTagInput;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.CoolingTowerRecipe;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CoolingTowerRecipeSerializer extends ICRecipeSerializer<CoolingTowerRecipe> {
    @Override public net.minecraft.world.item.ItemStack getIcon() { return MultiblockRegistry.COOLING_TOWER.iconStack(); }

    @Override public CoolingTowerRecipe readFromJson(ResourceLocation recipeID, JsonObject json, ICondition.IContext iContext) {
        ICFluidTagInput input0 = ICFluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input0"));
        ICFluidTagInput input1 = ICFluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input1"));
        FluidStack output0 = ICUtils.optionalFluidStack(json, "output0");
        FluidStack output1 = ICUtils.optionalFluidStack(json, "output1");
        FluidStack output2 = ICUtils.optionalFluidStack(json, "output2");
        int time = GsonHelper.getAsInt(json, "time");
        return new CoolingTowerRecipe(recipeID, output0, output1, output2, input0, input1, time);
    }

    @Override @Nullable public CoolingTowerRecipe fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer) {
        ICFluidTagInput input0 = ICFluidTagInput.read(buffer);
        ICFluidTagInput input1 = ICFluidTagInput.read(buffer);
        FluidStack output0 = buffer.readBoolean() ? buffer.readFluidStack() : null;
        FluidStack output1 = buffer.readBoolean() ? buffer.readFluidStack() : null;
        FluidStack output2 = buffer.readBoolean() ? buffer.readFluidStack() : null;
        int time = buffer.readInt();
        return new CoolingTowerRecipe(recipeId, output0, output1, output2, input0, input1, time);
    }

    @Override public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull CoolingTowerRecipe recipe) {
        recipe.input0.write(buffer);
        recipe.input1.write(buffer);
        buffer.writeBoolean(recipe.fluidOutput0 != null);
        if (recipe.fluidOutput0 != null) buffer.writeFluidStack(recipe.fluidOutput0);
        buffer.writeBoolean(recipe.fluidOutput1 != null);
        if (recipe.fluidOutput1 != null) buffer.writeFluidStack(recipe.fluidOutput1);
        buffer.writeBoolean(recipe.fluidOutput2 != null);
        if (recipe.fluidOutput2 != null) buffer.writeFluidStack(recipe.fluidOutput2);
        buffer.writeInt(recipe.getBaseTime());
    }
}
