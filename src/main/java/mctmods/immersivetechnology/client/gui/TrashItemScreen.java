package mctmods.immersivetechnology.client.gui;

import mctmods.immersivetechnology.client.gui.helper.ITContainerScreen;
import mctmods.immersivetechnology.common.blocks.metal.gui.TrashItemMenu;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TrashItemScreen extends ITContainerScreen<TrashItemMenu> {
    public TrashItemScreen(TrashItemMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, ITLib.makeTextureLocation("single_item"));
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 94;
    }
}
