package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.SteamTurbineRecipe;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SteamTurbineRecipeSerializer extends IERecipeSerializer<SteamTurbineRecipe>
{
    @Override
    public ItemStack getIcon()
    {
        return ITMultiblockProvider.STEAM_TURBINE.iconStack();
    }

    @Override
    public SteamTurbineRecipe readFromJson(ResourceLocation recipeId, JsonObject json, ICondition.IContext iContext)
    {
        JsonObject inputJson = json.get("input").getAsJsonObject();
        TagKey<Fluid> tag = TagKey.create(Registries.FLUID, new ResourceLocation(inputJson.get("tag").getAsString()));
        int inputAmount = inputJson.get("amount").getAsInt();

        FluidStack fluidOutput = null;
        if (json.has("output")) {
            JsonObject outJson = json.get("output").getAsJsonObject();
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(outJson.get("fluid").getAsString()));
            int outAmount = outJson.get("amount").getAsInt();
            fluidOutput = new FluidStack(fluid, outAmount);
        }

        int time = json.get("time").getAsInt();
        return new SteamTurbineRecipe(recipeId, tag, inputAmount, fluidOutput, time);
    }

    @Nullable
    @Override
    public SteamTurbineRecipe fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer)
    {
        TagKey<Fluid> tag = TagKey.create(Registries.FLUID, buffer.readResourceLocation());
        int inputAmount = buffer.readInt();
        boolean hasOutput = buffer.readBoolean();
        FluidStack fluidOutput = hasOutput ? FluidStack.readFromPacket(buffer) : null;
        int time = buffer.readInt();
        return new SteamTurbineRecipe(recipeId, tag, inputAmount, fluidOutput, time);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, SteamTurbineRecipe recipe)
    {
        buffer.writeResourceLocation(recipe.inputTag.location());
        buffer.writeInt(recipe.inputAmount);
        boolean hasOutput = recipe.fluidOutput != null;
        buffer.writeBoolean(hasOutput);
        if (hasOutput) {
            recipe.fluidOutput.writeToPacket(buffer);
        }
        buffer.writeInt(recipe.getTotalProcessTime());
    }
}