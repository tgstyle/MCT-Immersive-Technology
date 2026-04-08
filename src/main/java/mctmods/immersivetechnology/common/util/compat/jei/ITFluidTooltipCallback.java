package mctmods.immersivetechnology.common.util.compat.jei;

import mezz.jei.api.gui.ITooltipCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ITFluidTooltipCallback implements ITooltipCallback<FluidStack> {
    @Override public void onTooltip(int slotIndex, boolean input, @Nonnull FluidStack ingredient, @Nonnull List<String> tooltip) {
        if (ingredient.getFluid() != null) {
            ArrayList<String> fluidInfo = new ArrayList<>();
            Fluid f = ingredient.getFluid();
            if (f.getName().equals("potion")) {
                ItemStack potionItem = new ItemStack(Items.POTIONITEM);
                potionItem.setTagCompound(ingredient.tag);
                PotionUtils.addPotionTooltip(potionItem, fluidInfo, 1.0F);
            }
            if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips && GuiScreen.isShiftKeyDown()) {
                fluidInfo.add(TextFormatting.DARK_GRAY + "Fluid Registry: " + f.getName());
                fluidInfo.add(TextFormatting.DARK_GRAY + "Density: " + f.getDensity(ingredient));
                fluidInfo.add(TextFormatting.DARK_GRAY + "Temperature: " + f.getTemperature(ingredient));
                fluidInfo.add(TextFormatting.DARK_GRAY + "Viscosity: " + f.getViscosity(ingredient));
                if (ingredient.tag != null) { fluidInfo.add(TextFormatting.DARK_GRAY + "NBT Data: " + ingredient.tag); }
            }
            if (tooltip.size() > 1) tooltip.addAll(1, fluidInfo); else tooltip.addAll(fluidInfo);
        }
    }
}
