package mctmods.immersivetechnology.common.blocks.multiblocks.shapes;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import mctmods.immersivetechnology.common.util.ITLogger;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.helper.GenericShape;
import mctmods.immersivetechnology.common.util.multiblock.MultiblockJSONSchema;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AlternatorShape extends GenericShape {
    public static final AlternatorShape GETTER = new AlternatorShape();
    public static int WIDTH, HEIGHT, LENGTH;
    public static BlockPos MASTER_GRID_POS;
    public static MultiblockJSONSchema DATA;
    private static final List<List<AxisAlignedBB>> SHAPES;

    static {
        List<List<AxisAlignedBB>> rawShapes = new ArrayList<>();
        BlockPos masterPos = BlockPos.ORIGIN;
        MultiblockJSONSchema data;
        try {
            InputStream is = AlternatorShape.class.getResourceAsStream("/assets/immersivetech/multiblocks/alternator.json");
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                data = new Gson().fromJson(reader, MultiblockJSONSchema.class);
                reader.close();
                if (data != null) {
                    DATA = data;
                    WIDTH = data.width;
                    HEIGHT = data.height;
                    LENGTH = data.length;
                    for (int i = 0; i < WIDTH * HEIGHT * LENGTH; i++) rawShapes.add(new ArrayList<>());
                    if (data.shapeAABB != null && data.shapeAABB.isJsonArray()) {
                        JsonArray shapeArray = data.shapeAABB.getAsJsonArray();
                        int idx = 0;
                        for (JsonElement posElem : shapeArray) {
                            if (idx >= rawShapes.size()) break;
                            List<AxisAlignedBB> posShapes = rawShapes.get(idx);
                            if (posElem.isJsonNull() || !posElem.isJsonArray()) { idx++; continue; }
                            JsonArray posArray = posElem.getAsJsonArray();
                            for (JsonElement aabbElem : posArray) {
                                if (!aabbElem.isJsonArray()) continue;
                                JsonArray aabbArray = aabbElem.getAsJsonArray();
                                if (aabbArray.size() != 6) continue;
                                double[] vals = {0,0,0,0,0,0};
                                boolean valid = true;
                                for (int j = 0; j < 6; j++) {
                                    try { vals[j] = aabbArray.get(j).getAsDouble(); } catch (Exception e) { valid = false; break; }
                                }
                                if (valid) posShapes.add(new AxisAlignedBB(vals[0], vals[1], vals[2], vals[3], vals[4], vals[5]));
                            }
                            idx++;
                        }
                    }
                    masterPos = new BlockPos(data.master.x, data.master.y, data.master.z);
                }
                ITLogger.info("AlternatorShape loaded: SHAPES size=" + rawShapes.size() + ", master pos=" + masterPos);
            }
        } catch (Exception e) {
            ITLogger.error("Failed to load AlternatorShape: " + e.getMessage(), e);
        }
        SHAPES = rawShapes;
        MASTER_GRID_POS = masterPos;
        if(FMLCommonHandler.instance().getSide().isClient()) ITLogger.info("AlternatorShape loaded on client: SHAPES size=" + rawShapes.size());
    }

    public AlternatorShape() { super(WIDTH, HEIGHT, LENGTH, new int[]{0, 1, 0}); }

    @Override public List<AxisAlignedBB> getShape(BlockPos posInMultiblock) {
        int x = posInMultiblock.getX();
        int y = posInMultiblock.getY();
        int z = posInMultiblock.getZ();
        int index = x + z * WIDTH + y * WIDTH * LENGTH;
        if (index < 0 || index >= SHAPES.size()) return new ArrayList<>();
        return SHAPES.get(index);
    }
}
