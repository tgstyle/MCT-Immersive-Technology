package mctmods.immersivetechnology.common.integration.jei.category;

import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerLiquidRecipe;
import mctmods.immersivetechnology.client.gui.helper.ITFluidInfoArea;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ITBoilerLiquidCategory extends ITRecipeCategory<BoilerLiquidRecipe> {
    private final IDrawableStatic tankOverlay;

    public ITBoilerLiquidCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.BOILER_LIQUID, "block.immersivetechnology.boiler_liquid");
        ResourceLocation background = ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "textures/gui/boiler_liquid.png");
        IDrawableStatic back = guiHelper.drawableBuilder(background, 0, 0, 176, 74).setTextureSize(256, 256).build();
        setBackground(back);
        setIcon(ITMultiblockProvider.BOILER_LIQUID.iconStack());
        tankOverlay = helper.createDrawable(background, 177, 31, 20, 51);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull BoilerLiquidRecipe recipe, @NotNull IFocusGroup focuses) {
        List<FluidStack> inputs = recipe.input.getMatchingFluidStacks().stream()
                .map(fs -> {
                    FluidStack copy = fs.copy();
                    copy.setAmount(recipe.input.getAmount());
                    return copy;
                })
                .toList();
        var inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 80, 21)
                .addIngredients(ForgeTypes.FLUID_STACK, inputs)
                .setFluidRenderer(recipe.input.getAmount(), false, 16, 47);
        inputSlot.addRichTooltipCallback((slotView, tooltip) -> slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                ITFluidInfoArea.fillTooltip(fs, recipe.input.getAmount(), tooltip::add)));
    }

    @Override
    public void draw(@NotNull BoilerLiquidRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        tankOverlay.draw(guiGraphics, 78, 19);
        Font font = Minecraft.getInstance().font;
        Component timeComponent = Component.translatable("category.immersivetechnology.metal_multiblock.boiler_liquid.time", recipe.getTotalProcessTime(), recipe.input.getAmount()).withStyle(style -> style.withColor(TextColor.fromRgb(0xAAAAAA)));
        int timeWidth = font.width(timeComponent);
        int timeX = 80 + 8 - timeWidth / 2;
        guiGraphics.drawString(font, timeComponent, timeX, 0, 0xAAAAAA, true);
        Component heatComponent = Component.translatable("category.immersivetechnology.metal_multiblock.boiler_liquid.heat", String.format("%.2f", recipe.getHeatPerTick())).withStyle(style -> style.withColor(TextColor.fromRgb(0xAAAAAA)));
        int heatWidth = font.width(heatComponent);
        int heatX = 80 + 8 - heatWidth / 2;
        guiGraphics.drawString(font, heatComponent, heatX, 9, 0xAAAAAA, true);
    }
}
