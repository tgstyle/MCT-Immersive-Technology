package mctmods.immersivetechnology.client.menu.multiblock;

import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import blusunrize.immersiveengineering.client.gui.MixerScreen;
import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.common.blocks.multiblocks.gui.DistillerMenu;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import javax.annotation.Nonnull;
import java.util.List;

public class DistillerScreen extends IEContainerScreen<DistillerMenu> {
    private static final ResourceLocation TEXTURE = ITLib.makeTextureLocation("distiller");
    public DistillerScreen(DistillerMenu container, Inventory inventoryPlayer, Component title) { super(container, inventoryPlayer, title, TEXTURE); }

    @Nonnull
    @Override
    protected List<InfoArea> makeInfoAreas() {

        return ImmutableList.of(
            new EnergyInfoArea(this.leftPos + 158, this.topPos + 22, menu.energy),
            new FluidInfoArea(menu.tanks.waterInput(),
            new Rect2i(leftPos + 58, topPos + 21, 16, 47), 177, 31, 20, 51, TEXTURE),
            new FluidInfoArea(menu.tanks.output(),
            new Rect2i(leftPos + 112, topPos + 21, 16, 47), 177, 31, 20, 51, TEXTURE));
    }
}
