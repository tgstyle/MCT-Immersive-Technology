package mctmods.immersivetechnology.common.util;
import net.minecraft.client.resources.I18n;

public enum TranslationKey {

    OVERLAY_OSD_TRASH_ITEM_NORMAL_FIRST_LINE("overlay.immersivetech.osd.trash_item.normal.firstline"),
    OVERLAY_OSD_TRASH_FLUID_NORMAL_FIRST_LINE("overlay.immersivetech.osd.trash_fluid.normal.firstline"),
    OVERLAY_OSD_TRASH_ENERGY_NORMAL_FIRST_LINE("overlay.immersivetech.osd.trash_energy.normal.firstline"),
    OVERLAY_OSD_TRASH_ITEM_NORMAL_ALTERNATIVE("overlay.immersivetech.osd.trash_item.normal.alternative"),
    OVERLAY_OSD_TRASH_FLUID_NORMAL_ALTERNATIVE("overlay.immersivetech.osd.trash_fluid.normal.alternative"),
    OVERLAY_OSD_TRASH_ENERGY_NORMAL_ALTERNATIVE("overlay.immersivetech.osd.trash_energy.normal.alternative"),
    OVERLAY_OSD_BARREL_NORMAL_FIRST_LINE("overlay.immersivetech.osd.barrel.normal.firstline"),
    OVERLAY_OSD_FLUID_VALVE_NORMAL_FIRST_LINE("overlay.immersivetech.osd.fluid_valve.normal.firstline"),
    OVERLAY_OSD_FLUID_VALVE_SNEAKING_FIRST_LINE("overlay.immersivetech.osd.fluid_valve.sneaking.firstline"),
    OVERLAY_OSD_FLUID_VALVE_SNEAKING_SECOND_LINE("overlay.immersivetech.osd.fluid_valve.sneaking.secondline"),
    OVERLAY_OSD_CREATIVE_CRATE_NORMAL_FIRST_LINE("overlay.immersivetech.osd.creative_crate.normal.firstline"),
    OVERLAY_STEEL_TANK_NORMAL_FIRST_LINE("overlay.immersivetech.metal_multiblock.steel_tank.normal.firstline"),
    TILE_TRASH_ITEM_NAME("tile.immersivetech.metal_trash.trash_item.name"),
    GUI_EMPTY("gui.immersivetech.empty"),
    GUI_BOILER_HEAT_PER_TICK("gui.immersivetech.boiler.heat_per_tick"),
    GUI_BOILER_TOTAL_HEAT("gui.immersivetech.boiler.total_heat"),
    GUI_FLUID_VALVE_FIRSTLINE("gui.immersivetech.fluid_valve.firstline"),
    GUI_FLUID_VALVE_LIMIT_PACKET("gui.immersivetech.fluid_valve.limitpacket"),
    GUI_FLUID_VALVE_LIMIT_TIME("gui.immersivetech.fluid_valve.limittime"),
    GUI_FLUID_VALVE_LIMIT_DESTINATION("gui.immersivetech.fluid_valve.limitdestination"),
    GUI_LOAD_CONTROLLER_LIMIT_PACKET("gui.immersivetech.load_controller.limitpacket"),
    GUI_LOAD_CONTROLLER_LIMIT_TIME("gui.immersivetech.load_controller.limittime"),
    GUI_LOAD_CONTROLLER_LIMIT_DESTINATION("gui.immersivetech.load_controller.limitdestination"),
    OVERLAY_OSD_LOAD_CONTROLLER_NORMAL_FIRST_LINE("overlay.immersivetech.osd.load_controller.normal.firstline"),
    OVERLAY_OSD_LOAD_CONTROLLER_SNEAKING_FIRST_LINE("overlay.immersivetech.osd.load_controller.sneaking.firstline"),
    OVERLAY_OSD_LOAD_CONTROLLER_SNEAKING_SECOND_LINE("overlay.immersivetech.osd.load_controller.sneaking.secondline"),
    GUI_STACK_LIMITER_LIMIT_PACKET("gui.immersivetech.stack_limiter.limitpacket"),
    GUI_STACK_LIMITER_LIMIT_TIME("gui.immersivetech.stack_limiter.limittime"),
    GUI_STACK_LIMITER_LIMIT_DESTINATION("gui.immersivetech.stack_limiter.limitdestination"),
    OVERLAY_OSD_STACK_LIMITER_NORMAL_FIRST_LINE("overlay.immersivetech.osd.stack_limiter.normal.firstline"),
    OVERLAY_OSD_STACK_LIMITER_SNEAKING_FIRST_LINE("overlay.immersivetech.osd.stack_limiter.sneaking.firstline"),
    OVERLAY_OSD_STACK_LIMITER_SNEAKING_SECOND_LINE("overlay.immersivetech.osd.stack_limiter.sneaking.secondline"),
    KEYWORD_GENERATE("keyword.immersivetech.generate"),
    GUI_TICKS("gui.immersivetech.ticks"),
    GUI_SECONDS("gui.immersivetech.seconds"),
    OVERLAY_REDSTONE_OFF("overlay.immersivetech.redstone_off"),
    OVERLAY_REDSTONE_NORMAL("overlay.immersivetech.redstone_normal"),
    OVERLAY_REDSTONE_INVERTED("overlay.immersivetech.redstone_inverted"),
    GUI_GENERIC_MULTIBLOCK_TOOLTIP("gui.immersivetech.generic_multiblock_jei_tooltip"),
    CATEGORY_DISTILLER_CHANCE("category.immersivetech.metal_multiblock.distillerChance");

	public String location;
	TranslationKey(String location) {
		this.location = location;
	}

	public String text(boolean addSpaceBefore, boolean addSpaceAfter) {
		return (addSpaceBefore? " ":"") + I18n.format(location) + (addSpaceAfter? " ":"");
	}

	public String text(boolean addSpaceBefore) {
		return text(addSpaceBefore, false);
	}

	public String text() {
		return text(false, false);
	}

	public String format(boolean addSpaceBefore, boolean addSpaceAfter, Object... parameters) {
		return (addSpaceBefore? " ":"") + I18n.format(location, parameters) + (addSpaceAfter? " ":"");
	}

	public String format(boolean addSpaceBefore, Object... parameters) {
		return format(addSpaceBefore, false, parameters);
	}

	public String format(Object... parameters) {
		return format(false, false, parameters);
	}

}