package mctmods.immersivetechnology.common.util.compat.jei.electrolyticcrucbilebattery;

import com.immersiveconvergence.api.crafting.MultiblockRecipeBase;

import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.common.util.compat.jei.ITMultiblockRecipeWrapper;

import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ElectrolyticCrucibleBatteryWrapper extends ITMultiblockRecipeWrapper {
    public MultiblockRecipeBase recipe;

    public ElectrolyticCrucibleBatteryWrapper(MultiblockRecipeBase recipe) {
        super(recipe);
        this.recipe = recipe;
    }

    @Override @SideOnly(Side.CLIENT) public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        drawTimeText(minecraft, recipe.getTotalProcessTime(), 54, 8);
        minecraft.fontRenderer.drawString(TranslationKey.GUI_IF_PER_TICK.format(recipe.getTotalProcessEnergy()/recipe.getTotalProcessTime()),41, 20, 0x8B8B8B, true);
    }
}
