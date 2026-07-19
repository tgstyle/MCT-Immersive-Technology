package mctmods.immersivetechnology.core.integration.jei.category;

import mctmods.immersivetechnology.client.gui.helper.ITFluidInfoArea;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.GasTurbineRecipe;
import mctmods.immersivetechnology.core.integration.jei.JEIRecipeTypes;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockRegistry;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ITGasTurbineCategory extends ITRecipeCategory<GasTurbineRecipe> {
    private final IDrawableStatic tankOverlay;
    private final IDrawableAnimated turbineAndArrow;

    public ITGasTurbineCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.GAS_TURBINE, "block.immersivetechnology.gas_turbine");
        ResourceLocation background = ITLib.makeTextureLocation("turbine_jei");
        setRecipeBackground(helper.createDrawable(background, 0, 0, 116, 69));
        setIcon(ITMultiblockRegistry.GAS_TURBINE.iconStack());

        tankOverlay = helper.createDrawable(background, 118, 2, 16, 47);

        IDrawableStatic staticImage = helper.createDrawable(background, 0, 78, 32, 42);
        this.turbineAndArrow = helper.createAnimatedDrawable(staticImage, 200, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull GasTurbineRecipe recipe, @NotNull IFocusGroup focuses) {
        int tankCapacity = getTankCapacity(recipe);

        List<FluidStack> inputs = BuiltInRegistries.FLUID.getTag(recipe.fluidTag())
                .map(holders -> holders.stream()
                        .map(Holder::value)
                        .map(fluid -> new FluidStack(fluid, recipe.amount()))
                        .toList())
                .orElse(List.of());

        var inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 11, 11)
                .addIngredients(NeoForgeTypes.FLUID_STACK, inputs)
                .setFluidRenderer(tankCapacity, false, 16, 47);
        inputSlot.addRichTooltipCallback((slotView, tooltip) -> slotView.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK).ifPresent(fs ->
                ITFluidInfoArea.fillTooltip(fs, recipe.amount(), tooltip::add)));

        FluidStack fluidOut = (recipe.fluidOutput != null && !recipe.fluidOutput.isEmpty()) ? recipe.fluidOutput : FluidStack.EMPTY;
        var outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 89, 11)
                .addIngredient(NeoForgeTypes.FLUID_STACK, fluidOut)
                .setFluidRenderer(tankCapacity, false, 16, 47);
        outputSlot.addRichTooltipCallback((slotView, tooltip) -> slotView.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK).ifPresent(fs ->
                ITFluidInfoArea.fillTooltip(fs, recipe.fluidOutput != null ? recipe.fluidOutput.getAmount() : 0, tooltip::add)));
    }

    private int getTankCapacity(@NotNull GasTurbineRecipe recipe) {
        int tankCapacity = recipe.amount();
        if (recipe.fluidOutput != null && !recipe.fluidOutput.isEmpty()) tankCapacity = Math.max(tankCapacity, recipe.fluidOutput.getAmount());
        return tankCapacity;
    }

    @Override public void draw(@NotNull GasTurbineRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        getRecipeBackground().draw(guiGraphics, 0, 0);
        tankOverlay.draw(guiGraphics, 11, 11);
        tankOverlay.draw(guiGraphics, 89, 11);
        turbineAndArrow.draw(guiGraphics, 42, 18);
    }
}
