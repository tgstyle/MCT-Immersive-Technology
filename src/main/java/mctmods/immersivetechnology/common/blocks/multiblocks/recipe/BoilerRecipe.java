package mctmods.immersivetechnology.common.blocks.multiblocks.recipe;

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

import javax.annotation.Nonnull;

public class BoilerRecipe extends MultiblockRecipe
{
    public static RegistryObject<IERecipeSerializer<BoilerRecipe>> SERIALIZER;
    public static final CachedRecipeList<BoilerRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.BOILER);

    public final FluidStack output;
    public final FluidTagInput water;
    public final FluidTagInput fuel;
    private int heatPerTick;
    Lazy<Integer> totalProcessTime;
    Lazy<Integer> totalProcessEnergy;

    public BoilerRecipe(ResourceLocation id, FluidStack output, FluidTagInput water, FluidTagInput fuel, int heatPerTick)
    {
        super(LAZY_EMPTY, ITRecipeTypes.BOILER, id);
        this.output = output;
        this.water = water;
        this.fuel = fuel;
        this.heatPerTick = heatPerTick;
        totalProcessTime = Lazy.of(() -> 1);
        totalProcessEnergy = Lazy.of(() -> 1);

        this.fluidInputList = Lists.newArrayList(this.fuel);
        if(this.water!=null)
            this.fluidInputList.add(this.water);
        this.fluidOutputList = Lists.newArrayList(this.output);
    }

    @Override
    protected IERecipeSerializer<?> getIESerializer()
    {
        return SERIALIZER.get();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess)
    {
        return ItemStack.EMPTY;
    }

    public static BoilerRecipe findRecipe(Level level, FluidStack input0, @Nonnull FluidStack input1)
    {
        for(BoilerRecipe recipe : RECIPES.getRecipes(level))
        {
            if(!input0.isEmpty())
            {
                if(recipe.water!=null&&recipe.water.test(input0))
                {
                    if((recipe.fuel==null&&input1.isEmpty())||(recipe.fuel!=null&&recipe.fuel.test(input1)))
                        return recipe;
                }

                if(recipe.fuel!=null&&recipe.fuel.test(input0))
                {
                    if((recipe.water==null&&input1.isEmpty())||(recipe.water!=null&&recipe.water.test(input1)))
                        return recipe;
                }
            }
            else if(!input1.isEmpty())
            {
                if(recipe.water!=null&&recipe.water.test(input1)&&recipe.fuel==null)
                    return recipe;
                if(recipe.fuel!=null&&recipe.fuel.test(input1)&&recipe.water==null)
                    return recipe;
            }
        }
        return null;
    }

    public int getHeatPerTick()
    {
        return heatPerTick;
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
    public int getMultipleProcessTicks() {
        return 0;
    }
}
