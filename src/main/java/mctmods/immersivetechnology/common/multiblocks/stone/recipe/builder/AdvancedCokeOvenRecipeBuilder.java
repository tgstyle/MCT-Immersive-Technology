package mctmods.immersivetechnology.common.multiblocks.stone.recipe.builder;

import com.immersiveconvergence.api.crafting.ICIngredientWithSize;
import com.immersiveconvergence.api.crafting.ICTagOutput;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import mctmods.immersivetechnology.common.multiblocks.stone.recipe.AdvancedCokeOvenRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class AdvancedCokeOvenRecipeBuilder implements RecipeBuilder {
    private ICIngredientWithSize input;
    private ICTagOutput itemOutput;
    private int time;
    private int creosoteOutput;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    private AdvancedCokeOvenRecipeBuilder() {}

    public static AdvancedCokeOvenRecipeBuilder builder(Item input) {
        return new AdvancedCokeOvenRecipeBuilder().addInput(input);
    }

    public static AdvancedCokeOvenRecipeBuilder builder(TagKey<Item> input, int count) {
        return new AdvancedCokeOvenRecipeBuilder().addInput(input, count);
    }

    public AdvancedCokeOvenRecipeBuilder addInput(Item input) {
        this.input = new ICIngredientWithSize(Ingredient.of(input));
        return this;
    }

    public AdvancedCokeOvenRecipeBuilder addInput(TagKey<Item> input, int count) {
        this.input = new ICIngredientWithSize(Ingredient.of(input), count);
        return this;
    }

    public AdvancedCokeOvenRecipeBuilder addOutput(Item output) {
        this.itemOutput = new ICTagOutput(output);
        return this;
    }

    @SuppressWarnings("unused")
    public AdvancedCokeOvenRecipeBuilder addOutput(ItemStack output) {
        this.itemOutput = new ICTagOutput(output);
        return this;
    }

    public AdvancedCokeOvenRecipeBuilder addOutput(TagKey<Item> output, int count) {
        this.itemOutput = new ICTagOutput(output, count);
        return this;
    }

    public AdvancedCokeOvenRecipeBuilder setCreosote(int amount) {
        this.creosoteOutput = amount;
        return this;
    }

    public AdvancedCokeOvenRecipeBuilder setTime(int time) {
        this.time = time;
        return this;
    }

    @Override
    public @Nonnull AdvancedCokeOvenRecipeBuilder unlockedBy(@Nonnull String name, @Nonnull Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @Nonnull AdvancedCokeOvenRecipeBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public @Nonnull Item getResult() {
        return Items.AIR;
    }

    @Override
    public void save(RecipeOutput output, @Nonnull ResourceLocation id) {
        Advancement.Builder advancement = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancement::addCriterion);

        AdvancedCokeOvenRecipe recipe = new AdvancedCokeOvenRecipe(
                this.input, this.itemOutput, this.time, this.creosoteOutput);

        output.accept(id, recipe, advancement.build(id.withPrefix("recipes/")));
    }
}
