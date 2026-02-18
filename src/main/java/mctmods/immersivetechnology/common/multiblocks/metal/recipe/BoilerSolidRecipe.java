package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import com.google.common.collect.Lists;
import com.immersiveconvergence.api.HeatCapabilities;
import mctmods.immersivetechnology.core.registration.ITRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class BoilerSolidRecipe extends MultiblockRecipe {
    public static RegistryObject<IERecipeSerializer<BoilerSolidRecipe>> SERIALIZER;
    public static final CachedRecipeList<BoilerSolidRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.BOILER_SOLID);

    public IngredientWithSize input;
    private final double heatPerTick;
    private final double targetHeat;

    public BoilerSolidRecipe(ResourceLocation id, IngredientWithSize input, double heatPerTick, double targetHeat) {
        super(Lazy.of(() -> ItemStack.EMPTY), ITRecipeTypes.BOILER_SOLID, id);
        this.input = input;
        this.heatPerTick = heatPerTick;
        this.targetHeat = Math.min(targetHeat, HeatCapabilities.MAX_HEAT);
        setInputListWithSizes(Lists.newArrayList(this.input));
    }

    public static BoilerSolidRecipe findRecipe(Level level, ItemStack input) {
        for (BoilerSolidRecipe recipe : RECIPES.getRecipes(level)) {
            if (recipe.input.testIgnoringSize(input)) return recipe;
        }
        return null;
    }

    @Override
    @NotNull
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    protected IERecipeSerializer<?> getIESerializer() {
        return SERIALIZER.get();
    }

    @Override
    public int getTotalProcessTime() {
        return 0;
    }

    @Override
    public int getTotalProcessEnergy() {
        return 0;
    }

    @Override
    public int getMultipleProcessTicks() {
        return 0;
    }

    public double getHeatPerTick() {
        return heatPerTick;
    }

    public double getTargetHeat() {
        return targetHeat;
    }
}
