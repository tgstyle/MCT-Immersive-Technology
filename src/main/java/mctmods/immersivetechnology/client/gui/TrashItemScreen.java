package mctmods.immersivetechnology.client.gui;

import mctmods.immersivetechnology.client.gui.helper.ContainerScreen;
import mctmods.immersivetechnology.common.blocks.metal.gui.TrashItemMenu;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TrashItemScreen extends ContainerScreen<TrashItemMenu> {
    public TrashItemScreen(TrashItemMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, Reference.makeTextureLocation("single_item"));
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 94;
    }
}
