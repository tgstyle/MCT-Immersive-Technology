package mctmods.immersivetechnology.common.multiblocks.stone.recipe;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import blusunrize.immersiveengineering.common.register.IEFluids;
import mctmods.immersivetechnology.core.registration.RecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AdvancedCokeOvenRecipe extends MultiblockRecipe {
    public static RegistryObject<IERecipeSerializer<AdvancedCokeOvenRecipe>> SERIALIZER;
    public static final CachedRecipeList<AdvancedCokeOvenRecipe> RECIPES = new CachedRecipeList<>(RecipeTypes.ADVANCED_COKE_OVEN);

    private static final List<AdvancedCokeOvenRecipe> IE_COPIED_RECIPES = new ArrayList<>();

    public final IngredientWithSize input;
    public final Lazy<ItemStack> itemOutput;
    public final int time;
    public final int creosoteOutput;

    public AdvancedCokeOvenRecipe(ResourceLocation id, IngredientWithSize input, Lazy<ItemStack> itemOutput, int time, int creosoteOutput) {
        super(itemOutput, RecipeTypes.ADVANCED_COKE_OVEN, id);
        this.input = input;
        this.itemOutput = itemOutput;
        this.time = time;
        this.creosoteOutput = creosoteOutput;
        setInputListWithSizes(List.of(input));
        this.outputList = Lazy.of(() -> NonNullList.of(ItemStack.EMPTY, itemOutput.get()));
        this.fluidOutputList = List.of(new FluidStack(IEFluids.CREOSOTE.getStill(), creosoteOutput));
    }

    @Override public int getTotalProcessTime() { return time; }

    @Override public int getTotalProcessEnergy() { return 0; }

    public boolean matches(ItemStack stack) { return input.test(stack); }

    @Override protected IERecipeSerializer<AdvancedCokeOvenRecipe> getIESerializer() { return SERIALIZER.get(); }

    @Override public @NotNull ItemStack getResultItem(RegistryAccess access) { return this.itemOutput.get(); }

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

    public static void copyIECokeOvenRecipes(Level level) {
        if (!IE_COPIED_RECIPES.isEmpty()) return;

        for (CokeOvenRecipe r : CokeOvenRecipe.RECIPES.getRecipes(level)) {
            ItemStack[] ieStacks = r.input.getMatchingStacks();
            if (ieStacks.length == 0) continue;

            ItemStack testStack = ieStacks[0];

            boolean alreadyHas = RECIPES.getRecipes(level).stream().anyMatch(j -> j.matches(testStack));
            if (!alreadyHas) {
                AdvancedCokeOvenRecipe copied = new AdvancedCokeOvenRecipe(
                        ResourceLocation.fromNamespaceAndPath("immersivetechnology", "copied_ie/" + r.getId().getPath()),
                        r.input,
                        r.output,
                        r.time,
                        r.creosoteOutput
                );
                IE_COPIED_RECIPES.add(copied);
            }
        }
    }

    @Override public int getMultipleProcessTicks() { return 0; }
}
