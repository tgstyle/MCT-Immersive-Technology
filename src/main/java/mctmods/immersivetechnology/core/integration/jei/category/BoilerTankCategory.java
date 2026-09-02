package mctmods.immersivetechnology.core.integration.jei.category;

import com.immersiveconvergence.api.integration.jei.BaseRecipeCategory;
import com.immersiveconvergence.api.client.gui.GuiFluidArea;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerTankRecipe;
import mctmods.immersivetechnology.core.integration.jei.JEIRecipeTypes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;

public class BoilerTankCategory extends BaseRecipeCategory<BoilerTankRecipe> {

    private final IDrawableStatic tankOverlay;

    public BoilerTankCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.BOILER_TANK, "block.immersivetechnology.boiler_tank");

        ResourceLocation background = Reference.makeTextureLocation("boiler_tank");
        IDrawableStatic back = helper.drawableBuilder(background, 0, 0, 176, 77)
                .setTextureSize(256, 256)
                .build();

        setRecipeBackground(back);
        setIcon(MultiblockRegistry.BOILER_TANK.iconStack());

        tankOverlay = helper.createDrawable(background, 177, 31, 20, 51);
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull BoilerTankRecipe recipe, @Nonnull IFocusGroup focuses) {
        int tankCapacity = getTankCapacity(recipe);

        List<FluidStack> inputs = recipe.input.getMatchingFluidStacks().stream()
                .map(fs -> {
                    FluidStack copy = fs.copy();
                    copy.setAmount(recipe.input.getAmount());
                    return copy;
                })
                .toList();

        var inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 67, 20)
                .addIngredients(ForgeTypes.FLUID_STACK, inputs)
                .setFluidRenderer(tankCapacity, false, 16, 47);

        inputSlot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                        GuiFluidArea.fillTooltip(fs, recipe.input.getAmount(), tooltip::add)));

        var outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 92, 20)
                .addIngredient(ForgeTypes.FLUID_STACK, recipe.output)
                .setFluidRenderer(tankCapacity, false, 16, 47);

        outputSlot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                        GuiFluidArea.fillTooltip(fs, recipe.output.getAmount(), tooltip::add)));
    }

    private int getTankCapacity(@Nonnull BoilerTankRecipe recipe) {
        return Math.max(recipe.input.getAmount(), recipe.output.getAmount());
    }

    @Override
    public void draw(@Nonnull BoilerTankRecipe recipe, @Nonnull IRecipeSlotsView recipeSlotsView, @Nonnull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        getRecipeBackground().draw(guiGraphics, 0, 0);
        tankOverlay.draw(guiGraphics, 65, 18);
        tankOverlay.draw(guiGraphics, 90, 18);
    }
}
