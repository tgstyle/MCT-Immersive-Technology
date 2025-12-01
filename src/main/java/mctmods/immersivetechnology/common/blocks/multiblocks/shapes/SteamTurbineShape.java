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

public class SteamTurbineShape extends GenericShape {
    public static final SteamTurbineShape GETTER = new SteamTurbineShape();
    public static final int WIDTH = 3;
    public static final int HEIGHT = 4;
    public static final int LENGTH = 10;
    public static final BlockPos MASTER_GRID_POS;

    static {
        List<List<AxisAlignedBB>> rawShapes = new ArrayList<>(WIDTH * HEIGHT * LENGTH);
        BlockPos masterPos = BlockPos.ORIGIN;
        try {
            InputStream is = SteamTurbineShape.class.getResourceAsStream("/assets/immersivetech/multiblocks/steam_turbine.json");
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                MultiblockJSONSchema data = new Gson().fromJson(reader, MultiblockJSONSchema.class);
                reader.close();
                masterPos = new BlockPos(data.master.x, data.master.y, data.master.z);
                for (JsonElement posElem : data.shapeAABB) {
                    List<AxisAlignedBB> posShapes = new ArrayList<>();
                    if (posElem.isJsonNull() || !posElem.isJsonArray()) { rawShapes.add(posShapes); continue; }
                    JsonArray posArray = posElem.getAsJsonArray();
                    for (JsonElement aabbElem : posArray) {
                        JsonArray aabbArray = aabbElem.getAsJsonArray();
                        double[] vals = new double[6];
                        for (int i = 0; i < 6; i++) { vals[i] = aabbArray.get(i).getAsDouble(); }
                        posShapes.add(new AxisAlignedBB(vals[0], vals[1], vals[2], vals[3], vals[4], vals[5]));
                    }
                    rawShapes.add(posShapes);
                }
                ITLogger.info("AlternatorShape loaded: SHAPES size=" + rawShapes.size() + ", master pos=" + masterPos);
            }
        } catch (Exception ignored) { }
        GenericShape.SHAPES = rawShapes;
        MASTER_GRID_POS = masterPos;
        if(FMLCommonHandler.instance().getSide().isClient()) ITLogger.info("AlternatorShape loaded on client: SHAPES size=" + rawShapes.size());
    }

    public SteamTurbineShape() { super(WIDTH, HEIGHT, LENGTH, new int[]{1, 2, 0}); }

    @Override public List<AxisAlignedBB> getShape(BlockPos posInMultiblock) { return super.getShape(posInMultiblock); }
}
