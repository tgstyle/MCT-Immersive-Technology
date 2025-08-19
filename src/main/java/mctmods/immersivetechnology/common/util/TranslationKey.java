package mctmods.immersivetechnology.common.util;

import net.minecraft.network.chat.Component;

public enum TranslationKey {
    BLOCK_TRASH_ITEM_NAME("block.immersivetechnology.metal_trash.trash_item.name"),
    CATEGORY_DISTILLER_CHANCE("category.immersivetechnology.metal_multiblock.distillerChance"),
    GUI_BOILER_HEAT_PER_TICK("gui.immersivetechnology.boiler.heat_per_tick"),
    GUI_BOILER_TOTAL_HEAT("gui.immersivetechnology.boiler.total_heat"),
    GUI_EMPTY("gui.immersivetechnology.empty"),
    GUI_FLUID_VALVE_FIRSTLINE("gui.immersivetechnology.fluid_valve.firstline"),
    GUI_FLUID_VALVE_LIMIT_DESTINATION("gui.immersivetechnology.fluid_valve.limitdestination"),
    GUI_FLUID_VALVE_LIMIT_PACKET("gui.immersivetechnology.fluid_valve.limitpacket"),
    GUI_FLUID_VALVE_LIMIT_TIME("gui.immersivetechnology.fluid_valve.limittime"),
    GUI_GENERIC_MULTIBLOCK_TOOLTIP("gui.immersivetechnology.generic_multiblock_jei_tooltip"),
    GUI_IF_PER_TICK("gui.immersivetechnology.if_per_tick"),
    GUI_LOAD_CONTROLLER_LIMIT_DESTINATION("gui.immersivetechnology.load_controller.limitdestination"),
    GUI_LOAD_CONTROLLER_LIMIT_PACKET("gui.immersivetechnology.load_controller.limitpacket"),
    GUI_LOAD_CONTROLLER_LIMIT_TIME("gui.immersivetechnology.load_controller.limittime"),
    GUI_SECONDS("gui.immersivetechnology.seconds"),
    GUI_STACK_LIMITER_LIMIT_DESTINATION("gui.immersivetechnology.stack_limiter.limitdestination"),
    GUI_STACK_LIMITER_LIMIT_PACKET("gui.immersivetechnology.stack_limiter.limitpacket"),
    GUI_STACK_LIMITER_LIMIT_TIME("gui.immersivetechnology.stack_limiter.limittime"),
    GUI_TICKS("gui.immersivetechnology.ticks"),
    KEYWORD_GENERATE("keyword.immersivetechnology.generate"),
    KEYWORD_HEAT_LEVEL("keyword.immersivetechnology.heat_level"),
    KEYWORD_RPM("keyword.immersivetechnology.rotations_per_minute"),
    KEYWORD_SPEED("keyword.immersivetechnology.speed"),
    NO_GAS_ALLOWED("gui.immersivetechnology.no_gas_allowed"),
    OVERLAY_OSD_BARREL_NORMAL_FIRST_LINE("overlay.immersivetechnology.osd.barrel.normal.firstline"),
    OVERLAY_OSD_CREATIVE_CRATE_NORMAL_FIRST_LINE("overlay.immersivetechnology.osd.creative_crate.normal.firstline"),
    OVERLAY_OSD_FLUID_VALVE_NORMAL_FIRST_LINE("overlay.immersivetechnology.osd.fluid_valve.normal.firstline"),
    OVERLAY_OSD_FLUID_VALVE_SNEAKING_FIRST_LINE("overlay.immersivetechnology.osd.fluid_valve.sneaking.firstline"),
    OVERLAY_OSD_FLUID_VALVE_SNEAKING_SECOND_LINE("overlay.immersivetechnology.osd.fluid_valve.sneaking.secondline"),
    OVERLAY_OSD_LOAD_CONTROLLER_NORMAL_FIRST_LINE("overlay.immersivetechnology.osd.load_controller.normal.firstline"),
    OVERLAY_OSD_LOAD_CONTROLLER_SNEAKING_FIRST_LINE("overlay.immersivetechnology.osd.load_controller.sneaking.firstline"),
    OVERLAY_OSD_LOAD_CONTROLLER_SNEAKING_SECOND_LINE("overlay.immersivetechnology.osd.load_controller.sneaking.secondline"),
    OVERLAY_OSD_STACK_LIMITER_NORMAL_FIRST_LINE("overlay.immersivetechnology.osd.stack_limiter.normal.firstline"),
    OVERLAY_OSD_STACK_LIMITER_SNEAKING_FIRST_LINE("overlay.immersivetechnology.osd.stack_limiter.sneaking.firstline"),
    OVERLAY_OSD_STACK_LIMITER_SNEAKING_SECOND_LINE("overlay.immersivetechnology.osd.stack_limiter.sneaking.secondline"),
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
