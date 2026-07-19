package mctmods.immersivetechnology.core.util;

import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.network.chat.Component;

public enum TranslationKey {
    CATEGORY_DISTILLER_CHANCE("category." + ITLib.MODID + ".metal_multiblock.distiller.chance"),
    CATEGORY_SOLAR_MELTER_TEMP("category." + ITLib.MODID + ".metal_multiblock.solar_melter.temp"),
    CATEGORY_SOLAR_MELTER_TIME("category." + ITLib.MODID + ".metal_multiblock.solar_melter.time"),
    CATEGORY_SOLAR_TOWER_TEMP("category." + ITLib.MODID + ".metal_multiblock.solar_tower.temp"),
    CATEGORY_SOLAR_TOWER_TIME("category." + ITLib.MODID + ".metal_multiblock.solar_tower.time"),
    CHAT_RS_CONTROL_INVERTED_OFF("chat.immersiveengineering.info.rsControl.invertedOff"),
    CHAT_RS_CONTROL_INVERTED_ON("chat.immersiveengineering.info.rsControl.invertedOn"),
    CREATIVE_TAB("itemGroup." + ITLib.MODID),
    DESC_HOLD_SHIFT_FOR_INFO("desc." + ITLib.MODID + ".info.holdShiftForInfo"),
    GUI_APPLY("gui." + ITLib.MODID + ".apply"),
    GUI_CRATE_CREATIVE("gui." + ITLib.MODID + ".crate_creative"),
    GUI_DIRECTION_EAST("gui." + ITLib.MODID + ".direction_east"),
    GUI_DIRECTION_NORTH("gui." + ITLib.MODID + ".direction_north"),
    GUI_DIRECTION_SOUTH("gui." + ITLib.MODID + ".direction_south"),
    GUI_DIRECTION_WEST("gui." + ITLib.MODID + ".direction_west"),
    GUI_EMPTY("gui." + ITLib.MODID + ".empty"),
    GUI_ENERGY_STORED("gui." + ITLib.MODID + ".energy_stored"),
    GUI_FLUID_AMOUNT("gui." + ITLib.MODID + ".fluid_amount"),
    GUI_FLUID_CAPACITY("gui." + ITLib.MODID + ".fluid_capacity"),
    GUI_FLUID_DENSITY("gui." + ITLib.MODID + ".fluid_density"),
    GUI_FLUID_NBT("gui." + ITLib.MODID + ".fluid_nbt"),
    GUI_FLUID_REGISTRY("gui." + ITLib.MODID + ".fluid_registry"),
    GUI_FLUID_TEMPERATURE("gui." + ITLib.MODID + ".fluid_temperature"),
    GUI_FLUID_VISCOSITY("gui." + ITLib.MODID + ".fluid_viscosity"),
    GUI_FUEL_EMPTY("gui." + ITLib.MODID + ".fuel_empty"),
    GUI_HEAT_LEVEL_DETAILED("gui." + ITLib.MODID + ".heat_level_detailed"),
    GUI_INPUT_TANK_CLEARED("gui." + ITLib.MODID + ".input_tank_cleared"),
    GUI_INPUT_TANKS_CLEARED("gui." + ITLib.MODID + ".input_tanks_cleared"),
    GUI_ROTOR_CREATIVE("gui." + ITLib.MODID + ".rotor_creative"),
    GUI_ROTOR_CREATIVE_RPM("gui." + ITLib.MODID + ".rotor_creative_rpm"),
    GUI_STATUS("gui." + ITLib.MODID + ".status"),
    GUI_STATUS_ACTIVE("gui." + ITLib.MODID + ".status_active"),
    GUI_STATUS_INACTIVE("gui." + ITLib.MODID + ".status_inactive"),
    GUI_TEMPERATURE("gui." + ITLib.MODID + ".temperature"),
    GUI_TIMER("gui." + ITLib.MODID + ".timer"),
    GUI_VALVE_FIRST_LINE("gui." + ITLib.MODID + ".valve.firstline"),
    GUI_VALVE_FLUID("gui." + ITLib.MODID + ".valve_fluid"),
    GUI_VALVE_FLUID_LIMIT_DESTINATION("gui." + ITLib.MODID + ".valve_fluid.limit_destination"),
    GUI_VALVE_FLUID_LIMIT_PACKET("gui." + ITLib.MODID + ".valve_fluid.limit_packet"),
    GUI_VALVE_FLUID_LIMIT_TIME("gui." + ITLib.MODID + ".valve_fluid.limit_time"),
    GUI_VALVE_LIMITER("gui." + ITLib.MODID + ".valve_limiter"),
    GUI_VALVE_LIMITER_LIMIT_DESTINATION("gui." + ITLib.MODID + ".valve_limiter.limit_destination"),
    GUI_VALVE_LIMITER_LIMIT_PACKET("gui." + ITLib.MODID + ".valve_limiter.limit_packet"),
    GUI_VALVE_LIMITER_LIMIT_TIME("gui." + ITLib.MODID + ".valve_limiter.limit_time"),
    GUI_VALVE_LOAD("gui." + ITLib.MODID + ".valve_load"),
    GUI_VALVE_LOAD_LIMIT_DESTINATION("gui." + ITLib.MODID + ".valve_load.limit_destination"),
    GUI_VALVE_LOAD_LIMIT_PACKET("gui." + ITLib.MODID + ".valve_load.limit_packet"),
    GUI_VALVE_LOAD_LIMIT_TIME("gui." + ITLib.MODID + ".valve_load.limit_time"),
    NO_GAS_ALLOWED("gui." + ITLib.MODID + ".no_gas_allowed"),
    OVERLAY_OSD_BARREL_NORMAL_FIRST_LINE("overlay." + ITLib.MODID + ".osd.barrel.normal.firstline"),
    OVERLAY_OSD_TRASH_ENERGY_NORMAL_ALTERNATIVE("overlay." + ITLib.MODID + ".osd.trash_energy.normal.alternative"),
    OVERLAY_OSD_TRASH_ENERGY_NORMAL_FIRST_LINE("overlay." + ITLib.MODID + ".osd.trash_energy.normal.firstline"),
    OVERLAY_OSD_TRASH_FLUID_NORMAL_ALTERNATIVE("overlay." + ITLib.MODID + ".osd.trash_fluid.normal.alternative"),
    OVERLAY_OSD_TRASH_FLUID_NORMAL_FIRST_LINE("overlay." + ITLib.MODID + ".osd.trash_fluid.normal.firstline"),
    OVERLAY_OSD_TRASH_ITEM_NORMAL_ALTERNATIVE("overlay." + ITLib.MODID + ".osd.trash_item.normal.alternative"),
    OVERLAY_OSD_TRASH_ITEM_NORMAL_FIRST_LINE("overlay." + ITLib.MODID + ".osd.trash_item.normal.firstline"),
    OVERLAY_OSD_VALVE_FLUID_NORMAL_FIRST_LINE("overlay." + ITLib.MODID + ".osd.valve_fluid.normal.firstline"),
    OVERLAY_OSD_VALVE_FLUID_SNEAKING_FIRST_LINE("overlay." + ITLib.MODID + ".osd.valve_fluid.sneaking.firstline"),
    OVERLAY_OSD_VALVE_FLUID_SNEAKING_SECOND_LINE("overlay." + ITLib.MODID + ".osd.valve_fluid.sneaking.secondline"),
    OVERLAY_OSD_VALVE_LIMITER_NORMAL_FIRST_LINE("overlay." + ITLib.MODID + ".osd.valve_limiter.normal.firstline"),
    OVERLAY_OSD_VALVE_LIMITER_SNEAKING_FIRST_LINE("overlay." + ITLib.MODID + ".osd.valve_limiter.sneaking.firstline"),
    OVERLAY_OSD_VALVE_LIMITER_SNEAKING_SECOND_LINE("overlay." + ITLib.MODID + ".osd.valve_limiter.sneaking.secondline"),
    OVERLAY_OSD_VALVE_LOAD_NORMAL_FIRST_LINE("overlay." + ITLib.MODID + ".osd.valve_load.normal.firstline"),
    OVERLAY_OSD_VALVE_LOAD_SNEAKING_FIRST_LINE("overlay." + ITLib.MODID + ".osd.valve_load.sneaking.firstline"),
    OVERLAY_OSD_VALVE_LOAD_SNEAKING_SECOND_LINE("overlay." + ITLib.MODID + ".osd.valve_load.sneaking.secondline"),
    SOLAR_TOO_CLOSE("block." + ITLib.MODID + ".solartower.osd.too_close"),
    SOLAR_VERTICAL_STACK("block." + ITLib.MODID + ".solartower.osd.vertical_stack");

    public final String location;
    TranslationKey(String location) { this.location = location; }

    public String text(boolean addSpaceBefore, boolean addSpaceAfter) { return (addSpaceBefore ? " " : "") + Component.translatable(location).getString() + (addSpaceAfter ? " " : ""); }

    public String text() { return text(false, false); }

    public String format(boolean addSpaceBefore, boolean addSpaceAfter, Object... parameters) { return (addSpaceBefore ? " " : "") + String.format(Component.translatable(location).getString(), parameters) + (addSpaceAfter ? " " : ""); }

    public String format(Object... parameters) { return format(false, false, parameters); }

    public String getLocation() { return location; }
}
