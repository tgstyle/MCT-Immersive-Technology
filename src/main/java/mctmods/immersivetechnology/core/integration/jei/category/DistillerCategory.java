package mctmods.immersivetechnology.core.integration.jei.category;

import com.immersiveconvergence.api.integration.jei.BaseRecipeCategory;
import com.immersiveconvergence.api.client.gui.GuiFluidArea;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.DistillerRecipe;
import mctmods.immersivetechnology.core.integration.jei.JEIRecipeTypes;
import mctmods.immersivetechnology.core.util.TranslationKey;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;

public class DistillerCategory extends BaseRecipeCategory<DistillerRecipe> {

    private final IDrawableStatic tankOverlay;

    public DistillerCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.DISTILLER, "block.immersivetechnology.distiller");

        ResourceLocation background = Reference.makeTextureLocation("distiller");
        setRecipeBackground(helper.createDrawable(background, 0, 0, 176, 74));
        setIcon(MultiblockRegistry.DISTILLER.iconStack());

        tankOverlay = helper.createDrawable(background, 176, 31, 20, 51);
    }

    @Override public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull DistillerRecipe recipe, @Nonnull IFocusGroup focuses) {
        int tankCapacity = getTankCapacity(recipe);

        List<FluidStack> inputs = recipe.input.getMatchingFluidStacks().stream()
                .map(fs -> {
                    FluidStack copy = fs.copy();
                    copy.setAmount(recipe.input.getAmount());
                    return copy;
                })
                .toList();

        var inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 58, 21)
                .addIngredients(ForgeTypes.FLUID_STACK, inputs)
                .setFluidRenderer(tankCapacity, false, 16, 47);

        inputSlot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                        GuiFluidArea.fillTooltip(fs, recipe.input.getAmount(), tooltip::add)));

        FluidStack fluidOut = (recipe.fluidOutput != null && !recipe.fluidOutput.isEmpty()) ? recipe.fluidOutput : FluidStack.EMPTY;
        var outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 112, 21)
                .addIngredient(ForgeTypes.FLUID_STACK, fluidOut)
                .setFluidRenderer(tankCapacity, false, 16, 47);

        outputSlot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                        GuiFluidArea.fillTooltip(fs, recipe.fluidOutput != null ? recipe.fluidOutput.getAmount() : 0, tooltip::add)));

        var itemSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 80, 35)
                .addItemStack(recipe.itemOutput.isEmpty() ? net.minecraft.world.item.ItemStack.EMPTY : recipe.itemOutput);

        itemSlot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(VanillaTypes.ITEM_STACK).ifPresent(stack ->
                        tooltip.add(Component.translatable(TranslationKey.CATEGORY_DISTILLER_CHANCE.getLocation(),
                                String.format("%.2f%%", recipe.chance * 100)))));
    }

    private int getTankCapacity(DistillerRecipe recipe) {
        int tankCapacity = recipe.input.getAmount();
        if (recipe.fluidOutput != null && !recipe.fluidOutput.isEmpty()) tankCapacity = Math.max(tankCapacity, recipe.fluidOutput.getAmount());
        return tankCapacity;
    }

    @Override public void draw(@Nonnull DistillerRecipe recipe, @Nonnull IRecipeSlotsView recipeSlotsView, @Nonnull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        getRecipeBackground().draw(guiGraphics, 0, 0);
        tankOverlay.draw(guiGraphics, 55, 19);
        tankOverlay.draw(guiGraphics, 109, 19);
    }
}
