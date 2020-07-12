package mctmods.immersivetechnology.common.util;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;

public interface ITrashCanBounds extends IEBlockInterfaces.IBlockBounds {

	@Override
	default float[] getBlockBounds() {
		return new float[]{ .125f, 0, .125f, .875f, 1, .875f};
	}

}