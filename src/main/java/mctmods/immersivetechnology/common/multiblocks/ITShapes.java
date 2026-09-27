package mctmods.immersivetechnology.common.multiblocks;

import mctmods.immersivetechnology.core.lib.Reference;

import com.immersiveconvergence.api.multiblock.MultiblockData;
import com.immersiveconvergence.api.multiblock.MultiblockDataLoader;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.multiblock.ShapeData;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class ITShapes {
    private static final Map<String, ShapeData> SHAPES = new ConcurrentHashMap<>();

    static { MultiblockDataLoader.onReload(SHAPES::clear); }

    private ITShapes() {}

    public static ShapeData get(String name) { return SHAPES.computeIfAbsent(name, id -> ShapeData.load(Reference.class, Reference.MODID, id)); }

    public static void readPois(String name, Consumer<List<PoIJSONSchema>> reader) {
        Runnable read = () -> read(name, reader);
        read.run();
        MultiblockDataLoader.onReload(read);
    }

    private static void read(String name, Consumer<List<PoIJSONSchema>> reader) {
        try { reader.accept(pois(MultiblockDataLoader.loadMultiblockData(Reference.class, Reference.MODID, name))); }
        catch (RuntimeException e) {
            Reference.IT_LOGGER.error("Points of interest of {} are unusable, using the built-in copy", name, e);
            reader.accept(pois(MultiblockDataLoader.loadJarData(Reference.class, Reference.MODID, name)));
        }
    }

    private static List<PoIJSONSchema> pois(@Nullable MultiblockData data) { return data == null || data.pointsOfInterest == null ? List.of() : List.of(data.pointsOfInterest); }
}
