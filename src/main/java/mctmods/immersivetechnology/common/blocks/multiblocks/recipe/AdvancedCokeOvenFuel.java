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

import java.util.Iterator;

public class AdvancedCokeOvenFuel extends IESerializableRecipe {
    public static RegistryObject<IERecipeSerializer<AdvancedCokeOvenFuel>> SERIALIZER;
    public static final CachedRecipeList<AdvancedCokeOvenFuel> RECIPES = new CachedRecipeList<>(ITRecipeTypes.ADVANCED_COKE_OVEN_FUEL);

    public final Ingredient input;
    public final int burnTime;

    public AdvancedCokeOvenFuel(ResourceLocation id, Ingredient input, int burnTime) {
        super(LAZY_EMPTY, ITRecipeTypes.ADVANCED_COKE_OVEN_FUEL, id);
        this.input = input;
        this.burnTime = burnTime;
    }

    public static int getAdvCokeOvenFuelTime(Level level, ItemStack stack) {
        Iterator<AdvancedCokeOvenFuel> var2 = RECIPES.getRecipes(level).iterator();
        AdvancedCokeOvenFuel e;
        do {
            if (!var2.hasNext()) { return 0; }
            e = var2.next();
        } while (!e.input.test(stack));
        return e.burnTime;
    }

    protected IERecipeSerializer<AdvancedCokeOvenFuel> getIESerializer() {
        return SERIALIZER.get();
    }

    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess access) {
        return ItemStack.EMPTY;
    }
}
