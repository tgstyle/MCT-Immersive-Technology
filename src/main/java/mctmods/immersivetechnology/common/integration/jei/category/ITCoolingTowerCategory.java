package mctmods.immersivetechnology.common.integration.jei.category;

import mctmods.immersivetechnology.common.multiblocks.stone.recipe.CoolingTowerRecipe;
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

public class ITCoolingTowerCategory extends ITRecipeCategory<CoolingTowerRecipe> {
    private final IDrawableStatic tankOverlay;

    public ITCoolingTowerCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.COOLING_TOWER, "block.immersivetechnology.cooling_tower");
        ResourceLocation guiTexture = ITLib.makeTextureLocation("boiler_liquid");
        IDrawableStatic back = guiHelper.createBlankDrawable(176, 74);
        setBackground(back);
        setIcon(ITMultiblockProvider.COOLING_TOWER.iconStack());
        tankOverlay = helper.createDrawable(guiTexture, 177, 31, 20, 51);
    }

    @Override public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull CoolingTowerRecipe recipe, @NotNull IFocusGroup focuses) {
        List<FluidStack> inputs0 = recipe.input0.getMatchingFluidStacks().stream()
                .map(fs -> {
                    FluidStack copy = fs.copy();
                    copy.setAmount(recipe.input0.getAmount());
                    return copy;
                })
                .toList();
        var input0Slot = builder.addSlot(RecipeIngredientRole.INPUT, 38, 21)
                .addIngredients(ForgeTypes.FLUID_STACK, inputs0)
                .setFluidRenderer(recipe.input0.getAmount(), false, 16, 47);
        input0Slot.addRichTooltipCallback((slotView, tooltip) -> slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                ITFluidInfoArea.fillTooltip(fs, recipe.input0.getAmount(), tooltip::add)));

        List<FluidStack> inputs1 = recipe.input1.getMatchingFluidStacks().stream()
                .map(fs -> {
                    FluidStack copy = fs.copy();
                    copy.setAmount(recipe.input1.getAmount());
                    return copy;
                })
                .toList();
        var input1Slot = builder.addSlot(RecipeIngredientRole.INPUT, 62, 21)
                .addIngredients(ForgeTypes.FLUID_STACK, inputs1)
                .setFluidRenderer(recipe.input1.getAmount(), false, 16, 47);
        input1Slot.addRichTooltipCallback((slotView, tooltip) -> slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                ITFluidInfoArea.fillTooltip(fs, recipe.input1.getAmount(), tooltip::add)));

        if (recipe.fluidOutput0 != null && !recipe.fluidOutput0.isEmpty()) {
            var output0Slot = builder.addSlot(RecipeIngredientRole.OUTPUT, 98, 21)
                    .addIngredient(ForgeTypes.FLUID_STACK, recipe.fluidOutput0)
                    .setFluidRenderer(recipe.fluidOutput0.getAmount(), false, 16, 47);
            output0Slot.addRichTooltipCallback((slotView, tooltip) -> slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                    ITFluidInfoArea.fillTooltip(fs, recipe.fluidOutput0.getAmount(), tooltip::add)));
        }

        if (recipe.fluidOutput1 != null && !recipe.fluidOutput1.isEmpty()) {
            var output1Slot = builder.addSlot(RecipeIngredientRole.OUTPUT, 122, 21)
                    .addIngredient(ForgeTypes.FLUID_STACK, recipe.fluidOutput1)
                    .setFluidRenderer(recipe.fluidOutput1.getAmount(), false, 16, 47);
            output1Slot.addRichTooltipCallback((slotView, tooltip) -> slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                    ITFluidInfoArea.fillTooltip(fs, recipe.fluidOutput1.getAmount(), tooltip::add)));
        }

        if (recipe.fluidOutput2 != null && !recipe.fluidOutput2.isEmpty()) {
            var output2Slot = builder.addSlot(RecipeIngredientRole.OUTPUT, 146, 21)
                    .addIngredient(ForgeTypes.FLUID_STACK, recipe.fluidOutput2)
                    .setFluidRenderer(recipe.fluidOutput2.getAmount(), false, 16, 47);
            output2Slot.addRichTooltipCallback((slotView, tooltip) -> slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                    ITFluidInfoArea.fillTooltip(fs, recipe.fluidOutput2.getAmount(), tooltip::add)));
        }
    }

    @Override public void draw(@NotNull CoolingTowerRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        tankOverlay.draw(guiGraphics, 36, 19);
        tankOverlay.draw(guiGraphics, 60, 19);
        if (recipe.fluidOutput0 != null && !recipe.fluidOutput0.isEmpty()) tankOverlay.draw(guiGraphics, 96, 19);
        if (recipe.fluidOutput1 != null && !recipe.fluidOutput1.isEmpty()) tankOverlay.draw(guiGraphics, 120, 19);
        if (recipe.fluidOutput2 != null && !recipe.fluidOutput2.isEmpty()) tankOverlay.draw(guiGraphics, 144, 19);
    }
}
