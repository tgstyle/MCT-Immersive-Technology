package mctmods.immersivetechnology.common.integration.jei;

import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerRecipe;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ITBoilerCategory extends ITRecipeCategory<BoilerRecipe> {

    public ITBoilerCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.BOILER, "block.immersivetechnology.boiler");
        ResourceLocation background = ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "textures/gui/boiler_gui.png");
        IDrawableStatic back = guiHelper.drawableBuilder(background, 0, 0, 176, 77).setTextureSize(176, 166).build();
        setBackground(back);
        IDrawableStatic tankOverlay = helper.createDrawable(background, 177, 31, 20, 51);
        setIcon(ITMultiblockProvider.BOILER.iconStack());
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull BoilerRecipe recipe, @NotNull IFocusGroup focuses) { assert Minecraft.getInstance().level != null; }

    @Override
    public void draw(@NotNull BoilerRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) { super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY); }
}
