package mctmods.immersivetechnology.common.util.multiblock;

import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import com.google.gson.annotations.SerializedName;

public class PoIJSONSchema {
    public String name;
    public int[] pos;
    @SerializedName("facing")
    public String facingString;
    public RelativeBlockFace relativeFace;
    public int x;
}
