package mctmods.immersivetechnology.common.blocks.multiblocks.recipe;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import com.google.common.collect.Lists;
import mctmods.immersivetechnology.core.registration.ITRecipeTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class SolarMelterRecipe extends MultiblockRecipe {
    public static RegistryObject<IERecipeSerializer<SolarMelterRecipe>> SERIALIZER;
    public static final CachedRecipeList<SolarMelterRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.SOLAR_MELTER);

    public final FluidTagInput input;
    @Nullable
    public final FluidStack fluidOutput;
    private final int time;
    Lazy<Integer> totalProcessTime;

    public SolarMelterRecipe(ResourceLocation id, FluidTagInput input, @Nullable FluidStack fluidOutput, int time) {
        super(Lazy.of(() -> ItemStack.EMPTY), ITRecipeTypes.SOLAR_MELTER, id);
        this.input = input;
        this.fluidOutput = fluidOutput;
        this.time = time;
        totalProcessTime = Lazy.of(() -> this.time);
        this.fluidInputList = Lists.newArrayList(this.input);
        this.fluidOutputList = fluidOutput == null ? Lists.newArrayList() : Lists.newArrayList(fluidOutput);
    }

    @Override
    protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    public boolean matches(FluidStack fluid) { return input.test(fluid); }

    public static SolarMelterRecipe findRecipe(Level level, FluidStack fluid, @Nullable SolarMelterRecipe hint) {
        if (fluid.isEmpty()) return null;
        if (hint != null && hint.matches(fluid)) return hint;
        for (SolarMelterRecipe recipe : RECIPES.getRecipes(level)) { if (recipe.matches(fluid)) return recipe; }
        return null;
    }

    @Override
    public int getTotalProcessTime() { return totalProcessTime.get(); }

    @Override
    public int getMultipleProcessTicks() { return 0; }
}
