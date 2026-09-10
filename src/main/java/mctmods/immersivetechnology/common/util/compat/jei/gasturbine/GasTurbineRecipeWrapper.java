package mctmods.immersivetechnology.common.util.compat.jei.gasturbine;

import com.immersiveconvergence.api.crafting.ICRecipeBase;

import mctmods.immersivetechnology.common.util.compat.jei.ITMultiblockRecipeWrapper;

import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GasTurbineRecipeWrapper extends ITMultiblockRecipeWrapper {
    public ICRecipeBase recipe;

    public GasTurbineRecipeWrapper(ICRecipeBase recipe) {
        super(recipe);
        this.recipe = recipe;
    }

    @Override @SideOnly(Side.CLIENT) public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) { drawTimeText(minecraft, recipe.getTotalProcessTime(), 44, 10); }
}
