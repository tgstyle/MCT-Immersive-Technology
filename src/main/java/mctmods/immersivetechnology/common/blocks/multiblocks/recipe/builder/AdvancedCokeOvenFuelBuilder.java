package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.AdvancedCokeOvenFuel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class AdvancedCokeOvenFuelBuilder extends IEFinishedRecipe<AdvancedCokeOvenFuelBuilder> {
    private AdvancedCokeOvenFuelBuilder() {
        super(AdvancedCokeOvenFuel.SERIALIZER.get());
        this.maxResultCount = 0;
    }

    public static AdvancedCokeOvenFuelBuilder builder(ItemLike input) {
        return new AdvancedCokeOvenFuelBuilder().addInput(new ItemLike[]{input});
    }

    public static AdvancedCokeOvenFuelBuilder builder(ItemStack input) {
        return new AdvancedCokeOvenFuelBuilder().addInput(new ItemStack[]{input});
    }

    public static AdvancedCokeOvenFuelBuilder builder(TagKey<Item> input) {
        return new AdvancedCokeOvenFuelBuilder().addInput(Ingredient.of(input));
    }
}
