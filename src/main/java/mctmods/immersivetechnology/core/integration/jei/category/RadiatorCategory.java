package mctmods.immersivetechnology.core.integration.jei.category;

import mctmods.immersivetechnology.client.gui.helper.FluidInfoArea;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.RadiatorRecipe;
import mctmods.immersivetechnology.core.integration.jei.JEIRecipeTypes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
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

public class RadiatorCategory extends ModRecipeCategory<RadiatorRecipe> {

    private final IDrawableStatic tankOverlay;
    private final IDrawableAnimated arrow;
    private final IDrawableAnimated drops;

    public RadiatorCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.RADIATOR, "block.immersivetechnology.radiator");

        ResourceLocation background = Reference.makeTextureLocation("radiator_jei");
        IDrawableStatic back = helper.createDrawable(background, 0, 0, 159, 69);
        setRecipeBackground(back);
        setIcon(MultiblockRegistry.RADIATOR.iconStack());

        tankOverlay = helper.createDrawable(background, 161, 2, 16, 47);

        IDrawableStatic arrowStatic = helper.createDrawable(background, 17, 69, 32, 9);
        arrow = helper.createAnimatedDrawable(arrowStatic, 200, IDrawableAnimated.StartDirection.LEFT, false);

        IDrawableStatic dropsStatic = helper.createDrawable(background, 0, 69, 17, 23);
        drops = helper.createAnimatedDrawable(dropsStatic, 200, IDrawableAnimated.StartDirection.TOP, false);
    }

    @Override public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull RadiatorRecipe recipe, @NotNull IFocusGroup focuses) {
        int tankCapacity = getTankCapacity(recipe);

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

        inputSlot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                        FluidInfoArea.fillTooltip(fs, recipe.input.getAmount(), tooltip::add)));

        var outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 109, 11)
                .addIngredient(ForgeTypes.FLUID_STACK, recipe.fluidOutput)
                .setFluidRenderer(tankCapacity, false, 16, 47);

        outputSlot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                        FluidInfoArea.fillTooltip(fs, recipe.fluidOutput.getAmount(), tooltip::add)));
    }

    private int getTankCapacity(@NotNull RadiatorRecipe recipe) {
        return Math.max(recipe.input.getAmount(), recipe.fluidOutput.getAmount());
    }

    @Override public void draw(@NotNull RadiatorRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        getRecipeBackground().draw(guiGraphics, 0, 0);

        tankOverlay.draw(guiGraphics, 11, 11);
        tankOverlay.draw(guiGraphics, 109, 11);

        arrow.draw(guiGraphics, 52, 51);
        drops.draw(guiGraphics, 55, 32);
    }
}
