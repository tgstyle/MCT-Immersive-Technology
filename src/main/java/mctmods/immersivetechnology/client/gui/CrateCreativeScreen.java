package mctmods.immersivetechnology.client.gui;

import com.immersiveconvergence.api.client.gui.BaseContainerScreen;
import mctmods.immersivetechnology.common.blocks.wooden.gui.CrateCreativeMenu;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CrateCreativeScreen extends BaseContainerScreen<CrateCreativeMenu> {
    public CrateCreativeScreen(CrateCreativeMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, Reference.makeTextureLocation("crate"));
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 94;
    }
}
