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

public class HeatExchangerRecipe extends ICMultiblockRecipe {
    public static RegistryObject<ICRecipeSerializer<HeatExchangerRecipe>> SERIALIZER;
    public static final ICCachedRecipeList<HeatExchangerRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.HEAT_EXCHANGER);

    public final ICFluidTagInput input0;
    public final ICFluidTagInput input1;
    public final FluidStack output0;
    public final FluidStack output1;

    public HeatExchangerRecipe(ResourceLocation id, ICFluidTagInput input0, ICFluidTagInput input1, FluidStack output0, FluidStack output1, int energy, int time) {
        super(RecipeTypes.HEAT_EXCHANGER, id, time, energy);
        this.input0 = input0;
        this.input1 = input1;
        this.output0 = output0;
        this.output1 = output1;
    }

    public boolean matches(FluidStack fluid0, FluidStack fluid1) { return input0.test(fluid0) && (input1 == null || input1.test(fluid1)); }

    public static HeatExchangerRecipe findRecipe(Level level, FluidStack input0, FluidStack input1) { return findRecipe(level, input0, input1, null); }

    public static HeatExchangerRecipe findRecipe(Level level, FluidStack input0, FluidStack input1, @Nullable HeatExchangerRecipe hint) { return RECIPES.find(level, recipe -> recipe.matches(input0, input1), hint); }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }
}
