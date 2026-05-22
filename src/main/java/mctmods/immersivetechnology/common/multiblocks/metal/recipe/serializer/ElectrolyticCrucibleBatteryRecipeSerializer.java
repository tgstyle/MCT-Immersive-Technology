package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.ElectrolyticCrucibleBatteryRecipe;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ElectrolyticCrucibleBatteryRecipeSerializer extends IERecipeSerializer<ElectrolyticCrucibleBatteryRecipe> {

    @Override public ItemStack getIcon() { return ITMultiblockProvider.ELECTROLYTIC_CRUCIBLE_BATTERY.iconStack(); }

    @Override public ElectrolyticCrucibleBatteryRecipe readFromJson(ResourceLocation recipeID, JsonObject json, ICondition.IContext context) {
        FluidTagInput input = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input"));
        FluidStack fluidOutput0 = json.has("result0") ? ApiUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "result0")) : null;
        FluidStack fluidOutput1 = json.has("result1") ? ApiUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "result1")) : null;
        FluidStack fluidOutput2 = json.has("result2") ? ApiUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "result2")) : null;
        ItemStack itemOutput = json.has("item_output") ? ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "item_output")) : ItemStack.EMPTY;
        int energy = GsonHelper.getAsInt(json, "energy");
        int time = GsonHelper.getAsInt(json, "time");
        return new ElectrolyticCrucibleBatteryRecipe(recipeID, input, fluidOutput0, fluidOutput1, fluidOutput2, itemOutput, energy, time);
    }

    @Override @Nullable public ElectrolyticCrucibleBatteryRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        FluidTagInput input = FluidTagInput.read(buffer);
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

    @Override public void toNetwork(@NotNull FriendlyByteBuf buffer, ElectrolyticCrucibleBatteryRecipe recipe) {
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
        buffer.writeInt(recipe.getTotalProcessEnergy());
        buffer.writeInt(recipe.getTotalProcessTime());
    }
}
