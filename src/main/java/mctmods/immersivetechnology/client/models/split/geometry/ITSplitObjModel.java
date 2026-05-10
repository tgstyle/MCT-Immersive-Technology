package mctmods.immersivetechnology.client.models.split.geometry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ITSplitObjModel<Texture> {
    private final Map<String, ITGroup<Texture>> faces;
    private final List<ITPolygon<Texture>> allFaces;
    private final double minX, maxX, minY, maxY, minZ, maxZ;

    public ITSplitObjModel(List<ITPolygon<Texture>> allFaces) {
        this(Map.of("", new ITGroup<>(allFaces)));
    }

    public ITSplitObjModel(Map<String, ITGroup<Texture>> faces) {
        this.faces = ImmutableMap.copyOf(faces);
        ImmutableList.Builder<ITPolygon<Texture>> builder = ImmutableList.builderWithExpectedSize(faces.values().stream().mapToInt(g -> g.getFaces().size()).sum());
        for (ITGroup<Texture> g : faces.values()) {
            builder.addAll(g.getFaces());
        }
        this.allFaces = builder.build();

        double mx = Double.POSITIVE_INFINITY, Mx = Double.NEGATIVE_INFINITY;
        double my = Double.POSITIVE_INFINITY, My = Double.NEGATIVE_INFINITY;
        double mz = Double.POSITIVE_INFINITY, Mz = Double.NEGATIVE_INFINITY;
        for (ITPolygon<Texture> p : this.allFaces) {
            for (ITVertex v : p.getPoints()) {
                ITVec3d pos = v.position();
                mx = Math.min(mx, pos.x());
                Mx = Math.max(Mx, pos.x());
                my = Math.min(my, pos.y());
                My = Math.max(My, pos.y());
                mz = Math.min(mz, pos.z());
                Mz = Math.max(Mz, pos.z());
            }
        }
        this.minX = mx; this.maxX = Mx;
        this.minY = my; this.maxY = My;
        this.minZ = mz; this.maxZ = Mz;
    }

    public static <Texture> ITSplitObjModel<Texture> union(ITSplitObjModel<Texture> a, ITSplitObjModel<Texture> b) {
        List<ITGroup<Texture>> groups = new ArrayList<>();
        if (a != null) groups.addAll(a.faces.values());
        if (b != null) groups.addAll(b.faces.values());
        return new ITSplitObjModel<>(groups.stream().flatMap(g -> g.getFaces().stream()).collect(Collectors.toList()));
    }

    public Map<ITEpsilonMath.Sign, ITSplitObjModel<Texture>> split(ITPlane splitPlane) {
        Map<ITEpsilonMath.Sign, ITGroup<Texture>> result = new java.util.EnumMap<>(ITEpsilonMath.Sign.class);
        for (ITGroup<Texture> g : this.faces.values()) {
            g.split(splitPlane).forEach(e -> result.merge(e.getKey(), e.getValue(), ITGroup::merge));
        }
        return result.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ITSplitObjModel<>(Map.of("", e.getValue()))));
    }

    public ITSplitObjModel<Texture> translate(int axis, double amount) {
        return new ITSplitObjModel<>(this.allFaces.stream().map(p -> p.translate(axis, amount)).collect(Collectors.toList()));
    }

    public ITSplitObjModel<Texture> translate(ITVec3d offset) {
        return new ITSplitObjModel<>(this.allFaces.stream().map(p -> p.translate(offset)).collect(Collectors.toList()));
    }

    public ITSplitObjModel<Texture> quadify() {
        return new ITSplitObjModel<>(this.allFaces.stream().flatMap(p -> p.quadify().stream()).collect(Collectors.toList()));
    }

    public boolean isEmpty() {
        return this.allFaces.isEmpty();
    }

    public List<ITPolygon<Texture>> getFaces() {
        return this.allFaces;
    }

    public double getMinX() { return minX; }
    public double getMaxX() { return maxX; }
    public double getMinY() { return minY; }
    public double getMaxY() { return maxY; }
    public double getMinZ() { return minZ; }
    public double getMaxZ() { return maxZ; }

    public int getCenterX() { return (int) Math.round((minX + maxX) * 0.5); }
    public int getCenterY() { return (int) Math.round((minY + maxY) * 0.5); }
    public int getCenterZ() { return (int) Math.round((minZ + maxZ) * 0.5); }
}