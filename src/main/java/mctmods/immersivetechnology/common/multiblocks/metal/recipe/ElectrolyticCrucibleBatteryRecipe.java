package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

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
import java.util.List;

public class ElectrolyticCrucibleBatteryRecipe extends MultiblockRecipe {
    public static RegistryObject<IERecipeSerializer<ElectrolyticCrucibleBatteryRecipe>> SERIALIZER;
    public static final CachedRecipeList<ElectrolyticCrucibleBatteryRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.ELECTROLYTIC_CRUCIBLE_BATTERY);

    public final FluidTagInput fluidInput0;
    @Nullable public final FluidStack fluidOutput0;
    @Nullable public final FluidStack fluidOutput1;
    @Nullable public final FluidStack fluidOutput2;
    public final ItemStack itemOutput;

    private final int time;
    private final int energy;

    Lazy<Integer> totalProcessTime;
    Lazy<Integer> totalProcessEnergy;

    public ElectrolyticCrucibleBatteryRecipe(ResourceLocation id, FluidTagInput fluidInput0, @Nullable FluidStack fluidOutput0, @Nullable FluidStack fluidOutput1, @Nullable FluidStack fluidOutput2, ItemStack itemOutput, int energy, int time) {
        super(Lazy.of(() -> ItemStack.EMPTY), ITRecipeTypes.ELECTROLYTIC_CRUCIBLE_BATTERY, id);
        this.fluidInput0 = fluidInput0;
        this.fluidOutput0 = fluidOutput0;
        this.fluidOutput1 = fluidOutput1;
        this.fluidOutput2 = fluidOutput2;
        this.itemOutput = itemOutput;
        this.time = time;
        this.energy = energy;

        totalProcessTime = Lazy.of(() -> this.time);
        totalProcessEnergy = Lazy.of(() -> this.energy);

        this.fluidInputList = Lists.newArrayList(fluidInput0);

        List<FluidStack> outputs = Lists.newArrayList();
        if (fluidOutput0 != null) outputs.add(fluidOutput0);
        if (fluidOutput1 != null) outputs.add(fluidOutput1);
        if (fluidOutput2 != null) outputs.add(fluidOutput2);
        this.fluidOutputList = outputs.isEmpty() ? null : outputs;

        this.outputList = Lazy.of(NonNullList::create);
    }

    public boolean matches(FluidStack fluid) { return fluidInput0.test(fluid); }

    public static ElectrolyticCrucibleBatteryRecipe findRecipe(Level level, FluidStack input) { return findRecipe(level, input, null); }

    public static ElectrolyticCrucibleBatteryRecipe findRecipe(Level level, FluidStack input, @Nullable ElectrolyticCrucibleBatteryRecipe hint) {
        if (hint != null && hint.matches(input)) return hint;
        for (ElectrolyticCrucibleBatteryRecipe recipe : RECIPES.getRecipes(level)) { if (recipe.matches(input)) return recipe; }
        return null;
    }

    @Override @NotNull public ItemStack getResultItem(RegistryAccess registryAccess) { return ItemStack.EMPTY; }

    @Override protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override public int getTotalProcessTime() { return totalProcessTime.get(); }

    @Override public int getTotalProcessEnergy() { return totalProcessEnergy.get(); }

    @Override public int getMultipleProcessTicks() { return 0; }
}
