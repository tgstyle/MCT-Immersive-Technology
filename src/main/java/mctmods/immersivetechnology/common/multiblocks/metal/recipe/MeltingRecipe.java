package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.core.registration.RecipeTypes;

import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICFluidTagInput;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class MeltingRecipe extends ICMultiblockRecipe implements ISolarRecipe {
    public static RegistryObject<ICRecipeSerializer<MeltingRecipe>> SERIALIZER;
    public static final ICCachedRecipeList<MeltingRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.MELTING);

    public final ICFluidTagInput input;
    public final FluidStack fluidOutput;
    private final int time;
    public final double requiredTemp;

    public MeltingRecipe(ResourceLocation id, ICFluidTagInput input, @Nullable FluidStack fluidOutput, int time, double requiredTemp) {
        super(Lazy.of(() -> ItemStack.EMPTY), RecipeTypes.MELTING, id);
        this.input = input;
        this.fluidOutput = fluidOutput;
        this.time = time;
        this.requiredTemp = requiredTemp;
    }

    @Override public ICFluidTagInput input() { return input; }

    @Override public FluidStack fluidOutput() { return fluidOutput; }

    @Override public double requiredTemp() { return requiredTemp; }

    public boolean matches(FluidStack fluid) { return input.testIgnoringAmount(fluid) && fluid.getAmount() >= input.getAmount(); }

    @Nullable public static MeltingRecipe findRecipe(Level level, FluidStack fluid) { return findRecipe(level, fluid, null); }

    @Nullable public static MeltingRecipe findRecipe(Level level, FluidStack fluid, @Nullable MeltingRecipe hint) {
        if (fluid == null || fluid.isEmpty()) return null;
        if (hint != null && hint.matches(fluid)) return hint;
        for (MeltingRecipe recipe : RECIPES.getRecipes(level)) { if (recipe.matches(fluid)) return recipe; }
        return null;
    }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }

    @Override public int getMultipleProcessTicks() { return 1; }

    @Override public int getTotalProcessTime() { return time; }

    @Override public int getTotalProcessEnergy() { return time; }
}
