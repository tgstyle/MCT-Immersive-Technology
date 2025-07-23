package mctmods.immersivetechnology.common.integration.jei;

import mctmods.immersivetechnology.client.menu.multiblock.AdvCokeOvenScreen;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.AdvancedCokeOvenRecipe;
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

import java.util.List;
import java.util.function.Predicate;

@JeiPlugin
public class JEIIntegration implements IModPlugin {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "main");

    @Override
    public @NotNull ResourceLocation getPluginUid() { return ID; }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) { registration.addRecipeCategories(new ITAdvancedCokeOvenCategory(registration.getJeiHelpers().getGuiHelper())); }

    @Override
    public void registerRecipes(IRecipeRegistration registration) { registration.addRecipes(JEIRecipeTypes.ADV_COKE_OVEN, getRecipes()); }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) { registration.addRecipeCatalyst(ITMultiblockProvider.ADV_COKE_OVEN.iconStack(), JEIRecipeTypes.ADV_COKE_OVEN); }

    @Override
    public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        IModPlugin.super.registerGuiHandlers(registration);
        registration.addRecipeClickArea(AdvCokeOvenScreen.class, 56, 36, 14, 14, JEIRecipeTypes.ADV_COKE_OVEN);
    }

    private List<AdvancedCokeOvenRecipe> getRecipes() { return getFiltered($ -> true); }

    private List<AdvancedCokeOvenRecipe> getFiltered(Predicate<AdvancedCokeOvenRecipe> include) {
        assert Minecraft.getInstance().level != null;
        return AdvancedCokeOvenRecipe.RECIPES.getRecipes(Minecraft.getInstance().level).stream().filter(include).toList();
    }
}
