package mctmods.immersivetechnology.core.integration.jei.category;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerSolidRecipe;
import mctmods.immersivetechnology.core.integration.jei.JEIRecipeTypes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class BoilerSolidCategory extends RecipeCategory<BoilerSolidRecipe> {

    public BoilerSolidCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.BOILER_SOLID, "block.immersivetechnology.boiler_solid");

        ResourceLocation background = Reference.makeTextureLocation("boiler_solid");
        IDrawableStatic back = helper.drawableBuilder(background, 0, 0, 176, 77)
                .setTextureSize(256, 256)
                .build();

        setRecipeBackground(back);
        setIcon(MultiblockRegistry.BOILER_SOLID.iconStack());
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

        builder.addSlot(RecipeIngredientRole.INPUT, 44, 34)
                .addIngredients(VanillaTypes.ITEM_STACK, inputs);
    }

    @Override public void draw(@NotNull BoilerSolidRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        getRecipeBackground().draw(guiGraphics, 0, 0);
    }
}
