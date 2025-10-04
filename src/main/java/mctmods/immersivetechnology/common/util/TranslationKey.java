package mctmods.immersivetechnology.common.util;

import net.minecraft.network.chat.Component;

public enum TranslationKey {
    BLOCK_TRASH_ITEM_NAME("block.immersivetechnology.metal_trash.trash_item.name"),
    CATEGORY_DISTILLER_CHANCE("category.immersivetechnology.metal_multiblock.distillerChance"),
    GUI_BOILER_HEAT_PER_TICK("gui.immersivetechnology.boiler.heat_per_tick"),
    GUI_BOILER_TOTAL_HEAT("gui.immersivetechnology.boiler.total_heat"),
    GUI_APPLY("gui.immersivetechnology.apply"),    
    GUI_EMPTY("gui.immersivetechnology.empty"),
    GUI_SECONDS("gui.immersivetechnology.seconds"),
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
    GUI_GENERIC_MULTIBLOCK_TOOLTIP("gui.immersivetechnology.generic_multiblock_jei_tooltip"),
    GUI_IF_PER_TICK("gui.immersivetechnology.if_per_tick"),
    KEYWORD_GENERATE("keyword.immersivetechnology.generate"),
    KEYWORD_HEAT_LEVEL("keyword.immersivetechnology.heat_level"),
    KEYWORD_RPM("keyword.immersivetechnology.rotations_per_minute"),
    KEYWORD_SPEED("keyword.immersivetechnology.speed"),
    NO_GAS_ALLOWED("gui.immersivetechnology.no_gas_allowed"),
    OVERLAY_OSD_BARREL_NORMAL_FIRST_LINE("overlay.immersivetechnology.osd.barrel.normal.firstline"),
    OVERLAY_OSD_CREATIVE_CRATE_NORMAL_FIRST_LINE("overlay.immersivetechnology.osd.creative_crate.normal.firstline"),
    OVERLAY_OSD_VALVE_FLUID_NORMAL_FIRST_LINE("overlay.immersivetechnology.osd.valve_fluid.normal.firstline"),
    OVERLAY_OSD_VALVE_FLUID_SNEAKING_FIRST_LINE("overlay.immersivetechnology.osd.valve_fluid.sneaking.firstline"),
    OVERLAY_OSD_VALVE_FLUID_SNEAKING_SECOND_LINE("overlay.immersivetechnology.osd.valve_fluid.sneaking.secondline"),
    OVERLAY_OSD_VALVE_LOAD_NORMAL_FIRST_LINE("overlay.immersivetechnology.osd.valve_load.normal.firstline"),
    OVERLAY_OSD_VALVE_LOAD_SNEAKING_FIRST_LINE("overlay.immersivetechnology.osd.valve_load.sneaking.firstline"),
    OVERLAY_OSD_VALVE_LOAD_SNEAKING_SECOND_LINE("overlay.immersivetechnology.osd.valve_load.sneaking.secondline"),
    OVERLAY_OSD_VALVE_LIMITER_NORMAL_FIRST_LINE("overlay.immersivetechnology.osd.valve_limiter.normal.firstline"),
    OVERLAY_OSD_VALVE_LIMITER_SNEAKING_FIRST_LINE("overlay.immersivetechnology.osd.valve_limiter.sneaking.firstline"),
    OVERLAY_OSD_VALVE_LIMITER_SNEAKING_SECOND_LINE("overlay.immersivetechnology.osd.valve_limiter.sneaking.secondline"),
    OVERLAY_OSD_TRASH_ENERGY_NORMAL_ALTERNATIVE("overlay.immersivetechnology.osd.trash_energy.normal.alternative"),
    OVERLAY_OSD_TRASH_ENERGY_NORMAL_FIRST_LINE("overlay.immersivetechnology.osd.trash_energy.normal.firstline"),
    OVERLAY_OSD_TRASH_FLUID_NORMAL_ALTERNATIVE("overlay.immersivetechnology.osd.trash_fluid.normal.alternative"),
    OVERLAY_OSD_TRASH_FLUID_NORMAL_FIRST_LINE("overlay.immersivetechnology.osd.trash_fluid.normal.firstline"),
    OVERLAY_OSD_TRASH_ITEM_NORMAL_ALTERNATIVE("overlay.immersivetechnology.osd.trash_item.normal.alternative"),
    OVERLAY_REDSTONE_INVERTED("overlay.immersivetechnology.redstone_inverted"),
    OVERLAY_REDSTONE_NORMAL("overlay.immersivetechnology.redstone_normal"),
    OVERLAY_REDSTONE_OFF("overlay.immersivetechnology.redstone_off"),
    OVERLAY_STEEL_TANK_NORMAL_FIRST_LINE("overlay.immersivetechnology.multiblock.steel_tank.normal.firstline"),
    SOLAR_TOO_CLOSE("block.immersivetechnology.solartower.osd.too_close"),
    SOLAR_VERTICAL_STACK("block.immersivetechnology.solartower.osd.vertical_stack"),
    OVERLAY_OSD_TRASH_ITEM_NORMAL_FIRST_LINE("overlay.immersivetechnology.osd.trash_item.normal.firstline");

    public final String location;
    TranslationKey(String location) { this.location = location; }

    public String text(boolean addSpaceBefore, boolean addSpaceAfter) { return (addSpaceBefore ? " " : "") + Component.translatable(location).getString() + (addSpaceAfter ? " " : ""); }

    public String text() { return text(false, false); }

    public String format(boolean addSpaceBefore, boolean addSpaceAfter, Object... parameters) { return (addSpaceBefore ? " " : "") + String.format(Component.translatable(location).getString(), parameters) + (addSpaceAfter ? " " : ""); }

    public String format(Object... parameters) { return format(false, false, parameters); }

    public String getLocation() { return location; }
}
