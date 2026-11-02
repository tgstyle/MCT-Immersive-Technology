package mctmods.immersivetechnology.core.registration;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.ITContent;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import javax.annotation.Nonnull;

public class ITCreativeTab {
    public static CreativeTabs tab;

    public static void register() {
        tab = new CreativeTabs(ImmersiveTechnology.MODID) {
            @Override @Nonnull public ItemStack createIcon() { return new ItemStack(ITContent.blockValve != null ? ITContent.blockValve : ITContent.blockMetalDecoration, 1, 0); }
        };
    }
}
