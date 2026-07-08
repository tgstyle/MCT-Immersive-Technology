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

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerTankRecipe;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class BoilerTankRecipeBuilder implements RecipeBuilder {
    private TagKey<Fluid> fluidTag;
    private int amount;
    private FluidStack output;
    private int time;
    private double requiredHeat;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    private BoilerTankRecipeBuilder() {}

    public static BoilerTankRecipeBuilder builder() {
        return new BoilerTankRecipeBuilder();
    }

    public BoilerTankRecipeBuilder addInput(TagKey<Fluid> fluidTag, int amount) {
        this.fluidTag = fluidTag;
        this.amount = amount;
        return this;
    }

    public BoilerTankRecipeBuilder addOutput(Fluid outputFluid, int outputAmount) {
        this.output = new FluidStack(outputFluid, outputAmount);
        return this;
    }

    public BoilerTankRecipeBuilder setTime(int time) {
        this.time = time;
        return this;
    }

    public BoilerTankRecipeBuilder setRequiredHeat(double requiredHeat) {
        this.requiredHeat = requiredHeat;
        return this;
    }

    @Override
    public @NotNull BoilerTankRecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull BoilerTankRecipeBuilder group(@Nullable String group) {
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

        BoilerTankRecipe recipe = new BoilerTankRecipe(
                this.fluidTag, this.amount, this.output, this.time, this.requiredHeat);

        output.accept(id, recipe, advancement.build(id.withPrefix("recipes/")));
    }
}
