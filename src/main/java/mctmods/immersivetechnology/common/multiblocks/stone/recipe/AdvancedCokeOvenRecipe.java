package mctmods.immersivetechnology.common.multiblocks.stone.recipe;

import mctmods.immersivetechnology.core.registration.RecipeTypes;

import com.immersiveconvergence.api.ICMods;
import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICCokeOvenRecipe;
import com.immersiveconvergence.api.crafting.ICIngredientWithSize;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
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
import java.util.List;

public class AdvancedCokeOvenRecipe extends ICMultiblockRecipe {
    public static RegistryObject<ICRecipeSerializer<AdvancedCokeOvenRecipe>> SERIALIZER;
    public static final ICCachedRecipeList<AdvancedCokeOvenRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.ADVANCED_COKE_OVEN);
    private static final String LEGACY_COPIED_PREFIX = "copied_ie/";

    public final ICIngredientWithSize input;
    public final Lazy<ItemStack> itemOutput;
    public final int creosoteOutput;

    public AdvancedCokeOvenRecipe(ResourceLocation id, ICIngredientWithSize input, Lazy<ItemStack> itemOutput, int time, int creosoteOutput) {
        super(itemOutput, RecipeTypes.ADVANCED_COKE_OVEN, id, time, 0);
        this.input = input;
        this.itemOutput = itemOutput;
        this.creosoteOutput = creosoteOutput;
        setInputListWithSizes(List.of(input));
        this.outputList = Lazy.of(() -> NonNullList.of(ItemStack.EMPTY, itemOutput.get()));
        this.fluidOutputList = List.of(new FluidStack(ICCokeOvenRecipe.creosote(), creosoteOutput));
    }

    public boolean matches(ItemStack stack) { return input.test(stack); }

    @Override protected ICRecipeSerializer<AdvancedCokeOvenRecipe> getICSerializer() { return SERIALIZER.get(); }

    @Override public @Nonnull ItemStack getResultItem(@Nonnull RegistryAccess access) { return this.itemOutput.get(); }

    @Nullable public static AdvancedCokeOvenRecipe findRecipe(Level level, ItemStack input, @Nullable AdvancedCokeOvenRecipe hint) {
        if (input.isEmpty()) return null;
        AdvancedCokeOvenRecipe own = RECIPES.find(level, recipe -> recipe.matches(input), hint);
        return own != null ? own : wrap(ICCokeOvenRecipe.findRecipe(level, input));
    }

    @Nullable public static AdvancedCokeOvenRecipe getById(Level level, ResourceLocation id) {
        AdvancedCokeOvenRecipe own = RECIPES.getById(level, id);
        return own != null ? own : wrap(ICCokeOvenRecipe.getById(level, ieId(id)));
    }

    private static ResourceLocation ieId(ResourceLocation id) {
        if (id.getNamespace().equals("immersivetechnology") && id.getPath().startsWith(LEGACY_COPIED_PREFIX)) {
            return ICMods.ie(id.getPath().substring(LEGACY_COPIED_PREFIX.length()));
        }
        return id;
    }

    @Nullable private static AdvancedCokeOvenRecipe wrap(@Nullable ICCokeOvenRecipe recipe) { return recipe == null ? null : new AdvancedCokeOvenRecipe(recipe.id(), recipe.input(), recipe.output(), recipe.time(), recipe.creosoteOutput()); }
}
