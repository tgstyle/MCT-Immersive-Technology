package mctmods.immersivetechnology.common.multiblocks.metal.types;

import mctmods.immersivetechnology.common.blocks.BlockITBase;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;
import java.util.Locale;

public enum BlockType_MetalMultiblock implements IStringSerializable, BlockITBase.IBlockEnum {
    DISTILLER(true),
    SOLAR_TOWER(true),
    SOLAR_REFLECTOR(true),
    STEAM_TURBINE(true),
    BOILER(true),
    ALTERNATOR(true),
    DISTILLER_SLAVE(true),
    SOLAR_TOWER_SLAVE(true),
    STEAM_TURBINE_SLAVE(true),
    BOILER_SLAVE(true),
    ALTERNATOR_SLAVE(true),
    SOLAR_REFLECTOR_SLAVE(true),
    STEEL_TANK(true),
    STEEL_TANK_SLAVE(true),
    COOLING_TOWER(true),
    COOLING_TOWER_SLAVE(true);

    private final boolean needsCustomState;

    BlockType_MetalMultiblock(boolean needsCustomState) { this.needsCustomState = needsCustomState; }

    @Override public int getMeta() { return ordinal(); }

    @Override public boolean listForCreative() { return false; }

    @Override @Nonnull public String getName() { return this.toString().toLowerCase(Locale.ENGLISH); }

    public boolean needsCustomState() { return this.needsCustomState; }

    public String getCustomState() { return getName().toLowerCase(); }
}
