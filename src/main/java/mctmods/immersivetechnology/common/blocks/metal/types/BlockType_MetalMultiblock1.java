package mctmods.immersivetechnology.common.blocks.metal.types;

import mctmods.immersivetechnology.common.blocks.BlockITBase;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum BlockType_MetalMultiblock1 implements IStringSerializable, BlockITBase.IBlockEnum {

    GAS_TURBINE(true),
    GAS_TURBINE_SLAVE(true),
    HEAT_EXCHANGER(true),
    HEAT_EXCHANGER_SLAVE(true),
    HIGH_PRESSURE_STEAM_TURBINE(true),
    HIGH_PRESSURE_STEAM_TURBINE_SLAVE(true),
    ELECTROLYTIC_CRUCIBLE_BATTERY(true),
    ELECTROLYTIC_CRUCIBLE_BATTERY_SLAVE(true),
    MELTING_CRUCIBLE(true),
    MELTING_CRUCIBLE_SLAVE(true),
    RADIATOR(true),
    RADIATOR_SLAVE(true);

    private boolean needsCustomState;
    BlockType_MetalMultiblock1(boolean needsCustomState) {
        this.needsCustomState = needsCustomState;
    }

    @Override
    public int getMeta() {
        return ordinal();
    }

    @Override
    public boolean listForCreative() {
        return false;
    }

    @Override
    public String getName() {
        return this.toString().toLowerCase(Locale.ENGLISH);
    }

    public boolean needsCustomState() {
        return this.needsCustomState;
    }
    public String getCustomState() {
        return getName().toLowerCase();
    }

}