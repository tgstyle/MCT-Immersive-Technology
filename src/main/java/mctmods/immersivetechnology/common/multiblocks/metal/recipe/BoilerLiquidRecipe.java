package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import com.google.common.collect.Lists;
import com.immersiveconvergence.api.HeatCapabilities;
import mctmods.immersivetechnology.core.registration.ITRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class BoilerLiquidRecipe extends MultiblockRecipe {
    public static RegistryObject<IERecipeSerializer<BoilerLiquidRecipe>> SERIALIZER;
    public static final CachedRecipeList<BoilerLiquidRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.BOILER_LIQUID);

    public final FluidTagInput input;
    private final int time;
    private final double heatPerTick;
    private final double targetHeat;
    Lazy<Integer> totalProcessTime;

    public BoilerLiquidRecipe(ResourceLocation id, FluidTagInput input, int time, double heatPerTick, double targetHeat) {
        super(Lazy.of(() -> ItemStack.EMPTY), ITRecipeTypes.BOILER_LIQUID, id);
        this.input = input;
        this.time = time;
        this.heatPerTick = heatPerTick;
        this.targetHeat = Math.min(targetHeat, HeatCapabilities.MAX_HEAT);
        totalProcessTime = Lazy.of(() -> this.time);
        this.fluidInputList = Lists.newArrayList(this.input);
    }

    public static BoilerLiquidRecipe findRecipe(Level level, FluidStack input) {
        for (BoilerLiquidRecipe recipe : RECIPES.getRecipes(level)) { if (recipe.input.test(input)) return recipe; }
        return null;
    }

    @Override @NotNull public ItemStack getResultItem(RegistryAccess registryAccess) { return ItemStack.EMPTY; }

    @Override protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override public int getTotalProcessTime() { return totalProcessTime.get(); }

    @Override public int getTotalProcessEnergy() { return 0; }

    @Override public int getMultipleProcessTicks() { return 0; }

    public double getHeatPerTick() { return heatPerTick; }

    public double getTargetHeat() { return targetHeat; }
}
