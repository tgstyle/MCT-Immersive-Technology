package mctmods.immersivetechnology.common.blocks.stone.types;

import mctmods.immersivetechnology.common.blocks.BlockITBase;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;
import java.util.Locale;

public enum BlockType_StoneDecoration implements IStringSerializable, BlockITBase.IBlockEnum {
	COKEBRICK_REINFORCED;

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
