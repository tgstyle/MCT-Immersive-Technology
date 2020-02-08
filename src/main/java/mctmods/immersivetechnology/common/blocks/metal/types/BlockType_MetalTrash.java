package mctmods.immersivetechnology.common.blocks.metal.types;

import java.util.Locale;

import mctmods.immersivetechnology.common.blocks.BlockITBase;
import net.minecraft.util.IStringSerializable;

public enum BlockType_MetalTrash implements IStringSerializable, BlockITBase.IBlockEnum {
	TRASH_ITEM,
	TRASH_FLUID,
	TRASH_ENERGY;

	@Override
	public String getName() {
		return this.toString().toLowerCase(Locale.ENGLISH);
	}

	@Override
	public int getMeta() {
		return ordinal();
	}

	@Override
	public boolean listForCreative() {
		return true;
	}
}