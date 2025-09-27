package mctmods.immersivetechnology.common.util.multiblock;

import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import com.google.gson.annotations.SerializedName;

public class PoIJSONSchema {
    public String name;
    public int x;
    public int y;
    public int z;
    @SerializedName("facing")
    public String facingString;
    public RelativeBlockFace relativeFace;
}
