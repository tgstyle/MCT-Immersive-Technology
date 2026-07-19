package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.core.registration.ITRecipeTypes;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import com.google.common.collect.Lists;
import com.immersiveconvergence.api.HeatCapabilities;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;

public class BoilerTankRecipe extends MultiblockRecipe {
    public static RegistryObject<IERecipeSerializer<BoilerTankRecipe>> SERIALIZER;
    public static final CachedRecipeList<BoilerTankRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.BOILER_TANK);

    public final FluidTagInput input;
    public final FluidStack output;
    public final double requiredHeat;
    Lazy<Integer> totalProcessTime;

    public BoilerTankRecipe(ResourceLocation id, FluidTagInput input, FluidStack output, int time, double requiredHeat) {
        super(LAZY_EMPTY, ITRecipeTypes.BOILER_TANK, id);
        this.input = input;
        this.output = output;
        this.requiredHeat = Math.min(requiredHeat, HeatCapabilities.MAX_HEAT);
        totalProcessTime = Lazy.of(() -> time);

        this.fluidInputList = Lists.newArrayList(this.input);
        this.fluidOutputList = Lists.newArrayList(this.output);
    }

    @Override protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override @NotNull public ItemStack getResultItem(RegistryAccess registryAccess) { return ItemStack.EMPTY; }

    public boolean matches(FluidStack fluid) { return input.test(fluid); }

    public static BoilerTankRecipe findRecipe(Level level, FluidStack input) { return findRecipe(level, input, null); }

    public static BoilerTankRecipe findRecipe(Level level, FluidStack input, @Nullable BoilerTankRecipe hint) {
        if (hint != null && hint.matches(input)) return hint;
        for (BoilerTankRecipe recipe : RECIPES.getRecipes(level)) { if (recipe.matches(input)) return recipe; }
        return null;
    }

    @Override public int getTotalProcessTime() { return totalProcessTime.get(); }

    @Override public int getTotalProcessEnergy() { return 0; }

    @Override public int getMultipleProcessTicks() { return 0; }
}
