package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer.HeatExchangerRecipeSerializer;
import mctmods.immersivetechnology.core.registration.RecipeTypes;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HeatExchangerRecipe extends MultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, HeatExchangerRecipeSerializer> SERIALIZER;
    public static final CachedRecipeList<HeatExchangerRecipe> RECIPES = new CachedRecipeList<>(RecipeTypes.HEAT_EXCHANGER);

    public float timeModifier = 1;
    public float energyModifier = 1;

    public final TagKey<Fluid> input0Tag;
    private final int input0Amount;
    @Nullable public final TagKey<Fluid> input1Tag;
    private final int input1Amount;
    public final FluidStack output0;
    @Nullable public final FluidStack output1;

    private final int baseProcessTime;
    private final int baseProcessEnergy;

    public HeatExchangerRecipe(@SuppressWarnings("unused") ResourceLocation id, TagKey<Fluid> input0Tag, int input0Amount, @Nullable TagKey<Fluid> input1Tag, int input1Amount, FluidStack output0, @Nullable FluidStack output1, int energy, int time) {
        super(TagOutput.EMPTY, RecipeTypes.HEAT_EXCHANGER, time, energy, () -> new MultiblockRecipe.RecipeMultiplier(() -> 1.0, () -> 1.0));
        this.input0Tag = input0Tag;
        this.input0Amount = input0Amount;
        this.input1Tag = input1Tag;
        this.input1Amount = input1Amount;
        this.output0 = output0;
        this.output1 = output1;
        this.baseProcessTime = time;
        this.baseProcessEnergy = energy;
    }

    public HeatExchangerRecipe(TagKey<Fluid> input0Tag, int input0Amount, @Nullable TagKey<Fluid> input1Tag, int input1Amount, FluidStack output0, @Nullable FluidStack output1, int energy, int time) {
        this(ResourceLocation.fromNamespaceAndPath("immersivetechnology", "codec_generated"), input0Tag, input0Amount, input1Tag, input1Amount, output0, output1, energy, time);
    }

    public TagKey<Fluid> input0Tag() { return input0Tag; }
    public int input0Amount() { return input0Amount; }
    public int getInput0Amount() { return input0Amount; }
    public TagKey<Fluid> input1Tag() { return input1Tag; }
    public int input1Amount() { return input1Amount; }
    public int getInput1Amount() { return input1Amount; }
    public FluidStack output0() { return output0; }
    public FluidStack output1() { return output1; }

    public boolean matchesInput0(FluidStack stack) {
        return stack != null && stack.is(this.input0Tag);
    }

    public boolean matchesInput1(FluidStack stack) {
        return stack != null && (this.input1Tag == null || stack.is(this.input1Tag));
    }

    public static RecipeHolder<HeatExchangerRecipe> findRecipe(Level level, FluidStack input0, FluidStack input1) { return findRecipe(level, input0, input1, null); }

    public static RecipeHolder<HeatExchangerRecipe> findRecipe(Level level, FluidStack input0, FluidStack input1, @Nullable RecipeHolder<HeatExchangerRecipe> hint) {
        if ((input0 == null || input0.isEmpty()) && (input1 == null || input1.isEmpty())) return null;
        if (hint != null && hint.value().matchesInput0(input0) && hint.value().matchesInput1(input1)) return hint;
        for (RecipeHolder<HeatExchangerRecipe> holder : RECIPES.getRecipes(level)) {
            HeatExchangerRecipe recipe = holder.value();
            if (recipe.matchesInput0(input0) && recipe.matchesInput1(input1)) return holder;
        }
        return null;
    }

    @Override
    public int getTotalProcessTime() {
        return (int) (baseProcessTime * timeModifier);
    }

    @Override
    public int getTotalProcessEnergy() {
        return (int) (baseProcessEnergy * energyModifier);
    }

    @Override @NotNull public ItemStack getResultItem(HolderLookup.Provider registryAccess) { return ItemStack.EMPTY; }

    @Override protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override public int getMultipleProcessTicks() { return 0; }
}
