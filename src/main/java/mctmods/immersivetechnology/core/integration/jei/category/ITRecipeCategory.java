package mctmods.immersivetechnology.core.integration.jei.category;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public abstract class ITRecipeCategory<T> implements IRecipeCategory<T> {
    protected final IGuiHelper guiHelper;
    private final RecipeType<T> type;
    public MutableComponent title;
    private IDrawable recipeBackground;
    private IDrawable icon;
    protected final Font font;

    public ITRecipeCategory(IGuiHelper guiHelper, RecipeType<T> type, String localKey) {
        this.guiHelper = guiHelper;
        this.type = type;
        this.title = Component.translatable(localKey);
        this.font = Minecraft.getInstance().font;
    }

    protected void setRecipeBackground(IDrawable background) { this.recipeBackground = background; }

    @Nullable @Override public IDrawable getIcon() { return this.icon; }

    protected void setIcon(ItemStack stack) { this.setIcon(this.guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, stack)); }

    protected void setIcon(IDrawable icon) { this.icon = icon; }

    @Override public @NotNull Component getTitle() { return this.title; }

    @Override public final @NotNull RecipeType<T> getRecipeType() { return type; }

    @Override public int getWidth() { return recipeBackground.getWidth(); }

    @Override public int getHeight() { return recipeBackground.getHeight(); }

    protected IDrawable getRecipeBackground() { return recipeBackground; }
}
