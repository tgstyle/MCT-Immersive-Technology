package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.core.registration.RecipeTypes;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;

public class RadiatorRecipe extends MultiblockRecipe {
    public static RegistryObject<IERecipeSerializer<RadiatorRecipe>> SERIALIZER;
    public static final CachedRecipeList<RadiatorRecipe> RECIPES = new CachedRecipeList<>(RecipeTypes.RADIATOR);

    public final FluidStack fluidOutput;
    public final FluidTagInput input;
    public final int totalProcessTime;
    private static final Lazy<Integer> totalProcessEnergy = Lazy.of(() -> 0);

    public RadiatorRecipe(ResourceLocation id, FluidStack fluidOutput, FluidTagInput input, int time) {
        super(Lazy.of(() -> ItemStack.EMPTY), RecipeTypes.RADIATOR, id);
        this.fluidOutput = fluidOutput;
        this.input = input;
        this.totalProcessTime = time;
        this.fluidInputList = Lists.newArrayList(input);
        this.fluidOutputList = Lists.newArrayList(fluidOutput);
        this.outputList = Lazy.of(NonNullList::create);
    }

    public boolean matches(FluidStack fluidInput) { return input.test(fluidInput) && fluidInput.getAmount() >= input.getAmount(); }

    public static RadiatorRecipe findRecipe(Level level, FluidStack fluidInput) { return findRecipe(level, fluidInput, null); }

    public static RadiatorRecipe findRecipe(Level level, FluidStack fluidInput, @Nullable RadiatorRecipe hint) {
        if (fluidInput.isEmpty()) return null;
        if (hint != null && hint.matches(fluidInput)) return hint;
        for (RadiatorRecipe r : RECIPES.getRecipes(level)) { if (r.matches(fluidInput)) return r; }
        return null;
    }

    @Override public @NotNull ItemStack getResultItem(RegistryAccess registryAccess) { return ItemStack.EMPTY; }

    @Override protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override public int getTotalProcessTime() { return totalProcessTime; }

    @Override public int getTotalProcessEnergy() { return totalProcessEnergy.get(); }

    @Override public int getMultipleProcessTicks() { return 0; }
}
