package mctmods.immersivetechnology.client.gui;

import mctmods.immersivetechnology.common.gui.DistillerMenu;
import mctmods.immersivetechnology.core.lib.Reference;

import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.client.gui.BaseContainerScreen;
import com.immersiveconvergence.api.client.gui.GuiEnergyArea;
import com.immersiveconvergence.api.client.gui.GuiFluidArea;
import com.immersiveconvergence.api.client.gui.GuiInfoArea;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import java.util.List;
import javax.annotation.Nonnull;

public class DistillerScreen extends BaseContainerScreen<DistillerMenu> {
    private static final ResourceLocation TEXTURE = Reference.makeTextureLocation("distiller");
    public DistillerScreen(DistillerMenu container, Inventory inventoryPlayer, Component title) { super(container, inventoryPlayer, title, TEXTURE); }

    @Override @Nonnull protected List<GuiInfoArea> makeInfoAreas() {
        return ImmutableList.of(
                new GuiEnergyArea(this.leftPos + 158, this.topPos + 22, menu.energy),
                new GuiFluidArea(menu.tanks.input(), new Rect2i(this.leftPos + 58, this.topPos + 21, 16, 47), 177, 31, 20, 51, TEXTURE),
                new GuiFluidArea(menu.tanks.output(), new Rect2i(this.leftPos + 112, this.topPos + 21, 16, 47), 177, 31, 20, 51, TEXTURE));
    }
}
