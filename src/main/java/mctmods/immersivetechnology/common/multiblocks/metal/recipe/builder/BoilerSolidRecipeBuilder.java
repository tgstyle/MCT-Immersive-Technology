package mctmods.immersivetechnology.common.multiblocks.metal.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerSolidRecipe;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class BoilerSolidRecipeBuilder implements RecipeBuilder {
    private IngredientWithSize input;
    private double heatPerTick;
    private double targetHeat;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    private BoilerSolidRecipeBuilder() {}

    public static BoilerSolidRecipeBuilder builder() {
        return new BoilerSolidRecipeBuilder();
    }

    public BoilerSolidRecipeBuilder addInput(TagKey<Item> itemTag, int amount) {
        this.input = new IngredientWithSize(Ingredient.of(itemTag), amount);
        return this;
    }

    public BoilerSolidRecipeBuilder addInput(IngredientWithSize ingredient) {
        this.input = ingredient;
        return this;
    }

    public BoilerSolidRecipeBuilder setHeatPerTick(double heatPerTick) {
        this.heatPerTick = heatPerTick;
        return this;
    }

    public BoilerSolidRecipeBuilder setTargetHeat(double targetHeat) {
        this.targetHeat = targetHeat;
        return this;
    }

    @Override
    public @NotNull BoilerSolidRecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull BoilerSolidRecipeBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return Items.AIR;
    }

    @Override
    public void save(RecipeOutput output, @NotNull ResourceLocation id) {
        Advancement.Builder advancement = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancement::addCriterion);

        BoilerSolidRecipe recipe = new BoilerSolidRecipe(
                this.input, this.heatPerTick, this.targetHeat);

        output.accept(id, recipe, advancement.build(id.withPrefix("recipes/")));
    }
}
