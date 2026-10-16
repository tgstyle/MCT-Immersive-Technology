package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.crafting.ICFluidTagInput;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.ElectrolyticCrucibleBatteryRecipe;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ElectrolyticCrucibleBatteryRecipeSerializer extends ICRecipeSerializer<ElectrolyticCrucibleBatteryRecipe> {

    @Override public ItemStack getIcon() { return MultiblockRegistry.ELECTROLYTIC_CRUCIBLE_BATTERY.iconStack(); }

    @Override public ElectrolyticCrucibleBatteryRecipe readFromJson(ResourceLocation recipeID, JsonObject json, ICondition.IContext context) {
        ICFluidTagInput input = ICFluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input"));
        FluidStack fluidOutput0 = ICUtils.optionalFluidStack(json, "result0");
        FluidStack fluidOutput1 = ICUtils.optionalFluidStack(json, "result1");
        FluidStack fluidOutput2 = ICUtils.optionalFluidStack(json, "result2");
        ItemStack itemOutput = json.has("item_output") ? ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "item_output")) : ItemStack.EMPTY;
        int energy = GsonHelper.getAsInt(json, "energy");
        int time = GsonHelper.getAsInt(json, "time");
        return new ElectrolyticCrucibleBatteryRecipe(recipeID, input, fluidOutput0, fluidOutput1, fluidOutput2, itemOutput, energy, time);
    }

    @Override @Nullable public ElectrolyticCrucibleBatteryRecipe fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer) {
        ICFluidTagInput input = ICFluidTagInput.read(buffer);
        boolean hasOut0 = buffer.readBoolean();
        FluidStack out0 = hasOut0 ? buffer.readFluidStack() : null;
        boolean hasOut1 = buffer.readBoolean();
        FluidStack out1 = hasOut1 ? buffer.readFluidStack() : null;
        boolean hasOut2 = buffer.readBoolean();
        FluidStack out2 = hasOut2 ? buffer.readFluidStack() : null;
        boolean hasItem = buffer.readBoolean();
        ItemStack itemOut = hasItem ? buffer.readItem() : ItemStack.EMPTY;
        int energy = buffer.readInt();
        int time = buffer.readInt();
        return new ElectrolyticCrucibleBatteryRecipe(recipeId, input, out0, out1, out2, itemOut, energy, time);
    }

    @Override public void toNetwork(@Nonnull FriendlyByteBuf buffer, ElectrolyticCrucibleBatteryRecipe recipe) {
        recipe.fluidInput0.write(buffer);
        buffer.writeBoolean(recipe.fluidOutput0 != null);
        if (recipe.fluidOutput0 != null) buffer.writeFluidStack(recipe.fluidOutput0);
        buffer.writeBoolean(recipe.fluidOutput1 != null);
        if (recipe.fluidOutput1 != null) buffer.writeFluidStack(recipe.fluidOutput1);
        buffer.writeBoolean(recipe.fluidOutput2 != null);
        if (recipe.fluidOutput2 != null) buffer.writeFluidStack(recipe.fluidOutput2);
        boolean hasItem = !recipe.itemOutput.isEmpty();
        buffer.writeBoolean(hasItem);
        if (hasItem) buffer.writeItem(recipe.itemOutput);
        buffer.writeInt(recipe.getBaseEnergy());
        buffer.writeInt(recipe.getBaseTime());
    }
}
