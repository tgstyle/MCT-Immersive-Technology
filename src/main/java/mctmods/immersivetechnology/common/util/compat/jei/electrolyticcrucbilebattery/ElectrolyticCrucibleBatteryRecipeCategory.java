package mctmods.immersivetechnology.common.util.compat.jei.electrolyticcrucbilebattery;

import mctmods.immersivetechnology.api.crafting.ElectrolyticCrucibleBatteryRecipe;
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

public class ElectrolyticCrucibleBatteryRecipeCategory extends ITRecipeCategory<ElectrolyticCrucibleBatteryRecipe, ElectrolyticCrucibleBatteryWrapper> {
    public static ResourceLocation background = new ResourceLocation("immersivetech:textures/gui/gui_electrolytic_crucible_battery_jei.png");
    private final IDrawable tankOverlay;
    private final IDrawableAnimated arrow;

    public ElectrolyticCrucibleBatteryRecipeCategory(IGuiHelper helper) {
        super("electrolyticCrucibleBattery", "tile.immersivetech.metal_multiblock1.electrolytic_crucible_battery.name", helper.createDrawable(background, 0, 0, 176, 64), ElectrolyticCrucibleBatteryRecipe.class, GenericMultiblockIngredient.ELECTROLYTIC_CRUCIBLE_BATTERY);
        tankOverlay = helper.drawableBuilder(background, 178, 2, 16, 47).addPadding(-2, 2, -2, 2).build();
        IDrawableStatic staticImage = helper.createDrawable(background, 196, 0, 32, 18);
        this.arrow = helper.createAnimatedDrawable(staticImage, 200, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull ElectrolyticCrucibleBatteryWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
        List<List<FluidStack>> inputs = ingredients.getInputs(VanillaTypes.FLUID);
        List<List<FluidStack>> outputs = ingredients.getOutputs(VanillaTypes.FLUID);

        int tankSize = getMaxFluidAmount(inputs, outputs);

        int tankIndex = 0;
        IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
        guiFluidStacks.init(tankIndex, false, 12, 12, 16, 47, tankSize, true, tankOverlay);
        guiFluidStacks.set(tankIndex, inputs.get(0));

        tankIndex++;
        guiFluidStacks.init(tankIndex, false, 102, 12, 16, 47, tankSize, true, tankOverlay);
        guiFluidStacks.set(tankIndex, outputs.isEmpty() ? null : outputs.get(0));

        tankIndex++;
        guiFluidStacks.init(tankIndex, false, 125, 12, 16, 47, tankSize, true, tankOverlay);
        guiFluidStacks.set(tankIndex, outputs.size() < 2 ? null : outputs.get(1));

        tankIndex++;
        guiFluidStacks.init(tankIndex, false, 148, 12, 16, 47, tankSize, true, tankOverlay);
        guiFluidStacks.set(tankIndex, outputs.size() < 3 ? null : outputs.get(2));

        guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
    }

    @Override @Nonnull public IRecipeWrapper getRecipeWrapper(@Nonnull ElectrolyticCrucibleBatteryRecipe recipe) {
        return new ElectrolyticCrucibleBatteryWrapper(recipe);
    }

    @Override public void drawExtras(@Nonnull Minecraft minecraft) {
        arrow.draw(minecraft, 50, 39);
    }
}
