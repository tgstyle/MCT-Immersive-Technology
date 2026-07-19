package mctmods.immersivetechnology.core.integration.jei.category;

import mctmods.immersivetechnology.client.gui.helper.FluidInfoArea;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.CoolingTowerRecipe;
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

public class CoolingTowerCategory extends ModRecipeCategory<CoolingTowerRecipe> {

    private final IDrawableStatic tankOverlay;
    private final IDrawableAnimated arrow;
    private final IDrawableAnimated drops;

    public CoolingTowerCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.COOLING_TOWER, "block.immersivetechnology.cooling_tower");

        ResourceLocation background = Reference.makeTextureLocation("cooling_tower_jei");
        IDrawableStatic back = helper.createDrawable(background, 0, 0, 159, 69);
        setRecipeBackground(back);
        setIcon(MultiblockRegistry.COOLING_TOWER.iconStack());

        tankOverlay = helper.createDrawable(background, 161, 2, 16, 47);

        IDrawableStatic arrowStatic = helper.createDrawable(background, 17, 69, 32, 9);
        arrow = helper.createAnimatedDrawable(arrowStatic, 200, IDrawableAnimated.StartDirection.LEFT, false);

        IDrawableStatic dropsStatic = helper.createDrawable(background, 0, 69, 17, 23);
        drops = helper.createAnimatedDrawable(dropsStatic, 200, IDrawableAnimated.StartDirection.TOP, false);
    }

    @Override public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull CoolingTowerRecipe recipe, @NotNull IFocusGroup focuses) {
        int tankCapacity = getTankCapacity(recipe);

        List<FluidStack> inputs0 = recipe.input0.getMatchingFluidStacks().stream()
                .map(fs -> {
                    FluidStack copy = fs.copy();
                    copy.setAmount(recipe.input0.getAmount());
                    return copy;
                })
                .toList();

        var input0Slot = builder.addSlot(RecipeIngredientRole.INPUT, 11, 11)
                .addIngredients(ForgeTypes.FLUID_STACK, inputs0)
                .setFluidRenderer(tankCapacity, false, 16, 47);

        input0Slot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                        FluidInfoArea.fillTooltip(fs, recipe.input0.getAmount(), tooltip::add)));

        List<FluidStack> inputs1 = recipe.input1.getMatchingFluidStacks().stream()
                .map(fs -> {
                    FluidStack copy = fs.copy();
                    copy.setAmount(recipe.input1.getAmount());
                    return copy;
                })
                .toList();

        var input1Slot = builder.addSlot(RecipeIngredientRole.INPUT, 34, 11)
                .addIngredients(ForgeTypes.FLUID_STACK, inputs1)
                .setFluidRenderer(tankCapacity, false, 16, 47);

        input1Slot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                        FluidInfoArea.fillTooltip(fs, recipe.input1.getAmount(), tooltip::add)));

        FluidStack out0 = (recipe.fluidOutput0 != null && !recipe.fluidOutput0.isEmpty()) ? recipe.fluidOutput0 : FluidStack.EMPTY;
        var output0Slot = builder.addSlot(RecipeIngredientRole.OUTPUT, 86, 11)
                .addIngredient(ForgeTypes.FLUID_STACK, out0)
                .setFluidRenderer(tankCapacity, false, 16, 47);

        output0Slot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                        FluidInfoArea.fillTooltip(fs, recipe.fluidOutput0 != null ? recipe.fluidOutput0.getAmount() : 0, tooltip::add)));

        FluidStack out1 = (recipe.fluidOutput1 != null && !recipe.fluidOutput1.isEmpty()) ? recipe.fluidOutput1 : FluidStack.EMPTY;
        var output1Slot = builder.addSlot(RecipeIngredientRole.OUTPUT, 109, 11)
                .addIngredient(ForgeTypes.FLUID_STACK, out1)
                .setFluidRenderer(tankCapacity, false, 16, 47);

        output1Slot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                        FluidInfoArea.fillTooltip(fs, recipe.fluidOutput1 != null ? recipe.fluidOutput1.getAmount() : 0, tooltip::add)));

        FluidStack out2 = (recipe.fluidOutput2 != null && !recipe.fluidOutput2.isEmpty()) ? recipe.fluidOutput2 : FluidStack.EMPTY;
        var output2Slot = builder.addSlot(RecipeIngredientRole.OUTPUT, 132, 11)
                .addIngredient(ForgeTypes.FLUID_STACK, out2)
                .setFluidRenderer(tankCapacity, false, 16, 47);

        output2Slot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                        FluidInfoArea.fillTooltip(fs, recipe.fluidOutput2 != null ? recipe.fluidOutput2.getAmount() : 0, tooltip::add)));
    }

    private int getTankCapacity(@NotNull CoolingTowerRecipe recipe) {
        int tankCapacity = Math.max(recipe.input0.getAmount(), recipe.input1.getAmount());
        if (recipe.fluidOutput0 != null && !recipe.fluidOutput0.isEmpty()) { tankCapacity = Math.max(tankCapacity, recipe.fluidOutput0.getAmount()); }
        if (recipe.fluidOutput1 != null && !recipe.fluidOutput1.isEmpty()) { tankCapacity = Math.max(tankCapacity, recipe.fluidOutput1.getAmount()); }
        if (recipe.fluidOutput2 != null && !recipe.fluidOutput2.isEmpty()) { tankCapacity = Math.max(tankCapacity, recipe.fluidOutput2.getAmount()); }
        return tankCapacity;
    }

    @Override public void draw(@NotNull CoolingTowerRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        getRecipeBackground().draw(guiGraphics, 0, 0);

        tankOverlay.draw(guiGraphics, 11, 11);
        tankOverlay.draw(guiGraphics, 34, 11);
        tankOverlay.draw(guiGraphics, 86, 11);
        tankOverlay.draw(guiGraphics, 109, 11);
        tankOverlay.draw(guiGraphics, 132, 11);

        arrow.draw(guiGraphics, 52, 51);
        drops.draw(guiGraphics, 55, 32);
    }
}
