package mctmods.immersivetechnology.common.integration.jei;

import com.igteam.immersivegeology.core.lib.IGLib;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerRecipe;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class ITBoilerCategory extends ITRecipeCategory<BoilerRecipe>
{
    private final IDrawableStatic tankOverlay;

    public ITBoilerCategory(IGuiHelper helper)
    {
        super(helper, JEIRecipeTypes.BOILER, "block.immersivetechnology.boiler");
        ResourceLocation background = new ResourceLocation(IGLib.MODID, "textures/gui/boiler_gui.png");
        IDrawableStatic back = guiHelper.drawableBuilder(background, 0, 0, 176, 77).setTextureSize(176,166).build();
        setBackground(back);
        tankOverlay = helper.createDrawable(background, 177, 31, 20, 51);
        setIcon(ITMultiblockProvider.BOILER.iconStack());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BoilerRecipe recipe, IFocusGroup focuses)
    {
        assert Minecraft.getInstance().level!=null;
    }

    @Override
    public void draw(BoilerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
    }
}
