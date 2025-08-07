package mctmods.immersivetechnology.common.blocks.multiblocks.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DistillerShape extends GenericShape {
    public static final DistillerShape GETTER = new DistillerShape();

    @NotNull
    @Override
    protected List<AABB> getShape(BlockPos posInMultiblock) {
        final int bX = posInMultiblock.getX();
        final int bY = posInMultiblock.getY();
        final int bZ = posInMultiblock.getZ();

        List<AABB> main = new ArrayList<>();

        if (bX == 0 && bY == 0 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 0 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 0 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 1 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.3438D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.0938D));
            main.add(new AABB(0.0312D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 1 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 1 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 2 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.0938D, 0.5312D, 0.0938D));
            main.add(new AABB(0.0000D, 0.1562D, 0.0000D, 1.0000D, 0.5312D, 1.0000D));
            main.add(new AABB(0.0312D, 0.0000D, 0.0312D, 1.0000D, 0.5312D, 1.0000D));
            main.add(new AABB(0.0312D, 0.0000D, 0.0000D, 1.0000D, 0.0312D, 1.0000D));
            main.add(new AABB(0.9062D, 0.0000D, 0.0000D, 1.0000D, 0.5312D, 1.0000D));
            main.add(new AABB(0.9688D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.0938D));
            main.add(new AABB(0.9688D, 0.6562D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 2 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.0312D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.9062D, 1.0000D, 0.5312D, 0.9688D));
            main.add(new AABB(0.0000D, 0.0000D, 0.9062D, 0.0938D, 0.5312D, 1.0000D));
            main.add(new AABB(0.0000D, 0.1562D, 0.0000D, 1.0000D, 0.5312D, 1.0000D));
            main.add(new AABB(0.0312D, 0.0000D, 0.0000D, 1.0000D, 0.5312D, 0.9688D));
            main.add(new AABB(0.9062D, 0.0000D, 0.0000D, 1.0000D, 0.5312D, 1.0000D));
            main.add(new AABB(0.9688D, 0.0000D, 0.9062D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.9688D, 0.6562D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 2 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.0312D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.0938D, 0.5312D, 0.0312D));
            main.add(new AABB(0.0000D, 0.1562D, 0.0000D, 1.0000D, 0.5312D, 0.0312D));
            main.add(new AABB(0.9062D, 0.0000D, 0.0000D, 1.0000D, 0.5312D, 0.0312D));
            main.add(new AABB(0.9688D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.0312D));
        }
        if (bX == 1 && bY == 0 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 0 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5312D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.0312D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.0312D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.5938D, 0.7188D, 1.0000D, 0.9062D, 0.9062D));
            main.add(new AABB(0.0000D, 0.9688D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.9688D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 0 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5312D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.0312D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.9688D, 0.0000D, 1.0000D, 1.0000D, 0.0312D));
            main.add(new AABB(0.9688D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.0312D));
            main.add(new AABB(0.9688D, 0.9688D, 0.4688D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 1 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.3438D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.0938D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0312D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 1 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.9688D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.3438D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.0938D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 1 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.0312D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.3438D, 0.0312D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.0938D, 1.0000D, 0.0312D));
            main.add(new AABB(0.9688D, 0.0000D, 0.4688D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 2 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.0938D, 0.5312D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.0938D, 1.0000D, 0.0938D));
            main.add(new AABB(0.0000D, 0.6562D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0312D, 0.0000D, 0.0312D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 2 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5312D, 0.9688D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.0938D, 0.5312D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.9062D, 1.0000D, 1.0000D, 0.9688D));
            main.add(new AABB(0.0000D, 0.0000D, 0.9062D, 0.0938D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.6562D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0312D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.9688D));
        }
        if (bX == 1 && bY == 2 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.0938D, 1.0000D, 0.0312D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.0312D, 0.0312D, 1.0000D));
            main.add(new AABB(0.0000D, 0.6562D, 0.0000D, 1.0000D, 1.0000D, 0.0312D));
            main.add(new AABB(0.9688D, 0.0000D, 0.4688D, 1.0000D, 0.0312D, 1.0000D));
        }
        if (bX == 2 && bY == 0 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 0 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 0 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5312D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.0312D));
            main.add(new AABB(0.0000D, 0.9688D, 0.4688D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0938D, 0.0000D, 0.5938D, 0.2812D, 1.0000D, 0.9062D));
            main.add(new AABB(0.7188D, 0.0000D, 0.5938D, 0.9062D, 1.0000D, 0.9062D));
        }
        if (bX == 2 && bY == 1 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.3438D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0312D, 0.9688D, 1.0000D, 1.0000D));
            main.add(new AABB(0.9062D, 0.0000D, 0.0000D, 0.9688D, 1.0000D, 1.0000D));
            main.add(new AABB(0.9062D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.0938D));
        }
        if (bX == 2 && bY == 1 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.9688D, 1.0000D, 0.9688D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.3438D, 1.0000D));
            main.add(new AABB(0.9062D, 0.0000D, 0.0000D, 0.9688D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.5312D));
            main.add(new AABB(0.0000D, 0.0000D, 0.9062D, 1.0000D, 1.0000D, 0.9688D));
            main.add(new AABB(0.9062D, 0.0000D, 0.9062D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 1 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.3438D, 0.0312D));
            main.add(new AABB(0.0000D, 0.0000D, 0.4688D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.9062D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.0312D));
        }
        if (bX == 2 && bY == 2 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0312D, 0.9688D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.6562D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.9062D, 0.0000D, 0.0000D, 0.9688D, 1.0000D, 1.0000D));
            main.add(new AABB(0.9062D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.0938D));
        }
        if (bX == 2 && bY == 2 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.9688D, 1.0000D, 0.9688D));
            main.add(new AABB(0.0000D, 0.6562D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.9062D, 0.0000D, 0.0000D, 0.9688D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.0312D, 0.5312D));
            main.add(new AABB(0.0000D, 0.0000D, 0.9062D, 1.0000D, 1.0000D, 0.9688D));
            main.add(new AABB(0.9062D, 0.0000D, 0.9062D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 2 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.4688D, 1.0000D, 0.0312D, 1.0000D));
            main.add(new AABB(0.0000D, 0.6562D, 0.0000D, 1.0000D, 1.0000D, 0.0312D));
            main.add(new AABB(0.9062D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.0312D));
        }

        return main;
    }
}
