package mctmods.immersivetechnology.common.integration.jei;

import mctmods.immersivetechnology.client.gui.BoilerTankScreen;
import mctmods.immersivetechnology.client.gui.DistillerScreen;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerTankRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.DistillerRecipe;
import mctmods.immersivetechnology.common.integration.jei.category.ITBoilerTankCategory;
import mctmods.immersivetechnology.common.integration.jei.category.ITDistillerCategory;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static mctmods.immersivetechnology.client.gui.helper.ITFluidInfoArea.fillTooltip;

@SuppressWarnings({"unused"})
@JeiPlugin
public class JEIIntegration implements IModPlugin {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "main");
    private static IIngredientManager ingredientManager;

    @Override
    public @NotNull ResourceLocation getPluginUid() { return ID; }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ITBoilerTankCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ITDistillerCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(JEIRecipeTypes.BOILER_TANK, getBoilerRecipes());
        registration.addRecipes(JEIRecipeTypes.DISTILLER, getDistillerRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ITMultiblockProvider.BOILER_TANK.iconStack(), JEIRecipeTypes.BOILER_TANK);
        registration.addRecipeCatalyst(ITMultiblockProvider.DISTILLER.iconStack(), JEIRecipeTypes.DISTILLER);
    }

    @Override
    public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(DistillerScreen.class, 76, 37, 24, 17, JEIRecipeTypes.DISTILLER);

        registration.addGuiContainerHandler(BoilerTankScreen.class, new IGuiContainerHandler<>() {
            @Override
            public @NotNull Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(@NotNull BoilerTankScreen gui, double mouseX, double mouseY) {
                int relX = (int) (mouseX - gui.getLeftPos());
                int relY = (int) (mouseY - gui.getTopPos());
                FluidStack fs = null;
                Rect2i area = null;
                if (relX >= 65 && relX < 85 && relY >= 18 && relY < 69) {
                    fs = gui.getMenu().tanks.input().getFluid();
                    area = new Rect2i(gui.getLeftPos() + 65, gui.getTopPos() + 18, 20, 51);
                } else if (relX >= 90 && relX < 110 && relY >= 18 && relY < 69) {
                    fs = gui.getMenu().tanks.output().getFluid();
                    area = new Rect2i(gui.getLeftPos() + 90, gui.getTopPos() + 18, 20, 51);
                }
                if (fs != null && fs.getAmount() > 0) {
                    Rect2i finalArea = area;
                    return ingredientManager.createTypedIngredient(ForgeTypes.FLUID_STACK, fs).map(typedIngredient -> new IClickableIngredient<FluidStack>() {
                        @SuppressWarnings("removal")
                        @Override
                        public @NotNull ITypedIngredient<FluidStack> getTypedIngredient() { return typedIngredient; }

                        @Override
                        public @NotNull Rect2i getArea() { return finalArea; }
                    });
                }
                return Optional.empty();
            }

            @Override
            public @NotNull Collection<IGuiClickableArea> getGuiClickableAreas(@NotNull BoilerTankScreen gui, double guiMouseX, double guiMouseY) {
                List<IGuiClickableArea> areas = new ArrayList<>();
                areas.add(createBoilerClickableArea(65, gui.getMenu().tanks.input()));
                areas.add(createBoilerClickableArea(90, gui.getMenu().tanks.output()));
                return areas;
            }
        });
    }

    private static IGuiClickableArea createBoilerClickableArea(int x, IFluidTank tank) {
        Rect2i area = new Rect2i(x, 18, 20, 51);
        return new IGuiClickableArea() {
            @Override
            public @NotNull Rect2i getArea() { return area; }

            @Override
            public void getTooltip(@NotNull ITooltipBuilder tooltip) {
                FluidStack fs = tank.getFluid();
                fillTooltip(fs, tank.getCapacity(), tooltip::add);
                tooltip.add(Component.translatable("jei.tooltip.show.recipes"));
            }

            @Override
            public void onClick(@NotNull IFocusFactory focusFactory, @NotNull IRecipesGui recipesGui) {
                recipesGui.showTypes(List.of(JEIRecipeTypes.BOILER_TANK));
            }
        };
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) { ingredientManager = jeiRuntime.getIngredientManager(); }

    private List<BoilerTankRecipe> getBoilerRecipes() {
        assert Minecraft.getInstance().level != null;
        return new ArrayList<>(BoilerTankRecipe.RECIPES.getRecipes(Minecraft.getInstance().level));
    }

    private List<DistillerRecipe> getDistillerRecipes() {
        assert Minecraft.getInstance().level != null;
        return new ArrayList<>(DistillerRecipe.RECIPES.getRecipes(Minecraft.getInstance().level));
    }
}
