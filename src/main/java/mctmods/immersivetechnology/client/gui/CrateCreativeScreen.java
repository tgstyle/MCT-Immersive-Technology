package mctmods.immersivetechnology.client.gui;

import mctmods.immersivetechnology.client.gui.helper.ITContainerScreen;
import mctmods.immersivetechnology.common.blocks.wooden.gui.CrateCreativeMenu;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CrateCreativeScreen extends ITContainerScreen<CrateCreativeMenu> {
    public CrateCreativeScreen(CrateCreativeMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, ITLib.makeTextureLocation("crate"));
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 94;
    }
}
