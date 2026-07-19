package mctmods.immersivetechnology.core.util.multiblock;

import mctmods.immersivetechnology.core.lib.ITLib;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class ShapeData {
    public final Function<BlockPos, VoxelShape> getter;
    public final int width, height, length;
    public final BlockPos masterPos, triggerPos, clientOffset;
    public final float manualScale;
    public final List<BlockPos> symmetricTriggerOffsets;

    private ShapeData(Function<BlockPos, VoxelShape> getter, int width, int height, int length, BlockPos masterPos, BlockPos triggerPos, BlockPos clientOffset, float manualScale, List<BlockPos> symmetricTriggerOffsets) {
        this.getter = getter;
        this.width = width;
        this.height = height;
        this.length = length;
        this.masterPos = masterPos;
        this.triggerPos = triggerPos;
        this.clientOffset = clientOffset;
        this.manualScale = manualScale;
        this.symmetricTriggerOffsets = symmetricTriggerOffsets;
    }

    public static ShapeData load(String id, String category) {
        MultiblockData data = MultiblockDataLoader.loadMultiblockData(id);
        int[] dims = GenericShape.loadDimensions(id, category);
        int width = dims[0] + data.padShape[0];
        int height = dims[1] + data.padShape[1];
        int length = dims[2] + data.padShape[2];
        ITLib.IT_LOGGER.info("Loaded dimensions for {}: W={}, H={}, L={}", id, width, height, length);

        Function<BlockPos, VoxelShape> getter;
        if (width <= 0 || height <= 0 || length <= 0) {
            getter = FullblockShape.GETTER;
            width = height = length = 0;
            if (data.shapeAABB == null || !data.shapeAABB.isEmpty()) { ITLib.IT_LOGGER.error("Invalid dimensions loaded for {} multiblock.", id); }
        } else {
            int num = width * height * length;
            if (data.shapeAABB == null) {
                ITLib.IT_LOGGER.error("Failed to load shapes for {} multiblock. (shapeAABB null)", id);
                getter = FullblockShape.GETTER;
            } else if (data.shapeAABB.isEmpty()) {
                ITLib.IT_LOGGER.info("Using full block shape for {}.", id);
                getter = FullblockShape.GETTER;
            } else {
                List<List<AABB>> shapes = GenericShape.loadShapes(data, num);
                if (shapes == null) {
                    ITLib.IT_LOGGER.error("Failed to load shapes for {} multiblock.", id);
                    getter = FullblockShape.GETTER;
                } else {
                    boolean allFull = !shapes.isEmpty() && shapes.stream().allMatch(list -> list.size() == 1 && list.getFirst().equals(GenericShape.FULL_BLOCK));
                    getter = allFull ? FullblockShape.GETTER : new GenericShape.JsonShape(width, height, length, shapes);
                }
            }
        }

        BlockPos masterPos = null, triggerPos = null, clientOffset = null;
        List<BlockPos> symmetricTriggers = new ArrayList<>();
        if (data.pointsOfInterest != null) {
            for (PoIJSONSchema poi : data.pointsOfInterest) {
                switch (poi.name) {
                    case "master" -> masterPos = new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2]);
                    case "trigger" -> triggerPos = new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2]);
                    case "client_offset" -> clientOffset = new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2]);
                    case "symmetric_trigger" -> symmetricTriggers.add(new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2]));
                }
            }
        }

        return new ShapeData(getter, width, height, length, masterPos, triggerPos, clientOffset, data.manualScale, List.copyOf(symmetricTriggers));
    }
}
