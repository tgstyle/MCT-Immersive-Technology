package mctmods.immersivetechnology.common.multiblocks;

import mctmods.immersivetechnology.core.lib.Reference;

import com.immersiveconvergence.api.multiblock.MultiblockData;
import com.immersiveconvergence.api.multiblock.MultiblockDataLoader;
import com.immersiveconvergence.api.multiblock.ShapeData;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ITShapes {
    private static final Map<String, ShapeData> SHAPES = new ConcurrentHashMap<>();

    private ITShapes() {}

    public static ShapeData get(String name) { return SHAPES.computeIfAbsent(name, id -> ShapeData.load(Reference.class, Reference.MODID, id)); }

    public static MultiblockData data(String name) { return MultiblockDataLoader.loadMultiblockData(Reference.class, Reference.MODID, name); }
}
