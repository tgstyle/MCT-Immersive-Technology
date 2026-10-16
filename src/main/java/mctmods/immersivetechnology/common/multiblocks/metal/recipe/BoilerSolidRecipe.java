package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.core.registration.RecipeTypes;

import com.google.common.collect.Lists;
import com.immersiveconvergence.api.capability.HeatCapabilities;
import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICIngredientWithSize;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class BoilerSolidRecipe extends ICMultiblockRecipe {
    public static RegistryObject<ICRecipeSerializer<BoilerSolidRecipe>> SERIALIZER;
    public static final ICCachedRecipeList<BoilerSolidRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.BOILER_SOLID);

    public ICIngredientWithSize input;
    private final double heatPerTick;
    private final double targetHeat;

    public BoilerSolidRecipe(ResourceLocation id, ICIngredientWithSize input, double heatPerTick, double targetHeat) {
        super(RecipeTypes.BOILER_SOLID, id, 0, 0);
        this.input = input;
        this.heatPerTick = heatPerTick;
        this.targetHeat = Math.min(targetHeat, HeatCapabilities.maxHeat());
        setInputListWithSizes(Lists.newArrayList(this.input));
    }

    public boolean matches(ItemStack stack) { return input.testIgnoringSize(stack); }

    public static BoilerSolidRecipe findRecipe(Level level, ItemStack input) { return findRecipe(level, input, null); }

    public static BoilerSolidRecipe findRecipe(Level level, ItemStack input, @Nullable BoilerSolidRecipe hint) { return RECIPES.find(level, recipe -> recipe.matches(input), hint); }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }

    public double getHeatPerTick() { return heatPerTick; }

    public double getTargetHeat() { return targetHeat; }
}
