package mctmods.immersivetechnology.common.integration.jei;

import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.DistillerRecipe;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ITDistillerCategory extends ITRecipeCategory<DistillerRecipe> {
    private final IDrawableStatic tankOverlay;
    private final IDrawableAnimated arrow;

    public ITDistillerCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.DISTILLER, "block.immersivetechnology.distiller");
        ResourceLocation background = ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "textures/gui/distiller.png");
        setBackground(helper.createDrawable(background, 0, 0, 176, 74));
        setIcon(ITMultiblockProvider.DISTILLER.iconStack());
        tankOverlay = helper.createDrawable(background, 176, 31, 20, 51);
        arrow = helper.drawableBuilder(background, 176, 14, 24, 17).buildAnimated(20, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DistillerRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 56, 43).addIngredient(ForgeTypes.FLUID_STACK, recipe.water.getMatchingFluidStacks().get(0)).setFluidRenderer(recipe.water.getAmount(), false, 16, 47).setOverlay(tankOverlay, 0, 0);
        if (recipe.fluidOutput != null) { builder.addSlot(RecipeIngredientRole.OUTPUT, 112, 43).addIngredient(ForgeTypes.FLUID_STACK, recipe.fluidOutput).setFluidRenderer(recipe.fluidOutput.getAmount(), false, 16, 47).setOverlay(tankOverlay, 0, 0); }
        if (!recipe.itemOutput.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 130, 47).addItemStack(recipe.itemOutput).addRichTooltipCallback((slot, tooltip) -> tooltip.add(Component.translatable("category.immersivetechnology.metal_multiblock.distillerChance", String.format("%.2f", recipe.chance * 100)).withStyle(style -> style.withColor(TextColor.fromRgb(0xAAAAAA)))));
        }
    }

    @Override
    public void draw(@NotNull DistillerRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics graphics, double mouseX, double mouseY) { arrow.draw(graphics, 85, 47); }
}
