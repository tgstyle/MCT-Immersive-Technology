package mctmods.immersivetechnology.common.blocks.multiblocks.shapes;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.helper.GenericShape;
import mctmods.immersivetechnology.common.util.multiblock.MultiblockJSONSchema;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AlternatorShape extends GenericShape {
    public static final AlternatorShape GETTER = new AlternatorShape();
    public static final int WIDTH = 3;
    public static final int HEIGHT = 3;
    public static final int LENGTH = 4;
    private static final List<List<AxisAlignedBB>> SHAPES;

    static {
        SHAPES = new ArrayList<>(WIDTH * HEIGHT * LENGTH);
        try {
            InputStream is = AlternatorShape.class.getResourceAsStream("/assets/immersivetechnology/multiblocks/alternator.json");
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                MultiblockJSONSchema data = new Gson().fromJson(reader, MultiblockJSONSchema.class);
                reader.close();
                for (JsonElement posElem : data.shapeAABB) {
                    List<AxisAlignedBB> posShapes = new ArrayList<>();
                    if (posElem.isJsonNull() || !posElem.isJsonArray()) { SHAPES.add(posShapes); continue; }
                    JsonArray posArray = posElem.getAsJsonArray();
                    for (JsonElement aabbElem : posArray) {
                        JsonArray aabbArray = aabbElem.getAsJsonArray();
                        double[] vals = new double[6];
                        for (int i = 0; i < 6; i++) { vals[i] = aabbArray.get(i).getAsDouble(); }
                        posShapes.add(new AxisAlignedBB(vals[0], vals[1], vals[2], vals[3], vals[4], vals[5]));
                    }
                    SHAPES.add(posShapes);
                }
            }
        } catch (Exception e) { /* log error */ }
    }

    @Override
    public List<AxisAlignedBB> getShape(BlockPos posInMultiblock) {
        int x = posInMultiblock.getX();
        int y = posInMultiblock.getY();
        int z = posInMultiblock.getZ();
        int index = y * (WIDTH * LENGTH) + z * WIDTH + x;
        return (index < SHAPES.size()) ? SHAPES.get(index) : new ArrayList<>();
    }
}
