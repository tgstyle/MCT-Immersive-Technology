package mctmods.immersivetechnology.core.integration.jei.category;

import mctmods.immersivetechnology.client.gui.helper.ITFluidInfoArea;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.SolarTowerRecipe;
import mctmods.immersivetechnology.core.integration.jei.JEIRecipeTypes;
import mctmods.immersivetechnology.core.util.TranslationKey;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ITSolarTowerCategory extends ITRecipeCategory<SolarTowerRecipe> {

    private final IDrawableStatic tankOverlay;

    public ITSolarTowerCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.SOLAR_TOWER, "block.immersivetechnology.solar_tower");

        ResourceLocation background = ITLib.makeTextureLocation("solar");
        IDrawableStatic back = helper.drawableBuilder(background, 0, 0, 176, 74)
                .setTextureSize(256, 256)
                .build();

        setRecipeBackground(back);
        setIcon(ITMultiblockProvider.SOLAR_TOWER.iconStack());

        tankOverlay = helper.drawableBuilder(background, 177, 31, 20, 51)
                .setTextureSize(256, 256)
                .build();
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull SolarTowerRecipe recipe, @NotNull IFocusGroup focuses) {
        List<FluidStack> inputs = recipe.input.getMatchingFluidStacks().stream()
                .map(fs -> {
                    FluidStack copy = fs.copy();
                    copy.setAmount(recipe.input.getAmount());
                    return copy;
                })
                .toList();

        if (inputs.isEmpty()) {
            ResourceLocation biodieselRl = ResourceLocation.fromNamespaceAndPath("immersiveengineering", "biodiesel");
            var biodieselFluid = ForgeRegistries.FLUIDS.getValue(biodieselRl);
            FluidStack dummy = new FluidStack(
                    biodieselFluid != null && biodieselFluid != Fluids.EMPTY ? biodieselFluid : Fluids.LAVA,
                    recipe.input.getAmount()
            );
            inputs = List.of(dummy);
        }

        builder.addSlot(RecipeIngredientRole.INPUT, 102, 21)
                .addIngredients(ForgeTypes.FLUID_STACK, inputs)
                .setFluidRenderer(recipe.input.getAmount(), false, 16, 47)
                .setOverlay(tankOverlay, -2, -2)
                .addRichTooltipCallback((slotView, tooltip) ->
                        slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                                ITFluidInfoArea.fillTooltip(fs, recipe.input.getAmount(), tooltip::add)));

        if (recipe.fluidOutput != null) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 126, 21)
                    .addIngredient(ForgeTypes.FLUID_STACK, recipe.fluidOutput)
                    .setFluidRenderer(recipe.fluidOutput.getAmount(), false, 16, 47)
                    .setOverlay(tankOverlay, -2, -2)
                    .addRichTooltipCallback((slotView, tooltip) ->
                            slotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs ->
                                    ITFluidInfoArea.fillTooltip(fs, fs.getAmount(), tooltip::add)));
        }
    }

    @Override
    public void draw(@NotNull SolarTowerRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        getRecipeBackground().draw(guiGraphics, 0, 0);

        Font font = Minecraft.getInstance().font;

        Component timeComponent = Component.translatable(TranslationKey.CATEGORY_SOLAR_TOWER_TIME.getLocation(), recipe.getTotalProcessTime(), recipe.input.getAmount())
                .withStyle(style -> style.withColor(TextColor.fromRgb(0xAAAAAA)));
        int timeWidth = font.width(timeComponent);
        int timeX = 122 - timeWidth / 2;
        guiGraphics.drawString(font, timeComponent, timeX, 0, 0xAAAAAA, true);

        Component tempComponent = Component.translatable(TranslationKey.CATEGORY_SOLAR_TOWER_TEMP.getLocation(), recipe.requiredTemp)
                .withStyle(style -> style.withColor(TextColor.fromRgb(0xAAAAAA)));
        int tempWidth = font.width(tempComponent);
        int tempX = 122 - tempWidth / 2;
        guiGraphics.drawString(font, tempComponent, tempX, 9, 0xAAAAAA, true);
    }
}
