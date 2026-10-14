package mctmods.immersivetechnology.common.multiblocks.metal.recipe.builder;

import com.immersiveconvergence.api.crafting.ICIngredientWithSize;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class BoilerSolidRecipeBuilder implements RecipeBuilder {
    private ICIngredientWithSize input;
    private double heatPerTick;
    private double targetHeat;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    private BoilerSolidRecipeBuilder() {}

    public static BoilerSolidRecipeBuilder builder() {
        return new BoilerSolidRecipeBuilder();
    }

    public BoilerSolidRecipeBuilder addInput(TagKey<Item> itemTag, int amount) {
        this.input = new ICIngredientWithSize(Ingredient.of(itemTag), amount);
        return this;
    }

    public BoilerSolidRecipeBuilder addInput(ICIngredientWithSize ingredient) {
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
    public @Nonnull BoilerSolidRecipeBuilder unlockedBy(@Nonnull String name, @Nonnull Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @Nonnull BoilerSolidRecipeBuilder group(@Nullable String group) {
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

        BoilerSolidRecipe recipe = new BoilerSolidRecipe(
                this.input, this.heatPerTick, this.targetHeat);

        output.accept(id, recipe, advancement.build(id.withPrefix("recipes/")));
    }
}
