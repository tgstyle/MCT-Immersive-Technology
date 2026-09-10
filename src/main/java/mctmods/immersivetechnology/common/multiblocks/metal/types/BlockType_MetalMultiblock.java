package mctmods.immersivetechnology.common.multiblocks.metal.types;

import mctmods.immersivetechnology.common.blocks.BlockITBase;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;
import java.util.Locale;

public enum BlockType_MetalMultiblock implements IStringSerializable, BlockITBase.IBlockEnum {
    DISTILLER,
    SOLAR_TOWER,
    SOLAR_REFLECTOR,
    STEAM_TURBINE,
    BOILER_TANK,
    ALTERNATOR,
    DISTILLER_SLAVE,
    SOLAR_TOWER_SLAVE,
    STEAM_TURBINE_SLAVE,
    BOILER_TANK_SLAVE,
    ALTERNATOR_SLAVE,
    SOLAR_REFLECTOR_SLAVE,
    STEEL_TANK,
    STEEL_TANK_SLAVE,
    COOLING_TOWER,
    COOLING_TOWER_SLAVE;

    @Override public int getMeta() { return ordinal(); }

    @Override public boolean listForCreative() { return false; }

    @Override @Nonnull public String getName() { return this.toString().toLowerCase(Locale.ENGLISH); }
}
