package mctmods.immersivetechnology.common.blocks.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.Vec3;

public enum ITPlacementLimitation {
    SIDE_CLICKED((side, placer, hitPos) -> side),
    PISTON_LIKE((side, placer, hitPos) -> Direction.orderedByNearest(placer)[0]),
    PISTON_INVERTED((side, placer, hitPos) -> Direction.orderedByNearest(placer)[0].getOpposite()),
    HORIZONTAL((side, placer, hitPos) -> Direction.fromYRot(placer.getYRot())),
    VERTICAL((side, placer, hitPos) -> side == Direction.DOWN || side != Direction.UP && !(hitPos.y <= (double)0.5F) ? Direction.DOWN : Direction.UP),
    HORIZONTAL_AXIS((side, placer, hitPos) -> {
        Direction f = Direction.fromYRot(placer.getYRot());
        return f != Direction.SOUTH && f != Direction.WEST ? f : f.getOpposite();
    }),
    HORIZONTAL_QUADRANT((side, placer, hitPos) -> {
        if (side.getAxis() != Axis.Y) {
            return side.getOpposite();
        } else {
            double xFromMid = hitPos.x - (double)0.5F;
            double zFromMid = hitPos.z - (double)0.5F;
            double max = Math.max(Math.abs(xFromMid), Math.abs(zFromMid));
            if (max == Math.abs(xFromMid)) {
                return xFromMid < (double)0.0F ? Direction.WEST : Direction.EAST;
            } else {
                return zFromMid < (double)0.0F ? Direction.NORTH : Direction.SOUTH;
            }
        }
    }),
    HORIZONTAL_PREFER_SIDE((side, placer, hitPos) -> side.getAxis() != Axis.Y ? side.getOpposite() : placer.getDirection()),
    FIXED_DOWN((side, placer, hitPos) -> Direction.DOWN);

    private final DirectionGetter dirGetter;

    ITPlacementLimitation(DirectionGetter dirGetter) { this.dirGetter = dirGetter; }

    public Direction getDirectionForPlacement(Direction side, LivingEntity placer, Vec3 clickLocation) { return this.dirGetter.getDirectionForPlacement(side, placer, clickLocation); }

    public Direction getDirectionForPlacement(BlockPlaceContext context) {
        Vec3 clickLocation = context.getClickLocation();
        BlockPos pos = context.getClickedPos();
        clickLocation = clickLocation.subtract(pos.getX(), pos.getY(), pos.getZ());
        return this.getDirectionForPlacement(context.getClickedFace(), context.getPlayer(), clickLocation);
    }

    private interface DirectionGetter {
        Direction getDirectionForPlacement(Direction var1, LivingEntity var2, Vec3 var3);
    }
}
