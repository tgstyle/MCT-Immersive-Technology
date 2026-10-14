package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.core.registration.RecipeTypes;

import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICFluidTagInput;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HeatExchangerRecipe extends ICMultiblockRecipe {
    public static RegistryObject<ICRecipeSerializer<HeatExchangerRecipe>> SERIALIZER;
    public static final ICCachedRecipeList<HeatExchangerRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.HEAT_EXCHANGER);

    public static float timeModifier = 1;
    public static float energyModifier = 1;

    public final ICFluidTagInput input0;
    public final ICFluidTagInput input1;
    public final FluidStack output0;
    public final FluidStack output1;

    int totalProcessTime;
    int totalProcessEnergy;

    public HeatExchangerRecipe(ResourceLocation id, ICFluidTagInput input0, ICFluidTagInput input1, FluidStack output0, FluidStack output1, int energy, int time) {
        super(LAZY_EMPTY, RecipeTypes.HEAT_EXCHANGER, id);
        this.input0 = input0;
        this.input1 = input1;
        this.output0 = output0;
        this.output1 = output1;
        this.totalProcessTime = time;
        this.totalProcessEnergy = energy;
    }

    public HeatExchangerRecipe modifyTimeAndEnergy(Function<Double, Double> time, Function<Double, Double> energy) {
        this.totalProcessTime = (int)Math.floor(time.apply((double)this.totalProcessTime));
        this.totalProcessEnergy = (int)Math.floor(energy.apply((double)this.totalProcessEnergy));
        return this;
    }

    public boolean matches(FluidStack fluid0, FluidStack fluid1) { return input0.test(fluid0) && (input1 == null || input1.test(fluid1)); }

    public static HeatExchangerRecipe findRecipe(Level level, FluidStack input0, FluidStack input1) { return findRecipe(level, input0, input1, null); }

    public static HeatExchangerRecipe findRecipe(Level level, FluidStack input0, FluidStack input1, @Nullable HeatExchangerRecipe hint) {
        if (hint != null && hint.matches(input0, input1)) return hint;
        for (HeatExchangerRecipe recipe : RECIPES.getRecipes(level)) { if (recipe.matches(input0, input1)) return recipe; }
        return null;
    }

    @Override public int getMultipleProcessTicks() { return 0; }

    @Override public int getTotalProcessTime() { return this.totalProcessTime; }

    @Override public int getTotalProcessEnergy() { return this.totalProcessEnergy; }

    @Override @Nonnull public ItemStack getResultItem(RegistryAccess registryAccess) { return ItemStack.EMPTY; }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }
}
