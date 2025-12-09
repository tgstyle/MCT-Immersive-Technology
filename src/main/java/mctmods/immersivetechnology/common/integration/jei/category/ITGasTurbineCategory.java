package mctmods.immersivetechnology.common.integration.jei.category;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.GasTurbineRecipe;
import mctmods.immersivetechnology.client.gui.helper.ITFluidInfoArea;
import mctmods.immersivetechnology.common.integration.jei.JEIRecipeTypes;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ITGasTurbineCategory extends ITRecipeCategory<GasTurbineRecipe> {
    private final IDrawableStatic tankOverlay;

    public ITGasTurbineCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.GAS_TURBINE, "block.immersivetechnology.gas_turbine");
        ResourceLocation guiTexture = ITLib.makeTextureLocation("boiler_liquid");
        IDrawableStatic back = guiHelper.createBlankDrawable(176, 74);
        setBackground(back);
        setIcon(ITMultiblockProvider.GAS_TURBINE.iconStack());
        tankOverlay = helper.createDrawable(guiTexture, 177, 31, 20, 51);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull GasTurbineRecipe recipe, @NotNull IFocusGroup focuses) {
        List<FluidStack> inputs = recipe.input.getMatchingFluidStacks().stream()
                .map(fs -> {
                    FluidStack copy = fs.copy();
                    copy.setAmount(recipe.input.getAmount());
                    return copy;
                })
                .toList();
        var inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 80, 21)
                .addIngredients(ForgeTypes.FLUID_STACK, inputs)
                .setFluidRenderer(recipe.input.getAmount(), false, 16, 47);
        inputSlot.addRichTooltipCallback((slotView, tooltip) -> slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                ITFluidInfoArea.fillTooltip(fs, recipe.input.getAmount(), tooltip::add)));

        if (recipe.fluidOutput != null && !recipe.fluidOutput.isEmpty()) {
            var outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 122, 21)
                    .addIngredient(ForgeTypes.FLUID_STACK, recipe.fluidOutput)
                    .setFluidRenderer(recipe.fluidOutput.getAmount(), false, 16, 47);
            outputSlot.addRichTooltipCallback((slotView, tooltip) -> slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                    ITFluidInfoArea.fillTooltip(fs, recipe.fluidOutput.getAmount(), tooltip::add)));
        }
    }

    @Override
    public void draw(@NotNull GasTurbineRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        tankOverlay.draw(guiGraphics, 78, 19);
        if (recipe.fluidOutput != null && !recipe.fluidOutput.isEmpty()) tankOverlay.draw(guiGraphics, 120, 19);
    }
}
