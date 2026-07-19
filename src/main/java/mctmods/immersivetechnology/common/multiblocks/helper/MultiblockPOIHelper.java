package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import mctmods.immersivetechnology.core.util.multiblock.PoIJSONSchema;
import net.minecraft.core.BlockPos;
import com.google.common.collect.ImmutableList;

import java.util.List;

public final class MultiblockPOIHelper {

    private MultiblockPOIHelper() {}

    public static List<BlockPos> getPosList(List<PoIJSONSchema> rawPois, String name) { return rawPois.stream().filter(poi -> poi.name.equals(name)).map(poi -> new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2])).collect(ImmutableList.toImmutableList()); }

    public static RelativeBlockFace getFacing(List<PoIJSONSchema> rawPois, String name) {
        List<RelativeBlockFace> facings = rawPois.stream().filter(poi -> poi.name.equals(name)).flatMap(poi -> poi.relativeFaces.stream()).distinct().toList();
        if (facings.size() != 1) { throw new RuntimeException("Inconsistent facings for POI: " + name); }
        return facings.getFirst();
    }

    public static CapabilityPosition getCapabilityPosition(List<PoIJSONSchema> rawPois, String name) {
        List<BlockPos> positions = getPosList(rawPois, name);
        if (positions.isEmpty()) { throw new RuntimeException("No POI found for name: " + name); }
        RelativeBlockFace face = getFacing(rawPois, name);
        return new CapabilityPosition(positions.getFirst(), face);
    }

    public static List<CapabilityPosition> getCapabilityPositions(List<PoIJSONSchema> rawPois, String name) {
        List<CapabilityPosition> result = new java.util.ArrayList<>();
        for (PoIJSONSchema poi : rawPois) {
            if (poi.name.equals(name)) {
                BlockPos connPos = new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2]);
                for (RelativeBlockFace face : poi.relativeFaces) { result.add(new CapabilityPosition(connPos, face)); }
            }
        }
        if (result.isEmpty()) { throw new RuntimeException("No POI found for " + name); }
        return ImmutableList.copyOf(result);
    }
}
