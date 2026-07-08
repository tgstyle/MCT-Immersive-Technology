package mctmods.immersivetechnology.common.multiblocks.stone.recipe.builder;

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

import mctmods.immersivetechnology.common.multiblocks.stone.recipe.CoolingTowerRecipe;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class CoolingTowerRecipeBuilder implements RecipeBuilder {
    private TagKey<Fluid> inputTag0;
    private int amount0;
    private TagKey<Fluid> inputTag1;
    private int amount1;
    private FluidStack fluidOutput0;
    private FluidStack fluidOutput1;
    private FluidStack fluidOutput2;
    private int time;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    private CoolingTowerRecipeBuilder() {}

    public static CoolingTowerRecipeBuilder builder() {
        return new CoolingTowerRecipeBuilder();
    }

    public CoolingTowerRecipeBuilder addInput(TagKey<Fluid> fluidTag, int amount) {
        if (this.inputTag0 == null) {
            this.inputTag0 = fluidTag;
            this.amount0 = amount;
        } else {
            this.inputTag1 = fluidTag;
            this.amount1 = amount;
        }
        return this;
    }

    public CoolingTowerRecipeBuilder addOutput(FluidStack fluidStack) {
        if (this.fluidOutput0 == null) {
            this.fluidOutput0 = fluidStack;
        } else if (this.fluidOutput1 == null) {
            this.fluidOutput1 = fluidStack;
        } else {
            this.fluidOutput2 = fluidStack;
        }
        return this;
    }

    public CoolingTowerRecipeBuilder addOutput(Fluid fluid, int amount) {
        return addOutput(new FluidStack(fluid, amount));
    }

    public CoolingTowerRecipeBuilder setTime(int time) {
        this.time = time;
        return this;
    }

    @Override
    public @NotNull CoolingTowerRecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull CoolingTowerRecipeBuilder group(@Nullable String group) {
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

        CoolingTowerRecipe recipe = new CoolingTowerRecipe(
                this.fluidOutput0, this.fluidOutput1, this.fluidOutput2,
                this.inputTag0, this.amount0, this.inputTag1, this.amount1, this.time);

        output.accept(id, recipe, advancement.build(id.withPrefix("recipes/")));
    }
}
