package mctmods.immersivetechnology.common.blocks.multiblocks.recipe;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import mctmods.immersivetechnology.core.registration.ITRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class SteamTurbineRecipe extends IESerializableRecipe {
    public static RegistryObject<IERecipeSerializer<SteamTurbineRecipe>> SERIALIZER;

    public static CachedRecipeList<SteamTurbineRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.STEAM_TURBINE);

    public TagKey<Fluid> inputTag;
    public int inputAmount;
    @Nullable
    public FluidStack fluidOutput;
    private int time;

    public SteamTurbineRecipe(ResourceLocation id, TagKey<Fluid> inputTag, int inputAmount, @Nullable FluidStack fluidOutput, int time) {
        super(LAZY_EMPTY, ITRecipeTypes.STEAM_TURBINE, id);
        this.inputTag = inputTag;
        this.inputAmount = inputAmount;
        this.fluidOutput = fluidOutput;
        this.time = time;
    }

    @Override
    protected IERecipeSerializer<?> getIESerializer() {
        return SERIALIZER.get();
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    public boolean matches(FluidStack fluid) {
        return fluid.getFluid().is(inputTag) && fluid.getAmount() >= inputAmount;
    }

    public static SteamTurbineRecipe findFuel(Level level, FluidStack fluid, @Nullable SteamTurbineRecipe hint) {
        if (hint != null && hint.matches(fluid)) {
            return hint;
        }
        for (SteamTurbineRecipe recipe : RECIPES.getRecipes(level)) {
            if (recipe.matches(fluid)) {
                return recipe;
            }
        }
        return null;
    }

    public int getTotalProcessTime() {
        return time;
    }
}