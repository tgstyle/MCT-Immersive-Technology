package mctmods.immersivetechnology.client.gui;

import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.client.gui.helper.ITContainerScreen;
import mctmods.immersivetechnology.client.gui.helper.ITEnergyInfoArea;
import mctmods.immersivetechnology.client.gui.helper.ITFluidInfoArea;
import mctmods.immersivetechnology.client.gui.helper.ITInfoArea;
import mctmods.immersivetechnology.common.multiblocks.gui.DistillerMenu;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;

public class DistillerScreen extends ITContainerScreen<DistillerMenu> {
    private static final ResourceLocation TEXTURE = ITLib.makeTextureLocation("distiller");
    public DistillerScreen(DistillerMenu container, Inventory inventoryPlayer, Component title) { super(container, inventoryPlayer, title, TEXTURE); }

    @Nonnull
    @Override
    protected List<ITInfoArea> makeInfoAreas() {
        return ImmutableList.of(
                new ITEnergyInfoArea(this.leftPos + 158, this.topPos + 22, menu.energy),
                new ITFluidInfoArea(menu.tanks.input(), new Rect2i(this.leftPos + 58, this.topPos + 21, 16, 47), 177, 31, 20, 51, TEXTURE),
                new ITFluidInfoArea(menu.tanks.output(), new Rect2i(this.leftPos + 112, this.topPos + 21, 16, 47), 177, 31, 20, 51, TEXTURE));
    }
}
