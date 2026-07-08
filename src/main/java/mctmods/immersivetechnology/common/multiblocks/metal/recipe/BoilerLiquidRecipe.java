package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import com.immersiveconvergence.api.HeatCapabilities;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer.BoilerLiquidRecipeSerializer;
import mctmods.immersivetechnology.core.registration.ITRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoilerLiquidRecipe extends MultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, BoilerLiquidRecipeSerializer> SERIALIZER;
    public static final CachedRecipeList<BoilerLiquidRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.BOILER_LIQUID);

    public final TagKey<Fluid> fluidTag;
    private final int amount;
    private final int time;
    private final double heatPerTick;
    private final double targetHeat;
    private final Lazy<Integer> totalProcessTime;

    public BoilerLiquidRecipe(TagKey<Fluid> fluidTag, int amount, int time, double heatPerTick, double targetHeat) {
        super(TagOutput.EMPTY, ITRecipeTypes.BOILER_LIQUID, time, 0, () -> new MultiblockRecipe.RecipeMultiplier(() -> 1.0, () -> 1.0));
        this.fluidTag = fluidTag;
        this.amount = amount;
        this.time = time;
        this.heatPerTick = heatPerTick;
        this.targetHeat = Math.min(targetHeat, HeatCapabilities.MAX_HEAT);
        this.totalProcessTime = Lazy.of(() -> this.time);
    }

    public boolean matches(FluidStack stack) {
        return stack.is(this.fluidTag);
    }

    public int getInputAmount() {
        return amount;
    }

    public static BoilerLiquidRecipe findRecipe(Level level, FluidStack input, @Nullable BoilerLiquidRecipe hint) {
        if (input.isEmpty() || input.getAmount() <= 0) return null;
        if (hint != null && hint.matches(input)) return hint;

        for (RecipeHolder<BoilerLiquidRecipe> holder : RECIPES.getRecipes(level)) {
            BoilerLiquidRecipe recipe = holder.value();
            if (recipe.matches(input)) return recipe;
        }
        return null;
    }

    public static BoilerLiquidRecipe findRecipe(Level level, FluidStack input) {
        return findRecipe(level, input, null);
    }

    public TagKey<Fluid> fluidTag() { return fluidTag; }
    public int amount() { return amount; }
    public int time() { return time; }
    public double heatPerTick() { return heatPerTick; }
    public double targetHeat() { return targetHeat; }

    @Override public @NotNull ItemStack getResultItem(HolderLookup.Provider registryAccess) { return ItemStack.EMPTY; }

    @Override protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override public int getTotalProcessTime() { return totalProcessTime.get(); }

    @Override public int getTotalProcessEnergy() { return 0; }

    @Override public int getMultipleProcessTicks() { return 0; }

    public double getHeatPerTick() { return heatPerTick; }

    public double getTargetHeat() { return targetHeat; }
}
