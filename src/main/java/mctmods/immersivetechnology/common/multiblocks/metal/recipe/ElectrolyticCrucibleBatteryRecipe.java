package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer.ElectrolyticCrucibleBatteryRecipeSerializer;
import mctmods.immersivetechnology.core.registration.RecipeTypes;

import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.immersiveconvergence.api.crafting.ICTagOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;

public class ElectrolyticCrucibleBatteryRecipe extends ICMultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, ElectrolyticCrucibleBatteryRecipeSerializer> SERIALIZER;
    public static final ICCachedRecipeList<ElectrolyticCrucibleBatteryRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.ELECTROLYTIC_CRUCIBLE_BATTERY);

    public final TagKey<Fluid> fluidTag;
    private final int amount;
    @Nullable public final FluidStack fluidOutput0;
    @Nullable public final FluidStack fluidOutput1;
    @Nullable public final FluidStack fluidOutput2;
    public final ItemStack itemOutput;

    public ElectrolyticCrucibleBatteryRecipe(TagKey<Fluid> fluidTag, int amount, @Nullable FluidStack fluidOutput0, @Nullable FluidStack fluidOutput1, @Nullable FluidStack fluidOutput2, ItemStack itemOutput, int energy, int time) {
        super(ICTagOutput.EMPTY, RecipeTypes.ELECTROLYTIC_CRUCIBLE_BATTERY, time, energy);
        this.fluidTag = fluidTag;
        this.amount = amount;
        this.fluidOutput0 = fluidOutput0;
        this.fluidOutput1 = fluidOutput1;
        this.fluidOutput2 = fluidOutput2;
        this.itemOutput = itemOutput;
    }

    public TagKey<Fluid> fluidTag() { return fluidTag; }
    public int amount() { return amount; }
    public int getInputAmount() { return amount; }
    public FluidStack fluidOutput0() { return fluidOutput0; }
    public FluidStack fluidOutput1() { return fluidOutput1; }
    public FluidStack fluidOutput2() { return fluidOutput2; }
    public ItemStack itemOutput() { return itemOutput; }

    public boolean matches(FluidStack stack) {
        return stack.is(this.fluidTag);
    }

    public static RecipeHolder<ElectrolyticCrucibleBatteryRecipe> findRecipe(Level level, FluidStack input) { return findRecipe(level, input, null); }

    public static RecipeHolder<ElectrolyticCrucibleBatteryRecipe> findRecipe(Level level, FluidStack input, @Nullable RecipeHolder<ElectrolyticCrucibleBatteryRecipe> hint) {
        if (input.isEmpty() || input.getAmount() <= 0) return null;
        return RECIPES.findHolder(level, recipe -> recipe.matches(input), hint);
    }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }
}
