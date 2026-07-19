package mctmods.immersivetechnology.core.integration.jei.category;

import mctmods.immersivetechnology.client.gui.helper.FluidInfoArea;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.MeltingRecipe;
import mctmods.immersivetechnology.core.integration.jei.JEIRecipeTypes;
import mctmods.immersivetechnology.core.util.TranslationKey;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MeltingCategory extends RecipeCategory<MeltingRecipe> {

    private final IDrawableStatic tankOverlay;

    public MeltingCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.MELTING, "block.immersivetechnology.solar_melter");

        ResourceLocation background = Reference.makeTextureLocation("solar");
        IDrawableStatic back = helper.drawableBuilder(background, 0, 0, 176, 77)
                .setTextureSize(256, 256)
                .build();

        setRecipeBackground(back);
        setIcon(MultiblockRegistry.SOLAR_MELTER.iconStack());

        tankOverlay = helper.drawableBuilder(background, 177, 31, 20, 51)
                .setTextureSize(256, 256)
                .build();
    }

    @Override public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull MeltingRecipe recipe, @NotNull IFocusGroup focuses) {
        int tankCapacity = getTankCapacity(recipe);

        List<FluidStack> inputs = BuiltInRegistries.FLUID.getTag(recipe.inputTag())
                .map(holders -> holders.stream()
                        .map(Holder::value)
                        .map(fluid -> new FluidStack(fluid, recipe.inputAmount()))
                        .toList())
                .orElse(List.of());

        if (inputs.isEmpty()) {
            FluidStack dummy = new FluidStack(Fluids.LAVA, recipe.inputAmount());
            inputs = List.of(dummy);
        }

        var inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 102, 21)
                .addIngredients(NeoForgeTypes.FLUID_STACK, inputs)
                .setFluidRenderer(tankCapacity, false, 16, 47);

        inputSlot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK).ifPresent(fs ->
                        FluidInfoArea.fillTooltip(fs, recipe.inputAmount(), tooltip::add)));

        FluidStack fluidOut = (recipe.fluidOutput != null && !recipe.fluidOutput.isEmpty()) ? recipe.fluidOutput : FluidStack.EMPTY;
        var outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 126, 21)
                .addIngredient(NeoForgeTypes.FLUID_STACK, fluidOut)
                .setFluidRenderer(tankCapacity, false, 16, 47);

        outputSlot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK).ifPresent(fs ->
                        FluidInfoArea.fillTooltip(fs, recipe.fluidOutput != null ? recipe.fluidOutput.getAmount() : 0, tooltip::add)));
    }

    private int getTankCapacity(@NotNull MeltingRecipe recipe) {
        int tankCapacity = recipe.inputAmount();
        if (recipe.fluidOutput != null && !recipe.fluidOutput.isEmpty()) { tankCapacity = Math.max(tankCapacity, recipe.fluidOutput.getAmount()); }
        return tankCapacity;
    }

    @Override public void draw(@NotNull MeltingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        getRecipeBackground().draw(guiGraphics, 0, 0);

        tankOverlay.draw(guiGraphics, 100, 19);
        tankOverlay.draw(guiGraphics, 124, 19);

        Font font = Minecraft.getInstance().font;

        Component timeComponent = Component.translatable(TranslationKey.CATEGORY_SOLAR_MELTER_TIME.getLocation(), recipe.getTotalProcessTime(), recipe.inputAmount())
                .withStyle(style -> style.withColor(TextColor.fromRgb(0xAAAAAA)));
        int timeWidth = font.width(timeComponent);
        int timeX = 122 - timeWidth / 2;
        guiGraphics.drawString(font, timeComponent, timeX, 0, 0xAAAAAA, true);

        Component tempComponent = Component.translatable(TranslationKey.CATEGORY_SOLAR_MELTER_TEMP.getLocation(), recipe.requiredTemp)
                .withStyle(style -> style.withColor(TextColor.fromRgb(0xAAAAAA)));
        int tempWidth = font.width(tempComponent);
        int tempX = 122 - tempWidth / 2;
        guiGraphics.drawString(font, tempComponent, tempX, 9, 0xAAAAAA, true);
    }
}
