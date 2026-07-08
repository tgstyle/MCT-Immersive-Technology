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
import net.neoforged.neoforge.fluids.FluidStack;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.SolarTowerRecipe;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class SolarTowerRecipeBuilder implements RecipeBuilder {
    private TagKey<Fluid> inputTag;
    private int inputAmount;
    private FluidStack fluidOutput;
    private int time;
    private double requiredTemp;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    private SolarTowerRecipeBuilder() {}

    public static SolarTowerRecipeBuilder builder() {
        return new SolarTowerRecipeBuilder();
    }

    public SolarTowerRecipeBuilder addInput(TagKey<Fluid> tag, int amount) {
        this.inputTag = tag;
        this.inputAmount = amount;
        return this;
    }

    public SolarTowerRecipeBuilder addOutput(FluidStack output) {
        this.fluidOutput = output;
        return this;
    }

    public SolarTowerRecipeBuilder setTime(int time) {
        this.time = time;
        return this;
    }

    public SolarTowerRecipeBuilder setRequiredTemp(double temp) {
        this.requiredTemp = temp;
        return this;
    }

    @Override
    public @NotNull SolarTowerRecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull SolarTowerRecipeBuilder group(@Nullable String group) {
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

        SolarTowerRecipe recipe = new SolarTowerRecipe(
                this.inputTag, this.inputAmount, this.fluidOutput, this.time, this.requiredTemp);

        output.accept(id, recipe, advancement.build(id.withPrefix("recipes/")));
    }
}
