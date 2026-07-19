package mctmods.immersivetechnology.common.multiblocks.stone.recipe;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import blusunrize.immersiveengineering.common.register.IEFluids;
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
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AdvancedCokeOvenRecipe extends MultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, AdvancedCokeOvenRecipeSerializer> SERIALIZER;
    public static final CachedRecipeList<AdvancedCokeOvenRecipe> RECIPES = new CachedRecipeList<>(RecipeTypes.ADVANCED_COKE_OVEN);

    private static final List<RecipeHolder<AdvancedCokeOvenRecipe>> IE_COPIED_RECIPES = new ArrayList<>();

    public final IngredientWithSize input;
    public final TagOutput itemOutput;
    public final int time;
    public final int creosoteOutput;

    private static int copiedAtReload = -1;

    public AdvancedCokeOvenRecipe(IngredientWithSize input, TagOutput itemOutput, int time, int creosoteOutput) {
        super(itemOutput, RecipeTypes.ADVANCED_COKE_OVEN, time, 0, () -> new MultiblockRecipe.RecipeMultiplier(() -> 1.0, () -> 1.0));
        this.input = input;
        this.itemOutput = itemOutput;
        this.time = time;
        this.creosoteOutput = creosoteOutput;
        setInputListWithSizes(List.of(input));
        this.fluidOutputList = List.of(new FluidStack(IEFluids.CREOSOTE.getStill(), creosoteOutput));
    }

    public boolean matches(ItemStack stack) {
        return this.input.testIgnoringSize(stack);
    }

    @Override protected IERecipeSerializer<?> getIESerializer() {
        return SERIALIZER.get();
    }

    @Override @NotNull public ItemStack getResultItem(HolderLookup.Provider access) {
        return this.itemOutput.get();
    }

    @Override public int getMultipleProcessTicks() { return 0; }

    public IngredientWithSize input() { return input; }
    public TagOutput itemOutput() { return itemOutput; }
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
        if (copiedAtReload == CachedRecipeList.getReloadCount() && !IE_COPIED_RECIPES.isEmpty()) { return; }
        IE_COPIED_RECIPES.clear();
        copiedAtReload = CachedRecipeList.getReloadCount();
        for (RecipeHolder<CokeOvenRecipe> holder : CokeOvenRecipe.RECIPES.getRecipes(level)) {
            CokeOvenRecipe r = holder.value();
            ItemStack[] ieStacks = r.input.getMatchingStacks();
            boolean alreadyHas = false;
            if (ieStacks.length > 0) {
                for (RecipeHolder<AdvancedCokeOvenRecipe> h : RECIPES.getRecipes(level)) { if (h.value().matches(ieStacks[0])) { alreadyHas = true; break; } }
            }
            if (!alreadyHas) {
                AdvancedCokeOvenRecipe copied = new AdvancedCokeOvenRecipe(r.input, r.output, r.time, r.creosoteOutput);
                ResourceLocation copiedId = ResourceLocation.fromNamespaceAndPath("immersivetechnology", "advanced_coke_oven/copied/" + holder.id().getNamespace() + "/" + holder.id().getPath());
                IE_COPIED_RECIPES.add(new RecipeHolder<>(copiedId, copied));
            }
        }
    }
}
