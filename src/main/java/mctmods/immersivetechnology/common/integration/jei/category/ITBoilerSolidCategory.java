package mctmods.immersivetechnology.common.integration.jei.category;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerSolidRecipe;
import mctmods.immersivetechnology.common.integration.jei.JEIRecipeTypes;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import mezz.jei.api.constants.VanillaTypes;
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
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class ITBoilerSolidCategory extends ITRecipeCategory<BoilerSolidRecipe> {
    public ITBoilerSolidCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.BOILER_SOLID, "block.immersivetechnology.boiler_solid");
        ResourceLocation background = ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "textures/gui/boiler_solid.png");
        IDrawableStatic back = guiHelper.drawableBuilder(background, 0, 0, 176, 74).setTextureSize(256, 256).build();
        setBackground(back);
        setIcon(ITMultiblockProvider.BOILER_SOLID.iconStack());
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull BoilerSolidRecipe recipe, @NotNull IFocusGroup focuses) {
        List<ItemStack> inputs = Arrays.stream(recipe.input.getMatchingStacks())
                .map(stack -> {
                    ItemStack copy = stack.copy();
                    copy.setCount(recipe.input.getCount());
                    return copy;
                })
                .toList();
        builder.addSlot(RecipeIngredientRole.INPUT, 81, 36)
                .addIngredients(VanillaTypes.ITEM_STACK, inputs);
    }

    @Override
    public void draw(@NotNull BoilerSolidRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        ItemStack exampleStack = recipe.input.getMatchingStacks()[0].copy();
        exampleStack.setCount(recipe.input.getCount());
        int burnTime = ForgeHooks.getBurnTime(exampleStack, null);
        Component timeComponent = Component.translatable("category.immersivetechnology.metal_multiblock.boiler_solid.time", burnTime, recipe.input.getCount()).withStyle(style -> style.withColor(TextColor.fromRgb(0xAAAAAA)));
        int timeWidth = font.width(timeComponent);
        int timeX = 90 - timeWidth / 2;
        guiGraphics.drawString(font, timeComponent, timeX, 0, 0xAAAAAA, true);
        Component heatComponent = Component.translatable("category.immersivetechnology.metal_multiblock.boiler_solid.heat", String.format("%.2f", recipe.getHeatPerTick())).withStyle(style -> style.withColor(TextColor.fromRgb(0xAAAAAA)));
        int heatWidth = font.width(heatComponent);
        int heatX = 90 - heatWidth / 2;
        guiGraphics.drawString(font, heatComponent, heatX, 9, 0xAAAAAA, true);
    }
}
