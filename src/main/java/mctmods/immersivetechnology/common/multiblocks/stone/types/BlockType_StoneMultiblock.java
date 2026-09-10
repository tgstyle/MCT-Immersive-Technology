package mctmods.immersivetechnology.common.multiblocks.stone.types;

import mctmods.immersivetechnology.common.blocks.BlockITBase;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;
import java.util.Locale;

public enum BlockType_StoneMultiblock implements IStringSerializable, BlockITBase.IBlockEnum {
    ADVANCED_COKE_OVEN,
    ADVANCED_COKE_OVEN_SLAVE,
    COOLING_TOWER,
    COOLING_TOWER_SLAVE;

    @Override public int getMeta() { return ordinal(); }

    @Override public boolean listForCreative() { return false; }

    @Override @Nonnull public String getName() { return this.toString().toLowerCase(Locale.ENGLISH); }
}
