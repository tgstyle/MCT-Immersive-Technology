package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerLiquidLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerLiquidRecipe;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BoilerLiquidRecipeSerializer extends IERecipeSerializer<BoilerLiquidRecipe> {
    @Override public ItemStack getIcon() { return MultiblockRegistry.BOILER_LIQUID.iconStack(); }

    @Override public BoilerLiquidRecipe readFromJson(ResourceLocation recipeId, JsonObject json, ICondition.IContext context) {
        FluidTagInput input = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input"));
        int time = GsonHelper.getAsInt(json, "time");
        double heatPerTick = GsonHelper.getAsDouble(json, "heatPerTick");
        double targetHeat = GsonHelper.getAsDouble(json, "targetHeat", BoilerLiquidLogic.defaultWorkingHeatLevel());
        return new BoilerLiquidRecipe(recipeId, input, time, heatPerTick, targetHeat);
    }

    @Override @Nullable public BoilerLiquidRecipe fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer) {
        FluidTagInput input = FluidTagInput.read(buffer);
        int time = buffer.readInt();
        double heatPerTick = buffer.readDouble();
        double targetHeat = buffer.readDouble();
        return new BoilerLiquidRecipe(recipeId, input, time, heatPerTick, targetHeat);
    }

    @Override public void toNetwork(@Nonnull FriendlyByteBuf buffer, BoilerLiquidRecipe recipe) {
        recipe.input.write(buffer);
        buffer.writeInt(recipe.getTotalProcessTime());
        buffer.writeDouble(recipe.getHeatPerTick());
        buffer.writeDouble(recipe.getTargetHeat());
    }
}
