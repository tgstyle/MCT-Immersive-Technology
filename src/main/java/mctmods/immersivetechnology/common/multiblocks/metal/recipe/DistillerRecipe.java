package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.core.registration.RecipeTypes;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DistillerRecipe extends MultiblockRecipe {
    public static RegistryObject<IERecipeSerializer<DistillerRecipe>> SERIALIZER;
    public static final CachedRecipeList<DistillerRecipe> RECIPES = new CachedRecipeList<>(RecipeTypes.DISTILLER);

    public final FluidTagInput input;
    @Nullable public final FluidStack fluidOutput;
    public final ItemStack itemOutput;
    public final float chance;
    private final int time;
    private final int energy;
    Lazy<Integer> totalProcessTime;
    Lazy<Integer> totalProcessEnergy;

    public DistillerRecipe(ResourceLocation id, FluidTagInput input, @Nullable FluidStack fluidOutput, ItemStack itemOutput, float chance, int time, int energy) {
        super(Lazy.of(() -> ItemStack.EMPTY), RecipeTypes.DISTILLER, id);
        this.input = input;
        this.fluidOutput = fluidOutput;
        this.itemOutput = itemOutput;
        this.chance = chance;
        this.time = time;
        this.energy = energy;

        totalProcessTime = Lazy.of(() -> this.time);
        totalProcessEnergy = Lazy.of(() -> this.energy);

        this.fluidInputList = Lists.newArrayList(this.input);
        if (this.fluidOutput != null) this.fluidOutputList = Lists.newArrayList(this.fluidOutput);
        this.outputList = Lazy.of(NonNullList::create);
    }

    public boolean matches(FluidStack fluid) { return input.test(fluid); }

    public static DistillerRecipe findRecipe(Level level, FluidStack inputFluid) { return findRecipe(level, inputFluid, null); }

    public static DistillerRecipe findRecipe(Level level, FluidStack inputFluid, @Nullable DistillerRecipe hint) {
        if (hint != null && hint.matches(inputFluid)) return hint;
        for (DistillerRecipe recipe : RECIPES.getRecipes(level)) { if (recipe.matches(inputFluid)) return recipe; }
        return null;
    }

    @Override @Nonnull public ItemStack getResultItem(RegistryAccess registryAccess) { return ItemStack.EMPTY; }

    @Override protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override public int getTotalProcessTime() { return totalProcessTime.get(); }

    @Override public int getTotalProcessEnergy() { return totalProcessEnergy.get(); }

    @Override public int getMultipleProcessTicks() { return 0; }
}
