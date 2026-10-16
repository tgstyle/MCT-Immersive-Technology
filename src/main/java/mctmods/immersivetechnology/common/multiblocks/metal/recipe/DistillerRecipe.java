package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.core.registration.RecipeTypes;

import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICFluidTagInput;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class DistillerRecipe extends ICMultiblockRecipe {
    public static RegistryObject<ICRecipeSerializer<DistillerRecipe>> SERIALIZER;
    public static final ICCachedRecipeList<DistillerRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.DISTILLER);

    public final ICFluidTagInput input;
    @Nullable public final FluidStack fluidOutput;
    public final ItemStack itemOutput;
    public final float chance;

    public DistillerRecipe(ResourceLocation id, ICFluidTagInput input, @Nullable FluidStack fluidOutput, ItemStack itemOutput, float chance, int time, int energy) {
        super(RecipeTypes.DISTILLER, id, time, energy);
        this.input = input;
        this.fluidOutput = fluidOutput;
        this.itemOutput = itemOutput;
        this.chance = chance;
    }

    public boolean matches(FluidStack fluid) { return input.test(fluid); }

    public static DistillerRecipe findRecipe(Level level, FluidStack inputFluid) { return findRecipe(level, inputFluid, null); }

    public static DistillerRecipe findRecipe(Level level, FluidStack inputFluid, @Nullable DistillerRecipe hint) { return RECIPES.find(level, recipe -> recipe.matches(inputFluid), hint); }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }
}
