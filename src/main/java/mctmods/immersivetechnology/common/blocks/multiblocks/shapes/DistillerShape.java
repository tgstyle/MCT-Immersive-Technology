package mctmods.immersivetechnology.common.blocks.multiblocks.shapes;

import mctmods.immersivetechnology.common.util.multiblock.GenericShape;
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
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.3125D, 1.0000D));
            main.add(new AABB(0.0000D, 0.3125D, 0.0000D, 0.0625D, 1.0000D, 0.0625D));
            main.add(new AABB(0.0625D, 0.3125D, 0.0625D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.9375D, 0.3125D, 0.0000D, 1.0000D, 1.0000D, 0.0625D));
        }
        if (bX == 0 && bY == 1 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 1 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 2 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.0625D, 0.5000D, 0.0625D));
            main.add(new AABB(0.0000D, 0.1875D, 0.0625D, 1.0000D, 0.5000D, 1.0000D));
            main.add(new AABB(0.0625D, 0.0000D, 0.0625D, 1.0000D, 0.1875D, 1.0000D));
            main.add(new AABB(0.0625D, 0.1875D, 0.0000D, 1.0000D, 0.5000D, 0.0625D));
        }
        if (bX == 0 && bY == 2 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.9375D, 1.0000D, 0.5000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.1875D, 0.0000D, 1.0000D, 0.5000D, 0.9375D));
            main.add(new AABB(0.0625D, 0.0000D, 0.0000D, 1.0000D, 0.1875D, 0.9375D));
        }
        if (bX == 1 && bY == 0 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 0 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 0 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 1.0000D));
        }
        if (bX == 1 && bY == 1 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 1 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 2 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 2 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 0 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 0 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 0 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.5000D, 0.5000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 1 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.3125D, 1.0000D));
            main.add(new AABB(0.0000D, 0.3125D, 0.0625D, 0.9375D, 1.0000D, 1.0000D));
            main.add(new AABB(0.9375D, 0.3125D, 0.0000D, 1.0000D, 1.0000D, 0.0625D));
        }
        if (bX == 2 && bY == 1 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.9375D, 1.0000D, 0.9375D));
            main.add(new AABB(0.0000D, 0.0000D, 0.9375D, 1.0000D, 0.3125D, 1.0000D));
            main.add(new AABB(0.9375D, 0.0000D, 0.0000D, 1.0000D, 0.3125D, 0.9375D));
            main.add(new AABB(0.9375D, 0.3125D, 0.9375D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 1 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.5000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 2 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0625D, 0.9375D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.6875D, 0.0000D, 1.0000D, 1.0000D, 0.0625D));
            main.add(new AABB(0.9375D, 0.0000D, 0.0000D, 1.0000D, 0.6875D, 0.0625D));
            main.add(new AABB(0.9375D, 0.6875D, 0.0625D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 2 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.9375D, 1.0000D, 0.9375D));
            main.add(new AABB(0.0000D, 0.6875D, 0.9375D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.9375D, 0.0000D, 0.9375D, 1.0000D, 0.6875D, 1.0000D));
            main.add(new AABB(0.9375D, 0.6875D, 0.0000D, 1.0000D, 1.0000D, 0.9375D));
        }

        return main;
    }
}
