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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.DistillerRecipe;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class DistillerRecipeBuilder implements RecipeBuilder {
    private TagKey<Fluid> fluidTag;
    private int amount;
    private FluidStack fluidOutput;
    private ItemStack itemOutput = ItemStack.EMPTY;
    private float chance;
    private int time;
    private int energy;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    private DistillerRecipeBuilder() {}

    public static DistillerRecipeBuilder builder() {
        return new DistillerRecipeBuilder();
    }

    public DistillerRecipeBuilder addInput(TagKey<Fluid> fluidTag, int amount) {
        this.fluidTag = fluidTag;
        this.amount = amount;
        return this;
    }

    public DistillerRecipeBuilder addOutput(FluidStack fluidOutput) {
        this.fluidOutput = fluidOutput;
        return this;
    }

    public DistillerRecipeBuilder setTime(int time) {
        this.time = time;
        return this;
    }

    public DistillerRecipeBuilder setEnergy(int energy) {
        this.energy = energy;
        return this;
    }

    public DistillerRecipeBuilder addItemOutput(ItemStack item, float chance) {
        this.itemOutput = item;
        this.chance = chance;
        return this;
    }

    @Override
    public @NotNull DistillerRecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull DistillerRecipeBuilder group(@Nullable String group) {
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

        DistillerRecipe recipe = new DistillerRecipe(
                this.fluidTag, this.amount, this.fluidOutput, this.itemOutput, this.chance, this.time, this.energy);

        output.accept(id, recipe, advancement.build(id.withPrefix("recipes/")));
    }
}
