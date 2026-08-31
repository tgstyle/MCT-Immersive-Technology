package mctmods.immersivetechnology.common.multiblocks.metal.types;

import mctmods.immersivetechnology.common.blocks.BlockITBase;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;
import java.util.Locale;

public enum BlockType_MetalMultiblock2 implements IStringSerializable, BlockITBase.IBlockEnum {

    BOILER_SOLID(true),
    BOILER_SOLID_SLAVE(true);

    private final boolean needsCustomState;
    BlockType_MetalMultiblock2(boolean needsCustomState) {
        this.needsCustomState = needsCustomState;
    }

    @Override public int getMeta() {
        return ordinal();
    }

    @Override public boolean listForCreative() {
        return false;
    }

    @Override @Nonnull public String getName() {
        return this.toString().toLowerCase(Locale.ENGLISH);
    }

    public boolean needsCustomState() {
        return this.needsCustomState;
    }

    public String getCustomState() {
        return getName().toLowerCase();
    }
}
