package mctmods.immersivetechnology.core.integration.jei.category;

import mctmods.immersivetechnology.client.gui.helper.ITFluidInfoArea;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.GasTurbineRecipe;
import mctmods.immersivetechnology.core.integration.jei.JEIRecipeTypes;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
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
    private final IDrawableAnimated turbineAndArrow;

    public ITGasTurbineCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.GAS_TURBINE, "block.immersivetechnology.gas_turbine");
        ResourceLocation background = ITLib.makeTextureLocation("turbine_jei");
        setRecipeBackground(helper.createDrawable(background, 0, 0, 116, 69));
        setIcon(ITMultiblockProvider.GAS_TURBINE.iconStack());

        tankOverlay = helper.createDrawable(background, 118, 2, 16, 47);

        IDrawableStatic staticImage = helper.createDrawable(background, 0, 78, 32, 42);
        this.turbineAndArrow = helper.createAnimatedDrawable(staticImage, 200, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull GasTurbineRecipe recipe, @NotNull IFocusGroup focuses) {
        int tankCapacity = recipe.input.getAmount();
        if (recipe.fluidOutput != null && !recipe.fluidOutput.isEmpty()) tankCapacity = Math.max(tankCapacity, recipe.fluidOutput.getAmount());

        List<FluidStack> inputs = recipe.input.getMatchingFluidStacks().stream()
                .map(fs -> {
                    FluidStack copy = fs.copy();
                    copy.setAmount(recipe.input.getAmount());
                    return copy;
                })
                .toList();

        var inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 11, 11)
                .addIngredients(ForgeTypes.FLUID_STACK, inputs)
                .setFluidRenderer(tankCapacity, false, 16, 47);
        inputSlot.addRichTooltipCallback((slotView, tooltip) -> slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                ITFluidInfoArea.fillTooltip(fs, recipe.input.getAmount(), tooltip::add)));

        if (recipe.fluidOutput != null && !recipe.fluidOutput.isEmpty()) {
            var outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 89, 11)
                    .addIngredient(ForgeTypes.FLUID_STACK, recipe.fluidOutput)
                    .setFluidRenderer(tankCapacity, false, 16, 47);
            outputSlot.addRichTooltipCallback((slotView, tooltip) -> slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                    ITFluidInfoArea.fillTooltip(fs, recipe.fluidOutput.getAmount(), tooltip::add)));
        }
    }

    @Override public void draw(@NotNull GasTurbineRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        getRecipeBackground().draw(guiGraphics, 0, 0);
        tankOverlay.draw(guiGraphics, 11, 11);
        if (recipe.fluidOutput != null && !recipe.fluidOutput.isEmpty()) tankOverlay.draw(guiGraphics, 89, 11);
        turbineAndArrow.draw(guiGraphics, 42, 18);
    }
}
