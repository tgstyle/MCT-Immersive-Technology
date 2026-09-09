package mctmods.immersivetechnology.common.shared;

import com.immersiveconvergence.api.multiblock.ICBlockInterfaces;

import javax.annotation.Nonnull;

public interface ITrashCanBounds extends ICBlockInterfaces.IBlockBounds {
	@Override @Nonnull default float[] getBlockBounds() {
		return new float[]{ .125f, 0, .125f, .875f, 1, .875f};
	}
}
