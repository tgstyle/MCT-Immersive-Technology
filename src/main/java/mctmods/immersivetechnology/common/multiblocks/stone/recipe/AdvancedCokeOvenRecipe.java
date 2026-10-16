package mctmods.immersivetechnology.common.multiblocks.stone.recipe;

import mctmods.immersivetechnology.common.multiblocks.stone.recipe.serializer.AdvancedCokeOvenRecipeSerializer;
import mctmods.immersivetechnology.core.registration.RecipeTypes;

import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICCokeOvenRecipe;
import com.immersiveconvergence.api.crafting.ICIngredientWithSize;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.immersiveconvergence.api.crafting.ICTagOutput;
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
import java.util.List;

public class AdvancedCokeOvenRecipe extends ICMultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, AdvancedCokeOvenRecipeSerializer> SERIALIZER;
    public static final ICCachedRecipeList<AdvancedCokeOvenRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.ADVANCED_COKE_OVEN);
    private static final String LEGACY_COPIED_PREFIX = "advanced_coke_oven/copied/";

    public final ICIngredientWithSize input;
    public final ICTagOutput itemOutput;
    public final int creosoteOutput;

    public AdvancedCokeOvenRecipe(ICIngredientWithSize input, ICTagOutput itemOutput, int time, int creosoteOutput) {
        super(itemOutput, RecipeTypes.ADVANCED_COKE_OVEN, time, 0);
        this.input = input;
        this.itemOutput = itemOutput;
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

    @Override @Nonnull public ItemStack getResultItem(@Nonnull HolderLookup.Provider access) {
        return this.itemOutput.get();
    }

    public ICIngredientWithSize input() { return input; }
    public ICTagOutput itemOutput() { return itemOutput; }
    public int creosoteOutput() { return creosoteOutput; }

    @Nullable public static RecipeHolder<AdvancedCokeOvenRecipe> findRecipe(Level level, ItemStack input, @Nullable RecipeHolder<AdvancedCokeOvenRecipe> hint) {
        if (input.isEmpty()) { return null; }
        RecipeHolder<AdvancedCokeOvenRecipe> own = RECIPES.findHolder(level, recipe -> recipe.matches(input), hint);
        if (own != null) { return own; }
        ICCokeOvenRecipe ie = ICCokeOvenRecipe.findRecipe(level, input);
        return ie != null ? new RecipeHolder<>(ie.id(), wrap(ie)) : null;
    }

    @Nullable public static AdvancedCokeOvenRecipe getById(Level level, ResourceLocation id) {
        AdvancedCokeOvenRecipe own = RECIPES.getById(level, id);
        if (own != null) { return own; }
        ICCokeOvenRecipe ie = ICCokeOvenRecipe.getById(level, ieId(id));
        return ie != null ? wrap(ie) : null;
    }

    private static ResourceLocation ieId(ResourceLocation id) {
        if (!id.getNamespace().equals("immersivetechnology") || !id.getPath().startsWith(LEGACY_COPIED_PREFIX)) { return id; }
        String rest = id.getPath().substring(LEGACY_COPIED_PREFIX.length());
        int slash = rest.indexOf('/');
        return slash < 0 ? id : ResourceLocation.fromNamespaceAndPath(rest.substring(0, slash), rest.substring(slash + 1));
    }

    private static AdvancedCokeOvenRecipe wrap(ICCokeOvenRecipe recipe) { return new AdvancedCokeOvenRecipe(recipe.input(), recipe.output(), recipe.time(), recipe.creosoteOutput()); }
}
