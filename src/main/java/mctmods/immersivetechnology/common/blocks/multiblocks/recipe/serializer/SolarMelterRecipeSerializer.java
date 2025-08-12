package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.SolarMelterRecipe;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SolarMelterRecipeSerializer extends IERecipeSerializer<SolarMelterRecipe> {
    @Override
    public ItemStack getIcon() {
        return ITMultiblockProvider.SOLAR_MELTER.iconStack();
    }

    @Override
    public SolarMelterRecipe readFromJson(ResourceLocation resourceLocation, JsonObject jsonObject, ICondition.IContext iContext) { return null; }

    @Override
    public @Nullable SolarMelterRecipe fromNetwork(@NotNull ResourceLocation resourceLocation, @NotNull FriendlyByteBuf friendlyByteBuf) { return null; }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf friendlyByteBuf, @NotNull SolarMelterRecipe solarMelterRecipe) {}
}
