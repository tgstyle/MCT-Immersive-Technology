package ferro2000.immersivetech.common.integration.jei;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import ferro2000.immersivetech.ImmersiveTech;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapperFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public abstract class ITRecipeCategory<T, W extends IRecipeWrapper> implements IRecipeCategory<W>, IRecipeWrapperFactory<T>
{
	public String uniqueName;
	public String localizedName;
	private final IDrawable background;
	private final Class<T> recipeClass;
	private final ItemStack[] displayStacks;

	public ITRecipeCategory(String uniqueName, String localKey, IDrawable background, Class<T> recipeClass, ItemStack... displayStacks)
	{
		this.uniqueName = uniqueName;
		this.localizedName = I18n.format(localKey);
		this.background = background;
		this.recipeClass = recipeClass;
		this.displayStacks = displayStacks;
	}

	public void addCatalysts(IModRegistry registry)
	{
		for(ItemStack stack : displayStacks)
			registry.addRecipeCategoryCraftingItem(stack, getUid());
	}

	@Nullable
	@Override
	public IDrawable getIcon()
	{
		return null;
	}

	@Override
	public String getUid()
	{
		return "it."+uniqueName;
	}

	@Override
	public String getTitle()
	{
		return localizedName;
	}

	@Override
	public IDrawable getBackground()
	{
		return background;
	}

	@Override
	public void drawExtras(Minecraft minecraft)
	{
	}

	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY)
	{
		return Collections.emptyList();
	}

//	@Override
	public Class<T> getRecipeClass()
	{
		return this.recipeClass;
	}

//	@Override
	public String getRecipeCategoryUid()
	{
		return "it."+uniqueName;
	}

//	@Override
	public boolean isRecipeValid(T recipe)
	{
		return true;
	}

	@Override
	public String getModName()
	{
		return ImmersiveTech.NAME;
	}
}
