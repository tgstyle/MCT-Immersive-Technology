package mctmods.immersivetechnology.common.blocks.multiblocks.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GasTurbineShape extends GenericShape {
    public static final GasTurbineShape GETTER = new GasTurbineShape();

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
        if (bX == 0 && bY == 0 && bZ == 3) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 0 && bZ == 4) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 0 && bZ == 5) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 0 && bZ == 6) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 1 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 1 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.5000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.5000D, 1.0000D, 0.5000D, 1.0000D));
            main.add(new AABB(0.5000D, 0.5000D, 0.5000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 1 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 1.0000D));
            main.add(new AABB(0.5000D, 0.5000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 1 && bZ == 3) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.5000D, 0.5000D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.5000D, 0.5000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 1 && bZ == 4) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 1 && bZ == 5) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 1 && bZ == 6) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 2 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 1.0000D));
        }
        if (bX == 0 && bY == 2 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 0.5000D));
            main.add(new AABB(0.5000D, 0.0000D, 0.5000D, 1.0000D, 0.5000D, 1.0000D));
        }
        if (bX == 0 && bY == 2 && bZ == 2) {
            main.add(new AABB(0.5000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 1.0000D));
        }
        if (bX == 0 && bY == 2 && bZ == 3) {
            main.add(new AABB(0.0000D, 0.0000D, 0.5000D, 1.0000D, 0.5000D, 1.0000D));
            main.add(new AABB(0.5000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 1.0000D));
        }
        if (bX == 0 && bY == 2 && bZ == 4) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 1.0000D));
            main.add(new AABB(0.5000D, 0.5000D, 0.5000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 2 && bZ == 5) {
            main.add(new AABB(0.0000D, 0.0000D, 0.5000D, 1.0000D, 0.5000D, 1.0000D));
            main.add(new AABB(0.5000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 1.0000D));
        }
        if (bX == 0 && bY == 2 && bZ == 6) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 1.0000D));
        }
        if (bX == 1 && bY == 0 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 0 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 0 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 0 && bZ == 3) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 0 && bZ == 4) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 0 && bZ == 5) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 0 && bZ == 6) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 1 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.5000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.5000D, 1.0000D, 0.5000D, 1.0000D));
        }
        if (bX == 1 && bY == 1 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.5000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.5000D, 1.0000D, 0.5000D, 1.0000D));
        }
        if (bX == 1 && bY == 1 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 1.0000D));
        }
        if (bX == 1 && bY == 1 && bZ == 3) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.5000D, 0.5000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 1 && bZ == 4) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 1 && bZ == 5) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 1 && bZ == 6) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 2 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 2 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.5000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.5000D, 1.0000D, 0.5000D, 1.0000D));
        }
        if (bX == 1 && bY == 2 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 1.0000D));
        }
        if (bX == 1 && bY == 2 && bZ == 3) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.5000D, 0.5000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 2 && bZ == 4) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 2 && bZ == 5) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.5000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.5000D, 1.0000D, 0.5000D, 1.0000D));
        }
        if (bX == 1 && bY == 2 && bZ == 6) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 1.0000D));
        }
        if (bX == 2 && bY == 0 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 0 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 0 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 0 && bZ == 3) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 0 && bZ == 4) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 0 && bZ == 5) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 0 && bZ == 6) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 1 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 1 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.5000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.5000D, 1.0000D, 0.5000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.5000D, 0.5000D, 0.5000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 1 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.5000D, 0.0000D, 0.5000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 1 && bZ == 3) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.5000D, 0.0000D, 0.5000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.5000D, 0.5000D, 0.5000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 1 && bZ == 4) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 1 && bZ == 5) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 1 && bZ == 6) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 2 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 1.0000D));
        }
        if (bX == 2 && bY == 2 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 0.5000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.5000D, 0.5000D, 0.5000D, 1.0000D));
        }
        if (bX == 2 && bY == 2 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.5000D, 0.5000D, 1.0000D));
        }
        if (bX == 2 && bY == 2 && bZ == 3) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.5000D, 0.5000D, 1.0000D));
            main.add(new AABB(0.5000D, 0.0000D, 0.5000D, 1.0000D, 0.5000D, 1.0000D));
        }
        if (bX == 2 && bY == 2 && bZ == 4) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.5000D, 0.5000D, 0.5000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 2 && bZ == 5) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.5000D, 0.5000D, 1.0000D));
        }
        if (bX == 2 && bY == 2 && bZ == 6) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.5000D, 0.5000D, 1.0000D));
        }

        return main;
    }
}
