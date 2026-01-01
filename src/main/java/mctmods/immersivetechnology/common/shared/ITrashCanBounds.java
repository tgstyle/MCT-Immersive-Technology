package mctmods.immersivetechnology.common.shared;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;

import javax.annotation.Nonnull;

public interface ITrashCanBounds extends IEBlockInterfaces.IBlockBounds {
	@Override @Nonnull default float[] getBlockBounds() {
		return new float[]{ .125f, 0, .125f, .875f, 1, .875f};
	}
}
