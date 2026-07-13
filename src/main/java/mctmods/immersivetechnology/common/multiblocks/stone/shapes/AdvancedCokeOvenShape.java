package mctmods.immersivetechnology.common.multiblocks.stone.shapes;

import mctmods.immersivetechnology.common.multiblocks.metal.shapes.FullblockShape;
import mctmods.immersivetechnology.core.util.multiblock.GenericShape;
import mctmods.immersivetechnology.core.util.multiblock.MultiblockData;
import mctmods.immersivetechnology.core.util.multiblock.MultiblockDataLoader;
import mctmods.immersivetechnology.core.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.function.Function;

public class AdvancedCokeOvenShape {
    public static final MultiblockData DATA = MultiblockDataLoader.loadMultiblockData("advanced_coke_oven");
    public static final Function<BlockPos, VoxelShape> GETTER;
    public static int WIDTH, HEIGHT, LENGTH;
    public static BlockPos MASTER_POS;
    public static BlockPos TRIGGER_POS;
    public static BlockPos CLIENT_OFFSET;
    public static float MANUAL_SCALE;

    static {
        int[] dims = GenericShape.loadDimensions("advanced_coke_oven", "stone");
        WIDTH = dims[0] + DATA.padShape[0];
        HEIGHT = dims[1] + DATA.padShape[1];
        LENGTH = dims[2] + DATA.padShape[2];
        ITLib.IT_LOGGER.info("Loaded dimensions for advanced_coke_oven: W={}, H={}, L={}", WIDTH, HEIGHT, LENGTH);
        if (WIDTH <= 0 || HEIGHT <= 0 || LENGTH <= 0) {
            GETTER = FullblockShape.GETTER;
            WIDTH = HEIGHT = LENGTH = 0;
            if (DATA.shapeAABB == null || !DATA.shapeAABB.isEmpty()) {
                ITLib.IT_LOGGER.error("Invalid dimensions loaded for advanced_coke_oven multiblock.");
            }
        } else {
            int num = WIDTH * HEIGHT * LENGTH;
            if (DATA.shapeAABB == null) {
                ITLib.IT_LOGGER.error("Failed to load shapes for advanced_coke_oven multiblock. (shapeAABB null)");
                GETTER = FullblockShape.GETTER;
            } else if (DATA.shapeAABB.isEmpty()) {
                ITLib.IT_LOGGER.info("Using full block shape for advanced_coke_oven.");
                GETTER = FullblockShape.GETTER;
            } else {
                List<List<AABB>> shapes = GenericShape.loadShapes(DATA, num);
                if (shapes == null) {
                    ITLib.IT_LOGGER.error("Failed to load shapes for advanced_coke_oven multiblock.");
                    GETTER = FullblockShape.GETTER;
                } else {
                    boolean allFull = !shapes.isEmpty() && shapes.stream().allMatch(list -> list.size() == 1 && list.get(0).equals(GenericShape.FULL_BLOCK));
                    if (allFull) { GETTER = FullblockShape.GETTER; }
                    else { GETTER = new GenericShape.JsonShape(WIDTH, HEIGHT, LENGTH, shapes); }
                }
            }
        }
        MANUAL_SCALE = DATA.manualScale;
        if (DATA.pointsOfInterest != null) {
            for (PoIJSONSchema poi : DATA.pointsOfInterest) {
                switch (poi.name) {
                    case "master" -> MASTER_POS = new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2]);
                    case "trigger" -> TRIGGER_POS = new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2]);
                    case "client_offset" -> CLIENT_OFFSET = new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2]);
                }
            }
        }
    }
}
