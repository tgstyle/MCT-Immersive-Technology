package mctmods.immersivetechnology.common.util.multiblock;

import com.google.gson.JsonArray;

public class MultiblockJSONSchema {
    public String uniqueName;
    public int width, height, length;
    public MasterJSONSchema master;
    public PoIJSONSchema[] pointsOfInterest;
    public BlockJSONSchema[] palette;
    public String[] structure;
    public JsonArray shapeAABB;
}
