package mctmods.immersivetechnology.common.util.compat.jei.coolingtower;

import mctmods.immersivetechnology.api.crafting.CoolingTowerRecipe;
import mctmods.immersivetechnology.common.util.compat.jei.GenericMultiblockIngredient;
import mctmods.immersivetechnology.common.util.compat.jei.ITRecipeCategory;
import mctmods.immersivetechnology.common.util.compat.jei.JEIHelper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;

public class CoolingTowerRecipeCategory extends ITRecipeCategory<CoolingTowerRecipe, CoolingTowerRecipeWrapper> {
    public static ResourceLocation background = new ResourceLocation("immersivetech:textures/gui/cooling_tower_jei.png");
    private final IDrawable tankOverlay;
    private final IDrawableAnimated arrow;
    private final IDrawableAnimated drops;

    @SuppressWarnings("deprecation")
    public CoolingTowerRecipeCategory(IGuiHelper helper) {
        super("coolingTower", "tile.immersivetech.metal_multiblock.cooling_tower.name", helper.createDrawable(background, 0, 0, 159, 69), CoolingTowerRecipe.class, GenericMultiblockIngredient.COOLING_TOWER);
        tankOverlay = helper.createDrawable(background, 161, 2, 16, 47, -2, 2, -2, 2);
        IDrawableStatic staticImage = helper.createDrawable(background, 17, 69, 32, 9);
        this.arrow = helper.createAnimatedDrawable(staticImage, 200, IDrawableAnimated.StartDirection.LEFT, false);
        staticImage = helper.createDrawable(background, 0, 69, 17, 23);
        this.drops = helper.createAnimatedDrawable(staticImage, 200, IDrawableAnimated.StartDirection.TOP, false);
    }

    @SuppressWarnings("deprecation")
    @Override public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull CoolingTowerRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
        List<List<FluidStack>> inputs = ingredients.getInputs(FluidStack.class);
        List<List<FluidStack>> outputs = ingredients.getOutputs(FluidStack.class);

        int tankSize = getMaxFluidAmount(inputs, outputs);

        int tankIndex = 0;
        IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
        if (!inputs.isEmpty()) {
            guiFluidStacks.init(tankIndex, true, 11, 11, 16, 47, tankSize, true, tankOverlay);
            guiFluidStacks.set(tankIndex, inputs.get(0));
            if (inputs.size() == 2) {
                tankIndex++;
                guiFluidStacks.init(tankIndex, true, 34, 11, 16, 47, tankSize, true, tankOverlay);
                guiFluidStacks.set(tankIndex, inputs.get(1));
            }
        }

        if (!outputs.isEmpty()) {
            tankIndex++;
            guiFluidStacks.init(tankIndex, false, 86, 11, 16, 47, tankSize, true, tankOverlay);
            guiFluidStacks.set(tankIndex, outputs.get(0));
            if (outputs.size() == 2) {
                tankIndex++;
                guiFluidStacks.init(tankIndex, false, 109, 11, 16, 47, tankSize, true, tankOverlay);
                guiFluidStacks.set(tankIndex, outputs.get(1));
            }
            if (outputs.size() == 3) {
                tankIndex++;
                guiFluidStacks.init(tankIndex, false, 109, 11, 16, 47, tankSize, true, tankOverlay);
                guiFluidStacks.set(tankIndex, outputs.get(1));
                tankIndex++;
                guiFluidStacks.init(tankIndex, false, 132, 11, 16, 47, tankSize, true, tankOverlay);
                guiFluidStacks.set(tankIndex, outputs.get(2));
            }
        }
        guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
    }

    @Override @Nonnull public IRecipeWrapper getRecipeWrapper(@Nonnull CoolingTowerRecipe recipe) {
        return new CoolingTowerRecipeWrapper(recipe);
    }

    @Override public void drawExtras(@Nonnull Minecraft minecraft) {
        arrow.draw(minecraft, 52, 51);
        drops.draw(minecraft, 55, 32);
    }
}
