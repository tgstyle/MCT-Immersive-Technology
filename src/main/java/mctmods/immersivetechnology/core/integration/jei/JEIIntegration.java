package mctmods.immersivetechnology.core.integration.jei;

import mctmods.immersivetechnology.client.gui.*;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.*;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.CoolingTowerRecipe;
import mctmods.immersivetechnology.core.integration.jei.category.*;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static mctmods.immersivetechnology.client.gui.helper.FluidInfoArea.fillTooltip;

@SuppressWarnings({"unused"})
@JeiPlugin
public class JEIIntegration implements IModPlugin {

    private static final ResourceLocation ID = Reference.rl("main");
    private static IIngredientManager ingredientManager;

    @Override @NotNull public ResourceLocation getPluginUid() { return ID; }

    @Override public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new AdvancedCokeOvenCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new BoilerLiquidCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new BoilerSolidCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new BoilerTankCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new CoolingTowerCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new DistillerCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ElectrolyticCrucibleBatteryCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new GasTurbineCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new HeatExchangerCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new RadiatorCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new MeltingCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new SolarTowerCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new SteamTurbineCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(JEIRecipeTypes.ADVANCED_COKE_OVEN_CUSTOM, getAdvancedCokeOvenCustomRecipes());
        registration.addRecipes(JEIRecipeTypes.BOILER_LIQUID, getBoilerLiquidRecipes());
        registration.addRecipes(JEIRecipeTypes.BOILER_SOLID, getBoilerSolidRecipes());
        registration.addRecipes(JEIRecipeTypes.BOILER_TANK, getBoilerRecipes());
        registration.addRecipes(JEIRecipeTypes.COOLING_TOWER, getCoolingTowerRecipes());
        registration.addRecipes(JEIRecipeTypes.DISTILLER, getDistillerRecipes());
        registration.addRecipes(JEIRecipeTypes.ELECTROLYTIC_CRUCIBLE_BATTERY, getElectrolyticCrucibleBatteryRecipes());
        registration.addRecipes(JEIRecipeTypes.GAS_TURBINE, getGasTurbineRecipes());
        registration.addRecipes(JEIRecipeTypes.HEAT_EXCHANGER, getHeatExchangerRecipes());
        registration.addRecipes(JEIRecipeTypes.RADIATOR, getRadiatorRecipes());
        registration.addRecipes(JEIRecipeTypes.MELTING, getSolarMelterRecipes());
        registration.addRecipes(JEIRecipeTypes.SOLAR_TOWER, getSolarTowerRecipes());
        registration.addRecipes(JEIRecipeTypes.STEAM_TURBINE, getSteamTurbineRecipes());
    }

    @Override public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(MultiblockRegistry.ADVANCED_COKE_OVEN.iconStack(), JEIRecipeTypes.ADVANCED_COKE_OVEN);
        registration.addRecipeCatalyst(MultiblockRegistry.ADVANCED_COKE_OVEN.iconStack(), JEIRecipeTypes.ADVANCED_COKE_OVEN_CUSTOM);
        registration.addRecipeCatalyst(MultiblockRegistry.BOILER_LIQUID.iconStack(), JEIRecipeTypes.BOILER_LIQUID);
        registration.addRecipeCatalyst(MultiblockRegistry.BOILER_SOLID.iconStack(), JEIRecipeTypes.BOILER_SOLID);
        registration.addRecipeCatalyst(MultiblockRegistry.BOILER_TANK.iconStack(), JEIRecipeTypes.BOILER_TANK);
        registration.addRecipeCatalyst(MultiblockRegistry.COOLING_TOWER.iconStack(), JEIRecipeTypes.COOLING_TOWER);
        registration.addRecipeCatalyst(MultiblockRegistry.DISTILLER.iconStack(), JEIRecipeTypes.DISTILLER);
        registration.addRecipeCatalyst(MultiblockRegistry.ELECTROLYTIC_CRUCIBLE_BATTERY.iconStack(), JEIRecipeTypes.ELECTROLYTIC_CRUCIBLE_BATTERY);
        registration.addRecipeCatalyst(MultiblockRegistry.GAS_TURBINE.iconStack(), JEIRecipeTypes.GAS_TURBINE);
        registration.addRecipeCatalyst(MultiblockRegistry.HEAT_EXCHANGER.iconStack(), JEIRecipeTypes.HEAT_EXCHANGER);
        registration.addRecipeCatalyst(MultiblockRegistry.RADIATOR.iconStack(), JEIRecipeTypes.RADIATOR);
        registration.addRecipeCatalyst(MultiblockRegistry.RADIATOR_HORIZONTAL.iconStack(), JEIRecipeTypes.RADIATOR);
        registration.addRecipeCatalyst(MultiblockRegistry.SOLAR_MELTER.iconStack(), JEIRecipeTypes.MELTING);
        registration.addRecipeCatalyst(MultiblockRegistry.MELTING_CRUCIBLE.iconStack(), JEIRecipeTypes.MELTING);
        registration.addRecipeCatalyst(MultiblockRegistry.SOLAR_TOWER.iconStack(), JEIRecipeTypes.SOLAR_TOWER);
        registration.addRecipeCatalyst(MultiblockRegistry.STEAM_TURBINE.iconStack(), JEIRecipeTypes.STEAM_TURBINE);
    }

    @Override public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(AdvancedCokeOvenScreen.class, new IGuiContainerHandler<>() {
            @Override
            @SuppressWarnings("removal")
            @NotNull public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(@NotNull AdvancedCokeOvenScreen gui, double mouseX, double mouseY) {
                return Optional.empty();
            }

            @Override @NotNull public Collection<IGuiClickableArea> getGuiClickableAreas(@NotNull AdvancedCokeOvenScreen gui, double mouseX, double mouseY) {
                List<IGuiClickableArea> areas = new ArrayList<>();
                areas.add(createAdvancedCokeOvenClickableArea());
                return areas;
            }
        });

        registration.addGuiContainerHandler(BoilerLiquidScreen.class, new IGuiContainerHandler<>() {
            @Override
            @SuppressWarnings("removal")
            @NotNull public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(@NotNull BoilerLiquidScreen gui, double mouseX, double mouseY) {
                int relX = (int) (mouseX - gui.getLeftPos());
                int relY = (int) (mouseY - gui.getTopPos());
                if (relX >= 80 && relX < 96 && relY >= 20 && relY < 67) {
                    FluidStack fs = gui.getMenu().tanks.input1().getFluid();
                    if (fs.getAmount() > 0) {
                        Rect2i area = new Rect2i(gui.getLeftPos() + 80, gui.getTopPos() + 20, 16, 47);
                        return ingredientManager.createTypedIngredient(NeoForgeTypes.FLUID_STACK, fs, false)
                                .map(typed -> new IClickableIngredient<FluidStack>() {
                                    @Override @NotNull public ITypedIngredient<FluidStack> getTypedIngredient() { return typed; }
                                    @Override @NotNull public Rect2i getArea() { return area; }
                                });
                    }
                }
                return Optional.empty();
            }

            @Override @NotNull public Collection<IGuiClickableArea> getGuiClickableAreas(@NotNull BoilerLiquidScreen gui, double mouseX, double mouseY) {
                List<IGuiClickableArea> areas = new ArrayList<>();
                areas.add(createBoilerLiquidClickableArea(gui.getMenu().tanks.input1()));
                return areas;
            }
        });

        registration.addGuiContainerHandler(BoilerSolidScreen.class, new IGuiContainerHandler<>() {
            @Override
            @SuppressWarnings("removal")
            @NotNull public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(@NotNull BoilerSolidScreen gui, double mouseX, double mouseY) {
                int relX = (int) (mouseX - gui.getLeftPos());
                int relY = (int) (mouseY - gui.getTopPos());
                if (relX >= 80 && relX < 98 && relY >= 53 && relY < 71) {
                    ItemStack is = gui.getMenu().getSlot(0).getItem();
                    if (!is.isEmpty()) {
                        Rect2i area = new Rect2i(gui.getLeftPos() + 80, gui.getTopPos() + 53, 18, 18);
                        return ingredientManager.createTypedIngredient(VanillaTypes.ITEM_STACK, is, false)
                                .map(typed -> new IClickableIngredient<ItemStack>() {
                                    @Override @NotNull public ITypedIngredient<ItemStack> getTypedIngredient() { return typed; }
                                    @Override @NotNull public Rect2i getArea() { return area; }
                                });
                    }
                }
                return Optional.empty();
            }

            @Override @NotNull public Collection<IGuiClickableArea> getGuiClickableAreas(@NotNull BoilerSolidScreen gui, double mouseX, double mouseY) {
                List<IGuiClickableArea> areas = new ArrayList<>();
                areas.add(createBoilerSolidClickableArea());
                return areas;
            }
        });

        registration.addGuiContainerHandler(DistillerScreen.class, new IGuiContainerHandler<>() {
            @Override
            @SuppressWarnings("removal")
            @NotNull public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(@NotNull DistillerScreen gui, double mouseX, double mouseY) {
                int relX = (int) (mouseX - gui.getLeftPos());
                int relY = (int) (mouseY - gui.getTopPos());
                if (relX >= 56 && relX < 76 && relY >= 19 && relY < 70) {
                    FluidStack fs = gui.getMenu().tanks.input().getFluid();
                    if (fs.getAmount() > 0) {
                        Rect2i area = new Rect2i(gui.getLeftPos() + 56, gui.getTopPos() + 19, 20, 51);
                        return ingredientManager.createTypedIngredient(NeoForgeTypes.FLUID_STACK, fs, false)
                                .map(typed -> new IClickableIngredient<FluidStack>() {
                                    @Override @NotNull public ITypedIngredient<FluidStack> getTypedIngredient() { return typed; }
                                    @Override @NotNull public Rect2i getArea() { return area; }
                                });
                    }
                } else if (relX >= 112 && relX < 132 && relY >= 19 && relY < 70) {
                    FluidStack fs = gui.getMenu().tanks.output().getFluid();
                    if (fs.getAmount() > 0) {
                        Rect2i area = new Rect2i(gui.getLeftPos() + 112, gui.getTopPos() + 19, 20, 51);
                        return ingredientManager.createTypedIngredient(NeoForgeTypes.FLUID_STACK, fs, false)
                                .map(typed -> new IClickableIngredient<FluidStack>() {
                                    @Override @NotNull public ITypedIngredient<FluidStack> getTypedIngredient() { return typed; }
                                    @Override @NotNull public Rect2i getArea() { return area; }
                                });
                    }
                }
                return Optional.empty();
            }

            @Override @NotNull public Collection<IGuiClickableArea> getGuiClickableAreas(@NotNull DistillerScreen gui, double mouseX, double mouseY) {
                List<IGuiClickableArea> areas = new ArrayList<>();
                areas.add(createDistillerClickableArea(56, gui.getMenu().tanks.input()));
                areas.add(createDistillerClickableArea(112, gui.getMenu().tanks.output()));
                return areas;
            }
        });

        registration.addGuiContainerHandler(BoilerTankScreen.class, new IGuiContainerHandler<>() {
            @Override
            @SuppressWarnings("removal")
            @NotNull public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(@NotNull BoilerTankScreen gui, double mouseX, double mouseY) {
                int relX = (int) (mouseX - gui.getLeftPos());
                int relY = (int) (mouseY - gui.getTopPos());
                if (relX >= 65 && relX < 85 && relY >= 18 && relY < 69) {
                    FluidStack fs = gui.getMenu().tanks.input().getFluid();
                    if (fs.getAmount() > 0) {
                        Rect2i area = new Rect2i(gui.getLeftPos() + 65, gui.getTopPos() + 18, 20, 51);
                        return ingredientManager.createTypedIngredient(NeoForgeTypes.FLUID_STACK, fs, false)
                                .map(typed -> new IClickableIngredient<FluidStack>() {
                                    @Override @NotNull public ITypedIngredient<FluidStack> getTypedIngredient() { return typed; }
                                    @Override @NotNull public Rect2i getArea() { return area; }
                                });
                    }
                } else if (relX >= 90 && relX < 110 && relY >= 18 && relY < 69) {
                    FluidStack fs = gui.getMenu().tanks.output().getFluid();
                    if (fs.getAmount() > 0) {
                        Rect2i area = new Rect2i(gui.getLeftPos() + 90, gui.getTopPos() + 18, 20, 51);
                        return ingredientManager.createTypedIngredient(NeoForgeTypes.FLUID_STACK, fs, false)
                                .map(typed -> new IClickableIngredient<FluidStack>() {
                                    @Override @NotNull public ITypedIngredient<FluidStack> getTypedIngredient() { return typed; }
                                    @Override @NotNull public Rect2i getArea() { return area; }
                                });
                    }
                }
                return Optional.empty();
            }

            @Override @NotNull public Collection<IGuiClickableArea> getGuiClickableAreas(@NotNull BoilerTankScreen gui, double mouseX, double mouseY) {
                List<IGuiClickableArea> areas = new ArrayList<>();
                areas.add(createBoilerClickableArea(65, gui.getMenu().tanks.input()));
                areas.add(createBoilerClickableArea(90, gui.getMenu().tanks.output()));
                return areas;
            }
        });

        registration.addGuiContainerHandler(SolarScreen.class, new IGuiContainerHandler<>() {
            @Override
            @SuppressWarnings("removal")
            @NotNull public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(@NotNull SolarScreen gui, double mouseX, double mouseY) {
                int relX = (int) (mouseX - gui.getLeftPos());
                int relY = (int) (mouseY - gui.getTopPos());
                if (relX >= 102 && relX < 118 && relY >= 21 && relY < 68) {
                    FluidStack fs = gui.getMenu().inputTank.getFluid();
                    if (fs.getAmount() > 0) {
                        Rect2i area = new Rect2i(gui.getLeftPos() + 102, gui.getTopPos() + 21, 16, 47);
                        return ingredientManager.createTypedIngredient(NeoForgeTypes.FLUID_STACK, fs, false)
                                .map(typed -> new IClickableIngredient<FluidStack>() {
                                    @Override @NotNull public ITypedIngredient<FluidStack> getTypedIngredient() { return typed; }
                                    @Override @NotNull public Rect2i getArea() { return area; }
                                });
                    }
                } else if (relX >= 126 && relX < 142 && relY >= 21 && relY < 68) {
                    FluidStack fs = gui.getMenu().outputTank.getFluid();
                    if (fs.getAmount() > 0) {
                        Rect2i area = new Rect2i(gui.getLeftPos() + 126, gui.getTopPos() + 21, 16, 47);
                        return ingredientManager.createTypedIngredient(NeoForgeTypes.FLUID_STACK, fs, false)
                                .map(typed -> new IClickableIngredient<FluidStack>() {
                                    @Override @NotNull public ITypedIngredient<FluidStack> getTypedIngredient() { return typed; }
                                    @Override @NotNull public Rect2i getArea() { return area; }
                                });
                    }
                }
                return Optional.empty();
            }

            @Override @NotNull public Collection<IGuiClickableArea> getGuiClickableAreas(@NotNull SolarScreen gui, double mouseX, double mouseY) {
                List<IGuiClickableArea> areas = new ArrayList<>();
                areas.add(createSolarClickableArea(102, gui.getMenu().inputTank, gui));
                areas.add(createSolarClickableArea(126, gui.getMenu().outputTank, gui));
                return areas;
            }
        });

        registration.addGuiContainerHandler(MeltingCrucibleScreen.class, new IGuiContainerHandler<>() {
            @Override
            @SuppressWarnings("removal")
            @NotNull public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(@NotNull MeltingCrucibleScreen gui, double mouseX, double mouseY) {
                int relX = (int) (mouseX - gui.getLeftPos());
                int relY = (int) (mouseY - gui.getTopPos());
                if (relX >= 102 && relX < 118 && relY >= 21 && relY < 68) {
                    FluidStack fs = gui.getMenu().inputTank.getFluid();
                    if (fs.getAmount() > 0) {
                        Rect2i area = new Rect2i(gui.getLeftPos() + 102, gui.getTopPos() + 21, 16, 47);
                        return ingredientManager.createTypedIngredient(NeoForgeTypes.FLUID_STACK, fs, false)
                                .map(typed -> new IClickableIngredient<FluidStack>() {
                                    @Override @NotNull public ITypedIngredient<FluidStack> getTypedIngredient() { return typed; }
                                    @Override @NotNull public Rect2i getArea() { return area; }
                                });
                    }
                } else if (relX >= 126 && relX < 142 && relY >= 21 && relY < 68) {
                    FluidStack fs = gui.getMenu().outputTank.getFluid();
                    if (fs.getAmount() > 0) {
                        Rect2i area = new Rect2i(gui.getLeftPos() + 126, gui.getTopPos() + 21, 16, 47);
                        return ingredientManager.createTypedIngredient(NeoForgeTypes.FLUID_STACK, fs, false)
                                .map(typed -> new IClickableIngredient<FluidStack>() {
                                    @Override @NotNull public ITypedIngredient<FluidStack> getTypedIngredient() { return typed; }
                                    @Override @NotNull public Rect2i getArea() { return area; }
                                });
                    }
                }
                return Optional.empty();
            }

            @Override @NotNull public Collection<IGuiClickableArea> getGuiClickableAreas(@NotNull MeltingCrucibleScreen gui, double mouseX, double mouseY) {
                List<IGuiClickableArea> areas = new ArrayList<>();
                areas.add(createMeltingClickableArea(102, gui.getMenu().inputTank));
                areas.add(createMeltingClickableArea(126, gui.getMenu().outputTank));
                return areas;
            }
        });
    }

    private static IGuiClickableArea createAdvancedCokeOvenClickableArea() {
        Rect2i area = new Rect2i(58, 36, 11, 13);
        return new IGuiClickableArea() {
            @Override @NotNull public Rect2i getArea() { return area; }
            @Override public void getTooltip(@NotNull ITooltipBuilder tooltip) { tooltip.add(Component.translatable("jei.tooltip.show.recipes")); }
            @Override public void onClick(@NotNull IFocusFactory focusFactory, @NotNull IRecipesGui recipesGui) { recipesGui.showTypes(List.of(JEIRecipeTypes.ADVANCED_COKE_OVEN, JEIRecipeTypes.ADVANCED_COKE_OVEN_CUSTOM)); }
        };
    }

    private static IGuiClickableArea createBoilerLiquidClickableArea(IFluidTank tank) {
        Rect2i area = new Rect2i(80, 20, 16, 47);
        return new IGuiClickableArea() {
            @Override @NotNull public Rect2i getArea() { return area; }
            @Override public void getTooltip(@NotNull ITooltipBuilder tooltip) {
                fillTooltip(tank.getFluid(), tank.getCapacity(), tooltip::add);
                tooltip.add(Component.translatable("jei.tooltip.show.recipes"));
            }
            @Override public void onClick(@NotNull IFocusFactory focusFactory, @NotNull IRecipesGui recipesGui) { recipesGui.showTypes(List.of(JEIRecipeTypes.BOILER_LIQUID)); }
        };
    }

    private static IGuiClickableArea createBoilerSolidClickableArea() {
        Rect2i area = new Rect2i(81, 35, 14, 14);
        return new IGuiClickableArea() {
            @Override @NotNull public Rect2i getArea() { return area; }
            @Override public void getTooltip(@NotNull ITooltipBuilder tooltip) { tooltip.add(Component.translatable("jei.tooltip.show.recipes")); }
            @Override public void onClick(@NotNull IFocusFactory focusFactory, @NotNull IRecipesGui recipesGui) { recipesGui.showTypes(List.of(JEIRecipeTypes.BOILER_SOLID)); }
        };
    }

    private static IGuiClickableArea createBoilerClickableArea(int x, IFluidTank tank) {
        Rect2i area = new Rect2i(x, 18, 20, 51);
        return new IGuiClickableArea() {
            @Override @NotNull public Rect2i getArea() { return area; }
            @Override public void getTooltip(@NotNull ITooltipBuilder tooltip) {
                fillTooltip(tank.getFluid(), tank.getCapacity(), tooltip::add);
                tooltip.add(Component.translatable("jei.tooltip.show.recipes"));
            }
            @Override public void onClick(@NotNull IFocusFactory focusFactory, @NotNull IRecipesGui recipesGui) { recipesGui.showTypes(List.of(JEIRecipeTypes.BOILER_TANK)); }
        };
    }

    private static IGuiClickableArea createDistillerClickableArea(int x, IFluidTank tank) {
        Rect2i area = new Rect2i(x, 19, 20, 51);
        return new IGuiClickableArea() {
            @Override @NotNull public Rect2i getArea() { return area; }
            @Override public void getTooltip(@NotNull ITooltipBuilder tooltip) {
                fillTooltip(tank.getFluid(), tank.getCapacity(), tooltip::add);
                tooltip.add(Component.translatable("jei.tooltip.show.recipes"));
            }
            @Override public void onClick(@NotNull IFocusFactory focusFactory, @NotNull IRecipesGui recipesGui) { recipesGui.showTypes(List.of(JEIRecipeTypes.DISTILLER)); }
        };
    }

    private static IGuiClickableArea createSolarClickableArea(int x, IFluidTank tank, SolarScreen gui) {
        Rect2i area = new Rect2i(x, 21, 16, 47);
        return new IGuiClickableArea() {
            @Override @NotNull public Rect2i getArea() { return area; }
            @Override public void getTooltip(@NotNull ITooltipBuilder tooltip) {
                fillTooltip(tank.getFluid(), tank.getCapacity(), tooltip::add);
                tooltip.add(Component.translatable("jei.tooltip.show.recipes"));
            }
            @Override public void onClick(@NotNull IFocusFactory focusFactory, @NotNull IRecipesGui recipesGui) {
                recipesGui.showTypes(List.of(gui.isMelter ? JEIRecipeTypes.MELTING : JEIRecipeTypes.SOLAR_TOWER));
            }
        };
    }

    private static IGuiClickableArea createMeltingClickableArea(int x, IFluidTank tank) {
        Rect2i area = new Rect2i(x, 21, 16, 47);
        return new IGuiClickableArea() {
            @Override @NotNull public Rect2i getArea() { return area; }
            @Override public void getTooltip(@NotNull ITooltipBuilder tooltip) {
                fillTooltip(tank.getFluid(), tank.getCapacity(), tooltip::add);
                tooltip.add(Component.translatable("jei.tooltip.show.recipes"));
            }
            @Override public void onClick(@NotNull IFocusFactory focusFactory, @NotNull IRecipesGui recipesGui) {
                recipesGui.showTypes(List.of(JEIRecipeTypes.MELTING));
            }
        };
    }

    @Override public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) { ingredientManager = jeiRuntime.getIngredientManager(); }

    private List<AdvancedCokeOvenRecipe> getAdvancedCokeOvenCustomRecipes() {
        assert Minecraft.getInstance().level != null;
        return AdvancedCokeOvenRecipe.getAllRecipes(Minecraft.getInstance().level);
    }
    private List<BoilerLiquidRecipe> getBoilerLiquidRecipes() { assert Minecraft.getInstance().level != null; return BoilerLiquidRecipe.RECIPES.getRecipes(Minecraft.getInstance().level).stream().map(RecipeHolder::value).toList(); }
    private List<BoilerSolidRecipe> getBoilerSolidRecipes() { assert Minecraft.getInstance().level != null; return BoilerSolidRecipe.RECIPES.getRecipes(Minecraft.getInstance().level).stream().map(RecipeHolder::value).toList(); }
    private List<BoilerTankRecipe> getBoilerRecipes() { assert Minecraft.getInstance().level != null; return BoilerTankRecipe.RECIPES.getRecipes(Minecraft.getInstance().level).stream().map(RecipeHolder::value).toList(); }
    private List<CoolingTowerRecipe> getCoolingTowerRecipes() { assert Minecraft.getInstance().level != null; return CoolingTowerRecipe.RECIPES.getRecipes(Minecraft.getInstance().level).stream().map(RecipeHolder::value).toList(); }
    private List<DistillerRecipe> getDistillerRecipes() { assert Minecraft.getInstance().level != null; return DistillerRecipe.RECIPES.getRecipes(Minecraft.getInstance().level).stream().map(RecipeHolder::value).toList(); }
    private List<ElectrolyticCrucibleBatteryRecipe> getElectrolyticCrucibleBatteryRecipes() { assert Minecraft.getInstance().level != null; return ElectrolyticCrucibleBatteryRecipe.RECIPES.getRecipes(Minecraft.getInstance().level).stream().map(RecipeHolder::value).toList(); }
    private List<GasTurbineRecipe> getGasTurbineRecipes() { assert Minecraft.getInstance().level != null; return GasTurbineRecipe.RECIPES.getRecipes(Minecraft.getInstance().level).stream().map(RecipeHolder::value).toList(); }
    private List<HeatExchangerRecipe> getHeatExchangerRecipes() { assert Minecraft.getInstance().level != null; return HeatExchangerRecipe.RECIPES.getRecipes(Minecraft.getInstance().level).stream().map(RecipeHolder::value).toList(); }
    private List<RadiatorRecipe> getRadiatorRecipes() { assert Minecraft.getInstance().level != null; return RadiatorRecipe.RECIPES.getRecipes(Minecraft.getInstance().level).stream().map(RecipeHolder::value).toList(); }
    private List<MeltingRecipe> getSolarMelterRecipes() { assert Minecraft.getInstance().level != null; return MeltingRecipe.RECIPES.getRecipes(Minecraft.getInstance().level).stream().map(RecipeHolder::value).toList(); }
    private List<SolarTowerRecipe> getSolarTowerRecipes() { assert Minecraft.getInstance().level != null; return SolarTowerRecipe.RECIPES.getRecipes(Minecraft.getInstance().level).stream().map(RecipeHolder::value).toList(); }
    private List<SteamTurbineRecipe> getSteamTurbineRecipes() { assert Minecraft.getInstance().level != null; return SteamTurbineRecipe.RECIPES.getRecipes(Minecraft.getInstance().level).stream().map(RecipeHolder::value).toList(); }
}
