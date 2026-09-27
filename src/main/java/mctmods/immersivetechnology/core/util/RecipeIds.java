package mctmods.immersivetechnology.core.util;

import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class RecipeIds {
    @Nullable public static <R extends Recipe<?>> ResourceLocation holderId(CachedRecipeList<R> list, Level level, @Nullable R recipe) {
        if (recipe == null) { return null; }
        for (RecipeHolder<R> holder : list.getRecipes(level)) {
            if (holder.value() == recipe) { return holder.id(); }
        }
        return null;
    }
}
