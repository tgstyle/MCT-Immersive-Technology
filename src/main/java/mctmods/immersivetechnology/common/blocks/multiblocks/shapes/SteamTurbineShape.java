package mctmods.immersivetechnology.common.blocks.multiblocks.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SteamTurbineShape extends GenericShape {
    public static final SteamTurbineShape GETTER = new SteamTurbineShape();

    @NotNull
    @Override
    protected List<AABB> getShape(BlockPos posInMultiblock) {
        final int bX = posInMultiblock.getX();
        final int bY = posInMultiblock.getY();
        final int bZ = posInMultiblock.getZ();

        List<AABB> main = new ArrayList<>();

        if (bY == 0) {
            if (bX == 0 && bZ == 1) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 1 && bZ == 1) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 1) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 0 && bZ == 2) {
                main.add(new AABB(0, 0, 0, 1, 1, 0.5));
                main.add(new AABB(0.5, 0, 0.5, 1, 1, 1));
            }
            if (bX == 1 && bZ == 2) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 2) {
                main.add(new AABB(0, 0, 0, 1, 1, 0.5));
                main.add(new AABB(0, 0, 0.5, 0.5, 1, 1));
            }
            if (bX == 0 && bZ == 3) {
                main.add(new AABB(0.5, 0, 0, 1, 1, 0.5));
                main.add(new AABB(0, 0, 0.5, 1, 1, 1));
            }
            if (bX == 1 && bZ == 3) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 3) {
                main.add(new AABB(0, 0, 0.5, 1, 1, 1));
                main.add(new AABB(0, 0, 0, 0.5, 1, 1));
            }
            if (bX == 0 && bZ == 4) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 1 && bZ == 4) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 4) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 0 && bZ == 6) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 1 && bZ == 6) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 6) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 0 && bZ == 7) {
                main.add(new AABB(0, 0, 0, 1, 1, 0.5));
                main.add(new AABB(0.5, 0, 0.5, 1, 1, 1));
            }
            if (bX == 1 && bZ == 7) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 7) {
                main.add(new AABB(0, 0, 0, 1, 1, 0.5));
                main.add(new AABB(0, 0, 0.5, 0.5, 1, 1));
            }
            if (bX == 0 && bZ == 8) {
                main.add(new AABB(0.5, 0, 0, 1, 1, 1));
            }
            if (bX == 1 && bZ == 8) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 8) {
                main.add(new AABB(0, 0, 0, 0.5, 1, 1));
            }
            if (bX == 0 && bZ == 9) {
                main.add(new AABB(0, 0, 0, 1, 0.5, 1));
            }
            if (bX == 1 && bZ == 9) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 9) {
                main.add(new AABB(0, 0, 0, 1, 0.5, 1));
            }
        }

        if (bY == 1) {
            if (bX == 1 && bZ == 0) {
                main.add(new AABB(0.125, 0.125, 0, 0.875, 0.875, 1));
            }
            if (bX == 0 && bZ == 1) {
                main.add(new AABB(0.25, 0, 0.25, 1, 1, 1));
            }
            if (bX == 1 && bZ == 1) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 1) {
                main.add(new AABB(0, 0, 0.25, 0.75, 1, 1));
            }
            if (bX == 0 && bZ == 2) {
                main.add(new AABB(0.5, 0, 0, 1, 1, 1));
            }
            if (bX == 1 && bZ == 2) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 2) {
                main.add(new AABB(0, 0, 0, 0.5, 1, 1));
            }
            if (bX == 0 && bZ == 3) {
                main.add(new AABB(0.5, 0, 0, 1, 1, 1));
            }
            if (bX == 1 && bZ == 3) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 3) {
                main.add(new AABB(0, 0, 0, 0.5, 1, 1));
            }
            if (bX == 0 && bZ == 4) {
                main.add(new AABB(0.25, 0, 0, 1, 1, 0.75));
            }
            if (bX == 1 && bZ == 4) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 4) {
                main.add(new AABB(0, 0, 0, 0.75, 1, 0.75));
            }
            if (bX == 1 && bZ == 5) {
                main.add(new AABB(0.125, 0.125, 0, 0.875, 0.875, 1));
            }
            if (bX == 0 && bZ == 6) {
                main.add(new AABB(0.25, 0, 0.25, 1, 1, 1));
            }
            if (bX == 1 && bZ == 6) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 6) {
                main.add(new AABB(0, 0, 0.25, 0.75, 1, 1));
            }
            if (bX == 0 && bZ == 7) {
                main.add(new AABB(0.5, 0, 0, 1, 1, 1));
            }
            if (bX == 1 && bZ == 7) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 7) {
                main.add(new AABB(0, 0, 0, 0.75, 1, 1));
            }
            if (bX == 0 && bZ == 8) {
                main.add(new AABB(0.5, 0, 0, 1, 1, 1));
            }
            if (bX == 1 && bZ == 8) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 8) {
                main.add(new AABB(0, 0, 0, 0.75, 1, 1));
            }
            if (bX == 0 && bZ == 9) {
                main.add(new AABB(0, 0, 0, 0.5, 1, 1));
            }
            if (bX == 1 && bZ == 9) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 9) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
        }

        if (bY == 2) {
            if (bX == 0 && bZ == 1) {
                main.add(new AABB(0.5, 0, 0.25, 1, 0.5, 1));
            }
            if (bX == 1 && bZ == 1) {
                main.add(new AABB(0, 0, 0, 1, 0.5, 1));
            }
            if (bX == 2 && bZ == 1) {
                main.add(new AABB(0, 0, 0.25, 0.5, 0.5, 1));
            }
            if (bX == 0 && bZ == 2) {
                main.add(new AABB(0.5, 0, 0, 1, 0.5, 0.25));
            }
            if (bX == 1 && bZ == 2) {
                main.add(new AABB(0, 0, 0, 1, 0.5, 1));
                main.add(new AABB(0, 0, 0.5, 1, 1, 1));
            }
            if (bX == 2 && bZ == 2) {
                main.add(new AABB(0, 0, 0, 0.5, 0.5, 0.25));
            }
            if (bX == 0 && bZ == 3) {
                main.add(new AABB(0.5, 0, 0.75, 1, 0.5, 1));
            }
            if (bX == 1 && bZ == 3) {
                main.add(new AABB(0, 0, 0, 1, 0.5, 1));
                main.add(new AABB(0, 0, 0, 1, 1, 0.5));
            }
            if (bX == 2 && bZ == 3) {
                main.add(new AABB(0, 0, 0.75, 0.5, 0.5, 1));
            }
            if (bX == 0 && bZ == 4) {
                main.add(new AABB(0.5, 0, 0, 1, 0.5, 0.75));
            }
            if (bX == 1 && bZ == 4) {
                main.add(new AABB(0, 0, 0, 1, 0.5, 0.75));
            }
            if (bX == 2 && bZ == 4) {
                main.add(new AABB(0, 0, 0, 0.5, 0.5, 0.75));
            }
            if (bX == 0 && bZ == 6) {
                main.add(new AABB(0.5, 0, 0.25, 1, 0.5, 1));
            }
            if (bX == 1 && bZ == 6) {
                main.add(new AABB(0, 0, 0.25, 1, 0.5, 1));
            }
            if (bX == 2 && bZ == 6) {
                main.add(new AABB(0, 0, 0.25, 0.5, 0.5, 1));
            }
            if (bX == 0 && bZ == 7) {
                main.add(new AABB(0.5, 0, 0, 1, 0.5, 0.25));
            }
            if (bX == 1 && bZ == 7) {
                main.add(new AABB(0, 0, 0, 1, 0.5, 0.25));
            }
            if (bX == 2 && bZ == 7) {
                main.add(new AABB(0, 0, 0, 0.5, 0.5, 0.25));
            }

            if (bX == 1 && bZ == 9) {
                main.add(new AABB(0, 0, 0, 1, 0.5, 1));
            }
            if (bX == 2 && bZ == 9) {
                main.add(new AABB(0, 0, 0, 1, 0.5, 1));
            }
        }

        if (bY == 3) {
            if (bX == 1 && bZ == 2) {
                main.add(new AABB(0.125, 0.125, 0.625, 0.875, 0.875, 1));
            }
            if (bX == 1 && bZ == 3) {
                main.add(new AABB(0.125, 0.125, 0, 0.875, 0.875, 1));
            }
            if (bX == 1 && bZ == 4) {
                main.add(new AABB(0.125, 0.125, 0, 0.875, 0.875, 1));
            }
            if (bX == 1 && bZ == 5) {
                main.add(new AABB(0.125, 0.125, 0, 0.875, 0.875, 1));
            }
            if (bX == 1 && bZ == 6) {
                main.add(new AABB(0.125, 0.125, 0, 0.875, 0.875, 1));
            }
        }
        return main;
    }
}
