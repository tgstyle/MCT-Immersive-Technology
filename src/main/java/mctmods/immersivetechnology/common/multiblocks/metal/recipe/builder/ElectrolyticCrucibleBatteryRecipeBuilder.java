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

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.ElectrolyticCrucibleBatteryRecipe;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class ElectrolyticCrucibleBatteryRecipeBuilder implements RecipeBuilder {
    private TagKey<Fluid> fluidTag;
    private int amount;
    private FluidStack fluidOutput0;
    private FluidStack fluidOutput1;
    private FluidStack fluidOutput2;
    private ItemStack itemOutput = ItemStack.EMPTY;
    private int energy;
    private int time;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    private ElectrolyticCrucibleBatteryRecipeBuilder() {}

    public static ElectrolyticCrucibleBatteryRecipeBuilder builder() {
        return new ElectrolyticCrucibleBatteryRecipeBuilder();
    }

    public ElectrolyticCrucibleBatteryRecipeBuilder addInput(TagKey<Fluid> fluidTag, int amount) {
        this.fluidTag = fluidTag;
        this.amount = amount;
        return this;
    }

    public ElectrolyticCrucibleBatteryRecipeBuilder addOutput0(FluidStack fluidOutput) {
        this.fluidOutput0 = fluidOutput;
        return this;
    }

    public ElectrolyticCrucibleBatteryRecipeBuilder addOutput1(FluidStack fluidOutput) {
        this.fluidOutput1 = fluidOutput;
        return this;
    }

    public ElectrolyticCrucibleBatteryRecipeBuilder addOutput2(FluidStack fluidOutput) {
        this.fluidOutput2 = fluidOutput;
        return this;
    }

    public ElectrolyticCrucibleBatteryRecipeBuilder setEnergy(int energy) {
        this.energy = energy;
        return this;
    }

    public ElectrolyticCrucibleBatteryRecipeBuilder setTime(int time) {
        this.time = time;
        return this;
    }

    public ElectrolyticCrucibleBatteryRecipeBuilder addItemOutput(ItemStack item) {
        this.itemOutput = item;
        return this;
    }

    @Override
    public @NotNull ElectrolyticCrucibleBatteryRecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull ElectrolyticCrucibleBatteryRecipeBuilder group(@Nullable String group) {
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

        ElectrolyticCrucibleBatteryRecipe recipe = new ElectrolyticCrucibleBatteryRecipe(
                this.fluidTag, this.amount, this.fluidOutput0, this.fluidOutput1, this.fluidOutput2, this.itemOutput, this.energy, this.time);

        output.accept(id, recipe, advancement.build(id.withPrefix("recipes/")));
    }
}
