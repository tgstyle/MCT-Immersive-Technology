package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import com.immersiveconvergence.api.HeatCapabilities;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer.BoilerTankRecipeSerializer;
import mctmods.immersivetechnology.core.registration.ITRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

public class BoilerTankRecipe extends MultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, BoilerTankRecipeSerializer> SERIALIZER;
    public static final CachedRecipeList<BoilerTankRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.BOILER_TANK);

    public final TagKey<Fluid> fluidTag;
    private final int amount;
    public final FluidStack output;
    public final double requiredHeat;
    private final int time;

    public BoilerTankRecipe(TagKey<Fluid> fluidTag, int amount, FluidStack output, int time, double requiredHeat) {
        super(TagOutput.EMPTY, ITRecipeTypes.BOILER_TANK, time, 0, () -> new MultiblockRecipe.RecipeMultiplier(() -> 1.0, () -> 1.0));
        this.fluidTag = fluidTag;
        this.amount = amount;
        this.output = output;
        this.requiredHeat = Math.min(requiredHeat, HeatCapabilities.MAX_HEAT);
        this.time = time;
    }

    public TagKey<Fluid> fluidTag() { return fluidTag; }
    public int amount() { return amount; }
    public FluidStack output() { return output; }
    public int time() { return time; }
    public double requiredHeat() { return requiredHeat; }

    public boolean matches(FluidStack stack) {
        return stack.is(this.fluidTag);
    }

    public int getInputAmount() {
        return amount;
    }

    public static BoilerTankRecipe findRecipe(Level level, FluidStack input) {
        if (input.isEmpty() || input.getAmount() <= 0) return null;
        for (RecipeHolder<BoilerTankRecipe> holder : RECIPES.getRecipes(level)) {
            BoilerTankRecipe recipe = holder.value();
            if (recipe.matches(input)) return recipe;
        }
        return null;
    }

    @Override public @NotNull ItemStack getResultItem(HolderLookup.Provider registryAccess) { return ItemStack.EMPTY; }

    @Override protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override public int getTotalProcessTime() { return time; }

    @Override public int getTotalProcessEnergy() { return 0; }

    @Override public int getMultipleProcessTicks() { return 0; }
}
