package mctmods.immersivetechnology.common.shared.tileentities;

import com.immersiveconvergence.api.block.ICTileEntityBase;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

public class TileEntityITSlab extends ICTileEntityBase {
	public int slabType = 0;

	@Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
		slabType = nbt.getInteger("slabType");
		if (descPacket && world != null) this.markContainingBlockForUpdate(null);
	}

	@Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
		nbt.setInteger("slabType", slabType);
	}
}
