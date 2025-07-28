package mctmods.immersivetechnology.common.blocks.multiblocks.recipe;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
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

public class BoilerFuelRecipe extends MultiblockRecipe {
    public static RegistryObject<IERecipeSerializer<BoilerFuelRecipe>> SERIALIZER;
    public static final CachedRecipeList<BoilerFuelRecipe> FUEL_RECIPES = new CachedRecipeList<>(ITRecipeTypes.BOILER_FUEL);

    public FluidTagInput fuel;
    private final int time;
    private final int heatPerTick;
    Lazy<Integer> totalProcessTime;

    public BoilerFuelRecipe(ResourceLocation id, FluidTagInput fuel, int time, int heatPerTick) {
        super(Lazy.of(() -> ItemStack.EMPTY), ITRecipeTypes.BOILER_FUEL, id);
        this.fuel = fuel;
        this.time = time;
        this.heatPerTick = heatPerTick;
        totalProcessTime = Lazy.of(() -> this.time);
        this.fluidInputList = Lists.newArrayList(this.fuel);
    }

    public static BoilerFuelRecipe findFuel(Level level, FluidStack fuel) {
        for (BoilerFuelRecipe recipe : FUEL_RECIPES.getRecipes(level)) { if (recipe.fuel.test(fuel)) return recipe; }
        return null;
    }

    @Override
    public @NotNull ItemStack getResultItem(RegistryAccess registryAccess) { return ItemStack.EMPTY; }

    @Override
    protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override
    public int getTotalProcessTime() { return totalProcessTime.get(); }

    @Override
    public int getTotalProcessEnergy() { return 0; }

    @Override
    public int getMultipleProcessTicks() { return 0; }

    public int getHeatPerTick() { return heatPerTick; }
}
