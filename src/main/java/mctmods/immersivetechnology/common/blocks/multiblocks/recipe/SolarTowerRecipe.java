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

public class SolarTowerRecipe extends MultiblockRecipe {
    public static RegistryObject<IERecipeSerializer<SolarTowerRecipe>> SERIALIZER;
    public static final CachedRecipeList<SolarTowerRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.SOLAR_TOWER);

    public final FluidTagInput input;
    @Nullable
    public final FluidStack fluidOutput;
    private final int time;
    Lazy<Integer> totalProcessTime;

    public SolarTowerRecipe(ResourceLocation id, FluidTagInput input, @Nullable FluidStack fluidOutput, int time) {
        super(Lazy.of(() -> ItemStack.EMPTY), ITRecipeTypes.SOLAR_TOWER, id);
        this.input = input;
        this.fluidOutput = fluidOutput;
        this.time = time;
        totalProcessTime = Lazy.of(() -> this.time);
        this.fluidInputList = Lists.newArrayList(this.input);
        this.fluidOutputList = fluidOutput == null ? Lists.newArrayList() : Lists.newArrayList(fluidOutput);
    }

    public static @Nullable SolarTowerRecipe findRecipe(Level level, FluidStack fluid) {
        if (fluid == null || fluid.isEmpty()) return null;
        for (SolarTowerRecipe recipe : RECIPES.getRecipes(level)) {
            if (recipe.input.testIgnoringAmount(fluid) && fluid.getAmount() >= recipe.input.getAmount()) return recipe;
        }
        return null;
    }

    @Override
    protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override
    public int getTotalProcessTime() { return totalProcessTime.get(); }

    @Override
    public int getMultipleProcessTicks() { return 0; }
}
