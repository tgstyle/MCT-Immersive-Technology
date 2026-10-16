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

public class SteamTurbineRecipe extends ICMultiblockRecipe {
    public static RegistryObject<ICRecipeSerializer<SteamTurbineRecipe>> SERIALIZER;
    public static final ICCachedRecipeList<SteamTurbineRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.STEAM_TURBINE);

    public final ICFluidTagInput input;
    @Nullable public final FluidStack fluidOutput;
    public final float torque;

    public SteamTurbineRecipe(ResourceLocation id, ICFluidTagInput input, @Nullable FluidStack fluidOutput, int time, float torque) {
        super(RecipeTypes.STEAM_TURBINE, id, time, 0);
        this.input = input;
        this.fluidOutput = fluidOutput;
        this.torque = torque;
    }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }

    public boolean matches(FluidStack fluid) { return input.test(fluid); }

    public static SteamTurbineRecipe findRecipe(Level level, FluidStack fluid, @Nullable SteamTurbineRecipe hint) { return RECIPES.find(level, recipe -> recipe.matches(fluid), hint); }
}
