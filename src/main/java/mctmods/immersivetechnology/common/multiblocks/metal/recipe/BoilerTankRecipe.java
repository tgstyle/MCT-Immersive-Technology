package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import com.google.common.collect.Lists;
import mctmods.immersivetechnology.core.registration.ITRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class BoilerTankRecipe extends MultiblockRecipe {
    public static RegistryObject<IERecipeSerializer<BoilerTankRecipe>> SERIALIZER;
    public static final CachedRecipeList<BoilerTankRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.BOILER_TANK);

    public final FluidTagInput input;
    public final FluidStack output;
    Lazy<Integer> totalProcessTime;

    public BoilerTankRecipe(ResourceLocation id, FluidTagInput input, FluidStack output, int time) {
        super(LAZY_EMPTY, ITRecipeTypes.BOILER_TANK, id);
        this.input = input;
        this.output = output;
        totalProcessTime = Lazy.of(() -> time);

        this.fluidInputList = Lists.newArrayList(this.input);
        this.fluidOutputList = Lists.newArrayList(this.output);
    }

    @Override
    protected IERecipeSerializer<?> getIESerializer() {
        return SERIALIZER.get();
    }

    @Override
    public @NotNull ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    public static BoilerTankRecipe findRecipe(Level level, FluidStack input) {
        for (BoilerTankRecipe recipe : RECIPES.getRecipes(level)) { if (recipe.input.test(input)) return recipe; }
        return null;
    }

    @Override
    public int getTotalProcessTime() {
        return totalProcessTime.get();
    }

    @Override
    public int getTotalProcessEnergy() {
        return 0;
    }

    @Override
    public int getMultipleProcessTicks() {
        return 0;
    }
}
