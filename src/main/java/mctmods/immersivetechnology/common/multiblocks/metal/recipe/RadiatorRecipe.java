package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer.RadiatorRecipeSerializer;
import mctmods.immersivetechnology.core.registration.RecipeTypes;

import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.immersiveconvergence.api.crafting.ICTagOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;

public class RadiatorRecipe extends ICMultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, RadiatorRecipeSerializer> SERIALIZER;
    public static final ICCachedRecipeList<RadiatorRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.RADIATOR);

    private final ResourceLocation id;
    public final TagKey<Fluid> fluidTag;
    private final int amount;
    @Nullable public final FluidStack fluidOutput;

    public RadiatorRecipe(ResourceLocation id, TagKey<Fluid> fluidTag, int amount, @Nullable FluidStack fluidOutput, int time) {
        super(ICTagOutput.EMPTY, RecipeTypes.RADIATOR, time, 0);
        this.id = id;
        this.fluidTag = fluidTag;
        this.amount = amount;
        this.fluidOutput = fluidOutput;
    }

    public RadiatorRecipe(TagKey<Fluid> fluidTag, int amount, @Nullable FluidStack fluidOutput, int time) {
        this(ResourceLocation.fromNamespaceAndPath("immersivetechnology", "codec_generated"), fluidTag, amount, fluidOutput, time);
    }

    public ResourceLocation getId() { return id; }
    public TagKey<Fluid> fluidTag() { return fluidTag; }
    public int amount() { return amount; }
    public int getInputAmount() { return amount; }
    public FluidStack fluidOutput() { return fluidOutput; }

    public boolean matches(FluidStack stack) {
        return stack != null && stack.is(this.fluidTag);
    }

    @Nullable public static RadiatorRecipe findRecipe(Level level, FluidStack fluidInput) { return findRecipe(level, fluidInput, null); }

    @Nullable public static RadiatorRecipe findRecipe(Level level, FluidStack fluidInput, @Nullable RadiatorRecipe hint) {
        if (fluidInput == null || fluidInput.isEmpty()) return null;
        return RECIPES.find(level, recipe -> recipe.matches(fluidInput) && fluidInput.getAmount() >= recipe.getInputAmount(), hint);
    }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }
}
