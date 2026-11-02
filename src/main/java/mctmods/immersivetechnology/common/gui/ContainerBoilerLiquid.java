package mctmods.immersivetechnology.common.gui;

import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.boilerliquid.TileEntityBoilerLiquidMaster;
import mctmods.immersivetechnology.common.util.ITContainerHelper;

import com.immersiveconvergence.api.gui.ICContainerBase;
import com.immersiveconvergence.api.gui.ICSlot;
import javax.annotation.Nonnull;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerBoilerLiquid extends ICContainerBase<TileEntityBoilerLiquidMaster> {
	public ContainerBoilerLiquid(InventoryPlayer inventoryPlayer, TileEntityBoilerLiquidMaster tile) {
		super(inventoryPlayer, tile);

		slotCount = TileEntityBoilerLiquidMaster.slotCount;
		final TileEntityBoilerLiquidMaster tileF = tile;
		this.addSlotToContainer(new ICSlot.FluidContainer(this, this.inv, 0, 26, 17, 2) {
			@Override public boolean isItemValid(@Nonnull ItemStack itemStack) { return ITContainerHelper.acceptsMatchingFluid(itemStack, tileF.tanks[0]); }
		});
		this.addSlotToContainer(new ICSlot.Output(this, this.inv, 1, 26, 53));

		for (Slot slot : ITContainerHelper.playerInventorySlots(inventoryPlayer, 85)) { addSlotToContainer(slot); }
	}
}
