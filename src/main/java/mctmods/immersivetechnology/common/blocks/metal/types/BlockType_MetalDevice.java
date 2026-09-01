package mctmods.immersivetechnology.common.blocks.metal.types;

import mctmods.immersivetechnology.common.blocks.BlockITBase;

import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;
import java.util.Locale;

public enum BlockType_MetalDevice implements IStringSerializable, BlockITBase.IBlockEnum {
    ADVANCED_COKE_OVEN_BASEHEATER,
    ROTOR_CREATIVE,
    HEAT_CREATIVE;

    @Override @Nonnull public String getName() { return this.toString().toLowerCase(Locale.ENGLISH); }

    @Override public int getMeta() { return ordinal(); }

    @Override public boolean listForCreative() { return this == ADVANCED_COKE_OVEN_BASEHEATER; }
}
