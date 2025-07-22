package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.SolarTowerRecipe;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

public class SolarTowerRecipeSerializer extends IERecipeSerializer<SolarTowerRecipe>
{
    @Override
    public ItemStack getIcon() {
        return ITMultiblockProvider.BOILER.iconStack();
    }

    @Override
    public SolarTowerRecipe readFromJson(ResourceLocation resourceLocation, JsonObject jsonObject, ICondition.IContext iContext) {
        return null;
    }

    @Override
    public @Nullable SolarTowerRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
        return null;
    }

    @Override
    public void toNetwork(FriendlyByteBuf friendlyByteBuf, SolarTowerRecipe solarTowerRecipe) {

    }
}
