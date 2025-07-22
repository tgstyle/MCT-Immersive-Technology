package mctmods.immersivetechnology.common.blocks.multiblocks.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class AdvCokeOvenShape extends GenericShape
{
    public static final AdvCokeOvenShape GETTER = new AdvCokeOvenShape();

    @NotNull
    @Override
    protected List<AABB> getShape(BlockPos posInMultiblock) {
        final int bX = posInMultiblock.getX();
        final int bY = posInMultiblock.getY();
        final int bZ = posInMultiblock.getZ();

        List<AABB> main = new ArrayList<>();

        if (bY == 0) {
            if (bZ == 0 && bX == 0)
            {
                main.add(new AABB(0.375, 0, 0, 1, 0, 0.6875));
                main.add(new AABB(0.375, 1, 0, 1, 0, 0.6875));
            }

            if (bZ == 0 && bX == 1)
            {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
                main.add(new AABB(0.375, 1, 0.6875, 1, 1, 1));
            }

            if (bZ == 0 && bX == 2)
            {
                main.add(new AABB(0, 0, 0, 1, 0, 0.6875));
                main.add(new AABB(0.625, 1, 0, 0, 0, 0.6875));
            }
            if (bZ == 1 && bX == 2)
            {
                //main.add(new AABB(0.5, 0.0, 0.0, 1.0, 1.0, 1.0));
                main.add(new AABB(0.0, 0.0, 0.0, 0.5, 1.0, 1.0));
            }
            if (bZ == 1 && bX == 0)
            {
                //main.add(new AABB(0.5, 0.0, 0.0, 1.0, 1.0, 1.0));
                main.add(new AABB(0.5, 0.0, 0.0, 1, 1.0, 1.0));
            }

            if (bZ == 2 && bX == 1)
            {
                main.add(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
            }

            if (bZ == 1 && bX == 1)
            {
                main.add(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
            }

            if (bZ == 2 && bX == 0)
            {
                //main.add(new AABB(0.3125, 0.0, 0.3125, 0, 1.0, 0.6875));
                main.add(new AABB(0.6875, 0.0, 0, 1, 1.0, 0.3125));
            }

            if (bZ == 2 && bX == 2)
            {
                //main.add(new AABB(0.3125, 0.0, 0.3125, 0, 1.0, 0.6875));
                main.add(new AABB(0, 0.0, 0, 0.3125, 1.0, 0.3125));
            }
        }

        if (bY == 1) {
            if (bZ == 0 && bX == 0)
            {
                //main.add(new AABB(0.375, 0, 0, 1, 0, 0.6875));
                main.add(new AABB(0.375, 1, 0, 1, 0, 0.6875));
            }

            if (bZ == 0 && bX == 1)
            {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
                //main.add(new AABB(0.375, 1, 0.6875, 1, 1, 1));
            }

            if (bZ == 0 && bX == 2)
            {
                //main.add(new AABB(0, 0, 0, 1, 0, 0.6875));
                main.add(new AABB(0.625, 1, 0, 0, 0, 0.6875));
            }

            if (bZ == 1 && bX == 1)
            {
                main.add(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.5));
            }

            if (bZ == 2 && bX == 1)
            {
                main.add(new AABB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0));
            }

            if (bZ == 1 && bX == 0)
            {
                //main.add(new AABB(0.5, 0.0, 0.0, 1.0, 1.0, 1.0));
                main.add(new AABB(0.5, 0.0, 0.0, 1.0, 1.0, 1.0));
            }

            if (bZ == 1 && bX == 2)
            {
                //main.add(new AABB(0.5, 0.0, 0.0, 1.0, 1.0, 1.0));
                main.add(new AABB(0.0, 0.0, 0.0, 0.5, 1.0, 0.5));
            }

            if (bZ == 2 && bX == 0)
            {
                //main.add(new AABB(0.5, 0.0, 0.0, 1.0, 1.0, 1.0));
                main.add(new AABB(0.5, 0.0, 0.0, 1, 1.0, 0.5));
            }

            if (bZ == 2 && bX == 2)
            {
                //main.add(new AABB(0.5, 0.0, 0.0, 1.0, 1.0, 1.0));
                main.add(new AABB(0.0, 0.0, 0.0, 0.5, 1.0, 0.5));
            }
        }

        if (bY == 2) {
            if (bZ == 1 && bX == 1)
            {
                main.add(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
            }

            if (bZ == 0 && bX == 0)
            {
                //main.add(new AABB(0.375, 0, 0, 1, 0, 0.6875));
                main.add(new AABB(0.375, 0, 0, 1, 0.25, 0.6875));
            }

            if (bZ == 0 && bX == 1)
            {
                main.add(new AABB(0, 0, 0, 1, 0.25, 0.6875));
                //main.add(new AABB(0.375, 1, 0.6875, 1, 1, 1));
            }

            if (bZ == 0 && bX == 2)
            {
                //main.add(new AABB(0, 0, 0, 1, 0, 0.6875));
                main.add(new AABB(0.625, 0, 0, 0, 0.25, 0.6875));
            }
        }

        if (bY == 3)
        {
            if (bZ == 1 && bX == 1)
            {
                main.add(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
            }

        }

        return main;
    }
}
