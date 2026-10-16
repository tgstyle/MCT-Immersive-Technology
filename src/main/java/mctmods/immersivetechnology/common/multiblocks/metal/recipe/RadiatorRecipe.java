package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.core.registration.RecipeTypes;

import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICFluidTagInput;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class RadiatorRecipe extends ICMultiblockRecipe {
    public static RegistryObject<ICRecipeSerializer<RadiatorRecipe>> SERIALIZER;
    public static final ICCachedRecipeList<RadiatorRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.RADIATOR);

    public final FluidStack fluidOutput;
    public final ICFluidTagInput input;

    public RadiatorRecipe(ResourceLocation id, FluidStack fluidOutput, ICFluidTagInput input, int time) {
        super(RecipeTypes.RADIATOR, id, time, 0);
        this.fluidOutput = fluidOutput;
        this.input = input;
        this.fluidInputList = Lists.newArrayList(input);
        this.fluidOutputList = Lists.newArrayList(fluidOutput);
    }

    public boolean matches(FluidStack fluidInput) { return input.test(fluidInput) && fluidInput.getAmount() >= input.getAmount(); }

    public static RadiatorRecipe findRecipe(Level level, FluidStack fluidInput) { return findRecipe(level, fluidInput, null); }

    public static RadiatorRecipe findRecipe(Level level, FluidStack fluidInput, @Nullable RadiatorRecipe hint) {
        if (fluidInput.isEmpty()) return null;
        return RECIPES.find(level, recipe -> recipe.matches(fluidInput), hint);
    }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }
}
