package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer.MeltingRecipeSerializer;
import mctmods.immersivetechnology.core.registration.ITRecipeTypes;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
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

public class MeltingRecipe extends MultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, MeltingRecipeSerializer> SERIALIZER;
    public static final CachedRecipeList<MeltingRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.MELTING);

    private final ResourceLocation id;
    public final TagKey<Fluid> inputTag;
    private final int inputAmount;
    @Nullable public final FluidStack fluidOutput;
    private final int time;
    public final double requiredTemp;

    public MeltingRecipe(ResourceLocation id, TagKey<Fluid> inputTag, int inputAmount, @Nullable FluidStack fluidOutput, int time, double requiredTemp) {
        super(TagOutput.EMPTY, ITRecipeTypes.MELTING, time, 0, () -> new MultiblockRecipe.RecipeMultiplier(() -> 1.0, () -> 1.0));
        this.id = id;
        this.inputTag = inputTag;
        this.inputAmount = inputAmount;
        this.fluidOutput = fluidOutput;
        this.time = time;
        this.requiredTemp = requiredTemp;
    }

    public MeltingRecipe(TagKey<Fluid> inputTag, int inputAmount, @Nullable FluidStack fluidOutput, int time, double requiredTemp) {
        this(ResourceLocation.fromNamespaceAndPath("immersivetechnology", "codec_generated"), inputTag, inputAmount, fluidOutput, time, requiredTemp);
    }

    public ResourceLocation getId() { return id; }
    public TagKey<Fluid> inputTag() { return inputTag; }
    public int inputAmount() { return inputAmount; }
    public int getInputAmount() { return inputAmount; }
    public FluidStack fluidOutput() { return fluidOutput; }

    public boolean matches(FluidStack stack) {
        return stack != null && stack.is(this.inputTag);
    }

    @Nullable public static MeltingRecipe findRecipe(Level level, FluidStack fluid) { return findRecipe(level, fluid, null); }

    @Nullable public static MeltingRecipe findRecipe(Level level, FluidStack fluid, @Nullable MeltingRecipe hint) {
        if (fluid == null || fluid.isEmpty()) { return null; }
        if (hint != null && hint.matches(fluid) && fluid.getAmount() >= hint.getInputAmount()) { return hint; }
        for (var holder : RECIPES.getRecipes(level)) {
            MeltingRecipe recipe = holder.value();
            if (recipe.matches(fluid) && fluid.getAmount() >= recipe.getInputAmount()) { return recipe; }
        }
        return null;
    }

    @Nullable public static RecipeHolder<MeltingRecipe> findRecipeHolder(Level level, FluidStack fluid, @Nullable RecipeHolder<MeltingRecipe> hint) {
        if (fluid == null || fluid.isEmpty()) { return null; }
        if (hint != null && hint.value().matches(fluid) && fluid.getAmount() >= hint.value().getInputAmount()) { return hint; }
        for (RecipeHolder<MeltingRecipe> holder : RECIPES.getRecipes(level)) {
            MeltingRecipe recipe = holder.value();
            if (recipe.matches(fluid) && fluid.getAmount() >= recipe.getInputAmount()) { return holder; }
        }
        return null;
    }

    @Override @NotNull public ItemStack getResultItem(HolderLookup.Provider registryAccess) { return ItemStack.EMPTY; }

    @Override protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override public int getTotalProcessTime() { return time; }

    @Override public int getTotalProcessEnergy() { return 0; }

    @Override public int getMultipleProcessTicks() { return -1; }
}
