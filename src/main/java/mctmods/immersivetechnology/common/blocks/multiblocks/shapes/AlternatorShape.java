package mctmods.immersivetechnology.common.blocks.multiblocks.shapes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import mctmods.immersivetechnology.common.util.multiblock.GenericShape;
import mctmods.immersivetechnology.common.util.multiblock.MultiblockData;
import mctmods.immersivetechnology.common.util.multiblock.POIUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AlternatorShape extends GenericShape {
    public static final AlternatorShape GETTER = new AlternatorShape();
    public static final int WIDTH = 3;
    public static final int HEIGHT = 3;
    public static final int LENGTH = 4;
    private static final List<List<AABB>> SHAPES;

    static {
        MultiblockData data = POIUtils.loadMultiblockData("alternator");
        List<List<AABB>> shapes = new ArrayList<>(WIDTH * HEIGHT * LENGTH);
        if (data.shapeAABB != null) {
            for (JsonElement posElem : data.shapeAABB) {
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
        }
        SHAPES = shapes;
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
