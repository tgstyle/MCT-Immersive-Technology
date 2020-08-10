package mctmods.immersivetechnology.common.util.multiblock;

import net.minecraft.util.EnumFacing;

public class PoICache {

    public EnumFacing facing;
    public int position;

    public PoICache(EnumFacing facing, PoIJSONSchema poi, boolean isMirrored) {
        this(poi.facing.LocalToGlobal(facing), poi.position, poi.facing, isMirrored);
    }

    public PoICache(EnumFacing facing, int position, LocalFacing localFacing, boolean isMirrored) {
        this.position = position;
        this.facing = isMirrored && (localFacing == LocalFacing.LEFT || localFacing == LocalFacing.RIGHT)? facing.getOpposite() : facing;
    }

    public boolean isPoI(EnumFacing facing, int position) {
        return this.position == position && this.facing == facing;
    }
}
