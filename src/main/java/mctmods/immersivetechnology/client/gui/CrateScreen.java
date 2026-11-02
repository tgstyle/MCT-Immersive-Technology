package mctmods.immersivetechnology.client.gui;

import mctmods.immersivetechnology.common.gui.CrateMenu;
import mctmods.immersivetechnology.core.lib.Reference;

import com.immersiveconvergence.api.client.gui.BaseContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CrateScreen extends BaseContainerScreen<CrateMenu> {
    public CrateScreen(CrateMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, Reference.makeTextureLocation("crate"));
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 94;
    }
}
