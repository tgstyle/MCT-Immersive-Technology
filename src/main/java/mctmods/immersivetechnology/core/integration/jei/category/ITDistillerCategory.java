package mctmods.immersivetechnology.core.integration.jei.category;

import mctmods.immersivetechnology.client.gui.helper.ITFluidInfoArea;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.DistillerRecipe;
import mctmods.immersivetechnology.core.integration.jei.JEIRecipeTypes;
import mctmods.immersivetechnology.core.util.TranslationKey;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ITDistillerCategory extends ITRecipeCategory<DistillerRecipe> {

    private final IDrawableStatic tankOverlay;

    public ITDistillerCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.DISTILLER, "block.immersivetechnology.distiller");

        ResourceLocation background = ITLib.makeTextureLocation("distiller");
        setRecipeBackground(helper.createDrawable(background, 0, 0, 176, 74));
        setIcon(ITMultiblockProvider.DISTILLER.iconStack());

        tankOverlay = helper.createDrawable(background, 176, 31, 20, 51);
    }

    @Override public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull DistillerRecipe recipe, @NotNull IFocusGroup focuses) {
        int tankCapacity = getTankCapacity(recipe);

        List<FluidStack> inputs = BuiltInRegistries.FLUID.getTag(recipe.fluidTag())
                .map(holders -> holders.stream()
                        .map(Holder::value)
                        .map(fluid -> new FluidStack(fluid, recipe.amount()))
                        .toList())
                .orElse(List.of());

        var inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 58, 21)
                .addIngredients(NeoForgeTypes.FLUID_STACK, inputs)
                .setFluidRenderer(tankCapacity, false, 16, 47);

        inputSlot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK).ifPresent(fs ->
                        ITFluidInfoArea.fillTooltip(fs, recipe.amount(), tooltip::add)));

        FluidStack fluidOut = (recipe.fluidOutput != null && !recipe.fluidOutput.isEmpty()) ? recipe.fluidOutput : FluidStack.EMPTY;
        var outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 112, 21)
                .addIngredient(NeoForgeTypes.FLUID_STACK, fluidOut)
                .setFluidRenderer(tankCapacity, false, 16, 47);

        outputSlot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK).ifPresent(fs ->
                        ITFluidInfoArea.fillTooltip(fs, recipe.fluidOutput != null ? recipe.fluidOutput.getAmount() : 0, tooltip::add)));

        var itemSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 80, 35)
                .addItemStack(recipe.itemOutput.isEmpty() ? net.minecraft.world.item.ItemStack.EMPTY : recipe.itemOutput);

        itemSlot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(VanillaTypes.ITEM_STACK).ifPresent(stack ->
                        tooltip.add(Component.translatable(TranslationKey.CATEGORY_DISTILLER_CHANCE.getLocation(),
                                String.format("%.2f%%", recipe.chance * 100)))));
    }

    private int getTankCapacity(DistillerRecipe recipe) {
        int tankCapacity = recipe.amount();
        if (recipe.fluidOutput != null && !recipe.fluidOutput.isEmpty()) tankCapacity = Math.max(tankCapacity, recipe.fluidOutput.getAmount());
        return tankCapacity;
    }

    @Override public void draw(@NotNull DistillerRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        getRecipeBackground().draw(guiGraphics, 0, 0);
        tankOverlay.draw(guiGraphics, 55, 19);
        tankOverlay.draw(guiGraphics, 109, 19);
    }
}
