package mctmods.immersivetechnology.common.multiblocks.metal.recipe.builder;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.StyleNOXRecipe;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
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
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class StyleNOXRecipeBuilder implements RecipeBuilder {
    private IngredientWithSize input;
    private TagOutput output;
    private TagKey<Fluid> waterTag;
    private int waterAmount;
    private TagKey<Fluid> co2Tag;
    private int co2Amount;
    private FluidStack steamOutput;
    private int time;
    private int energy;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    private StyleNOXRecipeBuilder() {}

    public static StyleNOXRecipeBuilder builder() { return new StyleNOXRecipeBuilder(); }

    public StyleNOXRecipeBuilder addInput(IngredientWithSize input) {
        this.input = input;
        return this;
    }

    public StyleNOXRecipeBuilder addOutput(ItemStack output) {
        this.output = new TagOutput(output);
        return this;
    }

    public StyleNOXRecipeBuilder addWater(TagKey<Fluid> fluidTag, int amount) {
        this.waterTag = fluidTag;
        this.waterAmount = amount;
        return this;
    }

    public StyleNOXRecipeBuilder addCO2(TagKey<Fluid> fluidTag, int amount) {
        this.co2Tag = fluidTag;
        this.co2Amount = amount;
        return this;
    }

    public StyleNOXRecipeBuilder addSteamOutput(FluidStack output) {
        this.steamOutput = output;
        return this;
    }

    public StyleNOXRecipeBuilder setTime(int time) {
        this.time = time;
        return this;
    }

    public StyleNOXRecipeBuilder setEnergy(int energy) {
        this.energy = energy;
        return this;
    }

    @Override public @NotNull StyleNOXRecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override public @NotNull StyleNOXRecipeBuilder group(@Nullable String group) { return this; }

    @Override public @NotNull Item getResult() { return Items.AIR; }

    @Override public void save(RecipeOutput output, @NotNull ResourceLocation id) {
        Advancement.Builder advancement = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancement::addCriterion);

        StyleNOXRecipe recipe = new StyleNOXRecipe(
                this.input, this.output, this.waterTag, this.waterAmount,
                this.co2Tag, this.co2Amount, this.steamOutput, this.time, this.energy);

        output.accept(id, recipe, advancement.build(id.withPrefix("recipes/")));
    }
}
