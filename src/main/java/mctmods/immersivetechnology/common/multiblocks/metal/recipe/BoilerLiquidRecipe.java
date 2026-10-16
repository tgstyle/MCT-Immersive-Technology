package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer.BoilerLiquidRecipeSerializer;
import mctmods.immersivetechnology.core.registration.RecipeTypes;

import com.immersiveconvergence.api.capability.HeatCapabilities;
import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.immersiveconvergence.api.crafting.ICTagOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;

public class BoilerLiquidRecipe extends ICMultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, BoilerLiquidRecipeSerializer> SERIALIZER;
    public static final ICCachedRecipeList<BoilerLiquidRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.BOILER_LIQUID);

    public final TagKey<Fluid> fluidTag;
    private final int amount;
    private final double heatPerTick;
    private final double targetHeat;

    public BoilerLiquidRecipe(TagKey<Fluid> fluidTag, int amount, int time, double heatPerTick, double targetHeat) {
        super(ICTagOutput.EMPTY, RecipeTypes.BOILER_LIQUID, time, 0);
        this.fluidTag = fluidTag;
        this.amount = amount;
        this.heatPerTick = heatPerTick;
        this.targetHeat = Math.min(targetHeat, HeatCapabilities.maxHeat());
    }

    public boolean matches(FluidStack stack) {
        return stack.is(this.fluidTag);
    }

    public int getInputAmount() {
        return amount;
    }

    public static BoilerLiquidRecipe findRecipe(Level level, FluidStack input, @Nullable BoilerLiquidRecipe hint) {
        if (input.isEmpty() || input.getAmount() <= 0) return null;
        return RECIPES.find(level, recipe -> recipe.matches(input), hint);
    }

    public static BoilerLiquidRecipe findRecipe(Level level, FluidStack input) {
        return findRecipe(level, input, null);
    }

    public TagKey<Fluid> fluidTag() { return fluidTag; }
    public int amount() { return amount; }
    public double heatPerTick() { return heatPerTick; }
    public double targetHeat() { return targetHeat; }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }

    public double getHeatPerTick() { return heatPerTick; }

    public double getTargetHeat() { return targetHeat; }
}
