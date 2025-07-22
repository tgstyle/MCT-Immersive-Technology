package mctmods.immersivetechnology.client.menu.multiblock;

import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.common.blocks.multiblocks.gui.BoilerMenu;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;

public class BoilerScreen extends IEContainerScreen<BoilerMenu>
{
    private static final ResourceLocation TEXTURE = ITLib.makeTextureLocation("gui_boiler");

    public BoilerScreen(BoilerMenu container, Inventory inventoryPlayer, Component title)
    {
        super(container, inventoryPlayer, title, TEXTURE);
    }

    @Nonnull
    @Override
    protected List<InfoArea> makeInfoAreas()
    {
        return ImmutableList.of(
                new FluidInfoArea(menu.tanks.fuelInput(), new Rect2i(leftPos+13, topPos+20, 16, 47), 177, 31, 20, 51, TEXTURE),
                new FluidInfoArea(menu.tanks.waterInput(), new Rect2i(leftPos+100, topPos+20, 16, 47), 177, 31, 20, 51, TEXTURE),
                new FluidInfoArea(menu.tanks.output(), new Rect2i(leftPos+125, topPos+20, 16, 47), 177, 31, 20, 51, TEXTURE)
        );
    }
}
