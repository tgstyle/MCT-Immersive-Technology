package mctmods.immersivetechnology.core.integration.jei.category;

import com.immersiveconvergence.api.integration.jei.BaseRecipeCategory;
import com.immersiveconvergence.api.client.gui.GuiFluidArea;
import mctmods.immersivetechnology.common.multiblocks.gui.StyleNOXMenu;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.StyleNOXLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.StyleNOXRecipe;
import mctmods.immersivetechnology.core.integration.jei.JEIRecipeTypes;
import mctmods.immersivetechnology.core.util.TranslationKey;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class StyleNOXCategory extends BaseRecipeCategory<StyleNOXRecipe> {

    private final IDrawableStatic tankOverlay;

    public StyleNOXCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.STYLENOX, "block.immersivetechnology.stylenox");

        ResourceLocation background = Reference.makeTextureLocation("stylenox");
        setRecipeBackground(helper.createDrawable(background, 0, 0, 176, 74));
        setIcon(MultiblockRegistry.STYLENOX.iconStack());

        tankOverlay = helper.createDrawable(background, 177, 31, 20, 51);
    }

    @Override public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull StyleNOXRecipe recipe, @Nonnull IFocusGroup focuses) {
        List<ItemStack> fuel = Arrays.stream(recipe.input().getMatchingStacks()).map(stack -> stack.copyWithCount(1)).toList();
        int fuelCount = Math.min(recipe.input().getCount(), StyleNOXLogic.FUEL_SLOTS);
        for (int slot = 0; slot < fuelCount; slot++) { builder.addSlot(RecipeIngredientRole.INPUT, StyleNOXMenu.fuelSlotX(slot), StyleNOXMenu.fuelSlotY(slot)).addItemStacks(fuel); }

        builder.addSlot(RecipeIngredientRole.INPUT, 8, 21)
                .addIngredients(NeoForgeTypes.FLUID_STACK, tagStacks(recipe.waterTag(), recipe.waterAmount()))
                .setFluidRenderer(Math.max(1, recipe.waterAmount()), false, 16, 47)
                .addRichTooltipCallback((slotView, tooltip) ->
                        slotView.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK).ifPresent(fs ->
                                GuiFluidArea.fillTooltip(fs, recipe.waterAmount(), tooltip::add)));

        builder.addSlot(RecipeIngredientRole.INPUT, 27, 21)
                .addIngredients(NeoForgeTypes.FLUID_STACK, tagStacks(recipe.co2Tag(), recipe.co2Amount()))
                .setFluidRenderer(Math.max(1, recipe.co2Amount()), false, 16, 47)
                .addRichTooltipCallback((slotView, tooltip) ->
                        slotView.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK).ifPresent(fs ->
                                GuiFluidArea.fillTooltip(fs, recipe.co2Amount(), tooltip::add)));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 46, 21)
                .addIngredient(NeoForgeTypes.FLUID_STACK, recipe.steamOutput())
                .setFluidRenderer(Math.max(1, recipe.steamOutput().getAmount()), false, 16, 47)
                .addRichTooltipCallback((slotView, tooltip) ->
                        slotView.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK).ifPresent(fs ->
                                GuiFluidArea.fillTooltip(fs, recipe.steamOutput().getAmount(), tooltip::add)));

        builder.addSlot(RecipeIngredientRole.OUTPUT, StyleNOXMenu.SPENT_X, StyleNOXMenu.SPENT_Y)
                .addItemStack(recipe.output().get());
    }

    private static List<FluidStack> tagStacks(TagKey<Fluid> tag, int amount) {
        return BuiltInRegistries.FLUID.getTag(tag)
                .map(holders -> holders.stream()
                        .map(Holder::value)
                        .map(fluid -> new FluidStack(fluid, Math.max(1, amount)))
                        .toList())
                .orElse(List.of());
    }

    @Override public void draw(@Nonnull StyleNOXRecipe recipe, @Nonnull IRecipeSlotsView recipeSlotsView, @Nonnull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        getRecipeBackground().draw(guiGraphics, 0, 0);
        tankOverlay.draw(guiGraphics, 6, 19);
        tankOverlay.draw(guiGraphics, 25, 19);
        tankOverlay.draw(guiGraphics, 44, 19);
        Component time = Component.translatable(TranslationKey.CATEGORY_STYLENOX_TIME.getLocation(), recipe.getTotalProcessTime() / 20);
        guiGraphics.drawString(font, time, getWidth() / 2 - font.width(time) / 2, 0, 0xAAAAAA, true);
        Component energy = Component.translatable(TranslationKey.CATEGORY_STYLENOX_ENERGY.getLocation(), recipe.getTotalProcessEnergy());
        guiGraphics.drawString(font, energy, getWidth() / 2 - font.width(energy) / 2, 9, 0xAAAAAA, true);
    }
}
