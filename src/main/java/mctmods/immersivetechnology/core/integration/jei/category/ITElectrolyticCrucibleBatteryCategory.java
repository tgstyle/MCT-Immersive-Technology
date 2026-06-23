package mctmods.immersivetechnology.core.integration.jei.category;

import mctmods.immersivetechnology.client.gui.helper.ITFluidInfoArea;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.ElectrolyticCrucibleBatteryRecipe;
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

public class ITElectrolyticCrucibleBatteryCategory extends ITRecipeCategory<ElectrolyticCrucibleBatteryRecipe> {

    private final IDrawableStatic tankOverlay;
    private final IDrawableAnimated arrow;

    public ITElectrolyticCrucibleBatteryCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.ELECTROLYTIC_CRUCIBLE_BATTERY, "block.immersivetechnology.electrolytic_crucible_battery");

        ResourceLocation background = ITLib.makeTextureLocation("electrolytic_crucible_battery_jei");
        setRecipeBackground(helper.createDrawable(background, 0, 0, 176, 64));
        setIcon(ITMultiblockProvider.ELECTROLYTIC_CRUCIBLE_BATTERY.iconStack());

        tankOverlay = helper.createDrawable(background, 178, 2, 16, 47);

        IDrawableStatic arrowStatic = helper.createDrawable(background, 196, 0, 32, 18);
        arrow = helper.createAnimatedDrawable(arrowStatic, 200, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull ElectrolyticCrucibleBatteryRecipe recipe, @NotNull IFocusGroup focuses) {
        int tankCapacity = getTankCapacity(recipe);

        List<FluidStack> inputs = recipe.fluidInput0.getMatchingFluidStacks().stream()
                .map(fs -> {
                    FluidStack copy = fs.copy();
                    copy.setAmount(recipe.fluidInput0.getAmount());
                    return copy;
                })
                .toList();

        var inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 12, 12)
                .addIngredients(ForgeTypes.FLUID_STACK, inputs)
                .setFluidRenderer(tankCapacity, false, 16, 47);

        inputSlot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                        ITFluidInfoArea.fillTooltip(fs, recipe.fluidInput0.getAmount(), tooltip::add)));

        FluidStack out0 = (recipe.fluidOutput0 != null && !recipe.fluidOutput0.isEmpty()) ? recipe.fluidOutput0 : FluidStack.EMPTY;
        var output0Slot = builder.addSlot(RecipeIngredientRole.OUTPUT, 102, 12)
                .addIngredient(ForgeTypes.FLUID_STACK, out0)
                .setFluidRenderer(tankCapacity, false, 16, 47);

        output0Slot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                        ITFluidInfoArea.fillTooltip(fs, recipe.fluidOutput0 != null ? recipe.fluidOutput0.getAmount() : 0, tooltip::add)));

        FluidStack out1 = (recipe.fluidOutput1 != null && !recipe.fluidOutput1.isEmpty()) ? recipe.fluidOutput1 : FluidStack.EMPTY;
        var output1Slot = builder.addSlot(RecipeIngredientRole.OUTPUT, 125, 12)
                .addIngredient(ForgeTypes.FLUID_STACK, out1)
                .setFluidRenderer(tankCapacity, false, 16, 47);

        output1Slot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                        ITFluidInfoArea.fillTooltip(fs, recipe.fluidOutput1 != null ? recipe.fluidOutput1.getAmount() : 0, tooltip::add)));

        FluidStack out2 = (recipe.fluidOutput2 != null && !recipe.fluidOutput2.isEmpty()) ? recipe.fluidOutput2 : FluidStack.EMPTY;
        var output2Slot = builder.addSlot(RecipeIngredientRole.OUTPUT, 148, 12)
                .addIngredient(ForgeTypes.FLUID_STACK, out2)
                .setFluidRenderer(tankCapacity, false, 16, 47);

        output2Slot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                        ITFluidInfoArea.fillTooltip(fs, recipe.fluidOutput2 != null ? recipe.fluidOutput2.getAmount() : 0, tooltip::add)));
    }

    private int getTankCapacity(@NotNull ElectrolyticCrucibleBatteryRecipe recipe) {
        int tankCapacity = recipe.fluidInput0.getAmount();
        if (recipe.fluidOutput0 != null && !recipe.fluidOutput0.isEmpty()) { tankCapacity = Math.max(tankCapacity, recipe.fluidOutput0.getAmount()); }
        if (recipe.fluidOutput1 != null && !recipe.fluidOutput1.isEmpty()) { tankCapacity = Math.max(tankCapacity, recipe.fluidOutput1.getAmount()); }
        if (recipe.fluidOutput2 != null && !recipe.fluidOutput2.isEmpty()) { tankCapacity = Math.max(tankCapacity, recipe.fluidOutput2.getAmount()); }
        return tankCapacity;
    }

    @Override public void draw(@NotNull ElectrolyticCrucibleBatteryRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        getRecipeBackground().draw(guiGraphics, 0, 0);

        tankOverlay.draw(guiGraphics, 11, 11);
        tankOverlay.draw(guiGraphics, 101, 12);
        tankOverlay.draw(guiGraphics, 124, 12);
        tankOverlay.draw(guiGraphics, 147, 12);

        arrow.draw(guiGraphics, 50, 39);
    }
}
