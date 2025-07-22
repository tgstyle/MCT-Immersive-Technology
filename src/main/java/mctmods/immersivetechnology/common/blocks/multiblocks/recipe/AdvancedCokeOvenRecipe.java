package mctmods.immersivetechnology.common.blocks.multiblocks.recipe;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import mctmods.immersivetechnology.core.registration.ITRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class AdvancedCokeOvenRecipe extends MultiblockRecipe
{
    public static RegistryObject<IERecipeSerializer<AdvancedCokeOvenRecipe>> SERIALIZER;
    public static final CachedRecipeList<AdvancedCokeOvenRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.ADVANCED_COKE_OVEN);

    public final IngredientWithSize input;
    public final Lazy<ItemStack> output;
    public final int time;
    public final int creosoteOutput;

    public AdvancedCokeOvenRecipe(ResourceLocation id, Lazy<ItemStack> output, IngredientWithSize input, int time, int creosoteOutput)
    {
        super(output, ITRecipeTypes.ADVANCED_COKE_OVEN, id);
        this.output = output;
        this.input = input;
        this.time = time;
        this.creosoteOutput = creosoteOutput;
    }

    public boolean matches(ItemStack stack) {
        return input.test(stack);
    }

    @Override
    protected IERecipeSerializer getIESerializer()
    {
        return SERIALIZER.get();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access)
    {
        return this.output.get();
    }

    public static AdvancedCokeOvenRecipe findRecipe(Level level, ItemStack input)
    {
        return findRecipe(level, input, null);
    }

    public static AdvancedCokeOvenRecipe findRecipe(Level level, ItemStack input, @Nullable AdvancedCokeOvenRecipe hint)
    {
        if (input.isEmpty())
            return null;
        if (hint != null && hint.matches(input))
            return hint;
        for(AdvancedCokeOvenRecipe recipe : RECIPES.getRecipes(level))
            if(recipe.matches(input))
                return recipe;
        return null;
    }

    @Override
    public int getMultipleProcessTicks() {
        return 0;
    }
}
