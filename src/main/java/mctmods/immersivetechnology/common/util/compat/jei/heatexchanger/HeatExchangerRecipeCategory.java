package mctmods.immersivetechnology.common.util.compat.jei.heatexchanger;

import mctmods.immersivetechnology.api.crafting.HeatExchangerRecipe;
import mctmods.immersivetechnology.common.util.compat.jei.GenericMultiblockIngredient;
import mctmods.immersivetechnology.common.util.compat.jei.ITRecipeCategory;
import mctmods.immersivetechnology.common.util.compat.jei.JEIHelper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;

public class HeatExchangerRecipeCategory extends ITRecipeCategory<HeatExchangerRecipe, HeatExchangerRecipeWrapper> {

    public static ResourceLocation background = new ResourceLocation("immersivetech:textures/gui/gui_heat_exchanger.png");
    private final IDrawable tankOverlay;
    private final IDrawableAnimated arrow;
    private final IDrawableAnimated drops;

    @SuppressWarnings("deprecation")
    public HeatExchangerRecipeCategory(IGuiHelper helper) {
        super("heatExchanger", "tile.immersivetech.metal_multiblock1.heat_exchanger.name", helper.createDrawable(background, 0, 0, 176, 64), HeatExchangerRecipe.class, GenericMultiblockIngredient.HEAT_EXCHANGER);
        tankOverlay = helper.createDrawable(background, 178, 2, 16, 47, -2, 2, -2, 2);
        IDrawableStatic staticImage = helper.createDrawable(background, 196, 0, 32, 9);
        this.arrow = helper.createAnimatedDrawable(staticImage, 200, IDrawableAnimated.StartDirection.LEFT, false);
        staticImage = helper.createDrawable(background, 196, 9, 20, 15);
        this.drops = helper.createAnimatedDrawable(staticImage, 200, IDrawableAnimated.StartDirection.BOTTOM, false);
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull HeatExchangerRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
        List<List<FluidStack>> inputs = ingredients.getInputs(VanillaTypes.FLUID);
        List<List<FluidStack>> outputs = ingredients.getOutputs(VanillaTypes.FLUID);

        int tankSize = 0;
        for (List<FluidStack> lists : inputs) {
            for (FluidStack fluid : lists) if (fluid.amount > tankSize) tankSize = fluid.amount;
        }
        for (List<FluidStack> lists : outputs) {
            for (FluidStack fluid : lists) if (fluid.amount > tankSize) tankSize = fluid.amount;
        }

        int tankIndex = 0;
        IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
        guiFluidStacks.init(tankIndex, true, 35, 12, 16, 47, tankSize, true, tankOverlay);
        guiFluidStacks.set(tankIndex, !inputs.isEmpty() ? inputs.get(0) : null);
        tankIndex++;
        guiFluidStacks.init(tankIndex, true, 12, 12, 16, 47, tankSize, true, tankOverlay);
        guiFluidStacks.set(tankIndex, inputs.size() > 1 ? inputs.get(1) : null);
        tankIndex++;
        guiFluidStacks.init(tankIndex, false, 125, 12, 16, 47, tankSize, true, tankOverlay);
        guiFluidStacks.set(tankIndex, !outputs.isEmpty() ? outputs.get(0) : null);
        tankIndex++;
        guiFluidStacks.init(tankIndex, false, 148, 12, 16, 47, tankSize, true, tankOverlay);
        guiFluidStacks.set(tankIndex, outputs.size() > 1 ? outputs.get(1) : null);
        guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
    }

    @Override
    public @Nonnull IRecipeWrapper getRecipeWrapper(@Nonnull HeatExchangerRecipe recipe) { return new HeatExchangerRecipeWrapper(recipe); }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft) {
        arrow.draw(minecraft, 73, 50);
        drops.draw(minecraft, 73, 40);
    }
}
