package mctmods.immersivetechnology.common.multiblocks.stone.types;

import mctmods.immersivetechnology.common.blocks.BlockITBase;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;
import java.util.Locale;

public enum BlockType_StoneMultiblock implements IStringSerializable, BlockITBase.IBlockEnum {
    ADVANCED_COKE_OVEN(true),
    ADVANCED_COKE_OVEN_SLAVE(true);

    private final boolean needsCustomState;

    BlockType_StoneMultiblock(boolean needsCustomState) { this.needsCustomState = needsCustomState; }

    @Override public int getMeta() { return ordinal(); }

    @Override public boolean listForCreative() { return false; }

    @Nonnull @Override public String getName() { return this.toString().toLowerCase(Locale.ENGLISH); }

    public boolean needsCustomState() { return this.needsCustomState; }

    public String getCustomState() { return getName().toLowerCase(); }
}
