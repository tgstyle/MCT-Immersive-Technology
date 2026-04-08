package mctmods.immersivetechnology.common.util.compat.jei.advancedcokeoven;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;

import mctmods.immersivetechnology.common.util.compat.jei.GenericMultiblockIngredient;
import mctmods.immersivetechnology.common.util.compat.jei.ITRecipeCategory;
import mctmods.immersivetechnology.common.util.compat.jei.JEIHelper;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;

public class AdvancedCokeOvenRecipeCategory extends ITRecipeCategory<CokeOvenRecipe, AdvancedCokeOvenRecipeWrapper> {
    public static ResourceLocation background = new ResourceLocation("immersiveengineering:textures/gui/coke_oven.png");
    private final IDrawable tankOverlay;

    public AdvancedCokeOvenRecipeCategory(IGuiHelper helper) {
        super("advancedCokeOven", "tile.immersivetech.stone_multiblock.advanced_coke_oven.name", helper.createDrawable(background, 8, 13, 142, 60), CokeOvenRecipe.class, GenericMultiblockIngredient.ADVANCED_COKE_OVEN);
        tankOverlay = helper.drawableBuilder(background, 176, 31, 16, 47).addPadding(-2, 2, -2, 2).build();
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull AdvancedCokeOvenRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
        guiItemStacks.init(0, true, 21, 21);
        guiItemStacks.init(1, false, 76, 21);

        List<List<ItemStack>> itemInputs = ingredients.getInputs(VanillaTypes.ITEM);
        guiItemStacks.set(0, itemInputs.get(0));

        List<List<ItemStack>> itemOutputs = ingredients.getOutputs(VanillaTypes.ITEM);
        if (!itemOutputs.isEmpty()) { guiItemStacks.set(1, itemOutputs.get(0)); }

        List<List<FluidStack>> fluidOutputs = ingredients.getOutputs(VanillaTypes.FLUID);
        if (!fluidOutputs.isEmpty()) {
            List<FluidStack> lfs = fluidOutputs.get(0);
            if (!lfs.isEmpty()) {
                int capacity = lfs.get(0).amount <= 1000 ? 4000 : 12000;
                IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
                guiFluidStacks.init(0, false, 121, 7, 16, 47, capacity, false, tankOverlay);
                guiFluidStacks.set(0, lfs);
                guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
            }
        }
    }

    @Override @Nonnull public IRecipeWrapper getRecipeWrapper(@Nonnull CokeOvenRecipe recipe) { return new AdvancedCokeOvenRecipeWrapper(recipe); }
}
