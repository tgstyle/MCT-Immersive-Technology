package mctmods.immersivetechnology.common.multiblocks.stone.recipe;

import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICCokeOvenRecipe;
import com.immersiveconvergence.api.crafting.ICIngredientWithSize;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import mctmods.immersivetechnology.core.registration.RecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AdvancedCokeOvenRecipe extends ICMultiblockRecipe {
    public static RegistryObject<ICRecipeSerializer<AdvancedCokeOvenRecipe>> SERIALIZER;
    public static final ICCachedRecipeList<AdvancedCokeOvenRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.ADVANCED_COKE_OVEN);

    private static final List<AdvancedCokeOvenRecipe> IE_COPIED_RECIPES = new ArrayList<>();

    public final ICIngredientWithSize input;
    public final Lazy<ItemStack> itemOutput;
    public final int time;
    public final int creosoteOutput;

    public AdvancedCokeOvenRecipe(ResourceLocation id, ICIngredientWithSize input, Lazy<ItemStack> itemOutput, int time, int creosoteOutput) {
        super(itemOutput, RecipeTypes.ADVANCED_COKE_OVEN, id);
        this.input = input;
        this.itemOutput = itemOutput;
        this.time = time;
        this.creosoteOutput = creosoteOutput;
        setInputListWithSizes(List.of(input));
        this.outputList = Lazy.of(() -> NonNullList.of(ItemStack.EMPTY, itemOutput.get()));
        this.fluidOutputList = List.of(new FluidStack(ICCokeOvenRecipe.creosote(), creosoteOutput));
    }

    @Override public int getTotalProcessTime() { return time; }

    @Override public int getTotalProcessEnergy() { return 0; }

    public boolean matches(ItemStack stack) { return input.test(stack); }

    @Override protected ICRecipeSerializer<AdvancedCokeOvenRecipe> getICSerializer() { return SERIALIZER.get(); }

    @Override public @Nonnull ItemStack getResultItem(@Nonnull RegistryAccess access) { return this.itemOutput.get(); }

    public static AdvancedCokeOvenRecipe findRecipe(Level level, ItemStack input, @Nullable AdvancedCokeOvenRecipe hint) {
        if (input.isEmpty()) return null;
        if (hint != null && hint.matches(input)) return hint;

        copyIECokeOvenRecipes(level);

        for (AdvancedCokeOvenRecipe recipe : RECIPES.getRecipes(level)) {
            if (recipe.matches(input)) return recipe;
        }

        for (AdvancedCokeOvenRecipe recipe : IE_COPIED_RECIPES) {
            if (recipe.matches(input)) return recipe;
        }
        return null;
    }

    public static AdvancedCokeOvenRecipe getById(Level level, ResourceLocation id) {
        copyIECokeOvenRecipes(level);

        AdvancedCokeOvenRecipe r = RECIPES.getById(level, id);
        if (r != null) return r;

        for (AdvancedCokeOvenRecipe copied : IE_COPIED_RECIPES) {
            if (copied.getId().equals(id)) return copied;
        }
        return null;
    }

    public static boolean usesIERecipe(Level level, ICCokeOvenRecipe recipe) {
        ItemStack[] ieStacks = recipe.input().getMatchingStacks();
        if (ieStacks.length == 0) return false;

        ItemStack testStack = ieStacks[0];
        return RECIPES.getRecipes(level).stream().noneMatch(j -> j.matches(testStack));
    }

    public static void copyIECokeOvenRecipes(Level level) {
        if (!IE_COPIED_RECIPES.isEmpty()) return;

        for (ICCokeOvenRecipe r : ICCokeOvenRecipe.getRecipes(level)) {
            if (usesIERecipe(level, r)) {
                AdvancedCokeOvenRecipe copied = new AdvancedCokeOvenRecipe(
                        ResourceLocation.fromNamespaceAndPath("immersivetechnology", "copied_ie/" + r.id().getPath()),
                        r.input(),
                        r.output(),
                        r.time(),
                        r.creosoteOutput()
                );
                IE_COPIED_RECIPES.add(copied);
            }
        }
    }

    @Override public int getMultipleProcessTicks() { return 0; }
}
