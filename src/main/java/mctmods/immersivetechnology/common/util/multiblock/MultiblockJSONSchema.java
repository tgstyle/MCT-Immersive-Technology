package mctmods.immersivetechnology.common.util.multiblock;

public class MultiblockJSONSchema {

    public String uniqueName;
    public int width, height, length;
    public MasterJSONSchema master;
    public BlockJSONSchema[] palette;
    public String[] structure;
    public byte[][][] AABB;
    public PoIJSONSchema[] pointsOfInterest;

}
