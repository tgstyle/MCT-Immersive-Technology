package mctmods.immersivetechnology.client.integration.jei.coolingtower;

import mctmods.immersivetechnology.client.integration.jei.ITMultiblockRecipeWrapper;

import com.immersiveconvergence.api.crafting.ICRecipeBase;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CoolingTowerRecipeWrapper extends ITMultiblockRecipeWrapper {
    public ICRecipeBase recipe;

    public CoolingTowerRecipeWrapper(ICRecipeBase recipe) {
        super(recipe);
        this.recipe = recipe;
    }

    @Override @SideOnly(Side.CLIENT) public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) { drawTimeText(minecraft, recipe.getTotalProcessTime(), 54, 21); }
}
