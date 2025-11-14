package mctmods.immersivetechnology.common.multiblocks.metal.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.DistillerRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.Objects;

public class DistillerRecipeBuilder extends IEFinishedRecipe<DistillerRecipeBuilder> {
    public DistillerRecipeBuilder() {
        super(DistillerRecipe.SERIALIZER.get());
        this.maxInputCount = 2;
    }

    public static DistillerRecipeBuilder builder(FluidTagInput fluidIn, FluidStack primaryFluidOutput, int time, int energy) {
        return new DistillerRecipeBuilder().addFluidTag("input", fluidIn).addFluid("result", primaryFluidOutput).setTime(time).setEnergy(energy);
    }

    public DistillerRecipeBuilder addItemOutput(ItemStack item, float chance) {
        return this.addWriter(jsonObject -> {
            com.google.gson.JsonObject itemJson = new com.google.gson.JsonObject();
            itemJson.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item.getItem())).toString());
            if (item.getCount() > 1) itemJson.addProperty("count", item.getCount());
            itemJson.addProperty("chance", chance);
            jsonObject.add("item_output", itemJson);
        });
    }
}
