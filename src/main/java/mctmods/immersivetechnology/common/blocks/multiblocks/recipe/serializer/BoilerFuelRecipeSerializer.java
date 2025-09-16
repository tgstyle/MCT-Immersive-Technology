package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerLiquidRecipe;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoilerFuelRecipeSerializer extends IERecipeSerializer<BoilerLiquidRecipe> {
    @Override
    public ItemStack getIcon() { return ITMultiblockProvider.BOILER_LIQUID.iconStack(); }

    @Override
    public BoilerLiquidRecipe readFromJson(ResourceLocation recipeId, JsonObject json, ICondition.IContext context) {
        FluidTagInput input = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input"));
        int time = GsonHelper.getAsInt(json, "time");
        double heatPerTick = GsonHelper.getAsDouble(json, "heatPerTick");
        return new BoilerLiquidRecipe(recipeId, input, time, heatPerTick);
    }

    @Override
    public @Nullable BoilerLiquidRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        FluidTagInput input = FluidTagInput.read(buffer);
        int time = buffer.readInt();
        double heatPerTick = buffer.readDouble();
        return new BoilerLiquidRecipe(recipeId, input, time, heatPerTick);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, BoilerLiquidRecipe recipe) {
        recipe.input.write(buffer);
        buffer.writeInt(recipe.getTotalProcessTime());
        buffer.writeDouble(recipe.getHeatPerTick());
    }
}
