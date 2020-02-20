package mctmods.immersivetechnology.common.blocks.stone.types;

import mctmods.immersivetechnology.common.blocks.BlockITBase;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum BlockType_StoneMultiblock implements IStringSerializable, BlockITBase.IBlockEnum {
	COKE_OVEN_ADVANCED, COKE_OVEN_ADVANCED_SLAVE;

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
		return false;
	}

}