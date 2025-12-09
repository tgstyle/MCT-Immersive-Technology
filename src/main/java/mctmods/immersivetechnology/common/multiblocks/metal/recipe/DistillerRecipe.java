package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import com.google.common.collect.Lists;
import mctmods.immersivetechnology.core.registration.ITRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class DistillerRecipe extends MultiblockRecipe {
    public static RegistryObject<IERecipeSerializer<DistillerRecipe>> SERIALIZER;
    public static final CachedRecipeList<DistillerRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.DISTILLER);

    public final FluidTagInput input;
    @Nullable public final FluidStack fluidOutput;
    public final ItemStack itemOutput;
    public final float chance;
    private final int time;
    private final int energy;
    Lazy<Integer> totalProcessTime;
    Lazy<Integer> totalProcessEnergy;

    public DistillerRecipe(ResourceLocation id, FluidTagInput input, @Nullable FluidStack fluidOutput, ItemStack itemOutput, float chance, int time, int energy) {
        super(Lazy.of(() -> ItemStack.EMPTY), ITRecipeTypes.DISTILLER, id);
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

    public static DistillerRecipe findRecipe(Level level, FluidStack inputFluid) {
        for (DistillerRecipe recipe : RECIPES.getRecipes(level)) { if (recipe.input.test(inputFluid)) return recipe; }
        return null;
    }

    @Override @NotNull public ItemStack getResultItem(RegistryAccess registryAccess) { return ItemStack.EMPTY; }

    @Override protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override public int getTotalProcessTime() { return totalProcessTime.get(); }

    @Override public int getTotalProcessEnergy() { return totalProcessEnergy.get(); }

    @Override public int getMultipleProcessTicks() { return 0; }
}
