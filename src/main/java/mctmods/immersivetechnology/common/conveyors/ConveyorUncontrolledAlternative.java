package mctmods.immersivetechnology.common.conveyors;

import net.minecraft.tileentity.TileEntity;

public class ConveyorUncontrolledAlternative extends ConveyorBasicAlternative {
    @Override public boolean isActive(TileEntity tile) { return true; }
}
