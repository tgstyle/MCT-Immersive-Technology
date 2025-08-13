package mctmods.immersivetechnology.common.blocks.multiblocks.recipe;

import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import mctmods.immersivetechnology.core.registration.ITRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class AdvancedCokeOvenFuel extends IESerializableRecipe {
    public static RegistryObject<IERecipeSerializer<AdvancedCokeOvenFuel>> SERIALIZER;
    public static final CachedRecipeList<AdvancedCokeOvenFuel> RECIPES = new CachedRecipeList<>(ITRecipeTypes.ADVANCED_COKE_OVEN_FUEL);

    public final Ingredient input;
    public final int time;

    public AdvancedCokeOvenFuel(ResourceLocation id, Ingredient input, int time) {
        super(LAZY_EMPTY, ITRecipeTypes.ADVANCED_COKE_OVEN_FUEL, id);
        this.input = input;
        this.time = time;
    }

    public static int getBurnTime(Level level, ItemStack stack) {
        for (AdvancedCokeOvenFuel recipe : RECIPES.getRecipes(level)) { if (recipe.input.test(stack)) return recipe.time; }
        return 0;
    }

    protected IERecipeSerializer<AdvancedCokeOvenFuel> getIESerializer() { return SERIALIZER.get(); }

    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess access) { return ItemStack.EMPTY; }
}
