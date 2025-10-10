package mctmods.immersivetechnology.common.integration.jei;

import mctmods.immersivetechnology.client.gui.BoilerLiquidScreen;
import mctmods.immersivetechnology.client.gui.BoilerSolidScreen;
import mctmods.immersivetechnology.client.gui.BoilerTankScreen;
import mctmods.immersivetechnology.client.gui.DistillerScreen;
import mctmods.immersivetechnology.client.gui.SolarScreen;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerLiquidRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerSolidRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerTankRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.DistillerRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.SolarMelterRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.SolarTowerRecipe;
import mctmods.immersivetechnology.common.integration.jei.category.ITBoilerLiquidCategory;
import mctmods.immersivetechnology.common.integration.jei.category.ITBoilerSolidCategory;
import mctmods.immersivetechnology.common.integration.jei.category.ITBoilerTankCategory;
import mctmods.immersivetechnology.common.integration.jei.category.ITDistillerCategory;
import mctmods.immersivetechnology.common.integration.jei.category.ITSolarMelterCategory;
import mctmods.immersivetechnology.common.integration.jei.category.ITSolarTowerCategory;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
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
import net.minecraft.world.item.ItemStack;
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
        registration.addRecipeCategories(new ITBoilerLiquidCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ITBoilerSolidCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ITBoilerTankCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ITDistillerCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ITSolarMelterCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ITSolarTowerCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(JEIRecipeTypes.BOILER_LIQUID, getBoilerLiquidRecipes());
        registration.addRecipes(JEIRecipeTypes.BOILER_SOLID, getBoilerSolidRecipes());
        registration.addRecipes(JEIRecipeTypes.BOILER_TANK, getBoilerRecipes());
        registration.addRecipes(JEIRecipeTypes.DISTILLER, getDistillerRecipes());
        registration.addRecipes(JEIRecipeTypes.SOLAR_MELTER, getSolarMelterRecipes());
        registration.addRecipes(JEIRecipeTypes.SOLAR_TOWER, getSolarTowerRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ITMultiblockProvider.BOILER_LIQUID.iconStack(), JEIRecipeTypes.BOILER_LIQUID);
        registration.addRecipeCatalyst(ITMultiblockProvider.BOILER_SOLID.iconStack(), JEIRecipeTypes.BOILER_SOLID);
        registration.addRecipeCatalyst(ITMultiblockProvider.BOILER_TANK.iconStack(), JEIRecipeTypes.BOILER_TANK);
        registration.addRecipeCatalyst(ITMultiblockProvider.DISTILLER.iconStack(), JEIRecipeTypes.DISTILLER);
        registration.addRecipeCatalyst(ITMultiblockProvider.SOLAR_MELTER.iconStack(), JEIRecipeTypes.SOLAR_MELTER);
        registration.addRecipeCatalyst(ITMultiblockProvider.SOLAR_TOWER.iconStack(), JEIRecipeTypes.SOLAR_TOWER);
    }

    @Override
    public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(BoilerLiquidScreen.class, new IGuiContainerHandler<>() {
            @Override
            public @NotNull Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(@NotNull BoilerLiquidScreen gui, double mouseX, double mouseY) {
                int relX = (int) (mouseX - gui.getLeftPos());
                int relY = (int) (mouseY - gui.getTopPos());
                FluidStack fs = null;
                Rect2i area = null;
                if (relX >= 80 && relX < 96 && relY >= 20 && relY < 67) {
                    fs = gui.getMenu().tanks.input1().getFluid();
                    area = new Rect2i(gui.getLeftPos() + 80, gui.getTopPos() + 20, 16, 47);
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
            public @NotNull Collection<IGuiClickableArea> getGuiClickableAreas(@NotNull BoilerLiquidScreen gui, double guiMouseX, double guiMouseY) {
                List<IGuiClickableArea> areas = new ArrayList<>();
                areas.add(createBoilerLiquidClickableArea(gui.getMenu().tanks.input1()));
                return areas;
            }
        });
        registration.addGuiContainerHandler(BoilerSolidScreen.class, new IGuiContainerHandler<>() {
            @Override
            public @NotNull Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(@NotNull BoilerSolidScreen gui, double mouseX, double mouseY) {
                int relX = (int) (mouseX - gui.getLeftPos());
                int relY = (int) (mouseY - gui.getTopPos());
                ItemStack is = ItemStack.EMPTY;
                Rect2i area = null;
                if (relX >= 80 && relX < 98 && relY >= 53 && relY < 71) {
                    is = gui.getMenu().getSlot(0).getItem();
                    area = new Rect2i(gui.getLeftPos() + 80, gui.getTopPos() + 53, 18, 18);
                }
                if (!is.isEmpty()) {
                    Rect2i finalArea = area;
                    return ingredientManager.createTypedIngredient(VanillaTypes.ITEM_STACK, is).map(typedIngredient -> new IClickableIngredient<ItemStack>() {
                        @SuppressWarnings("removal")
                        @Override
                        public @NotNull ITypedIngredient<ItemStack> getTypedIngredient() { return typedIngredient; }

                        @Override
                        public @NotNull Rect2i getArea() {
                            assert finalArea != null;
                            return finalArea; }
                    });
                }
                return Optional.empty();
            }

            @Override
            public @NotNull Collection<IGuiClickableArea> getGuiClickableAreas(@NotNull BoilerSolidScreen gui, double guiMouseX, double guiMouseY) {
                List<IGuiClickableArea> areas = new ArrayList<>();
                areas.add(createBoilerSolidClickableArea());
                return areas;
            }
        });
        registration.addGuiContainerHandler(DistillerScreen.class, new IGuiContainerHandler<>() {
            @Override
            public @NotNull Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(@NotNull DistillerScreen gui, double mouseX, double mouseY) {
                int relX = (int) (mouseX - gui.getLeftPos());
                int relY = (int) (mouseY - gui.getTopPos());
                FluidStack fs = null;
                Rect2i area = null;
                if (relX >= 56 && relX < 76 && relY >= 19 && relY < 70) {
                    fs = gui.getMenu().tanks.input().getFluid();
                    area = new Rect2i(gui.getLeftPos() + 56, gui.getTopPos() + 19, 20, 51);
                } else if (relX >= 112 && relX < 132 && relY >= 19 && relY < 70) {
                    fs = gui.getMenu().tanks.output().getFluid();
                    area = new Rect2i(gui.getLeftPos() + 112, gui.getTopPos() + 19, 20, 51);
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
            public @NotNull Collection<IGuiClickableArea> getGuiClickableAreas(@NotNull DistillerScreen gui, double guiMouseX, double guiMouseY) {
                List<IGuiClickableArea> areas = new ArrayList<>();
                areas.add(createDistillerClickableArea(56, gui.getMenu().tanks.input()));
                areas.add(createDistillerClickableArea(112, gui.getMenu().tanks.output()));
                return areas;
            }
        });

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
        registration.addGuiContainerHandler(SolarScreen.class, new IGuiContainerHandler<>() {
            @Override
            public @NotNull Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(@NotNull SolarScreen gui, double mouseX, double mouseY) {
                int relX = (int) (mouseX - gui.getLeftPos());
                int relY = (int) (mouseY - gui.getTopPos());
                FluidStack fs = null;
                Rect2i area = null;
                if (relX >= 102 && relX < 118 && relY >= 21 && relY < 68) {
                    fs = gui.getMenu().inputTank.getFluid();
                    area = new Rect2i(gui.getLeftPos() + 102, gui.getTopPos() + 21, 16, 47);
                } else if (relX >= 126 && relX < 142 && relY >= 21 && relY < 68) {
                    fs = gui.getMenu().outputTank.getFluid();
                    area = new Rect2i(gui.getLeftPos() + 126, gui.getTopPos() + 21, 16, 47);
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
            public @NotNull Collection<IGuiClickableArea> getGuiClickableAreas(@NotNull SolarScreen gui, double guiMouseX, double guiMouseY) {
                List<IGuiClickableArea> areas = new ArrayList<>();
                areas.add(createSolarClickableArea(102, gui.getMenu().inputTank, gui));
                areas.add(createSolarClickableArea(126, gui.getMenu().outputTank, gui));
                return areas;
            }
        });
    }

    private static IGuiClickableArea createBoilerLiquidClickableArea(IFluidTank tank) {
        Rect2i area = new Rect2i(80, 20, 16, 47);
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
                recipesGui.showTypes(List.of(JEIRecipeTypes.BOILER_LIQUID));
            }
        };
    }

    private static IGuiClickableArea createBoilerSolidClickableArea() {
        Rect2i area = new Rect2i(81, 35, 14, 14);
        return new IGuiClickableArea() {
            @Override
            public @NotNull Rect2i getArea() { return area; }

            @Override
            public void getTooltip(@NotNull ITooltipBuilder tooltip) {
                tooltip.add(Component.translatable("jei.tooltip.show.recipes"));
            }

            @Override
            public void onClick(@NotNull IFocusFactory focusFactory, @NotNull IRecipesGui recipesGui) {
                recipesGui.showTypes(List.of(JEIRecipeTypes.BOILER_SOLID));
            }
        };
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

    private static IGuiClickableArea createDistillerClickableArea(int x, IFluidTank tank) {
        Rect2i area = new Rect2i(x, 19, 20, 51);
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
                recipesGui.showTypes(List.of(JEIRecipeTypes.DISTILLER));
            }
        };
    }

    private static IGuiClickableArea createSolarClickableArea(int x, IFluidTank tank, SolarScreen gui) {
        Rect2i area = new Rect2i(x, 21, 16, 47);
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
                recipesGui.showTypes(List.of(gui.isMelter ? JEIRecipeTypes.SOLAR_MELTER : JEIRecipeTypes.SOLAR_TOWER));
            }
        };
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) { ingredientManager = jeiRuntime.getIngredientManager(); }

    private List<BoilerLiquidRecipe> getBoilerLiquidRecipes() {
        assert Minecraft.getInstance().level != null;
        return new ArrayList<>(BoilerLiquidRecipe.RECIPES.getRecipes(Minecraft.getInstance().level));
    }

    private List<BoilerSolidRecipe> getBoilerSolidRecipes() {
        assert Minecraft.getInstance().level != null;
        return new ArrayList<>(BoilerSolidRecipe.RECIPES.getRecipes(Minecraft.getInstance().level));
    }

    private List<BoilerTankRecipe> getBoilerRecipes() {
        assert Minecraft.getInstance().level != null;
        return new ArrayList<>(BoilerTankRecipe.RECIPES.getRecipes(Minecraft.getInstance().level));
    }

    private List<DistillerRecipe> getDistillerRecipes() {
        assert Minecraft.getInstance().level != null;
        return new ArrayList<>(DistillerRecipe.RECIPES.getRecipes(Minecraft.getInstance().level));
    }

    private List<SolarMelterRecipe> getSolarMelterRecipes() {
        assert Minecraft.getInstance().level != null;
        return new ArrayList<>(SolarMelterRecipe.RECIPES.getRecipes(Minecraft.getInstance().level));
    }

    private List<SolarTowerRecipe> getSolarTowerRecipes() {
        assert Minecraft.getInstance().level != null;
        return new ArrayList<>(SolarTowerRecipe.RECIPES.getRecipes(Minecraft.getInstance().level));
    }
}
