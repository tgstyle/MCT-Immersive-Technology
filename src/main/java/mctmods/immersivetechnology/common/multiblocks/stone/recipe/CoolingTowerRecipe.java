package mctmods.immersivetechnology.common.multiblocks.stone.recipe;

import mctmods.immersivetechnology.core.registration.RecipeTypes;

import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICFluidTagInput;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.google.common.collect.Lists;
import com.immersiveconvergence.api.util.RecipeCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class CoolingTowerRecipe extends ICMultiblockRecipe {
    public static RegistryObject<ICRecipeSerializer<CoolingTowerRecipe>> SERIALIZER;
    public static final ICCachedRecipeList<CoolingTowerRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.COOLING_TOWER);

    public final FluidStack fluidOutput0;
    public final FluidStack fluidOutput1;
    public final FluidStack fluidOutput2;
    public final ICFluidTagInput input0;
    public final ICFluidTagInput input1;

    public CoolingTowerRecipe(ResourceLocation id, FluidStack fluidOutput0, FluidStack fluidOutput1, FluidStack fluidOutput2, ICFluidTagInput input0, ICFluidTagInput input1, int time) {
        super(RecipeTypes.COOLING_TOWER, id, time, 0);
        this.fluidOutput0 = fluidOutput0;
        this.fluidOutput1 = fluidOutput1;
        this.fluidOutput2 = fluidOutput2;
        this.input0 = input0;
        this.input1 = input1;
        this.fluidInputList = Lists.newArrayList(input0, input1);
        this.fluidOutputList = Lists.newArrayList(fluidOutput0, fluidOutput1, fluidOutput2);
    }

    public boolean matches(FluidStack fluid0, FluidStack fluid1) { return input0.test(fluid0) && fluid0.getAmount() >= input0.getAmount() && input1.test(fluid1) && fluid1.getAmount() >= input1.getAmount(); }

    public static CoolingTowerRecipe findRecipe(Level level, FluidStack fluidInput0, FluidStack fluidInput1) { return findRecipe(level, fluidInput0, fluidInput1, null); }

    public static CoolingTowerRecipe findRecipe(Level level, FluidStack fluidInput0, FluidStack fluidInput1, @Nullable CoolingTowerRecipe hint) {
        if (fluidInput0.isEmpty() || fluidInput1.isEmpty()) return null;
        return RECIPES.find(level, recipe -> recipe.matches(fluidInput0, fluidInput1), hint);
    }

    @Nullable public static CoolingTowerRecipe findOriented(Level level, FluidStack tank0, FluidStack tank1, RecipeCache.TriFunction<Level, FluidStack, FluidStack, CoolingTowerRecipe> direct, RecipeCache.TriFunction<Level, FluidStack, FluidStack, CoolingTowerRecipe> reversed) {
        CoolingTowerRecipe recipe = direct.apply(level, tank0, tank1);
        if (recipe != null) { return recipe; }
        recipe = reversed.apply(level, tank1, tank0);
        return recipe == null ? null : new CoolingTowerRecipe(recipe.getId(), recipe.fluidOutput0, recipe.fluidOutput1, recipe.fluidOutput2, recipe.input1, recipe.input0, recipe.getBaseTime());
    }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }
}
