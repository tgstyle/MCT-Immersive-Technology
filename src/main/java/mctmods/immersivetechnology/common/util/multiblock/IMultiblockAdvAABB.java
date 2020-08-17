package mctmods.immersivetechnology.common.util.multiblock;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import mctmods.immersivetechnology.api.ITUtils;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.ArrayList;
import java.util.List;

public interface IMultiblockAdvAABB extends IEBlockInterfaces.IAdvancedCollisionBounds, IEBlockInterfaces.IAdvancedSelectionBounds {

    byte[][][] GetAABBArray();
    TileEntityMultiblockPart<?> This();

    default List<AxisAlignedBB> Inject(List <AxisAlignedBB> list, double A, double B, double C, double D, double minY, double maxY) {
        double[] boundingArray = ITUtils.alternativeSmartBoundingBox(A, B, C, D, minY, maxY, This().facing, This().mirrored? This().facing.rotateYCCW() : This().facing.rotateY());
        list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(This().getPos().getX(), This().getPos().getY(), This().getPos().getZ()));
        return list;
    }

    default List<AxisAlignedBB> Inject(List <AxisAlignedBB> list, byte[] box) {
        return Inject(list,
                ConvertRange(0, 16, 0, 1, box[0]),
                ConvertRange(0, 16, 0, 1, box[1]),
                ConvertRange(0, 16, 0, 1, box[2]),
                ConvertRange(0, 16, 0, 1, box[3]),
                ConvertRange(0, 16, 0, 1, box[4]),
                ConvertRange(0, 16, 0, 1, box[5]));
    }

    default double ConvertRange(
            int originalStart, int originalEnd, // original range
            double newStart, double newEnd, // desired range
            byte value) {
        double scale = (newEnd - newStart) / (originalEnd - originalStart);
        return (newStart + ((value - originalStart) * scale));
    }

    @Override
    default List <AxisAlignedBB> getAdvancedColisionBounds() {
        return getAdvancedSelectionBounds();
    }

    @Override
    default List <AxisAlignedBB> getAdvancedSelectionBounds() {
        byte[][][] array = GetAABBArray();
        List<AxisAlignedBB> list = new ArrayList<>();
        byte[][] aabbs = array[This().pos];
        if(aabbs == null || aabbs.length == 0) return null;
        for(int index = 0; index < aabbs.length; index++) {
            Inject(list, aabbs[index]);
        }
        return list;
    }

}
