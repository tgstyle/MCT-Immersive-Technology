package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerFuelRecipe;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoilerFuelRecipeSerializer extends IERecipeSerializer<BoilerFuelRecipe> {
    @Override
    public ItemStack getIcon() { return ITMultiblockProvider.BOILER.iconStack(); }

    @Override
    public BoilerFuelRecipe readFromJson(ResourceLocation recipeId, JsonObject json, ICondition.IContext context) {
        FluidTagInput fuel = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "fuel"));
        int time = GsonHelper.getAsInt(json, "time");
        int heatPerTick = GsonHelper.getAsInt(json, "heatPerTick");
        return new BoilerFuelRecipe(recipeId, fuel, time, heatPerTick);
    }

    @Override
    public @Nullable BoilerFuelRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        FluidTagInput fuel = FluidTagInput.read(buffer);
        int time = buffer.readInt();
        int heatPerTick = buffer.readInt();
        return new BoilerFuelRecipe(recipeId, fuel, time, heatPerTick);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, BoilerFuelRecipe recipe) {
        recipe.fuel.write(buffer);
        buffer.writeInt(recipe.getTotalProcessTime());
        buffer.writeInt(recipe.getHeatPerTick());
    }
}
