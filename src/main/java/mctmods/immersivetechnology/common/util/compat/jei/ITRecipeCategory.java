package mctmods.immersivetechnology.common.util.compat.jei;

import mctmods.immersivetechnology.ImmersiveTechnology;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapperFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fluids.FluidStack;

@SuppressWarnings("unused")
public abstract class ITRecipeCategory<T, W extends IRecipeWrapper> implements IRecipeCategory<W>, IRecipeWrapperFactory<T> {
	public String uniqueName;
	public String localizedName;
	private final IDrawable background;
	private final Class<T> recipeClass;
	private final GenericMultiblockIngredient[] displayStacks;

	public ITRecipeCategory(String uniqueName, String localKey, IDrawable background, Class<T> recipeClass, GenericMultiblockIngredient... displayStacks) {
		this.uniqueName = uniqueName;
		this.localizedName = I18n.format(localKey);
		this.background = background;
		this.recipeClass = recipeClass;
		this.displayStacks = displayStacks;
	}

	public void addCatalysts(IModRegistry registry) {
		for (GenericMultiblockIngredient stack : displayStacks) if (stack != null) registry.addRecipeCatalyst(stack, getUid());
	}

    @Override @Nonnull public String getUid() { return "it." + uniqueName; }

	@Override @Nonnull public String getTitle() { return localizedName; }

	@Override @Nonnull public IDrawable getBackground() { return background; }

	@Override public void drawExtras(@Nonnull Minecraft minecraft) { }

	@Override @Nonnull public List<String> getTooltipStrings(int mouseX, int mouseY) { return Collections.emptyList(); }

	public Class<T> getRecipeClass() { return this.recipeClass; }

	public String getRecipeCategoryUid() { return "it." + uniqueName; }

	public boolean isRecipeValid(T recipe) { return true; }

	@Override @Nonnull public String getModName() { return ImmersiveTechnology.NAME; }

	@SafeVarargs protected static int getMaxFluidAmount(List<List<FluidStack>>... fluidLists) {
		int max = 0;
		for (List<List<FluidStack>> fluidList : fluidLists) {
			for (List<FluidStack> stacks : fluidList) {
				for (FluidStack fluid : stacks) { if (fluid.amount > max) { max = fluid.amount; } }
			}
		}
		return max;
	}
}
