package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
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

public class GasTurbineRecipe extends IESerializableRecipe {
    public static RegistryObject<IERecipeSerializer<GasTurbineRecipe>> SERIALIZER;
    public static final CachedRecipeList<GasTurbineRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.GAS_TURBINE);

    public final FluidTagInput input;
    @Nullable public final FluidStack fluidOutput;
    public final float torque;
    private final int time;
    Lazy<Integer> totalProcessTime;

    public GasTurbineRecipe(ResourceLocation id, FluidTagInput input, @Nullable FluidStack fluidOutput, int time, float torque) {
        super(LAZY_EMPTY, ITRecipeTypes.GAS_TURBINE, id);
        this.input = input;
        this.fluidOutput = fluidOutput;
        this.time = time;
        this.torque = torque;
        totalProcessTime = Lazy.of(() -> this.time);
    }

    @Override protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override @NotNull public ItemStack getResultItem(@NotNull RegistryAccess registryAccess) { return ItemStack.EMPTY; }

    public boolean matches(FluidStack fluid) { return input.test(fluid); }

    public static GasTurbineRecipe findRecipe(Level level, FluidStack fluid, @Nullable GasTurbineRecipe hint) {
        if (hint != null && hint.matches(fluid)) return hint;
        for (GasTurbineRecipe recipe : RECIPES.getRecipes(level)) { if (recipe.matches(fluid)) return recipe; }
        return null;
    }

    public int getTotalProcessTime() { return totalProcessTime.get(); }
}
