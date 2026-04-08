package mctmods.immersivetechnology.common.multiblocks.metal.shapes;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import mctmods.immersivetechnology.common.util.ITLogger;
import mctmods.immersivetechnology.common.multiblocks.shapes.GenericShape;
import mctmods.immersivetechnology.common.util.multiblock.MultiblockJSONSchema;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MeltingCrucibleShape extends GenericShape {
    public static MeltingCrucibleShape GETTER = new MeltingCrucibleShape();
    public static int WIDTH, HEIGHT, LENGTH;
    public static BlockPos MASTER_GRID_POS;
    public static MultiblockJSONSchema DATA;
    private static final List<List<AxisAlignedBB>> SHAPES;

    static {
        List<List<AxisAlignedBB>> rawShapes = new ArrayList<>();
        String[] structure = new String[0];
        BlockPos masterPos = BlockPos.ORIGIN;
        MultiblockJSONSchema data;
        try {
            InputStream is = MeltingCrucibleShape.class.getResourceAsStream("/assets/immersivetech/multiblocks/melting_crucible.json");
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                data = new Gson().fromJson(reader, MultiblockJSONSchema.class);
                reader.close();
                if (data != null) {
                    DATA = data;
                    WIDTH = data.width;
                    HEIGHT = data.height;
                    LENGTH = data.length;
                    if (data.structure != null) {
                        structure = data.structure;
                    }
                    int totalPositions = WIDTH * HEIGHT * LENGTH;
                    for (int i = 0; i < totalPositions; i++) rawShapes.add(new ArrayList<>());
                    if (data.shapeAABB != null && data.shapeAABB.isJsonArray()) {
                        JsonArray shapeArray = data.shapeAABB.getAsJsonArray();
                        int idx = 0;
                        for (JsonElement posElem : shapeArray) {
                            if (idx >= rawShapes.size()) break;
                            List<AxisAlignedBB> posShapes = rawShapes.get(idx);
                            if (posElem.isJsonNull() || !posElem.isJsonArray()) {
                                idx++;
                                continue;
                            }
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
                    int structIdx = 0;
                    for (int y = 0; y < HEIGHT; y++) {
                        for (int z = 0; z < LENGTH; z++) {
                            if (structIdx >= structure.length) break;
                            String layer = structure[structIdx++];
                            if (layer.length() != WIDTH) continue;
                            for (int x = 0; x < WIDTH; x++) {
                                int posIdx = x + z * WIDTH + y * WIDTH * LENGTH;
                                if (posIdx >= rawShapes.size()) continue;
                                List<AxisAlignedBB> posShapes = rawShapes.get(posIdx);
                                char blockChar = layer.charAt(x);
                                if (blockChar != ' ' && posShapes.isEmpty()) {
                                    posShapes.add(new AxisAlignedBB(0,0,0,1,1,1));
                                }
                            }
                        }
                    }
                    masterPos = new BlockPos(data.master.x, data.master.y, data.master.z);
                }
                ITLogger.info("MeltingCrucibleShape loaded: SHAPES size=" + rawShapes.size() + ", master pos=" + masterPos);
            }
        } catch (Exception e) {
            ITLogger.error("Failed to load MeltingCrucibleShape: " + e.getMessage(), e);
        }
        SHAPES = rawShapes;
        MASTER_GRID_POS = masterPos;
        if(FMLCommonHandler.instance().getSide().isClient()) ITLogger.info("MeltingCrucibleShape loaded on client: SHAPES size=" + rawShapes.size());
    }

    public MeltingCrucibleShape() { super(WIDTH, HEIGHT, LENGTH, new int[]{0, 0, 0}); }

    @Override public List<AxisAlignedBB> getShape(BlockPos posInMultiblock) {
        int x = posInMultiblock.getX();
        int y = posInMultiblock.getY();
        int z = posInMultiblock.getZ();
        int index = x + z * WIDTH + y * WIDTH * LENGTH;
        if (index < 0 || index >= SHAPES.size()) return new ArrayList<>();
        return SHAPES.get(index);
    }
}
