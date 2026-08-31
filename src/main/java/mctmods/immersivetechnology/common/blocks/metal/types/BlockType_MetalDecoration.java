package mctmods.immersivetechnology.common.blocks.metal.types;

import mctmods.immersivetechnology.common.blocks.BlockITBase;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;
import java.util.Locale;

public enum BlockType_MetalDecoration implements IStringSerializable, BlockITBase.IBlockEnum {
	TECHNOLOGY_ENGINEERING;

	@Override @Nonnull public String getName() {
		return this.toString().toLowerCase(Locale.ENGLISH);
	}

	@Override public int getMeta() {
		return ordinal();
	}

	@Override public boolean listForCreative() {
		return true;
	}
}
