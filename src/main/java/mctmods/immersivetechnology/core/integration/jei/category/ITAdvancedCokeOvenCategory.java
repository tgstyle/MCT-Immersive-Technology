package mctmods.immersivetechnology.core.integration.jei.category;

import blusunrize.immersiveengineering.common.register.IEFluids;
import mctmods.immersivetechnology.client.gui.helper.ITFluidInfoArea;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetechnology.core.integration.jei.JEIRecipeTypes;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ITAdvancedCokeOvenCategory extends ITRecipeCategory<AdvancedCokeOvenRecipe> {

    private final IDrawableStatic tankOverlay;
    private final IDrawableAnimated flame;

    public ITAdvancedCokeOvenCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.ADVANCED_COKE_OVEN_CUSTOM, "block.immersivetechnology.advanced_coke_oven");

        ResourceLocation background = ITLib.makeTextureLocation("immersiveengineering", "coke_oven");
        IDrawableStatic back = helper.createDrawable(background, 26, 16, 123, 55);
        setRecipeBackground(back);
        setIcon(ITMultiblockProvider.ADVANCED_COKE_OVEN.iconStack());

        tankOverlay = helper.createDrawable(background, 178, 33, 16, 47);

        IDrawableStatic flameStatic = helper.createDrawable(background, 177, 0, 14, 14);
        flame = helper.createAnimatedDrawable(flameStatic, 500, IDrawableAnimated.StartDirection.TOP, true);
    }

    @Override public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull AdvancedCokeOvenRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 19).addItemStacks(Arrays.asList(recipe.input.getMatchingStacks()));

        ItemStack itemOut = recipe.itemOutput.get();
        builder.addSlot(RecipeIngredientRole.OUTPUT, 59, 19).addItemStack(itemOut.isEmpty() ? ItemStack.EMPTY : itemOut);

        FluidStack fluidOut = (recipe.creosoteOutput > 0) ? new FluidStack(IEFluids.CREOSOTE.getStill(), recipe.creosoteOutput) : FluidStack.EMPTY;
        int tankCapacity = Math.max(1000, recipe.creosoteOutput);

        var fluidSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 103, 4)
                .addIngredient(NeoForgeTypes.FLUID_STACK, fluidOut)
                .setFluidRenderer(tankCapacity, false, 16, 47);

        fluidSlot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK).ifPresent(fs ->
                        ITFluidInfoArea.fillTooltip(fs, recipe.creosoteOutput, tooltip::add)));
    }

    @Override public void draw(@NotNull AdvancedCokeOvenRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        getRecipeBackground().draw(guiGraphics, 0, 0);
        tankOverlay.draw(guiGraphics, 103, 4);
        flame.draw(guiGraphics, 31, 20);
    }
}
