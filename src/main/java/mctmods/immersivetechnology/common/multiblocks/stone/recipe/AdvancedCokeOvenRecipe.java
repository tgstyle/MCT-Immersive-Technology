package mctmods.immersivetechnology.common.multiblocks.stone.recipe;

import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICCokeOvenRecipe;
import com.immersiveconvergence.api.crafting.ICIngredientWithSize;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.immersiveconvergence.api.crafting.ICTagOutput;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.serializer.AdvancedCokeOvenRecipeSerializer;
import mctmods.immersivetechnology.core.registration.RecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AdvancedCokeOvenRecipe extends ICMultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, AdvancedCokeOvenRecipeSerializer> SERIALIZER;
    public static final ICCachedRecipeList<AdvancedCokeOvenRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.ADVANCED_COKE_OVEN);

    private static final List<RecipeHolder<AdvancedCokeOvenRecipe>> IE_COPIED_RECIPES = new ArrayList<>();

    public final ICIngredientWithSize input;
    public final ICTagOutput itemOutput;
    public final int time;
    public final int creosoteOutput;

    private static int copiedAtReload = -1;

    public AdvancedCokeOvenRecipe(ICIngredientWithSize input, ICTagOutput itemOutput, int time, int creosoteOutput) {
        super(itemOutput, RecipeTypes.ADVANCED_COKE_OVEN, time, 0);
        this.input = input;
        this.itemOutput = itemOutput;
        this.time = time;
        this.creosoteOutput = creosoteOutput;
        setInputListWithSizes(List.of(input));
        this.fluidOutputList = List.of(new FluidStack(ICCokeOvenRecipe.creosote(), creosoteOutput));
    }

    public boolean matches(ItemStack stack) {
        return this.input.testIgnoringSize(stack);
    }

    @Override protected ICRecipeSerializer<?> getICSerializer() {
        return SERIALIZER.get();
    }

    @Override @Nonnull public ItemStack getResultItem(HolderLookup.Provider access) {
        return this.itemOutput.get();
    }

    @Override public int getMultipleProcessTicks() { return 0; }

    public ICIngredientWithSize input() { return input; }
    public ICTagOutput itemOutput() { return itemOutput; }
    public int time() { return time; }
    public int creosoteOutput() { return creosoteOutput; }

    @Nullable public static RecipeHolder<AdvancedCokeOvenRecipe> findRecipe(Level level, ItemStack input, @Nullable RecipeHolder<AdvancedCokeOvenRecipe> hint) {
        if (input.isEmpty()) { return null; }
        if (hint != null && hint.value().matches(input)) { return hint; }

        copyIECokeOvenRecipes(level);

        for (RecipeHolder<AdvancedCokeOvenRecipe> holder : RECIPES.getRecipes(level)) {
            if (holder.value().matches(input)) { return holder; }
        }

        for (RecipeHolder<AdvancedCokeOvenRecipe> holder : IE_COPIED_RECIPES) {
            if (holder.value().matches(input)) { return holder; }
        }
        return null;
    }

    @Nullable public static AdvancedCokeOvenRecipe getById(Level level, ResourceLocation id) {
        copyIECokeOvenRecipes(level);

        for (RecipeHolder<AdvancedCokeOvenRecipe> holder : RECIPES.getRecipes(level)) {
            if (holder.id().equals(id)) { return holder.value(); }
        }
        for (RecipeHolder<AdvancedCokeOvenRecipe> holder : IE_COPIED_RECIPES) {
            if (holder.id().equals(id)) { return holder.value(); }
        }
        return null;
    }

    public static List<AdvancedCokeOvenRecipe> getAllRecipes(Level level) {
        copyIECokeOvenRecipes(level);
        List<AdvancedCokeOvenRecipe> all = new ArrayList<>();
        RECIPES.getRecipes(level).stream().map(RecipeHolder::value).forEach(all::add);
        IE_COPIED_RECIPES.stream().map(RecipeHolder::value).forEach(all::add);
        return all;
    }

    public static void copyIECokeOvenRecipes(Level level) {
        if (copiedAtReload == ICCachedRecipeList.reloadCount() && !IE_COPIED_RECIPES.isEmpty()) { return; }
        IE_COPIED_RECIPES.clear();
        copiedAtReload = ICCachedRecipeList.reloadCount();
        for (ICCokeOvenRecipe r : ICCokeOvenRecipe.getRecipes(level)) {
            ItemStack[] ieStacks = r.input().getMatchingStacks();
            boolean alreadyHas = false;
            if (ieStacks.length > 0) {
                for (RecipeHolder<AdvancedCokeOvenRecipe> h : RECIPES.getRecipes(level)) { if (h.value().matches(ieStacks[0])) { alreadyHas = true; break; } }
            }
            if (!alreadyHas) {
                AdvancedCokeOvenRecipe copied = new AdvancedCokeOvenRecipe(r.input(), r.output(), r.time(), r.creosoteOutput());
                ResourceLocation copiedId = ResourceLocation.fromNamespaceAndPath("immersivetechnology", "advanced_coke_oven/copied/" + r.id().getNamespace() + "/" + r.id().getPath());
                IE_COPIED_RECIPES.add(new RecipeHolder<>(copiedId, copied));
            }
        }
    }
}
