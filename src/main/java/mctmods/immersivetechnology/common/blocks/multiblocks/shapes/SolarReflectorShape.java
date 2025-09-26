package mctmods.immersivetechnology.common.blocks.multiblocks.shapes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import mctmods.immersivetechnology.common.util.multiblock.GenericShape;
import mctmods.immersivetechnology.common.util.multiblock.MultiblockData;
import mctmods.immersivetechnology.common.util.multiblock.MultiblockDataLoader;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SolarReflectorShape extends GenericShape {
    public static final MultiblockData DATA = MultiblockDataLoader.loadMultiblockData("solar_reflector");
    public static final Function<BlockPos, VoxelShape> GETTER;
    public static int WIDTH, HEIGHT, LENGTH;

    private final List<List<AABB>> SHAPES;

    private SolarReflectorShape(List<List<AABB>> shapes) { this.SHAPES = shapes; }

    static {
        int[] dims = loadDimensions("solar_reflector", "metal");
        WIDTH = dims[0] + DATA.padShape[0];
        HEIGHT = dims[1] + DATA.padShape[1];
        LENGTH = dims[2] + DATA.padShape[2];
        ITLib.IT_LOGGER.info("Loaded dimensions for solar_reflector: W={}, H={}, L={}", WIDTH, HEIGHT, LENGTH);
        if (WIDTH <= 0 || HEIGHT <= 0 || LENGTH <= 0) {
            ITLib.IT_LOGGER.error("Invalid dimensions loaded for solar_reflector multiblock.");
            GETTER = FullblockShape.GETTER;
            WIDTH = HEIGHT = LENGTH = 0;
        } else {
            int num = WIDTH * HEIGHT * LENGTH;
            List<List<AABB>> shapes = new ArrayList<>(num);
            boolean error = false;
            if (DATA.shapeAABB == null) { error = true; }
            else if (!DATA.shapeAABB.isEmpty()) {
                for (JsonElement posElem : DATA.shapeAABB) {
                    List<AABB> posShapes = new ArrayList<>();
                    if (posElem.isJsonNull() || !posElem.isJsonArray()) { shapes.add(posShapes); continue; }
                    JsonArray posArray = posElem.getAsJsonArray();
                    for (JsonElement aabbElem : posArray) {
                        JsonArray aabbArray = aabbElem.getAsJsonArray();
                        double[] vals = new double[6];
                        for (int i = 0; i < 6; i++) { vals[i] = aabbArray.get(i).getAsDouble(); }
                        posShapes.add(new AABB(vals[0], vals[1], vals[2], vals[3], vals[4], vals[5]));
                    }
                    shapes.add(posShapes);
                }
                if (shapes.size() != num) { error = true; }
            }
            if (error) {
                ITLib.IT_LOGGER.error("Failed to load shapes for solar_reflector multiblock.");
                GETTER = FullblockShape.GETTER;
            } else {
                boolean allFull = shapes.isEmpty() || shapes.stream().allMatch(List::isEmpty);
                if (allFull) { GETTER = FullblockShape.GETTER; }
                else { GETTER = new SolarReflectorShape(shapes); }
            }
        }
    }

    @NotNull
    @Override
    protected List<AABB> getShape(BlockPos posInMultiblock) {
        int x = posInMultiblock.getX();
        int y = posInMultiblock.getY();
        int z = posInMultiblock.getZ();
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT || z < 0 || z >= LENGTH) { return new ArrayList<>(); }
        int index = y * (WIDTH * LENGTH) + z * WIDTH + x;
        if (index < SHAPES.size()) {
            List<AABB> shape = SHAPES.get(index);
            if (!shape.isEmpty()) { return shape; }
        }
        return List.of(new AABB(0D, 0D, 0D, 1D, 1D, 1D));
    }
}
