package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICFluidTagInput;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.immersiveconvergence.api.crafting.ICSerializableRecipe;
import mctmods.immersivetechnology.core.registration.RecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SteamTurbineRecipe extends ICSerializableRecipe {
    public static RegistryObject<ICRecipeSerializer<SteamTurbineRecipe>> SERIALIZER;
    public static final ICCachedRecipeList<SteamTurbineRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.STEAM_TURBINE);

    public final ICFluidTagInput input;
    @Nullable public final FluidStack fluidOutput;
    public final float torque;
    private final int time;
    Lazy<Integer> totalProcessTime;

    public SteamTurbineRecipe(ResourceLocation id, ICFluidTagInput input, @Nullable FluidStack fluidOutput, int time, float torque) {
        super(LAZY_EMPTY, RecipeTypes.STEAM_TURBINE, id);
        this.input = input;
        this.fluidOutput = fluidOutput;
        this.time = time;
        this.torque = torque;
        totalProcessTime = Lazy.of(() -> this.time);
    }

    @Override
    protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }

    @Override @Nonnull public ItemStack getResultItem(@Nonnull RegistryAccess registryAccess) { return ItemStack.EMPTY; }

    public boolean matches(FluidStack fluid) { return input.test(fluid); }

    public static SteamTurbineRecipe findRecipe(Level level, FluidStack fluid, @Nullable SteamTurbineRecipe hint) {
        if (hint != null && hint.matches(fluid)) return hint;
        for (SteamTurbineRecipe recipe : RECIPES.getRecipes(level)) { if (recipe.matches(fluid)) return recipe; }
        return null;
    }

    public int getTotalProcessTime() { return totalProcessTime.get(); }
}
