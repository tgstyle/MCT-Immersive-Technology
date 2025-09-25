package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.serializer;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.CoolingTowerRecipe;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CoolingTowerRecipeSerializer extends IERecipeSerializer<CoolingTowerRecipe> {
    @Override
    public net.minecraft.world.item.ItemStack getIcon() { return ITMultiblockProvider.COOLING_TOWER.iconStack(); }

    @Override
    public CoolingTowerRecipe readFromJson(ResourceLocation recipeID, JsonObject json, ICondition.IContext iContext) {
        FluidTagInput input0 = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input0"));
        FluidTagInput input1 = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input1"));
        FluidStack output0 = json.has("output0") ? ApiUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "output0")) : null;
        FluidStack output1 = json.has("output1") ? ApiUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "output1")) : null;
        FluidStack output2 = json.has("output2") ? ApiUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "output2")) : null;
        int time = GsonHelper.getAsInt(json, "time");
        return new CoolingTowerRecipe(recipeID, output0, output1, output2, input0, input1, time);
    }

    @Override
    public @Nullable CoolingTowerRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        FluidTagInput input0 = FluidTagInput.read(buffer);
        FluidTagInput input1 = FluidTagInput.read(buffer);
        FluidStack output0 = buffer.readBoolean() ? buffer.readFluidStack() : null;
        FluidStack output1 = buffer.readBoolean() ? buffer.readFluidStack() : null;
        FluidStack output2 = buffer.readBoolean() ? buffer.readFluidStack() : null;
        int time = buffer.readInt();
        return new CoolingTowerRecipe(recipeId, output0, output1, output2, input0, input1, time);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull CoolingTowerRecipe recipe) {
        recipe.input0.write(buffer);
        recipe.input1.write(buffer);
        buffer.writeBoolean(recipe.fluidOutput0 != null);
        if (recipe.fluidOutput0 != null) buffer.writeFluidStack(recipe.fluidOutput0);
        buffer.writeBoolean(recipe.fluidOutput1 != null);
        if (recipe.fluidOutput1 != null) buffer.writeFluidStack(recipe.fluidOutput1);
        buffer.writeBoolean(recipe.fluidOutput2 != null);
        if (recipe.fluidOutput2 != null) buffer.writeFluidStack(recipe.fluidOutput2);
        buffer.writeInt(recipe.getTotalProcessTime());
    }
}
