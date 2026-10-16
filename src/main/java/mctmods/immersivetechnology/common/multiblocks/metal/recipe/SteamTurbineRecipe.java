package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer.SteamTurbineRecipeSerializer;
import mctmods.immersivetechnology.core.registration.RecipeTypes;

import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.immersiveconvergence.api.crafting.ICTagOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;

public class SteamTurbineRecipe extends ICMultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, SteamTurbineRecipeSerializer> SERIALIZER;
    public static final ICCachedRecipeList<SteamTurbineRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.STEAM_TURBINE);

    public final TagKey<Fluid> fluidTag;
    private final int amount;
    @Nullable public final FluidStack fluidOutput;
    public final float torque;

    public SteamTurbineRecipe(TagKey<Fluid> fluidTag, int amount, @Nullable FluidStack fluidOutput, int time, float torque) {
        super(ICTagOutput.EMPTY, RecipeTypes.STEAM_TURBINE, time, 0);
        this.fluidTag = fluidTag;
        this.amount = amount;
        this.fluidOutput = fluidOutput;
        this.torque = torque;
    }

    public TagKey<Fluid> fluidTag() { return fluidTag; }
    public int amount() { return amount; }
    public int getInputAmount() { return amount; }
    public FluidStack fluidOutput() { return fluidOutput; }
    public float torque() { return torque; }

    public boolean matches(FluidStack fluid) {
        return fluid.is(this.fluidTag);
    }

    public static SteamTurbineRecipe findRecipe(Level level, FluidStack fluid, @Nullable SteamTurbineRecipe hint) { return RECIPES.find(level, recipe -> recipe.matches(fluid), hint); }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }
}
