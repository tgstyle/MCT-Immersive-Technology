package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer.HeatExchangerRecipeSerializer;
import mctmods.immersivetechnology.core.registration.RecipeTypes;

import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.immersiveconvergence.api.crafting.ICTagOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;

public class HeatExchangerRecipe extends ICMultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, HeatExchangerRecipeSerializer> SERIALIZER;
    public static final ICCachedRecipeList<HeatExchangerRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.HEAT_EXCHANGER);

    public final TagKey<Fluid> input0Tag;
    private final int input0Amount;
    @Nullable public final TagKey<Fluid> input1Tag;
    private final int input1Amount;
    public final FluidStack output0;
    @Nullable public final FluidStack output1;

    public HeatExchangerRecipe(@SuppressWarnings("unused") ResourceLocation id, TagKey<Fluid> input0Tag, int input0Amount, @Nullable TagKey<Fluid> input1Tag, int input1Amount, FluidStack output0, @Nullable FluidStack output1, int energy, int time) {
        super(ICTagOutput.EMPTY, RecipeTypes.HEAT_EXCHANGER, time, energy);
        this.input0Tag = input0Tag;
        this.input0Amount = input0Amount;
        this.input1Tag = input1Tag;
        this.input1Amount = input1Amount;
        this.output0 = output0;
        this.output1 = output1;
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
        return RECIPES.findHolder(level, recipe -> recipe.matchesInput0(input0) && recipe.matchesInput1(input1), hint);
    }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }
}
