package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer.ElectrolyticCrucibleBatteryRecipeSerializer;
import mctmods.immersivetechnology.core.registration.RecipeTypes;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
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
import org.jetbrains.annotations.Nullable;

public class ElectrolyticCrucibleBatteryRecipe extends MultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, ElectrolyticCrucibleBatteryRecipeSerializer> SERIALIZER;
    public static final CachedRecipeList<ElectrolyticCrucibleBatteryRecipe> RECIPES = new CachedRecipeList<>(RecipeTypes.ELECTROLYTIC_CRUCIBLE_BATTERY);

    public final TagKey<Fluid> fluidTag;
    private final int amount;
    @Nullable public final FluidStack fluidOutput0;
    @Nullable public final FluidStack fluidOutput1;
    @Nullable public final FluidStack fluidOutput2;
    public final ItemStack itemOutput;

    private final int time;
    private final int energy;

    public ElectrolyticCrucibleBatteryRecipe(TagKey<Fluid> fluidTag, int amount, @Nullable FluidStack fluidOutput0, @Nullable FluidStack fluidOutput1, @Nullable FluidStack fluidOutput2, ItemStack itemOutput, int energy, int time) {
        super(TagOutput.EMPTY, RecipeTypes.ELECTROLYTIC_CRUCIBLE_BATTERY, time, energy, () -> new MultiblockRecipe.RecipeMultiplier(() -> 1.0, () -> 1.0));
        this.fluidTag = fluidTag;
        this.amount = amount;
        this.fluidOutput0 = fluidOutput0;
        this.fluidOutput1 = fluidOutput1;
        this.fluidOutput2 = fluidOutput2;
        this.itemOutput = itemOutput;
        this.time = time;
        this.energy = energy;
    }

    public TagKey<Fluid> fluidTag() { return fluidTag; }
    public int amount() { return amount; }
    public int getInputAmount() { return amount; }
    public FluidStack fluidOutput0() { return fluidOutput0; }
    public FluidStack fluidOutput1() { return fluidOutput1; }
    public FluidStack fluidOutput2() { return fluidOutput2; }
    public ItemStack itemOutput() { return itemOutput; }
    public int time() { return time; }
    public int energy() { return energy; }

    public boolean matches(FluidStack stack) {
        return stack.is(this.fluidTag);
    }

    public static RecipeHolder<ElectrolyticCrucibleBatteryRecipe> findRecipe(Level level, FluidStack input) { return findRecipe(level, input, null); }

    public static RecipeHolder<ElectrolyticCrucibleBatteryRecipe> findRecipe(Level level, FluidStack input, @Nullable RecipeHolder<ElectrolyticCrucibleBatteryRecipe> hint) {
        if (input.isEmpty() || input.getAmount() <= 0) return null;
        if (hint != null && hint.value().matches(input)) return hint;
        for (RecipeHolder<ElectrolyticCrucibleBatteryRecipe> holder : RECIPES.getRecipes(level)) {
            ElectrolyticCrucibleBatteryRecipe recipe = holder.value();
            if (recipe.matches(input)) return holder;
        }
        return null;
    }

    @Override @NotNull public ItemStack getResultItem(HolderLookup.Provider registryAccess) { return ItemStack.EMPTY; }

    @Override protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override public int getTotalProcessTime() { return time; }

    @Override public int getTotalProcessEnergy() { return energy; }

    @Override public int getMultipleProcessTicks() { return 0; }
}
