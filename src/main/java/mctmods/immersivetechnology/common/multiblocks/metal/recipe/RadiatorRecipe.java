package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer.RadiatorRecipeSerializer;
import mctmods.immersivetechnology.core.registration.ITRecipeTypes;
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

public class RadiatorRecipe extends MultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, RadiatorRecipeSerializer> SERIALIZER;
    public static final CachedRecipeList<RadiatorRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.RADIATOR);

    private final ResourceLocation id;
    public final TagKey<Fluid> fluidTag;
    private final int amount;
    @Nullable public final FluidStack fluidOutput;
    private final int time;

    public RadiatorRecipe(ResourceLocation id, TagKey<Fluid> fluidTag, int amount, @Nullable FluidStack fluidOutput, int time) {
        super(TagOutput.EMPTY, ITRecipeTypes.RADIATOR, time, 0, () -> new MultiblockRecipe.RecipeMultiplier(() -> 1.0, () -> 1.0));
        this.id = id;
        this.fluidTag = fluidTag;
        this.amount = amount;
        this.fluidOutput = fluidOutput;
        this.time = time;
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

    @Nullable public static RadiatorRecipe findRecipe(Level level, FluidStack fluidInput) {
        if (fluidInput == null || fluidInput.isEmpty()) return null;
        for (var holder : RECIPES.getRecipes(level)) {
            RadiatorRecipe recipe = holder.value();
            if (recipe.matches(fluidInput) && fluidInput.getAmount() >= recipe.getInputAmount()) return recipe;
        }
        return null;
    }

    @Override @NotNull public ItemStack getResultItem(HolderLookup.Provider registryAccess) { return ItemStack.EMPTY; }

    @Override protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override public int getTotalProcessTime() { return time; }

    @Override public int getTotalProcessEnergy() { return 0; }

    @Override public int getMultipleProcessTicks() { return 0; }
}
