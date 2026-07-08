package mctmods.immersivetechnology.common.multiblocks.metal.recipe.builder;

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
import net.minecraft.world.level.material.Fluid;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerLiquidRecipe;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class BoilerLiquidRecipeBuilder implements RecipeBuilder {
    private TagKey<Fluid> fluidTag;
    private int amount;
    private int time;
    private double heatPerTick;
    private double targetHeat;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    private BoilerLiquidRecipeBuilder() {}

    public static BoilerLiquidRecipeBuilder builder() {
        return new BoilerLiquidRecipeBuilder();
    }

    public BoilerLiquidRecipeBuilder addInput(TagKey<Fluid> fluidTag, int amount) {
        this.fluidTag = fluidTag;
        this.amount = amount;
        return this;
    }

    public BoilerLiquidRecipeBuilder setTime(int time) {
        this.time = time;
        return this;
    }

    public BoilerLiquidRecipeBuilder setHeatPerTick(double heatPerTick) {
        this.heatPerTick = heatPerTick;
        return this;
    }

    public BoilerLiquidRecipeBuilder setTargetHeat(double targetHeat) {
        this.targetHeat = targetHeat;
        return this;
    }

    @Override
    public @NotNull BoilerLiquidRecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull BoilerLiquidRecipeBuilder group(@Nullable String group) {
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

        BoilerLiquidRecipe recipe = new BoilerLiquidRecipe(
                this.fluidTag, this.amount, this.time, this.heatPerTick, this.targetHeat);

        output.accept(id, recipe, advancement.build(id.withPrefix("recipes/")));
    }
}
