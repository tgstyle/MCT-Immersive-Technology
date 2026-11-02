package mctmods.immersivetechnology.common.gui;

import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.boilersolid.TileEntityBoilerSolidMaster;
import mctmods.immersivetechnology.common.util.ITContainerHelper;

import com.immersiveconvergence.api.gui.ICContainerBase;
import com.immersiveconvergence.api.gui.ICSlot;
import javax.annotation.Nonnull;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerBoilerSolid extends ICContainerBase<TileEntityBoilerSolidMaster> {
	public ContainerBoilerSolid(InventoryPlayer inventoryPlayer, TileEntityBoilerSolidMaster tile) {
		super(inventoryPlayer, tile);

		slotCount = TileEntityBoilerSolidMaster.slotCount;
		final TileEntityBoilerSolidMaster tileF = tile;
		this.addSlotToContainer(new ICSlot(this, this.inv, 0, 44, 34) {
			@Override public boolean isItemValid(@Nonnull ItemStack itemStack) { return tileF.isStackValid(0, itemStack); }
		});

		for (Slot slot : ITContainerHelper.playerInventorySlots(inventoryPlayer, 85)) { addSlotToContainer(slot); }
	}
}
