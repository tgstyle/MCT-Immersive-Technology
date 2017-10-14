package ferro2000.immersivetech.common;

import javax.annotation.Nonnull;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import ferro2000.immersivetech.ImmersiveTech;
import ferro2000.immersivetech.api.ITLib;
import ferro2000.immersivetech.client.gui.GuiBoiler;
import ferro2000.immersivetech.client.gui.GuiDistiller;
import ferro2000.immersivetech.client.gui.GuiSolarTower;
import ferro2000.immersivetech.client.gui.GuiTimer;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityBoiler;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityDistiller;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntitySolarTower;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityTimer;
import ferro2000.immersivetech.common.gui.ContainerBoiler;
import ferro2000.immersivetech.common.gui.ContainerDistiller;
import ferro2000.immersivetech.common.gui.ContainerSolarTower;
import ferro2000.immersivetech.common.gui.ContainerTimer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler {
	
	public void preInit(){}

	public void preInitEnd(){}
	
	public void init(){}

	public void initEnd(){}
	
	public void postInit(){}

	public void postInitEnd(){}
	
	public static <T extends TileEntity & IGuiTile> void openGuiForTile(@Nonnull EntityPlayer player, @Nonnull T tile)
	{
		player.openGui(ImmersiveTech.instance, tile.getGuiID(), tile.getWorld(), tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
	}
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tile = world.getTileEntity(new BlockPos(x,y,z));
		if(tile instanceof IGuiTile){
			Object gui = null;
			if(ID==ITLib.GUIID_Distiller && tile instanceof TileEntityDistiller)
				gui = new ContainerDistiller(player.inventory, (TileEntityDistiller) tile);
			if(ID==ITLib.GUIID_Solar_Tower && tile instanceof TileEntitySolarTower)
				gui = new ContainerSolarTower(player.inventory, (TileEntitySolarTower) tile);
			if(ID==ITLib.GUIID_Boiler && tile instanceof TileEntityBoiler)
				gui = new ContainerBoiler(player.inventory, (TileEntityBoiler) tile);
			if(ID==ITLib.GUIID_Timer && tile instanceof TileEntityTimer)
				gui = new ContainerTimer(player.inventory, (TileEntityTimer) tile);
			if(gui!=null)
				((IGuiTile)tile).onGuiOpened(player, false);
			return gui;
		}
		return null;
	}
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tile = world.getTileEntity(new BlockPos(x,y,z));
		if(tile instanceof IGuiTile){
			Object gui = null;
			if(ID==ITLib.GUIID_Distiller && tile instanceof TileEntityDistiller)
				gui = new GuiDistiller(player.inventory, (TileEntityDistiller) tile);
			if(ID==ITLib.GUIID_Solar_Tower && tile instanceof TileEntitySolarTower) 
				gui = new GuiSolarTower(player.inventory, (TileEntitySolarTower) tile);
			if(ID==ITLib.GUIID_Boiler && tile instanceof TileEntityBoiler) 
				gui = new GuiBoiler(player.inventory, (TileEntityBoiler) tile);
			if(ID==ITLib.GUIID_Timer && tile instanceof TileEntityTimer)
				gui = new GuiTimer(player.inventory, (TileEntityTimer) tile);
			return gui;
		}
		return null;
	}
	
	public EntityPlayer getClientPlayer()
	{
		return null;
	}
	
	public World getClientWorld()
	{
		return null;
	}

}
