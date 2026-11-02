package mctmods.immersivetechnology.client.integration.jei.electrolyticcruciblebattery;

import mctmods.immersivetechnology.client.integration.jei.ITMultiblockRecipeWrapper;
import mctmods.immersivetechnology.common.util.TranslationKey;

import com.immersiveconvergence.api.crafting.ICRecipeBase;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ElectrolyticCrucibleBatteryWrapper extends ITMultiblockRecipeWrapper {
    public ICRecipeBase recipe;

    public ElectrolyticCrucibleBatteryWrapper(ICRecipeBase recipe) {
        super(recipe);
        this.recipe = recipe;
    }

    @Override @SideOnly(Side.CLIENT) public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        drawTimeText(minecraft, recipe.getTotalProcessTime(), 54, 8);
        minecraft.fontRenderer.drawString(TranslationKey.GUI_IF_PER_TICK.format(recipe.getTotalProcessEnergy()/recipe.getTotalProcessTime()),41, 20, 0x8B8B8B, true);
    }
}
