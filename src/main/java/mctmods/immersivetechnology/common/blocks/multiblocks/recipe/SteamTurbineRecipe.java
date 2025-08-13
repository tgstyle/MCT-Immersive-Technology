package mctmods.immersivetechnology.common.blocks.multiblocks.recipe;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
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
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class SteamTurbineRecipe extends IESerializableRecipe {
    public static RegistryObject<IERecipeSerializer<SteamTurbineRecipe>> SERIALIZER;
    public static final CachedRecipeList<SteamTurbineRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.STEAM_TURBINE);

    public final FluidTagInput input;
    @Nullable
    public final FluidStack fluidOutput;
    private final int time;
    Lazy<Integer> totalProcessTime;

    public SteamTurbineRecipe(ResourceLocation id, FluidTagInput input, @Nullable FluidStack fluidOutput, int time) {
        super(LAZY_EMPTY, ITRecipeTypes.STEAM_TURBINE, id);
        this.input = input;
        this.fluidOutput = fluidOutput;
        this.time = time;
        totalProcessTime = Lazy.of(() -> this.time);
    }

    @Override
    protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) { return ItemStack.EMPTY; }

    public boolean matches(FluidStack fluid) { return input.test(fluid); }

    public static SteamTurbineRecipe findRecipe(Level level, FluidStack fluid, @Nullable SteamTurbineRecipe hint) {
        if (hint != null && hint.matches(fluid)) return hint;
        for (SteamTurbineRecipe recipe : RECIPES.getRecipes(level)) { if (recipe.matches(fluid)) return recipe; }
        return null;
    }

    public int getTotalProcessTime() { return totalProcessTime.get(); }
}
