package mctmods.immersivetechnology.core.integration.jei.category;

import mctmods.immersivetechnology.client.gui.helper.ITFluidInfoArea;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.HeatExchangerRecipe;
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

public class ITHeatExchangerCategory extends ITRecipeCategory<HeatExchangerRecipe> {
    private final IDrawableStatic tankOverlay;
    private final IDrawableAnimated arrow;
    private final IDrawableAnimated drops;

    public ITHeatExchangerCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.HEAT_EXCHANGER, "block.immersivetechnology.heat_exchanger");
        ResourceLocation background = ITLib.makeTextureLocation("heat_exchanger_jei");
        setBackground(helper.createDrawable(background, 0, 0, 176, 64));
        setIcon(ITMultiblockProvider.HEAT_EXCHANGER.iconStack());

        tankOverlay = helper.createDrawable(background, 178, 2, 16, 47);
        arrow = helper.drawableBuilder(background, 196, 0, 32, 9)
                .buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, false);
        drops = helper.drawableBuilder(background, 196, 9, 20, 15)
                .buildAnimated(200, IDrawableAnimated.StartDirection.BOTTOM, false);
    }

    @Override public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull HeatExchangerRecipe recipe, @NotNull IFocusGroup focuses) {
        int tankCapacity = Math.max(recipe.input0.getAmount(), recipe.output0.getAmount());
        if (recipe.input1 != null) tankCapacity = Math.max(tankCapacity, recipe.input1.getAmount());
        if (recipe.output1 != null) tankCapacity = Math.max(tankCapacity, recipe.output1.getAmount());

        List<FluidStack> in0 = recipe.input0.getMatchingFluidStacks().stream()
                .map(fs -> {
                    FluidStack copy = fs.copy();
                    copy.setAmount(recipe.input0.getAmount());
                    return copy;
                }).toList();
        var slotIn0 = builder.addSlot(RecipeIngredientRole.INPUT, 35, 12)
                .addIngredients(ForgeTypes.FLUID_STACK, in0)
                .setFluidRenderer(tankCapacity, false, 16, 47);
        slotIn0.addRichTooltipCallback((view, tt) -> view.getDisplayedIngredient(ForgeTypes.FLUID_STACK)
                .ifPresent(fs -> ITFluidInfoArea.fillTooltip(fs, recipe.input0.getAmount(), tt::add)));

        if (recipe.input1 != null) {
            List<FluidStack> in1 = recipe.input1.getMatchingFluidStacks().stream()
                    .map(fs -> {
                        FluidStack copy = fs.copy();
                        copy.setAmount(recipe.input1.getAmount());
                        return copy;
                    }).toList();
            var slotIn1 = builder.addSlot(RecipeIngredientRole.INPUT, 12, 12)
                    .addIngredients(ForgeTypes.FLUID_STACK, in1)
                    .setFluidRenderer(tankCapacity, false, 16, 47);
            slotIn1.addRichTooltipCallback((view, tt) -> view.getDisplayedIngredient(ForgeTypes.FLUID_STACK)
                    .ifPresent(fs -> ITFluidInfoArea.fillTooltip(fs, recipe.input1.getAmount(), tt::add)));
        }

        var slotOut0 = builder.addSlot(RecipeIngredientRole.OUTPUT, 125, 12)
                .addIngredient(ForgeTypes.FLUID_STACK, recipe.output0)
                .setFluidRenderer(tankCapacity, false, 16, 47);
        slotOut0.addRichTooltipCallback((view, tt) -> view.getDisplayedIngredient(ForgeTypes.FLUID_STACK)
                .ifPresent(fs -> ITFluidInfoArea.fillTooltip(fs, recipe.output0.getAmount(), tt::add)));

        if (recipe.output1 != null) {
            var slotOut1 = builder.addSlot(RecipeIngredientRole.OUTPUT, 148, 12)
                    .addIngredient(ForgeTypes.FLUID_STACK, recipe.output1)
                    .setFluidRenderer(tankCapacity, false, 16, 47);
            slotOut1.addRichTooltipCallback((view, tt) -> view.getDisplayedIngredient(ForgeTypes.FLUID_STACK)
                    .ifPresent(fs -> ITFluidInfoArea.fillTooltip(fs, recipe.output1.getAmount(), tt::add)));
        }
    }

    @Override public void draw(@NotNull HeatExchangerRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics graphics, double mouseX, double mouseY) {
        tankOverlay.draw(graphics, 35, 12);
        if (recipe.input1 != null) tankOverlay.draw(graphics, 12, 12);
        tankOverlay.draw(graphics, 125, 12);
        if (recipe.output1 != null) tankOverlay.draw(graphics, 148, 12);

        arrow.draw(graphics, 73, 50);
        drops.draw(graphics, 73, 40);
    }
}
