package mctmods.immersivetechnology.common.blocks.multiblocks.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class AdvancedCokeOvenShape extends GenericShape {
    public static final AdvancedCokeOvenShape GETTER = new AdvancedCokeOvenShape();

    @NotNull
    @Override
    protected List<AABB> getShape(BlockPos posInMultiblock) {
        final int bX = posInMultiblock.getX();
        final int bY = posInMultiblock.getY();
        final int bZ = posInMultiblock.getZ();

        List<AABB> main = new ArrayList<>();

        if (bX == 2 && bY == 0 && bZ == 1) {
            main.add(new AABB(0.2500D, 0.8750D, 0.8750D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 0 && bZ == 2) {
            main.add(new AABB(0.2500D, 0.8750D, 0.0000D, 1.0000D, 1.0000D, 0.7500D));
            main.add(new AABB(0.6250D, 0.8750D, 0.7500D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 1 && bZ == 1) {
            main.add(new AABB(0.2500D, 0.0000D, 0.8750D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 1 && bZ == 2) {
            main.add(new AABB(0.2500D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.1250D));
            main.add(new AABB(0.2500D, 0.0000D, 0.1250D, 1.0000D, 0.1250D, 0.7500D));
            main.add(new AABB(0.2500D, 0.1250D, 0.1250D, 0.5000D, 1.0000D, 0.7500D));
            main.add(new AABB(0.5000D, 0.1250D, 0.6250D, 1.0000D, 1.0000D, 0.7500D));
            main.add(new AABB(0.6250D, 0.0000D, 0.7500D, 1.0000D, 0.1250D, 1.0000D));
            main.add(new AABB(0.6250D, 0.1250D, 0.7500D, 0.7500D, 1.0000D, 1.0000D));
            main.add(new AABB(0.7500D, 0.8750D, 0.7500D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 2 && bZ == 1) {
            main.add(new AABB(0.2500D, 0.0000D, 0.8750D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 2 && bZ == 2) {
            main.add(new AABB(0.2500D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.1250D));
            main.add(new AABB(0.2500D, 0.0000D, 0.1250D, 0.5000D, 1.0000D, 0.7500D));
            main.add(new AABB(0.3750D, 0.3750D, 0.7500D, 1.0000D, 0.6250D, 1.0000D));
            main.add(new AABB(0.5000D, 0.0000D, 0.6250D, 1.0000D, 1.0000D, 0.7500D));
            main.add(new AABB(0.5000D, 0.1250D, 0.7500D, 0.7500D, 0.2500D, 1.0000D));
            main.add(new AABB(0.5000D, 0.2500D, 0.7500D, 0.6250D, 1.0000D, 1.0000D));
            main.add(new AABB(0.5000D, 0.3750D, 0.5000D, 1.0000D, 0.6250D, 1.0000D));
            main.add(new AABB(0.6250D, 0.0000D, 0.7500D, 1.0000D, 0.1250D, 1.0000D));
            main.add(new AABB(0.6250D, 0.8750D, 0.7500D, 0.7500D, 1.0000D, 1.0000D));
        }
        if (bX == 3 && bY == 0 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.8750D, 0.8750D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 3 && bY == 0 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.8750D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 3 && bY == 1 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.8750D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 3 && bY == 1 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.1250D));
            main.add(new AABB(0.0000D, 0.0000D, 0.1250D, 1.0000D, 0.1250D, 1.0000D));
            main.add(new AABB(0.0000D, 0.1250D, 0.6250D, 1.0000D, 1.0000D, 0.7500D));
            main.add(new AABB(0.0000D, 0.8750D, 0.7500D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 3 && bY == 2 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.8750D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 3 && bY == 2 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.1250D));
            main.add(new AABB(0.0000D, 0.0000D, 0.6250D, 1.0000D, 1.0000D, 0.7500D));
            main.add(new AABB(0.0000D, 0.0000D, 0.7500D, 1.0000D, 0.1250D, 1.0000D));
            main.add(new AABB(0.0000D, 0.3750D, 0.5000D, 1.0000D, 0.6250D, 1.0000D));
        }
        if (bX == 4 && bY == 0 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.8750D, 0.8750D, 0.7500D, 1.0000D, 1.0000D));
        }
        if (bX == 4 && bY == 0 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.8750D, 0.0000D, 0.7500D, 1.0000D, 0.7500D));
            main.add(new AABB(0.0000D, 0.8750D, 0.7500D, 0.3750D, 1.0000D, 1.0000D));
        }
        if (bX == 4 && bY == 1 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.8750D, 0.7500D, 1.0000D, 1.0000D));
        }
        if (bX == 4 && bY == 1 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.7500D, 1.0000D, 0.1250D));
            main.add(new AABB(0.0000D, 0.0000D, 0.1250D, 0.7500D, 0.1250D, 0.7500D));
            main.add(new AABB(0.0000D, 0.0000D, 0.7500D, 0.3750D, 0.1250D, 1.0000D));
            main.add(new AABB(0.0000D, 0.1250D, 0.6250D, 0.7500D, 1.0000D, 0.7500D));
            main.add(new AABB(0.0000D, 0.8750D, 0.7500D, 0.3750D, 1.0000D, 1.0000D));
            main.add(new AABB(0.2500D, 0.1250D, 0.7500D, 0.3750D, 1.0000D, 1.0000D));
            main.add(new AABB(0.5000D, 0.1250D, 0.1250D, 0.7500D, 1.0000D, 0.7500D));
        }
        if (bX == 4 && bY == 2 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.8750D, 0.7500D, 1.0000D, 1.0000D));
        }
        if (bX == 4 && bY == 2 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.7500D, 1.0000D, 0.1250D));
            main.add(new AABB(0.0000D, 0.0000D, 0.6250D, 0.7500D, 1.0000D, 0.7500D));
            main.add(new AABB(0.0000D, 0.0000D, 0.7500D, 0.3750D, 0.1250D, 1.0000D));
            main.add(new AABB(0.0000D, 0.3750D, 0.5000D, 0.7500D, 0.6250D, 0.7500D));
            main.add(new AABB(0.0000D, 0.3750D, 0.7500D, 0.6250D, 0.6250D, 1.0000D));
            main.add(new AABB(0.2500D, 0.1250D, 0.7500D, 0.5000D, 0.2500D, 1.0000D));
            main.add(new AABB(0.2500D, 0.8750D, 0.7500D, 0.5000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.3750D, 0.2500D, 0.7500D, 0.5000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.5000D, 0.0000D, 0.1250D, 0.7500D, 1.0000D, 0.7500D));
        }

        return main;
    }
}
