package mctmods.immersivetechnology.common.blocks.multiblocks.recipe;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import com.google.common.collect.Lists;
import mctmods.immersivetechnology.core.registration.ITRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DistillerRecipe extends MultiblockRecipe
{
    public static RegistryObject<IERecipeSerializer<DistillerRecipe>> SERIALIZER;

    public static CachedRecipeList<DistillerRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.DISTILLER);

    public FluidTagInput water;
    @Nullable
    public FluidStack fluidOutput;
    private int time;
    private int energy;
    Lazy<Integer> totalProcessTime;
    Lazy<Integer> totalProcessEnergy;

    public <T extends Recipe<?>> DistillerRecipe(ResourceLocation id, FluidTagInput water, @Nullable FluidStack fluidOutput, int time, int energy) {
        super(LAZY_EMPTY, ITRecipeTypes.SOLAR_TOWER, id);
        this.water = water;
        this.fluidOutput = fluidOutput;
        this.time = time;
        this.energy = energy;

        totalProcessTime = Lazy.of(() -> this.time);
        totalProcessEnergy = Lazy.of(() -> this.energy);

        this.fluidInputList = Lists.newArrayList(this.water);
        if(this.water!=null)
            this.fluidInputList.add(this.water);
        this.fluidOutputList = Lists.newArrayList(this.fluidOutput);
    }

    public static DistillerRecipe findRecipe(Level level, @Nonnull FluidStack input)
    {
        for(DistillerRecipe recipe : RECIPES.getRecipes(level))
        {
            if(!input.isEmpty())
            {
                if(recipe.water!=null&&recipe.water.test(input))
                    return recipe;
                if(recipe.water==null)
                    return recipe;
            }
        }
        return null;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess)
    {
        return ItemStack.EMPTY;
    }

    @Override
    protected IERecipeSerializer<?> getIESerializer()
    {
        return SERIALIZER.get();
    }

    @Override
    public int getTotalProcessTime()
    {
        return totalProcessTime.get();
    }

    @Override
    public int getTotalProcessEnergy()
    {
        return totalProcessEnergy.get();
    }

    @Override
    public int getMultipleProcessTicks()
    {
        return 0;
    }
}
