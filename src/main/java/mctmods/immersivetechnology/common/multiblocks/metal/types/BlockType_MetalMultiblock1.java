package mctmods.immersivetechnology.common.multiblocks.metal.types;

import mctmods.immersivetechnology.common.blocks.BlockITBase;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;
import java.util.Locale;

public enum BlockType_MetalMultiblock1 implements IStringSerializable, BlockITBase.IBlockEnum {
    GAS_TURBINE,
    GAS_TURBINE_SLAVE,
    HEAT_EXCHANGER,
    HEAT_EXCHANGER_SLAVE,
    HIGH_PRESSURE_STEAM_TURBINE,
    HIGH_PRESSURE_STEAM_TURBINE_SLAVE,
    ELECTROLYTIC_CRUCIBLE_BATTERY,
    ELECTROLYTIC_CRUCIBLE_BATTERY_SLAVE,
    MELTING_CRUCIBLE,
    MELTING_CRUCIBLE_SLAVE,
    RADIATOR,
    RADIATOR_SLAVE,
    SOLAR_MELTER,
    SOLAR_MELTER_SLAVE,
    BOILER_LIQUID,
    BOILER_LIQUID_SLAVE;

    @Override public int getMeta() { return ordinal(); }

    @Override public boolean listForCreative() { return false; }

    @Override @Nonnull public String getName() { return this.toString().toLowerCase(Locale.ENGLISH); }
}
