package mctmods.immersivetechnology.common.blocks.multiblocks.shapes;

import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.helper.GenericShape;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CoolingTowerShape extends GenericShape {
    public static final CoolingTowerShape GETTER = new CoolingTowerShape();

    @NotNull
    @Override
    protected List<AABB> getShape(BlockPos posInMultiblock) {
        final int bX = posInMultiblock.getX();
        final int bY = posInMultiblock.getY();
        final int bZ = posInMultiblock.getZ();

        List<AABB> main = new ArrayList<>();



        return main;
    }
}
