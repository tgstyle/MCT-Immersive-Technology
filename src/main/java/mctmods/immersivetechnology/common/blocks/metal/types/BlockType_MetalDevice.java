package mctmods.immersivetechnology.common.blocks.metal.types;

import java.util.Locale;

import mctmods.immersivetechnology.common.blocks.BlockITBase;

import net.minecraft.util.IStringSerializable;

public enum BlockType_MetalDevice implements IStringSerializable, BlockITBase.IBlockEnum {
	COKE_OVEN_PREHEATER;

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