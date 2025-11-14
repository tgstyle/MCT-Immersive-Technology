package mctmods.immersivetechnology.common.integration.jei.category;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerTankRecipe;
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

public class ITBoilerTankCategory extends ITRecipeCategory<BoilerTankRecipe> {
    private final IDrawableStatic tankOverlay;

    public ITBoilerTankCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.BOILER_TANK, "block.immersivetechnology.boiler_tank");
        ResourceLocation background = ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "textures/gui/boiler_tank.png");
        IDrawableStatic back = guiHelper.drawableBuilder(background, 0, 0, 176, 74).setTextureSize(256, 256).build();
        setBackground(back);
        setIcon(ITMultiblockProvider.BOILER_TANK.iconStack());
        tankOverlay = helper.createDrawable(background, 177, 31, 20, 51);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull BoilerTankRecipe recipe, @NotNull IFocusGroup focuses) {
        List<FluidStack> inputs = recipe.input.getMatchingFluidStacks().stream()
                .map(fs -> {
                    FluidStack copy = fs.copy();
                    copy.setAmount(recipe.input.getAmount());
                    return copy;
                })
                .toList();
        var inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 67, 21)
                .addIngredients(ForgeTypes.FLUID_STACK, inputs)
                .setFluidRenderer(recipe.input.getAmount(), false, 16, 47);
        inputSlot.addRichTooltipCallback((slotView, tooltip) -> slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                ITFluidInfoArea.fillTooltip(fs, recipe.input.getAmount(), tooltip::add)));
        var outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 92, 21)
                .addIngredient(ForgeTypes.FLUID_STACK, recipe.output)
                .setFluidRenderer(recipe.output.getAmount(), false, 16, 47);
        outputSlot.addRichTooltipCallback((slotView, tooltip) -> slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                ITFluidInfoArea.fillTooltip(fs, fs.getAmount(), tooltip::add)));
    }

    @Override
    public void draw(@NotNull BoilerTankRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        tankOverlay.draw(guiGraphics, 65, 19);
        tankOverlay.draw(guiGraphics, 90, 19);
    }
}
