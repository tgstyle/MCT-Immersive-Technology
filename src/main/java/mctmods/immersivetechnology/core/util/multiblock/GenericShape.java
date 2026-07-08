package mctmods.immersivetechnology.core.util.multiblock;

import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class GenericShape implements Function<BlockPos, VoxelShape> {

    public static final AABB FULL_BLOCK = new AABB(0D, 0D, 0D, 1D, 1D, 1D);

    public static int[] loadDimensions(String multiblockName, String category) {
        String path = "/assets/" + ITLib.MODID + "/models/multiblock/" + category + "/obj/" + multiblockName + "/" + multiblockName + ".obj";
        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
        double minZ = Double.MAX_VALUE, maxZ = Double.MIN_VALUE;
        InputStream is = GenericShape.class.getResourceAsStream(path);
        boolean hasVertices = false;
        if (is == null) { ITLib.IT_LOGGER.error("OBJ file not found at resource path: {} for multiblock: {}", path, multiblockName); }
        else {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("v ")) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 4) {
                            double x = Double.parseDouble(parts[1]);
                            double y = Double.parseDouble(parts[2]);
                            double z = Double.parseDouble(parts[3]);
                            minX = Math.min(minX, x);
                            maxX = Math.max(maxX, x);
                            minY = Math.min(minY, y);
                            maxY = Math.max(maxY, y);
                            minZ = Math.min(minZ, z);
                            maxZ = Math.max(maxZ, z);
                            hasVertices = true;
                        }
                    }
                }
                if (!hasVertices) { ITLib.IT_LOGGER.warn("OBJ file loaded but no vertices ('v ') found in: {} for multiblock: {}", path, multiblockName); }
            } catch (Exception e) { ITLib.IT_LOGGER.error("Error reading OBJ file at {} for multiblock: {}", path, multiblockName, e); }
        }
        double dx = maxX - minX;
        double dy = maxY - minY;
        double dz = maxZ - minZ;
        if (dx <= 0 || dy <= 0 || dz <= 0 || !hasVertices) { return new int[]{0, 0, 0}; }
        else { return new int[]{computeDim(dx), computeDim(dy), computeDim(dz)}; }
    }

    private static int computeDim(double d) {
        double floor = Math.floor(d);
        if (d == floor) { return (int) floor; }
        else { return (int) Math.ceil(d); }
    }

    public static List<List<AABB>> loadShapes(MultiblockData data, int expectedNum) {
        if (data.shapeAABB == null) { return null; }
        List<List<AABB>> shapes = new ArrayList<>(expectedNum);
        for (JsonElement posElem : data.shapeAABB) {
            List<AABB> posShapes = new ArrayList<>();
            if (posElem.isJsonNull() || !posElem.isJsonArray()) { shapes.add(posShapes); continue; }
            JsonArray posArray = posElem.getAsJsonArray();
            if (posArray.isEmpty()) { posShapes.add(FULL_BLOCK); }
            for (JsonElement aabbElem : posArray) {
                if (!aabbElem.isJsonArray()) { continue; }
                JsonArray aabbArray = aabbElem.getAsJsonArray();
                if (aabbArray.size() != 6) { continue; }
                double[] vals = new double[6];
                for (int i = 0; i < 6; i++) { vals[i] = aabbArray.get(i).getAsDouble(); }
                posShapes.add(new AABB(vals[0], vals[1], vals[2], vals[3], vals[4], vals[5]));
            }
            shapes.add(posShapes);
        }
        if (shapes.size() != expectedNum) { return null; }
        return shapes;
    }

    private static VoxelShape toVoxelShape(AABB aabb) { return (aabb == null) ? Shapes.empty() : Shapes.create(aabb); }

    @Override
    public VoxelShape apply(BlockPos posInMultiblock) {
        List<AABB> list = getShape(posInMultiblock);
        if (list.isEmpty()) { return Shapes.empty(); }
        VoxelShape base = toVoxelShape(list.getFirst());
        if (list.size() > 1) {
            return list.subList(1, list.size()).stream()
                    .map(GenericShape::toVoxelShape)
                    .reduce(base, Shapes::or);
        }
        return base;
    }

    protected abstract List<AABB> getShape(BlockPos posInMultiblock);

    public static class JsonShape extends GenericShape {
        private final int WIDTH, HEIGHT, LENGTH;
        private final List<List<AABB>> SHAPES;

        public JsonShape(int width, int height, int length, List<List<AABB>> shapes) {
            this.WIDTH = width;
            this.HEIGHT = height;
            this.LENGTH = length;
            this.SHAPES = shapes;
        }

        @Override
        protected List<AABB> getShape(BlockPos posInMultiblock) {
            int x = posInMultiblock.getX();
            int y = posInMultiblock.getY();
            int z = posInMultiblock.getZ();
            if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT || z < 0 || z >= LENGTH) { return new ArrayList<>(); }
            int index = y * (WIDTH * LENGTH) + z * WIDTH + x;
            if (index < 0 || index >= SHAPES.size()) { return new ArrayList<>(); }
            return SHAPES.get(index);
        }
    }
}
