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

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.HeatExchangerRecipe;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class HeatExchangerRecipeBuilder implements RecipeBuilder {
    private TagKey<Fluid> input0Tag;
    private int input0Amount;
    private TagKey<Fluid> input1Tag;
    private int input1Amount;
    private FluidStack output0;
    private FluidStack output1;
    private int energy;
    private int time;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    private HeatExchangerRecipeBuilder() {}

    public static HeatExchangerRecipeBuilder builder() {
        return new HeatExchangerRecipeBuilder();
    }

    public HeatExchangerRecipeBuilder addInput0(TagKey<Fluid> fluidTag, int amount) {
        this.input0Tag = fluidTag;
        this.input0Amount = amount;
        return this;
    }

    public HeatExchangerRecipeBuilder addInput1(TagKey<Fluid> fluidTag, int amount) {
        this.input1Tag = fluidTag;
        this.input1Amount = amount;
        return this;
    }

    public HeatExchangerRecipeBuilder addOutput0(FluidStack output) {
        this.output0 = output;
        return this;
    }

    public HeatExchangerRecipeBuilder addOutput1(FluidStack output) {
        this.output1 = output;
        return this;
    }

    public HeatExchangerRecipeBuilder setEnergy(int energy) {
        this.energy = energy;
        return this;
    }

    public HeatExchangerRecipeBuilder setTime(int time) {
        this.time = time;
        return this;
    }

    @Override
    public @NotNull HeatExchangerRecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull HeatExchangerRecipeBuilder group(@Nullable String group) {
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

        HeatExchangerRecipe recipe = new HeatExchangerRecipe(
                this.input0Tag, this.input0Amount, this.input1Tag, this.input1Amount,
                this.output0, this.output1, this.energy, this.time);

        output.accept(id, recipe, advancement.build(id.withPrefix("recipes/")));
    }
}
