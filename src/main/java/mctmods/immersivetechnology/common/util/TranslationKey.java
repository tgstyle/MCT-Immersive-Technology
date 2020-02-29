package mctmods.immersivetechnology.common.util;
import net.minecraft.client.resources.I18n;

public enum TranslationKey {

    OVERLAY_OSD_TRASH_ITEM_NORMAL_FIRST_LINE("overlay.immersivetech.osd.trash_item.normal.firstline"),
    OVERLAY_OSD_TRASH_ITEM_SNEAKING_FIRST_LINE("overlay.immersivetech.osd.trash_item.sneaking.firstline"),
    OVERLAY_OSD_TRASH_ITEM_SNEAKING_SECOND_LINE("overlay.immersivetech.osd.trash_item.sneaking.secondline"),
    OVERLAY_OSD_TRASH_FLUID_NORMAL_FIRST_LINE("overlay.immersivetech.osd.trash_fluid.normal.firstline"),
    OVERLAY_OSD_TRASH_FLUID_SNEAKING_FIRST_LINE("overlay.immersivetech.osd.trash_fluid.sneaking.firstline"),
    OVERLAY_OSD_TRASH_FLUID_SNEAKING_SECOND_LINE("overlay.immersivetech.osd.trash_fluid.sneaking.secondline"),
    OVERLAY_OSD_TRASH_ENERGY_NORMAL_FIRST_LINE("overlay.immersivetech.osd.trash_energy.normal.firstline"),
    OVERLAY_OSD_TRASH_ENERGY_SNEAKING_FIRST_LINE("overlay.immersivetech.osd.trash_energy.sneaking.firstline"),
    OVERLAY_OSD_TRASH_ENERGY_SNEAKING_SECOND_LINE("overlay.immersivetech.osd.trash_energy.sneaking.secondline"),
    OVERLAY_OSD_BARREL_NORMAL_FIRST_LINE("overlay.immersivetech.osd.barrel.normal.firstline"),
    OVERLAY_OSD_BARREL_SNEAKING_FIRST_LINE("overlay.immersivetech.osd.barrel.sneaking.firstline"),
    OVERLAY_OSD_BARREL_SNEAKING_SECOND_LINE("overlay.immersivetech.osd.barrel.sneaking.secondline"),
    OVERLAY_STEEL_TANK_NORMAL_FIRST_LINE("overlay.immersivetech.metal_multiblock.steel_tank.normal.firstline"),
    TILE_TRASH_ITEM_NAME("tile.immersivetech.metal_trash.trash_item.name"),
    GUI_EMPTY("gui.immersivetech.empty"),
    GUI_BOILER_HEAT_PER_TICK("gui.immersivetech.boiler.heat_per_tick"),
    GUI_BOILER_TOTAL_HEAT("gui.immersivetech.boiler.total_heat"),
    KEYWORD_GENERATE("keyword.immersivetech.generate"),
    GUI_TICKS("gui.immersivetech.ticks"),
    GUI_SECONDS("gui.immersivetech.seconds");

    String location;
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
