package mctmods.immersivetechnology.core.util.multiblock;

import com.google.gson.JsonArray;

public class MultiblockData {
    public JsonArray shapeAABB;
    public PoIJSONSchema[] pointsOfInterest;
    public int[] padShape = new int[3];
    public float manualScale;
}
