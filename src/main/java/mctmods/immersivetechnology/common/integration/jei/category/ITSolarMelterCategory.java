package mctmods.immersivetechnology.common.integration.jei.category;

import mctmods.immersivetechnology.client.gui.helper.ITFluidInfoArea;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.SolarMelterRecipe;
import mctmods.immersivetechnology.common.integration.jei.JEIRecipeTypes;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ITSolarMelterCategory extends ITRecipeCategory<SolarMelterRecipe> {
    private final IDrawableStatic tankOverlay;

    public ITSolarMelterCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.SOLAR_MELTER, "block.immersivetechnology.solar_melter");
        ResourceLocation background = ITLib.makeTextureLocation("solar");
        IDrawableStatic back = guiHelper.drawableBuilder(background, 0, 0, 176, 74).setTextureSize(256, 256).build();
        setBackground(back);
        setIcon(ITMultiblockProvider.SOLAR_MELTER.iconStack());
        tankOverlay = helper.createDrawable(background, 177, 31, 20, 51);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull SolarMelterRecipe recipe, @NotNull IFocusGroup focuses) {
        List<FluidStack> inputs = recipe.input.getMatchingFluidStacks().stream()
                .map(fs -> {
                    FluidStack copy = fs.copy();
                    copy.setAmount(recipe.input.getAmount());
                    return copy;
                })
                .toList();
        var inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 102, 21)
                .addIngredients(ForgeTypes.FLUID_STACK, inputs)
                .setFluidRenderer(recipe.input.getAmount(), false, 16, 47);
        inputSlot.addRichTooltipCallback((slotView, tooltip) -> slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                ITFluidInfoArea.fillTooltip(fs, recipe.input.getAmount(), tooltip::add)));
        if (recipe.fluidOutput != null) {
            var outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 126, 21)
                    .addIngredient(ForgeTypes.FLUID_STACK, recipe.fluidOutput)
                    .setFluidRenderer(recipe.fluidOutput.getAmount(), false, 16, 47);
            outputSlot.addRichTooltipCallback((slotView, tooltip) -> slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                    ITFluidInfoArea.fillTooltip(fs, fs.getAmount(), tooltip::add)));
        }
    }

    @Override
    public void draw(@NotNull SolarMelterRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        tankOverlay.draw(guiGraphics, 100, 19);
        if (recipe.fluidOutput != null) { tankOverlay.draw(guiGraphics, 124, 19); }
        Font font = Minecraft.getInstance().font;
        Component timeComponent = Component.translatable(TranslationKey.CATEGORY_SOLAR_MELTER_TIME.getLocation(), recipe.getTotalProcessTime(), recipe.input.getAmount()).withStyle(style -> style.withColor(TextColor.fromRgb(0xAAAAAA)));
        int timeWidth = font.width(timeComponent);
        int timeX = 122 - timeWidth / 2;
        guiGraphics.drawString(font, timeComponent, timeX, 0, 0xAAAAAA, true);
        Component tempComponent = Component.translatable(TranslationKey.CATEGORY_SOLAR_MELTER_TEMP.getLocation(), recipe.requiredTemp).withStyle(style -> style.withColor(TextColor.fromRgb(0xAAAAAA)));
        int tempWidth = font.width(tempComponent);
        int tempX = 122 - tempWidth / 2;
        guiGraphics.drawString(font, tempComponent, tempX, 9, 0xAAAAAA, true);
    }
}
