package mctmods.immersivetechnology.core.integration.jei.category;

import mctmods.immersivetechnology.client.gui.helper.FluidInfoArea;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerLiquidRecipe;
import mctmods.immersivetechnology.core.integration.jei.JEIRecipeTypes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

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
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull BoilerLiquidRecipe recipe, @NotNull IFocusGroup focuses) {
        int tankCapacity = getTankCapacity(recipe);

        List<FluidStack> inputs = BuiltInRegistries.FLUID.getTag(recipe.fluidTag())
                .map(holders -> holders.stream()
                        .map(Holder::value)
                        .map(fluid -> new FluidStack(fluid, tankCapacity))
                        .toList())
                .orElse(List.of());

        if (inputs.isEmpty()) {
            ResourceLocation biodieselRl = ResourceLocation.fromNamespaceAndPath("immersiveengineering", "biodiesel");
            var biodieselFluid = BuiltInRegistries.FLUID.get(biodieselRl);
            FluidStack dummy = new FluidStack(
                    biodieselFluid != Fluids.EMPTY ? biodieselFluid : Fluids.LAVA,
                    tankCapacity
            );
            inputs = List.of(dummy);
        }

        var inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 80, 20)
                .addIngredients(NeoForgeTypes.FLUID_STACK, inputs)
                .setFluidRenderer(tankCapacity, false, 16, 47);

        inputSlot.addRichTooltipCallback((slotView, tooltip) ->
                slotView.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK).ifPresent(fs ->
                        FluidInfoArea.fillTooltip(fs, tankCapacity, tooltip::add)));
    }

    private int getTankCapacity(@NotNull BoilerLiquidRecipe recipe) {
        return recipe.amount();
    }

    @Override
    public void draw(@NotNull BoilerLiquidRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        getRecipeBackground().draw(guiGraphics, 0, 0);
        tankOverlay.draw(guiGraphics, 78, 18);
    }
}
