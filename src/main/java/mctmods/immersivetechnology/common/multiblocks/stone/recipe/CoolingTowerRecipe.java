package mctmods.immersivetechnology.common.multiblocks.stone.recipe;

import mctmods.immersivetechnology.common.multiblocks.stone.recipe.serializer.CoolingTowerRecipeSerializer;
import mctmods.immersivetechnology.core.registration.RecipeTypes;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CoolingTowerRecipe extends MultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, CoolingTowerRecipeSerializer> SERIALIZER;
    public static final CachedRecipeList<CoolingTowerRecipe> RECIPES = new CachedRecipeList<>(RecipeTypes.COOLING_TOWER);

    private final ResourceLocation id;
    public final TagKey<Fluid> inputTag0;
    private final int amount0;
    public final TagKey<Fluid> inputTag1;
    private final int amount1;
    public final FluidStack fluidOutput0;
    public final FluidStack fluidOutput1;
    public final FluidStack fluidOutput2;
    private final int time;

    public CoolingTowerRecipe(ResourceLocation id, FluidStack fluidOutput0, FluidStack fluidOutput1, FluidStack fluidOutput2, TagKey<Fluid> inputTag0, int amount0, TagKey<Fluid> inputTag1, int amount1, int time) {
        super(TagOutput.EMPTY, RecipeTypes.COOLING_TOWER, time, 0, () -> new MultiblockRecipe.RecipeMultiplier(() -> 1.0, () -> 1.0));
        this.id = id;
        this.inputTag0 = inputTag0;
        this.amount0 = amount0;
        this.inputTag1 = inputTag1;
        this.amount1 = amount1;
        this.fluidOutput0 = fluidOutput0;
        this.fluidOutput1 = fluidOutput1;
        this.fluidOutput2 = fluidOutput2;
        this.time = time;
    }

    public CoolingTowerRecipe(FluidStack fluidOutput0, FluidStack fluidOutput1, FluidStack fluidOutput2, TagKey<Fluid> inputTag0, int amount0, TagKey<Fluid> inputTag1, int amount1, int time) {
        this(ResourceLocation.fromNamespaceAndPath("immersivetechnology", "codec_generated"), fluidOutput0, fluidOutput1, fluidOutput2, inputTag0, amount0, inputTag1, amount1, time);
    }

    public ResourceLocation getId() { return id; }
    public TagKey<Fluid> inputTag0() { return inputTag0; }
    public int amount0() { return amount0; }
    public int getInput0Amount() { return amount0; }
    public TagKey<Fluid> inputTag1() { return inputTag1; }
    public int amount1() { return amount1; }
    public int getInput1Amount() { return amount1; }
    public FluidStack fluidOutput0() { return fluidOutput0; }
    public FluidStack fluidOutput1() { return fluidOutput1; }
    public FluidStack fluidOutput2() { return fluidOutput2; }

    public boolean matches(FluidStack in0, FluidStack in1) {
        return in0 != null && in0.is(this.inputTag0) && in1 != null && in1.is(this.inputTag1);
    }

    @Nullable public static CoolingTowerRecipe findRecipe(Level level, FluidStack fluidInput0, FluidStack fluidInput1) { return findRecipe(level, fluidInput0, fluidInput1, null); }

    @Nullable public static CoolingTowerRecipe findRecipe(Level level, FluidStack fluidInput0, FluidStack fluidInput1, @Nullable CoolingTowerRecipe hint) {
        if (fluidInput0 == null || fluidInput0.isEmpty() || fluidInput1 == null || fluidInput1.isEmpty()) return null;
        if (hint != null && hint.matches(fluidInput0, fluidInput1) && fluidInput0.getAmount() >= hint.getInput0Amount() && fluidInput1.getAmount() >= hint.getInput1Amount()) return hint;
        for (var holder : RECIPES.getRecipes(level)) {
            CoolingTowerRecipe recipe = holder.value();
            if (recipe.matches(fluidInput0, fluidInput1) && fluidInput0.getAmount() >= recipe.getInput0Amount() && fluidInput1.getAmount() >= recipe.getInput1Amount()) return recipe;
        }
        return null;
    }

    @Override @NotNull public ItemStack getResultItem(HolderLookup.Provider registryAccess) { return ItemStack.EMPTY; }

    @Override protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override public int getTotalProcessTime() { return time; }

    @Override public int getTotalProcessEnergy() { return 0; }

    @Override public int getMultipleProcessTicks() { return 0; }
}
