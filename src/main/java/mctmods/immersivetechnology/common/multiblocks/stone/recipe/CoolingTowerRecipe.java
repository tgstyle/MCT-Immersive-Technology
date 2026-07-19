package mctmods.immersivetechnology.common.multiblocks.stone.recipe;

import mctmods.immersivetechnology.core.registration.ITRecipeTypes;

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

public class CoolingTowerRecipe extends MultiblockRecipe {
    public static RegistryObject<IERecipeSerializer<CoolingTowerRecipe>> SERIALIZER;
    public static final CachedRecipeList<CoolingTowerRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.COOLING_TOWER);

    public final FluidStack fluidOutput0;
    public final FluidStack fluidOutput1;
    public final FluidStack fluidOutput2;
    public final FluidTagInput input0;
    public final FluidTagInput input1;
    public final int totalProcessTime;
    private static final Lazy<Integer> totalProcessEnergy = Lazy.of(() -> 0);

    public CoolingTowerRecipe(ResourceLocation id, FluidStack fluidOutput0, FluidStack fluidOutput1, FluidStack fluidOutput2, FluidTagInput input0, FluidTagInput input1, int time) {
        super(Lazy.of(() -> ItemStack.EMPTY), ITRecipeTypes.COOLING_TOWER, id);
        this.fluidOutput0 = fluidOutput0;
        this.fluidOutput1 = fluidOutput1;
        this.fluidOutput2 = fluidOutput2;
        this.input0 = input0;
        this.input1 = input1;
        this.totalProcessTime = time;
        this.fluidInputList = Lists.newArrayList(input0, input1);
        this.fluidOutputList = Lists.newArrayList(fluidOutput0, fluidOutput1, fluidOutput2);
        this.outputList = Lazy.of(NonNullList::create);
    }

    public boolean matches(FluidStack fluid0, FluidStack fluid1) { return input0.test(fluid0) && fluid0.getAmount() >= input0.getAmount() && input1.test(fluid1) && fluid1.getAmount() >= input1.getAmount(); }

    public static CoolingTowerRecipe findRecipe(Level level, FluidStack fluidInput0, FluidStack fluidInput1) { return findRecipe(level, fluidInput0, fluidInput1, null); }

    public static CoolingTowerRecipe findRecipe(Level level, FluidStack fluidInput0, FluidStack fluidInput1, @Nullable CoolingTowerRecipe hint) {
        if (fluidInput0.isEmpty() || fluidInput1.isEmpty()) return null;
        if (hint != null && hint.matches(fluidInput0, fluidInput1)) return hint;
        for (CoolingTowerRecipe r : RECIPES.getRecipes(level)) { if (r.matches(fluidInput0, fluidInput1)) return r; }
        return null;
    }

    @Override public @NotNull ItemStack getResultItem(RegistryAccess registryAccess) { return ItemStack.EMPTY; }

    @Override protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override public int getTotalProcessTime() { return totalProcessTime; }

    @Override public int getTotalProcessEnergy() { return totalProcessEnergy.get(); }

    @Override public int getMultipleProcessTicks() { return 0; }
}
