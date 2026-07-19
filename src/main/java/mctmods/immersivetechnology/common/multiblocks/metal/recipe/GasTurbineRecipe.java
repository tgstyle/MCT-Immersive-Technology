package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer.GasTurbineRecipeSerializer;
import mctmods.immersivetechnology.core.registration.RecipeTypes;
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

public class GasTurbineRecipe extends MultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, GasTurbineRecipeSerializer> SERIALIZER;
    public static final CachedRecipeList<GasTurbineRecipe> RECIPES = new CachedRecipeList<>(RecipeTypes.GAS_TURBINE);

    public final TagKey<Fluid> fluidTag;
    private final int amount;
    @Nullable public final FluidStack fluidOutput;
    public final float torque;
    private final int time;

    public GasTurbineRecipe(TagKey<Fluid> fluidTag, int amount, @Nullable FluidStack fluidOutput, int time, float torque) {
        super(TagOutput.EMPTY, RecipeTypes.GAS_TURBINE, time, 0, () -> new MultiblockRecipe.RecipeMultiplier(() -> 1.0, () -> 1.0));
        this.fluidTag = fluidTag;
        this.amount = amount;
        this.fluidOutput = fluidOutput;
        this.time = time;
        this.torque = torque;
    }

    public TagKey<Fluid> fluidTag() { return fluidTag; }
    public int amount() { return amount; }
    public int getInputAmount() { return amount; }
    public FluidStack fluidOutput() { return fluidOutput; }
    public float torque() { return torque; }
    public int time() { return time; }

    public boolean matches(FluidStack fluid) {
        return fluid.is(this.fluidTag);
    }

    public static GasTurbineRecipe findRecipe(Level level, FluidStack fluid, @Nullable GasTurbineRecipe hint) {
        if (hint != null && hint.matches(fluid)) return hint;
        for (RecipeHolder<GasTurbineRecipe> holder : RECIPES.getRecipes(level)) {
            GasTurbineRecipe recipe = holder.value();
            if (recipe.matches(fluid)) return recipe;
        }
        return null;
    }

    @Override protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override @NotNull public ItemStack getResultItem(@NotNull HolderLookup.Provider registryAccess) { return ItemStack.EMPTY; }

    @Override public int getTotalProcessTime() { return time; }

    @Override public int getTotalProcessEnergy() { return 0; }

    @Override public int getMultipleProcessTicks() { return 0; }
}
