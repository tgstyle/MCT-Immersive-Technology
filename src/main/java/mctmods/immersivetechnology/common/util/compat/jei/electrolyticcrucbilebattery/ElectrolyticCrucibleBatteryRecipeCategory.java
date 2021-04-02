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

    @SuppressWarnings("deprecation")
    public ElectrolyticCrucibleBatteryRecipeCategory(IGuiHelper helper) {
        super("electrolyticCrucibleBattery", "tile.immersivetech.metal_multiblock1.electrolytic_crucible_battery.name", helper.createDrawable(background, 0, 0, 176, 64), ElectrolyticCrucibleBatteryRecipe.class, GenericMultiblockIngredient.ELECTROLYTIC_CRUCIBLE_BATTERY);
        tankOverlay = helper.createDrawable(background, 178, 2, 16, 47, -2, 2, -2, 2);
        IDrawableStatic staticImage = helper.createDrawable(background, 196, 0, 32, 18);
        this.arrow = helper.createAnimatedDrawable(staticImage, 200, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull ElectrolyticCrucibleBatteryWrapper recipeWrapper, IIngredients ingredients) {
        List<List<FluidStack>> inputs = ingredients.getInputs(VanillaTypes.FLUID);
        List<List<FluidStack>> outputs = ingredients.getOutputs(VanillaTypes.FLUID);

        int tankSize = 0;
        for(List<FluidStack> lists : inputs) {
            for(FluidStack fluid : lists) if(fluid.amount > tankSize) tankSize = fluid.amount;
        }
        for(List<FluidStack> lists : outputs) {
            for(FluidStack fluid : lists) if(fluid.amount > tankSize) tankSize = fluid.amount;
        }

        int tankIndex = 0;
        IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
        guiFluidStacks.init(tankIndex, false, 12, 12, 16, 47, tankSize, true, tankOverlay);
        guiFluidStacks.set(tankIndex, inputs.get(0));

        if(!outputs.isEmpty()) {
            tankIndex++;
            guiFluidStacks.init(tankIndex, false, 102, 12, 16, 47, tankSize, true, tankOverlay);
            guiFluidStacks.set(tankIndex, outputs.get(0));
            if(outputs.size() >= 2) {
                tankIndex++;
                guiFluidStacks.init(tankIndex, false, 125, 12, 16, 47, tankSize, true, tankOverlay);
                guiFluidStacks.set(tankIndex, outputs.get(1));
            }
            if(outputs.size() == 3) {
                tankIndex++;
                guiFluidStacks.init(tankIndex, false, 148, 12, 16, 47, tankSize, true, tankOverlay);
                guiFluidStacks.set(tankIndex, outputs.get(2));
            }
        }
        guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(ElectrolyticCrucibleBatteryRecipe recipe) {
        return new ElectrolyticCrucibleBatteryWrapper(recipe);
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        arrow.draw(minecraft, 50, 39);
    }
}