package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.core.registration.RecipeTypes;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import com.google.common.collect.Lists;
import com.immersiveconvergence.api.capability.HeatCapabilities;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;

public class BoilerSolidRecipe extends MultiblockRecipe {
    public static RegistryObject<IERecipeSerializer<BoilerSolidRecipe>> SERIALIZER;
    public static final CachedRecipeList<BoilerSolidRecipe> RECIPES = new CachedRecipeList<>(RecipeTypes.BOILER_SOLID);

    public IngredientWithSize input;
    private final double heatPerTick;
    private final double targetHeat;

    public BoilerSolidRecipe(ResourceLocation id, IngredientWithSize input, double heatPerTick, double targetHeat) {
        super(Lazy.of(() -> ItemStack.EMPTY), RecipeTypes.BOILER_SOLID, id);
        this.input = input;
        this.heatPerTick = heatPerTick;
        this.targetHeat = Math.min(targetHeat, HeatCapabilities.MAX_HEAT);
        setInputListWithSizes(Lists.newArrayList(this.input));
    }

    public boolean matches(ItemStack stack) { return input.testIgnoringSize(stack); }

    public static BoilerSolidRecipe findRecipe(Level level, ItemStack input) { return findRecipe(level, input, null); }

    public static BoilerSolidRecipe findRecipe(Level level, ItemStack input, @Nullable BoilerSolidRecipe hint) {
        if (hint != null && hint.matches(input)) return hint;
        for (BoilerSolidRecipe recipe : RECIPES.getRecipes(level)) { if (recipe.matches(input)) return recipe; }
        return null;
    }

    @Override @NotNull public ItemStack getResultItem(RegistryAccess registryAccess) { return ItemStack.EMPTY; }

    @Override protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override public int getTotalProcessTime() { return 0; }

    @Override public int getTotalProcessEnergy() { return 0; }

    @Override public int getMultipleProcessTicks() { return 0; }

    public double getHeatPerTick() { return heatPerTick; }

    public double getTargetHeat() { return targetHeat; }
}
