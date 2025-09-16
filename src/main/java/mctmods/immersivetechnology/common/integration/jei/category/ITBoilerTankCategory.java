package mctmods.immersivetechnology.common.integration.jei.category;

import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerTankRecipe;
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
import org.jetbrains.annotations.NotNull;

public class ITBoilerTankCategory extends ITRecipeCategory<BoilerTankRecipe> {
    private final IDrawableStatic tankOverlay;

    public ITBoilerTankCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.BOILER_TANK, "block.immersivetechnology.boiler_tank");
        ResourceLocation background = ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "textures/gui/boiler_tank.png");
        IDrawableStatic back = guiHelper.drawableBuilder(background, 0, 0, 176, 77).setTextureSize(256, 256).build();
        setBackground(back);
        tankOverlay = helper.createDrawable(background, 177, 31, 20, 51);
        setIcon(ITMultiblockProvider.BOILER_TANK.iconStack());
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull BoilerTankRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 65, 18)
                .addIngredients(ForgeTypes.FLUID_STACK, recipe.input.getMatchingFluidStacks())
                .setFluidRenderer(recipe.input.getAmount(), false, 20, 51)
                .setOverlay(tankOverlay, 0, 0);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 90, 18)
                .addIngredient(ForgeTypes.FLUID_STACK, recipe.output)
                .setFluidRenderer(recipe.output.getAmount(), false, 20, 51)
                .setOverlay(tankOverlay, 0, 0);
    }

    @Override
    public void draw(@NotNull BoilerTankRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) { super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY); }
}
