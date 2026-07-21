package mctmods.immersivetechnology.common.util.multiblock;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import mctmods.immersivetechnology.common.util.ITLogger;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class ShapeData extends GenericShape {
    public final MultiblockJSONSchema data;
    public final BlockPos masterPos;
    private final List<List<AxisAlignedBB>> shapes;

    private ShapeData(int width, int height, int length, MultiblockJSONSchema data, BlockPos masterPos, List<List<AxisAlignedBB>> shapes) {
        super(width, height, length, null);
        this.data = data;
        this.masterPos = masterPos;
        this.shapes = shapes;
    }

    public static ShapeData load(String id) {
        MultiblockJSONSchema data = MultiblockUtils.Load("multiblocks/" + id + ".json");
        if (data == null) { ITLogger.error("Missing multiblock JSON: " + id); return new ShapeData(0, 0, 0, null, BlockPos.ORIGIN, new ArrayList<>()); }

        int width = data.width, height = data.height, length = data.length;
        List<List<AxisAlignedBB>> shapes = new ArrayList<>();
        for (int i = 0; i < width * height * length; i++) { shapes.add(new ArrayList<>()); }

        if (data.shapeAABB != null) {
            int idx = 0;
            for (JsonElement posElem : data.shapeAABB) {
                if (idx >= shapes.size()) { break; }
                if (posElem.isJsonArray()) {
                    for (JsonElement aabbElem : posElem.getAsJsonArray()) {
                        if (!aabbElem.isJsonArray()) { continue; }
                        JsonArray box = aabbElem.getAsJsonArray();
                        if (box.size() == 6) { shapes.get(idx).add(new AxisAlignedBB(box.get(0).getAsDouble(), box.get(1).getAsDouble(), box.get(2).getAsDouble(), box.get(3).getAsDouble(), box.get(4).getAsDouble(), box.get(5).getAsDouble())); }
                    }
                }
                idx++;
            }
        }

        String[] structure = data.structure != null ? data.structure : new String[0];
        int structIdx = 0;
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                if (structIdx >= structure.length) { break; }
                String layer = structure[structIdx++];
                if (layer.length() != width) { continue; }
                for (int x = 0; x < width; x++) {
                    int posIdx = x + z * width + y * width * length;
                    if (posIdx >= shapes.size()) { continue; }
                    List<AxisAlignedBB> posShapes = shapes.get(posIdx);
                    if (layer.charAt(x) != ' ' && posShapes.isEmpty()) { posShapes.add(new AxisAlignedBB(0, 0, 0, 1, 1, 1)); }
                }
            }
        }

        BlockPos masterPos = data.master != null ? new BlockPos(data.master.x, data.master.y, data.master.z) : BlockPos.ORIGIN;
        ITLogger.info(id + " shape loaded: SHAPES size=" + shapes.size() + ", master pos=" + masterPos);
        return new ShapeData(width, height, length, data, masterPos, shapes);
    }

    @Override public List<AxisAlignedBB> getShape(BlockPos posInMultiblock) {
        int index = posInMultiblock.getX() + posInMultiblock.getZ() * width + posInMultiblock.getY() * width * length;
        return (index >= 0 && index < shapes.size()) ? shapes.get(index) : new ArrayList<>();
    }
}
