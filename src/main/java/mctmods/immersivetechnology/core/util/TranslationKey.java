package mctmods.immersivetechnology.core.util;

import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.network.chat.Component;

public enum TranslationKey {
    CATEGORY_DISTILLER_CHANCE("category." + Reference.MODID + ".metal_multiblock.distiller.chance"),
    CATEGORY_SOLAR_MELTER_TEMP("category." + Reference.MODID + ".metal_multiblock.solar_melter.temp"),
    CATEGORY_SOLAR_MELTER_TIME("category." + Reference.MODID + ".metal_multiblock.solar_melter.time"),
    CATEGORY_SOLAR_TOWER_TEMP("category." + Reference.MODID + ".metal_multiblock.solar_tower.temp"),
    CATEGORY_SOLAR_TOWER_TIME("category." + Reference.MODID + ".metal_multiblock.solar_tower.time"),
    CHAT_RS_CONTROL_INVERTED_OFF("chat.immersiveengineering.info.rsControl.invertedOff"),
    CHAT_RS_CONTROL_INVERTED_ON("chat.immersiveengineering.info.rsControl.invertedOn"),
    CREATIVE_TAB("itemGroup." + Reference.MODID),
    GUI_APPLY("gui." + Reference.MODID + ".apply"),
    GUI_CRATE_CREATIVE("gui." + Reference.MODID + ".crate_creative"),
    GUI_DIRECTION_EAST("gui." + Reference.MODID + ".direction_east"),
    GUI_DIRECTION_NORTH("gui." + Reference.MODID + ".direction_north"),
    GUI_DIRECTION_SOUTH("gui." + Reference.MODID + ".direction_south"),
    GUI_DIRECTION_WEST("gui." + Reference.MODID + ".direction_west"),
    GUI_EMPTY("gui." + Reference.MODID + ".empty"),
    GUI_HEAT_LEVEL_DETAILED("gui." + Reference.MODID + ".heat_level_detailed"),
    GUI_TEMPERATURE("gui." + Reference.MODID + ".temperature"),
    GUI_TIMER("gui." + Reference.MODID + ".timer"),
    GUI_VALVE_FIRST_LINE("gui." + Reference.MODID + ".valve.firstline"),
    GUI_VALVE_FLUID("gui." + Reference.MODID + ".valve_fluid"),
    GUI_VALVE_FLUID_LIMIT_DESTINATION("gui." + Reference.MODID + ".valve_fluid.limit_destination"),
    GUI_VALVE_FLUID_LIMIT_PACKET("gui." + Reference.MODID + ".valve_fluid.limit_packet"),
    GUI_VALVE_FLUID_LIMIT_TIME("gui." + Reference.MODID + ".valve_fluid.limit_time"),
    GUI_VALVE_LIMITER("gui." + Reference.MODID + ".valve_limiter"),
    GUI_VALVE_LIMITER_LIMIT_DESTINATION("gui." + Reference.MODID + ".valve_limiter.limit_destination"),
    GUI_VALVE_LIMITER_LIMIT_PACKET("gui." + Reference.MODID + ".valve_limiter.limit_packet"),
    GUI_VALVE_LIMITER_LIMIT_TIME("gui." + Reference.MODID + ".valve_limiter.limit_time"),
    GUI_VALVE_LOAD("gui." + Reference.MODID + ".valve_load"),
    GUI_VALVE_LOAD_LIMIT_DESTINATION("gui." + Reference.MODID + ".valve_load.limit_destination"),
    GUI_VALVE_LOAD_LIMIT_PACKET("gui." + Reference.MODID + ".valve_load.limit_packet"),
    GUI_VALVE_LOAD_LIMIT_TIME("gui." + Reference.MODID + ".valve_load.limit_time"),
    NO_GAS_ALLOWED("gui." + Reference.MODID + ".no_gas_allowed"),
    OVERLAY_OSD_BARREL_NORMAL_FIRST_LINE("overlay." + Reference.MODID + ".osd.barrel.normal.firstline"),
    OVERLAY_OSD_CREATIVE_CRATE_NORMAL_FIRST_LINE("overlay." + Reference.MODID + ".osd.creative_crate.normal.firstline"),
    OVERLAY_OSD_TRASH_ENERGY_NORMAL_ALTERNATIVE("overlay." + Reference.MODID + ".osd.trash_energy.normal.alternative"),
    OVERLAY_OSD_TRASH_ENERGY_NORMAL_FIRST_LINE("overlay." + Reference.MODID + ".osd.trash_energy.normal.firstline"),
    OVERLAY_OSD_TRASH_FLUID_NORMAL_ALTERNATIVE("overlay." + Reference.MODID + ".osd.trash_fluid.normal.alternative"),
    OVERLAY_OSD_TRASH_FLUID_NORMAL_FIRST_LINE("overlay." + Reference.MODID + ".osd.trash_fluid.normal.firstline"),
    OVERLAY_OSD_TRASH_ITEM_NORMAL_ALTERNATIVE("overlay." + Reference.MODID + ".osd.trash_item.normal.alternative"),
    OVERLAY_OSD_TRASH_ITEM_NORMAL_FIRST_LINE("overlay." + Reference.MODID + ".osd.trash_item.normal.firstline"),
    OVERLAY_OSD_VALVE_FLUID_NORMAL_FIRST_LINE("overlay." + Reference.MODID + ".osd.valve_fluid.normal.firstline"),
    OVERLAY_OSD_VALVE_FLUID_SNEAKING_FIRST_LINE("overlay." + Reference.MODID + ".osd.valve_fluid.sneaking.firstline"),
    OVERLAY_OSD_VALVE_FLUID_SNEAKING_SECOND_LINE("overlay." + Reference.MODID + ".osd.valve_fluid.sneaking.secondline"),
    OVERLAY_OSD_VALVE_LIMITER_NORMAL_FIRST_LINE("overlay." + Reference.MODID + ".osd.valve_limiter.normal.firstline"),
    OVERLAY_OSD_VALVE_LIMITER_SNEAKING_FIRST_LINE("overlay." + Reference.MODID + ".osd.valve_limiter.sneaking.firstline"),
    OVERLAY_OSD_VALVE_LIMITER_SNEAKING_SECOND_LINE("overlay." + Reference.MODID + ".osd.valve_limiter.sneaking.secondline"),
    OVERLAY_OSD_VALVE_LOAD_NORMAL_FIRST_LINE("overlay." + Reference.MODID + ".osd.valve_load.normal.firstline"),
    OVERLAY_OSD_VALVE_LOAD_SNEAKING_FIRST_LINE("overlay." + Reference.MODID + ".osd.valve_load.sneaking.firstline"),
    OVERLAY_OSD_VALVE_LOAD_SNEAKING_SECOND_LINE("overlay." + Reference.MODID + ".osd.valve_load.sneaking.secondline"),
    SOLAR_TOO_CLOSE("block." + Reference.MODID + ".solartower.osd.too_close"),
    SOLAR_VERTICAL_STACK("block." + Reference.MODID + ".solartower.osd.vertical_stack");

    public final String location;
    TranslationKey(String location) { this.location = location; }

    public String text(boolean addSpaceBefore, boolean addSpaceAfter) { return (addSpaceBefore ? " " : "") + Component.translatable(location).getString() + (addSpaceAfter ? " " : ""); }

    public String text() { return text(false, false); }

    public String format(boolean addSpaceBefore, boolean addSpaceAfter, Object... parameters) { return (addSpaceBefore ? " " : "") + String.format(Component.translatable(location).getString(), parameters) + (addSpaceAfter ? " " : ""); }

    public String format(Object... parameters) { return format(false, false, parameters); }

    public String getLocation() { return location; }
}
