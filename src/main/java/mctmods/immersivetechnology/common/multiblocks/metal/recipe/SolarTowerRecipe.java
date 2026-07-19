package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.core.registration.RecipeTypes;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;
import javax.annotation.Nullable;

public class SolarTowerRecipe extends MultiblockRecipe {
    public static RegistryObject<IERecipeSerializer<SolarTowerRecipe>> SERIALIZER;
    public static final CachedRecipeList<SolarTowerRecipe> RECIPES = new CachedRecipeList<>(RecipeTypes.SOLAR_TOWER);

    public final FluidTagInput input;
    public final FluidStack fluidOutput;
    private final int time;
    public final double requiredTemp;

    public SolarTowerRecipe(ResourceLocation id, FluidTagInput input, @Nullable FluidStack fluidOutput, int time, double requiredTemp) {
        super(Lazy.of(() -> ItemStack.EMPTY), RecipeTypes.SOLAR_TOWER, id);
        this.input = input;
        this.fluidOutput = fluidOutput;
        this.time = time;
        this.requiredTemp = requiredTemp;
        this.fluidInputList = Lists.newArrayList(this.input);
        this.fluidOutputList = fluidOutput == null ? Lists.newArrayList() : Lists.newArrayList(fluidOutput);
    }

    public boolean matches(FluidStack fluid) { return input.testIgnoringAmount(fluid) && fluid.getAmount() >= input.getAmount(); }

    @Nullable public static SolarTowerRecipe findRecipe(Level level, FluidStack fluid) { return findRecipe(level, fluid, null); }

    @Nullable public static SolarTowerRecipe findRecipe(Level level, FluidStack fluid, @Nullable SolarTowerRecipe hint) {
        if (fluid == null || fluid.isEmpty()) return null;
        if (hint != null && hint.matches(fluid)) return hint;
        for (SolarTowerRecipe recipe : RECIPES.getRecipes(level)) { if (recipe.matches(fluid)) return recipe; }
        return null;
    }

    @Override protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override public int getMultipleProcessTicks() { return 0; }

    @Override public int getTotalProcessTime() { return time; }

    @Override public int getTotalProcessEnergy() { return 0; }
}
