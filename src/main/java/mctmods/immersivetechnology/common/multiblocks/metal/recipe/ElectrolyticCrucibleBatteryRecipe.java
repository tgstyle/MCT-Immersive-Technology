package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.core.registration.RecipeTypes;

import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICFluidTagInput;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ElectrolyticCrucibleBatteryRecipe extends ICMultiblockRecipe {
    public static RegistryObject<ICRecipeSerializer<ElectrolyticCrucibleBatteryRecipe>> SERIALIZER;
    public static final ICCachedRecipeList<ElectrolyticCrucibleBatteryRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.ELECTROLYTIC_CRUCIBLE_BATTERY);

    public final ICFluidTagInput fluidInput0;
    @Nullable public final FluidStack fluidOutput0;
    @Nullable public final FluidStack fluidOutput1;
    @Nullable public final FluidStack fluidOutput2;
    public final ItemStack itemOutput;

    private final int time;
    private final int energy;

    Lazy<Integer> totalProcessTime;
    Lazy<Integer> totalProcessEnergy;

    public ElectrolyticCrucibleBatteryRecipe(ResourceLocation id, ICFluidTagInput fluidInput0, @Nullable FluidStack fluidOutput0, @Nullable FluidStack fluidOutput1, @Nullable FluidStack fluidOutput2, ItemStack itemOutput, int energy, int time) {
        super(Lazy.of(() -> ItemStack.EMPTY), RecipeTypes.ELECTROLYTIC_CRUCIBLE_BATTERY, id);
        this.fluidInput0 = fluidInput0;
        this.fluidOutput0 = fluidOutput0;
        this.fluidOutput1 = fluidOutput1;
        this.fluidOutput2 = fluidOutput2;
        this.itemOutput = itemOutput;
        this.time = time;
        this.energy = energy;

        totalProcessTime = Lazy.of(() -> this.time);
        totalProcessEnergy = Lazy.of(() -> this.energy);

        this.outputList = Lazy.of(NonNullList::create);
    }

    public boolean matches(FluidStack fluid) { return fluidInput0.test(fluid); }

    public static ElectrolyticCrucibleBatteryRecipe findRecipe(Level level, FluidStack input) { return findRecipe(level, input, null); }

    public static ElectrolyticCrucibleBatteryRecipe findRecipe(Level level, FluidStack input, @Nullable ElectrolyticCrucibleBatteryRecipe hint) {
        if (hint != null && hint.matches(input)) return hint;
        for (ElectrolyticCrucibleBatteryRecipe recipe : RECIPES.getRecipes(level)) { if (recipe.matches(input)) return recipe; }
        return null;
    }

    @Override @Nonnull public ItemStack getResultItem(@Nonnull RegistryAccess registryAccess) { return ItemStack.EMPTY; }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }

    @Override public int getTotalProcessTime() { return totalProcessTime.get(); }

    @Override public int getTotalProcessEnergy() { return totalProcessEnergy.get(); }

    @Override public int getMultipleProcessTicks() { return 0; }
}
