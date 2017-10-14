package ferro2000.immersivetech.common.gui;

import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityTimer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public class ContainerTimer extends Container {

	TileEntityTimer tile;
	
	public ContainerTimer(InventoryPlayer inventoryPlayer, TileEntityTimer tile)
	{
		this.tile=tile;
		
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
		for (int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 143));
		
	}
	
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return tile!=null && tile.getWorld().getTileEntity(tile.getPos())==tile && player.getDistanceSq(tile.getPos().getX()+.5, tile.getPos().getY()+.5, tile.getPos().getZ()+.5)<=64;
	}

}
