package mctmods.immersivetechnology.core.integration.jei.category;

import mctmods.immersivetechnology.client.gui.helper.FluidInfoArea;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerLiquidRecipe;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.List;

public class BoilerLiquidCategory extends ModRecipeCategory<BoilerLiquidRecipe> {

    private final IDrawableStatic tankOverlay;

    public BoilerLiquidCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.BOILER_LIQUID, "block.immersivetechnology.boiler_liquid");

        ResourceLocation background = Reference.makeTextureLocation("boiler_liquid");
        IDrawableStatic back = helper.drawableBuilder(background, 0, 0, 176, 77)
                .setTextureSize(256, 256)
                .build();

        setRecipeBackground(back);
        setIcon(MultiblockRegistry.BOILER_LIQUID.iconStack());

        tankOverlay = helper.createDrawable(background, 177, 31, 20, 51);
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull BoilerLiquidRecipe recipe, @Nonnull IFocusGroup focuses) {
        int tankCapacity = getTankCapacity(recipe);

        List<FluidStack> inputs = recipe.input.getMatchingFluidStacks().stream()
                .map(fs -> {
                    FluidStack copy = fs.copy();
                    copy.setAmount(tankCapacity);
                    return copy;
                })
                .toList();

        if (inputs.isEmpty()) {
            ResourceLocation biodieselRl = ResourceLocation.fromNamespaceAndPath("immersiveengineering", "biodiesel");
            var biodieselFluid = ForgeRegistries.FLUIDS.getValue(biodieselRl);
            FluidStack dummy = new FluidStack(
                    biodieselFluid != null && biodieselFluid != Fluids.EMPTY ? biodieselFluid : Fluids.LAVA,
                    tankCapacity
            );
            inputs = List.of(dummy);
        }

        var inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 80, 20)
                .addIngredients(ForgeTypes.FLUID_STACK, inputs)
                .setFluidRenderer(tankCapacity, false, 16, 47);

        inputSlot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                        FluidInfoArea.fillTooltip(fs, tankCapacity, tooltip::add)));
    }

    private int getTankCapacity(@Nonnull BoilerLiquidRecipe recipe) {
        return recipe.input.getAmount();
    }

    @Override
    public void draw(@Nonnull BoilerLiquidRecipe recipe, @Nonnull IRecipeSlotsView recipeSlotsView, @Nonnull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        getRecipeBackground().draw(guiGraphics, 0, 0);
        tankOverlay.draw(guiGraphics, 78, 18);
    }
}
