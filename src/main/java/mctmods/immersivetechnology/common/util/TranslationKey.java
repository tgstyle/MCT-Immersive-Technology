package mctmods.immersivetechnology.common.util;

import net.minecraft.network.chat.Component;

public enum TranslationKey {
    CATEGORY_BOILER_LIQUID_HEAT("category.immersivetechnology.metal_multiblock.boiler_liquid.heat"),
    CATEGORY_BOILER_LIQUID_TIME("category.immersivetechnology.metal_multiblock.boiler_liquid.time"),
    CATEGORY_BOILER_SOLID_HEAT("category.immersivetechnology.metal_multiblock.boiler_solid.heat"),
    CATEGORY_BOILER_SOLID_TIME("category.immersivetechnology.metal_multiblock.boiler_solid.time"),
    CATEGORY_DISTILLER_CHANCE("category.immersivetechnology.metal_multiblock.distiller.chance"),
    CATEGORY_SOLAR_MELTER_TEMP("category.immersivetechnology.metal_multiblock.solar_melter.temp"),
    CATEGORY_SOLAR_MELTER_TIME("category.immersivetechnology.metal_multiblock.solar_melter.time"),
    CATEGORY_SOLAR_TOWER_TEMP("category.immersivetechnology.metal_multiblock.solar_tower.temp"),
    CATEGORY_SOLAR_TOWER_TIME("category.immersivetechnology.metal_multiblock.solar_tower.time"),
    CHAT_RS_CONTROL_INVERTED_OFF("chat.immersiveengineering.info.rsControl.invertedOff"),
    CHAT_RS_CONTROL_INVERTED_ON("chat.immersiveengineering.info.rsControl.invertedOn"),
    CREATIVE_TAB("itemGroup.immersivetechnology"),
    DESC_HOLD_SHIFT_FOR_INFO("desc.immersivetechnology.info.holdShiftForInfo"),
    GUI_APPLY("gui.immersivetechnology.apply"),
    GUI_BOILER_HEAT_PER_TICK("gui.immersivetechnology.boiler.heat_per_tick"),
    GUI_BOILER_TOTAL_HEAT("gui.immersivetechnology.boiler.total_heat"),
    GUI_DIRECTION_EAST("gui.immersivetechnology.direction_east"),
    GUI_DIRECTION_NORTH("gui.immersivetechnology.direction_north"),
    GUI_DIRECTION_SOUTH("gui.immersivetechnology.direction_south"),
    GUI_DIRECTION_WEST("gui.immersivetechnology.direction_west"),
    GUI_EMPTY("gui.immersivetechnology.empty"),
    GUI_ENERGY_STORED("gui.immersivetechnology.energy_stored"),
    GUI_FLUID_AMOUNT("gui.immersivetechnology.fluid_amount"),
    GUI_FLUID_CAPACITY("gui.immersivetechnology.fluid_capacity"),
    GUI_FLUID_DENSITY("gui.immersivetechnology.fluid_density"),
    GUI_FLUID_NBT("gui.immersivetechnology.fluid_nbt"),
    GUI_FLUID_REGISTRY("gui.immersivetechnology.fluid_registry"),
    GUI_FLUID_TEMPERATURE("gui.immersivetechnology.fluid_temperature"),
    GUI_FLUID_VISCOSITY("gui.immersivetechnology.fluid_viscosity"),
    GUI_FUEL_EMPTY("gui.immersivetechnology.fuel_empty"),
    GUI_GENERIC_MULTIBLOCK_TOOLTIP("gui.immersivetechnology.generic_multiblock_jei_tooltip"),
    GUI_HEAT_LEVEL("gui.immersivetechnology.heat_level"),
    GUI_HEAT_LEVEL_DETAILED("gui.immersivetechnology.heat_level_detailed"),
    GUI_IF_PER_TICK("gui.immersivetechnology.if_per_tick"),
    GUI_INPUT_TANK_CLEARED("gui.immersivetechnology.input_tank_cleared"),
    GUI_INPUT_TANKS_CLEARED("gui.immersivetechnology.input_tanks_cleared"),
    GUI_ROTOR_CREATIVE("gui.immersivetechnology.rotor_creative"),
    GUI_ROTOR_CREATIVE_RPM("gui.immersivetechnology.rotor_creative_rpm"),
    GUI_SECONDS("gui.immersivetechnology.seconds"),
    GUI_STATUS("gui.immersivetechnology.status"),
    GUI_STATUS_ACTIVE("gui.immersivetechnology.status_active"),
    GUI_STATUS_INACTIVE("gui.immersivetechnology.status_inactive"),
    GUI_TEMPERATURE("gui.immersivetechnology.temperature"),
    GUI_TICKS("gui.immersivetechnology.ticks"),
    GUI_VALVE_FIRST_LINE("gui.immersivetechnology.valve.firstline"),
    GUI_VALVE_FLUID("gui.immersivetechnology.valve_fluid"),
    GUI_VALVE_FLUID_LIMIT_DESTINATION("gui.immersivetechnology.valve_fluid.limit_destination"),
    GUI_VALVE_FLUID_LIMIT_PACKET("gui.immersivetechnology.valve_fluid.limit_packet"),
    GUI_VALVE_FLUID_LIMIT_TIME("gui.immersivetechnology.valve_fluid.limit_time"),
    GUI_VALVE_LIMITER("gui.immersivetechnology.valve_limiter"),
    GUI_VALVE_LIMITER_LIMIT_DESTINATION("gui.immersivetechnology.valve_limiter.limit_destination"),
    GUI_VALVE_LIMITER_LIMIT_PACKET("gui.immersivetechnology.valve_limiter.limit_packet"),
    GUI_VALVE_LIMITER_LIMIT_TIME("gui.immersivetechnology.valve_limiter.limit_time"),
    GUI_VALVE_LOAD("gui.immersivetechnology.valve_load"),
    GUI_VALVE_LOAD_LIMIT_DESTINATION("gui.immersivetechnology.valve_load.limit_destination"),
    GUI_VALVE_LOAD_LIMIT_PACKET("gui.immersivetechnology.valve_load.limit_packet"),
    GUI_VALVE_LOAD_LIMIT_TIME("gui.immersivetechnology.valve_load.limit_time"),
    NO_GAS_ALLOWED("gui.immersivetechnology.no_gas_allowed"),
    OVERLAY_OSD_BARREL_NORMAL_FIRST_LINE("overlay.immersivetechnology.osd.barrel.normal.firstline"),
    OVERLAY_OSD_TRASH_ENERGY_NORMAL_ALTERNATIVE("overlay.immersivetechnology.osd.trash_energy.normal.alternative"),
    OVERLAY_OSD_TRASH_ENERGY_NORMAL_FIRST_LINE("overlay.immersivetechnology.osd.trash_energy.normal.firstline"),
    OVERLAY_OSD_TRASH_FLUID_NORMAL_ALTERNATIVE("overlay.immersivetechnology.osd.trash_fluid.normal.alternative"),
    OVERLAY_OSD_TRASH_FLUID_NORMAL_FIRST_LINE("overlay.immersivetechnology.osd.trash_fluid.normal.firstline"),
    OVERLAY_OSD_TRASH_ITEM_NORMAL_ALTERNATIVE("overlay.immersivetechnology.osd.trash_item.normal.alternative"),
    OVERLAY_OSD_TRASH_ITEM_NORMAL_FIRST_LINE("overlay.immersivetechnology.osd.trash_item.normal.firstline"),
    OVERLAY_OSD_VALVE_FLUID_NORMAL_FIRST_LINE("overlay.immersivetechnology.osd.valve_fluid.normal.firstline"),
    OVERLAY_OSD_VALVE_FLUID_SNEAKING_FIRST_LINE("overlay.immersivetechnology.osd.valve_fluid.sneaking.firstline"),
    OVERLAY_OSD_VALVE_FLUID_SNEAKING_SECOND_LINE("overlay.immersivetechnology.osd.valve_fluid.sneaking.secondline"),
    OVERLAY_OSD_VALVE_LIMITER_NORMAL_FIRST_LINE("overlay.immersivetechnology.osd.valve_limiter.normal.firstline"),
    OVERLAY_OSD_VALVE_LIMITER_SNEAKING_FIRST_LINE("overlay.immersivetechnology.osd.valve_limiter.sneaking.firstline"),
    OVERLAY_OSD_VALVE_LIMITER_SNEAKING_SECOND_LINE("overlay.immersivetechnology.osd.valve_limiter.sneaking.secondline"),
    OVERLAY_OSD_VALVE_LOAD_NORMAL_FIRST_LINE("overlay.immersivetechnology.osd.valve_load.normal.firstline"),
    OVERLAY_OSD_VALVE_LOAD_SNEAKING_FIRST_LINE("overlay.immersivetechnology.osd.valve_load.sneaking.firstline"),
    OVERLAY_OSD_VALVE_LOAD_SNEAKING_SECOND_LINE("overlay.immersivetechnology.osd.valve_load.sneaking.secondline"),
    SOLAR_TOO_CLOSE("block.immersivetechnology.solartower.osd.too_close"),
    SOLAR_VERTICAL_STACK("block.immersivetechnology.solartower.osd.vertical_stack");

    public final String location;
    TranslationKey(String location) { this.location = location; }

    public String text(boolean addSpaceBefore, boolean addSpaceAfter) { return (addSpaceBefore ? " " : "") + Component.translatable(location).getString() + (addSpaceAfter ? " " : ""); }

    public String text() { return text(false, false); }

    public String format(boolean addSpaceBefore, boolean addSpaceAfter, Object... parameters) { return (addSpaceBefore ? " " : "") + String.format(Component.translatable(location).getString(), parameters) + (addSpaceAfter ? " " : ""); }

    public String format(Object... parameters) { return format(false, false, parameters); }

    public String getLocation() { return location; }
}
