package mctmods.immersivetechnology.common.blocks.multiblocks.recipe;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import com.igteam.immersivegeology.common.block.multiblocks.recipe.BloomeryFuel;
import com.igteam.immersivegeology.core.registration.IGRecipeTypes;
import mctmods.immersivetechnology.core.registration.ITRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

import java.util.Iterator;

public class AdvancedCokeOvenFuel extends IESerializableRecipe
{
    public static RegistryObject<IERecipeSerializer<AdvancedCokeOvenFuel>> SERIALIZER;
    public static final CachedRecipeList<BloomeryFuel> RECIPES;
    public final Ingredient input;
    public final int burnTime;

    public AdvancedCokeOvenFuel(ResourceLocation id, Ingredient input, int burnTime) {
        super(LAZY_EMPTY, ITRecipeTypes.ADV_COKE_OVEN_FUEL, id);
        this.input = input;
        this.burnTime = burnTime;
    }

    public static int getAdvCokeOvenFuelTime(Level level, ItemStack stack) {
        Iterator var2 = RECIPES.getRecipes(level).iterator();

        BloomeryFuel e;
        do {
            if (!var2.hasNext()) {
                return 0;
            }

            e = (BloomeryFuel)var2.next();
        } while(!e.input.test(stack));

        return e.burnTime;
    }

    public static boolean isValidAdvCokeOvenFuel(Level level, ItemStack stack) {
        return getAdvCokeOvenFuelTime(level, stack) > 0;
    }

    protected IERecipeSerializer<AdvancedCokeOvenFuel> getIESerializer() {
        return (IERecipeSerializer)SERIALIZER.get();
    }

    public ItemStack getResultItem(RegistryAccess access) {
        return ItemStack.EMPTY;
    }

    static {
        RECIPES = new CachedRecipeList(IGRecipeTypes.BLOOMERY_FUEL);
    }
}
