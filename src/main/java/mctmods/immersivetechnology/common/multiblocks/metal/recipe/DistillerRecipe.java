package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer.DistillerRecipeSerializer;
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
import org.jetbrains.annotations.Nullable;

public class DistillerRecipe extends MultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, DistillerRecipeSerializer> SERIALIZER;
    public static final CachedRecipeList<DistillerRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.DISTILLER);

    public final TagKey<Fluid> fluidTag;
    private final int amount;
    @Nullable public final FluidStack fluidOutput;
    public final ItemStack itemOutput;
    public final float chance;
    private final int time;
    private final int energy;

    public DistillerRecipe(TagKey<Fluid> fluidTag, int amount, @Nullable FluidStack fluidOutput, ItemStack itemOutput, float chance, int time, int energy) {
        super(TagOutput.EMPTY, ITRecipeTypes.DISTILLER, time, energy, () -> new MultiblockRecipe.RecipeMultiplier(() -> 1.0, () -> 1.0));
        this.fluidTag = fluidTag;
        this.amount = amount;
        this.fluidOutput = fluidOutput;
        this.itemOutput = itemOutput;
        this.chance = chance;
        this.time = time;
        this.energy = energy;
    }

    public TagKey<Fluid> fluidTag() { return fluidTag; }
    public int amount() { return amount; }
    public FluidStack fluidOutput() { return fluidOutput; }
    public ItemStack itemOutput() { return itemOutput; }
    public float chance() { return chance; }
    public int time() { return time; }
    public int energy() { return energy; }

    public boolean matches(FluidStack stack) { return stack.is(this.fluidTag); }

    public int getInputAmount() { return amount; }

    public static RecipeHolder<DistillerRecipe> findRecipe(Level level, FluidStack inputFluid) {
        if (inputFluid.isEmpty() || inputFluid.getAmount() <= 0) { return null; }
        for (RecipeHolder<DistillerRecipe> holder : RECIPES.getRecipes(level)) {
            if (holder.value().matches(inputFluid)) { return holder; }
        }
        return null;
    }

    @Override @NotNull public ItemStack getResultItem(HolderLookup.Provider registryAccess) { return ItemStack.EMPTY; }

    @Override protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override public int getTotalProcessTime() { return time; }

    @Override public int getTotalProcessEnergy() { return energy; }

    @Override public int getMultipleProcessTicks() { return 0; }
}
