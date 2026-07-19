package mctmods.immersivetechnology.client.models.split.geometry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SplitObjModel<Texture> {
    private final Map<String, Group<Texture>> faces;
    private final List<Polygon<Texture>> allFaces;
    private final double minX, maxX, minY, maxY, minZ, maxZ;

    public SplitObjModel(List<Polygon<Texture>> allFaces) {
        this(Map.of("", new Group<>(allFaces)));
    }

    public SplitObjModel(Map<String, Group<Texture>> faces) {
        this.faces = ImmutableMap.copyOf(faces);
        ImmutableList.Builder<Polygon<Texture>> builder = ImmutableList.builderWithExpectedSize(faces.values().stream().mapToInt(g -> g.getFaces().size()).sum());
        for (Group<Texture> g : faces.values()) { builder.addAll(g.getFaces()); }
        this.allFaces = builder.build();

        double mx = Double.POSITIVE_INFINITY, Mx = Double.NEGATIVE_INFINITY;
        double my = Double.POSITIVE_INFINITY, My = Double.NEGATIVE_INFINITY;
        double mz = Double.POSITIVE_INFINITY, Mz = Double.NEGATIVE_INFINITY;
        for (Polygon<Texture> p : this.allFaces) {
            for (Vertex v : p.getPoints()) {
                Vec3d pos = v.position();
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

    public static <Texture> SplitObjModel<Texture> union(SplitObjModel<Texture> a, SplitObjModel<Texture> b) {
        List<Group<Texture>> groups = new ArrayList<>();
        if (a != null) groups.addAll(a.faces.values());
        if (b != null) groups.addAll(b.faces.values());
        return new SplitObjModel<>(groups.stream().flatMap(g -> g.getFaces().stream()).collect(Collectors.toList()));
    }

    public Map<EpsilonMath.Sign, SplitObjModel<Texture>> split(ModPlane splitPlane) {
        Map<EpsilonMath.Sign, Group<Texture>> result = new java.util.EnumMap<>(EpsilonMath.Sign.class);
        for (Group<Texture> g : this.faces.values()) { g.split(splitPlane).forEach(e -> result.merge(e.getKey(), e.getValue(), Group::merge)); }
        return result.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new SplitObjModel<>(Map.of("", e.getValue()))));
    }

    public SplitObjModel<Texture> translate(int axis, double amount) {
        return new SplitObjModel<>(this.allFaces.stream().map(p -> p.translate(axis, amount)).collect(Collectors.toList()));
    }

    public SplitObjModel<Texture> translate(Vec3d offset) {
        return new SplitObjModel<>(this.allFaces.stream().map(p -> p.translate(offset)).collect(Collectors.toList()));
    }

    public SplitObjModel<Texture> quadify() {
        return new SplitObjModel<>(this.allFaces.stream().flatMap(p -> p.quadify().stream()).collect(Collectors.toList()));
    }

    public boolean isEmpty() {
        return this.allFaces.isEmpty();
    }

    public List<Polygon<Texture>> getFaces() {
        return this.allFaces;
    }

    public double getMinX() { return minX; }
    public double getMaxX() { return maxX; }
    public double getMinY() { return minY; }
    public double getMaxY() { return maxY; }
    public double getMinZ() { return minZ; }
    public double getMaxZ() { return maxZ; }
}
