package mctmods.immersivetechnology.common.integration.jei;

import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

import java.util.Arrays;

public class ITAdvancedCokeOvenCategory extends ITRecipeCategory<AdvancedCokeOvenRecipe>
{
    private final IDrawableStatic tankOverlay;
    private final IDrawableAnimated flame;

    public ITAdvancedCokeOvenCategory(IGuiHelper helper)
    {
        super(helper, JEIRecipeTypes.ADV_COKE_OVEN, "block.immersivetechnology.coke_oven_advanced");
        ResourceLocation background = new ResourceLocation(ITLib.MODID, "textures/gui/coke_oven_advanced.png");
        setBackground(helper.createDrawable(background, 26, 16, 123, 55));
        setIcon(ITMultiblockProvider.ADV_COKE_OVEN.iconStack());
        tankOverlay = helper.createDrawable(background, 178, 33, 16, 47);
        flame = helper.drawableBuilder(background, 177, 0, 14, 14).buildAnimated(500, IDrawableAnimated.StartDirection.TOP, true);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AdvancedCokeOvenRecipe recipe, IFocusGroup focuses)
    {
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 19)
                .addItemStacks(Arrays.asList(recipe.input.getMatchingStacks()));

        IRecipeSlotBuilder outputSlotBuilder = builder.addSlot(RecipeIngredientRole.OUTPUT, 59, 19);
        if(!recipe.output.get().isEmpty())
            outputSlotBuilder.addItemStack(recipe.output.get());

        if(recipe.creosoteOutput > 0){
            int tankSize = Math.max(FluidType.BUCKET_VOLUME,  recipe.creosoteOutput);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 103, 4)
                    .setFluidRenderer(tankSize, false, 16, 47)
                    .setOverlay(tankOverlay, 0, 0)
                    .addIngredient(ForgeTypes.FLUID_STACK, new FluidStack(IEFluids.CREOSOTE.getStill(), recipe.creosoteOutput))
                    .addTooltipCallback(JEIHelper.fluidTooltipCallback);
        }
    }

    @Override
    public void draw(AdvancedCokeOvenRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY)
    {
        flame.draw(graphics, 31, 20);
    }
}
