package mctmods.immersivetechnology.common.integration.jei;

import mctmods.immersivetechnology.client.gui.AdvancedCokeOvenScreen;
import mctmods.immersivetechnology.client.gui.BoilerScreen;
import mctmods.immersivetechnology.client.gui.DistillerScreen;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.DistillerRecipe;
import mctmods.immersivetechnology.common.integration.jei.category.ITAdvancedCokeOvenCategory;
import mctmods.immersivetechnology.common.integration.jei.category.ITBoilerCategory;
import mctmods.immersivetechnology.common.integration.jei.category.ITDistillerCategory;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings("unused")
@JeiPlugin
public class JEIIntegration implements IModPlugin {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "main");

    @Override
    public @NotNull ResourceLocation getPluginUid() { return ID; }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ITAdvancedCokeOvenCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ITBoilerCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ITDistillerCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(JEIRecipeTypes.ADVANCED_COKE_OVEN, getAdvancedCokeOvenRecipes());
        registration.addRecipes(JEIRecipeTypes.BOILER, getBoilerRecipes());
        registration.addRecipes(JEIRecipeTypes.DISTILLER, getDistillerRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ITMultiblockProvider.ADVANCED_COKE_OVEN.iconStack(), JEIRecipeTypes.ADVANCED_COKE_OVEN);
        registration.addRecipeCatalyst(ITMultiblockProvider.BOILER.iconStack(), JEIRecipeTypes.BOILER);
        registration.addRecipeCatalyst(ITMultiblockProvider.DISTILLER.iconStack(), JEIRecipeTypes.DISTILLER);
    }

    @Override
    public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(AdvancedCokeOvenScreen.class, 56, 36, 14, 14, JEIRecipeTypes.ADVANCED_COKE_OVEN);
        registration.addRecipeClickArea(BoilerScreen.class, 76, 37, 24, 17, JEIRecipeTypes.BOILER);
        registration.addRecipeClickArea(DistillerScreen.class, 76, 37, 24, 17, JEIRecipeTypes.DISTILLER);
    }

    private List<AdvancedCokeOvenRecipe> getAdvancedCokeOvenRecipes() { return getFiltered($ -> true); }

    private List<AdvancedCokeOvenRecipe> getFiltered(Predicate<AdvancedCokeOvenRecipe> include) {
        assert Minecraft.getInstance().level != null;
        return AdvancedCokeOvenRecipe.RECIPES.getRecipes(Minecraft.getInstance().level).stream().filter(include).toList();
    }

    private List<BoilerRecipe> getBoilerRecipes() {
        assert Minecraft.getInstance().level != null;
        return new ArrayList<>(BoilerRecipe.RECIPES.getRecipes(Minecraft.getInstance().level)); }

    private List<DistillerRecipe> getDistillerRecipes() {
        assert Minecraft.getInstance().level != null;
        return new ArrayList<>(DistillerRecipe.RECIPES.getRecipes(Minecraft.getInstance().level)); }
}
