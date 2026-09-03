package mctmods.immersivetechnology.common.multiblocks;

import com.immersiveconvergence.api.multiblock.ShapeData;

import mctmods.immersivetechnology.ImmersiveTechnology;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ITShapes {
    private static final Map<String, ShapeData> SHAPES = new ConcurrentHashMap<>();

    private ITShapes() {}

    public static ShapeData get(String name) { return SHAPES.computeIfAbsent(name, id -> ShapeData.load(ImmersiveTechnology.MODID, id)); }
}
