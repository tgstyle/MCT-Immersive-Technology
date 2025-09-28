package mctmods.immersivetechnology.common.blocks.multiblocks.shapes;

import mctmods.immersivetechnology.common.util.multiblock.GenericShape;
import mctmods.immersivetechnology.common.util.multiblock.MultiblockData;
import mctmods.immersivetechnology.common.util.multiblock.MultiblockDataLoader;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.function.Function;

public class BoilerTankShape {
    public static final MultiblockData DATA = MultiblockDataLoader.loadMultiblockData("boiler_tank");
    public static final Function<BlockPos, VoxelShape> GETTER;
    public static int WIDTH, HEIGHT, LENGTH;

    static {
        int[] dims = GenericShape.loadDimensions("boiler_tank", "metal");
        WIDTH = dims[0] + DATA.padShape[0];
        HEIGHT = dims[1] + DATA.padShape[1];
        LENGTH = dims[2] + DATA.padShape[2];
        ITLib.IT_LOGGER.info("Loaded dimensions for boiler_tank: W={}, H={}, L={}", WIDTH, HEIGHT, LENGTH);
        if (WIDTH <= 0 || HEIGHT <= 0 || LENGTH <= 0) {
            GETTER = FullblockShape.GETTER;
            WIDTH = HEIGHT = LENGTH = 0;
            if (DATA.shapeAABB == null || !DATA.shapeAABB.isEmpty()) {
                ITLib.IT_LOGGER.error("Invalid dimensions loaded for boiler_tank multiblock.");
            }
        } else {
            int num = WIDTH * HEIGHT * LENGTH;
            if (DATA.shapeAABB == null) {
                ITLib.IT_LOGGER.error("Failed to load shapes for boiler_tank multiblock. (shapeAABB null)");
                GETTER = FullblockShape.GETTER;
            } else if (DATA.shapeAABB.isEmpty()) {
                ITLib.IT_LOGGER.info("Using full block shape for boiler_tank.");
                GETTER = FullblockShape.GETTER;
            } else {
                List<List<AABB>> shapes = GenericShape.loadShapes(DATA, num);
                if (shapes == null) {
                    ITLib.IT_LOGGER.error("Failed to load shapes for boiler_tank multiblock.");
                    GETTER = FullblockShape.GETTER;
                } else {
                    boolean allFull = !shapes.isEmpty() && shapes.stream().allMatch(list -> list.size() == 1 && list.get(0).equals(GenericShape.FULL_BLOCK));
                    if (allFull) { GETTER = FullblockShape.GETTER; }
                    else { GETTER = new GenericShape.JsonShape(WIDTH, HEIGHT, LENGTH, shapes); }
                }
            }
        }
    }
}
