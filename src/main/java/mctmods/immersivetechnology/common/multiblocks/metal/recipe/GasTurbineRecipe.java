package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.core.registration.RecipeTypes;

import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICFluidTagInput;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class GasTurbineRecipe extends ICMultiblockRecipe {
    public static RegistryObject<ICRecipeSerializer<GasTurbineRecipe>> SERIALIZER;
    public static final ICCachedRecipeList<GasTurbineRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.GAS_TURBINE);

    public final ICFluidTagInput input;
    @Nullable public final FluidStack fluidOutput;
    public final float torque;

    public GasTurbineRecipe(ResourceLocation id, ICFluidTagInput input, @Nullable FluidStack fluidOutput, int time, float torque) {
        super(RecipeTypes.GAS_TURBINE, id, time, 0);
        this.input = input;
        this.fluidOutput = fluidOutput;
        this.torque = torque;
    }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }

    public boolean matches(FluidStack fluid) { return input.test(fluid); }

    public static GasTurbineRecipe findRecipe(Level level, FluidStack fluid, @Nullable GasTurbineRecipe hint) { return RECIPES.find(level, recipe -> recipe.matches(fluid), hint); }
}
