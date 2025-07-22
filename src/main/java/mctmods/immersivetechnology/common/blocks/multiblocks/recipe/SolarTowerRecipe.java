package mctmods.immersivetechnology.common.blocks.multiblocks.recipe;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import mctmods.immersivetechnology.core.registration.ITRecipeTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class SolarTowerRecipe extends MultiblockRecipe
{
    public static RegistryObject<IERecipeSerializer<SolarTowerRecipe>> SERIALIZER;

    public static CachedRecipeList<SolarTowerRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.SOLAR_TOWER);

    public TagKey<Fluid> inputTag;
    public int inputAmount;
    @Nullable
    public FluidStack fluidOutput;
    private int time;

    protected <T extends Recipe<?>> SolarTowerRecipe(ResourceLocation id, TagKey<Fluid> inputTag, int inputAmount, @Nullable FluidStack fluidOutput, int time) {
        super(LAZY_EMPTY, ITRecipeTypes.SOLAR_TOWER, id);
        this.inputTag = inputTag;
        this.inputAmount = inputAmount;
        this.fluidOutput = fluidOutput;
        this.time = time;
    }

    @Override
    protected IERecipeSerializer<?> getIESerializer()
    {
        return SERIALIZER.get();
    }

    public int getTotalProcessTime() {
        return time;
    }

    @Override
    public int getMultipleProcessTicks()
    {
        return 0;
    }
}
