package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.crafting.ICFluidTagInput;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.HeatExchangerRecipe;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class HeatExchangerRecipeSerializer extends ICRecipeSerializer<HeatExchangerRecipe> {
    @Override public ItemStack getIcon() { return MultiblockRegistry.HEAT_EXCHANGER.iconStack(); }

    @Override public HeatExchangerRecipe readFromJson(ResourceLocation id, JsonObject json, ICondition.IContext context) {
        ICFluidTagInput input0 = ICFluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input0"));
        ICFluidTagInput input1 = json.has("input1") ? ICFluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input1")) : null;
        FluidStack output0 = ICUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "output0"));
        FluidStack output1 = json.has("output1") ? ICUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "output1")) : null;
        int energy = GsonHelper.getAsInt(json, "energy");
        int time = GsonHelper.getAsInt(json, "time");
        return new HeatExchangerRecipe(id, input0, input1, output0, output1, energy, time)
                .modifyTimeAndEnergy(t -> t * HeatExchangerRecipe.timeModifier, e -> e * HeatExchangerRecipe.energyModifier);
    }

    @Override public HeatExchangerRecipe fromNetwork(@Nonnull ResourceLocation id, @Nonnull FriendlyByteBuf buffer) {
        ICFluidTagInput input0 = ICFluidTagInput.read(buffer);
        boolean hasInput1 = buffer.readBoolean();
        ICFluidTagInput input1 = hasInput1 ? ICFluidTagInput.read(buffer) : null;
        FluidStack output0 = buffer.readFluidStack();
        boolean hasOutput1 = buffer.readBoolean();
        FluidStack output1 = hasOutput1 ? buffer.readFluidStack() : null;
        int energy = buffer.readInt();
        int time = buffer.readInt();
        return new HeatExchangerRecipe(id, input0, input1, output0, output1, energy, time);
    }

    @Override public void toNetwork(@Nonnull FriendlyByteBuf buffer, HeatExchangerRecipe recipe) {
        recipe.input0.write(buffer);
        buffer.writeBoolean(recipe.input1 != null);
        if (recipe.input1 != null) recipe.input1.write(buffer);
        buffer.writeFluidStack(recipe.output0);
        buffer.writeBoolean(recipe.output1 != null);
        if (recipe.output1 != null) buffer.writeFluidStack(recipe.output1);
        buffer.writeInt(recipe.getTotalProcessEnergy());
        buffer.writeInt(recipe.getTotalProcessTime());
    }
}
