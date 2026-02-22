package mctmods.immersivetechnology.client.models.split.geometry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.*;
import java.util.stream.Collectors;

public class ITSplitObjModel<Texture> {
    private final Map<String, ITGroup<Texture>> faces;
    private final List<ITPolygon<Texture>> allFaces;

    public ITSplitObjModel(List<ITPolygon<Texture>> allFaces) {
        this(Map.of("", new ITGroup<>(allFaces)));
    }

    public ITSplitObjModel(Map<String, ITGroup<Texture>> faces) {
        this.faces = ImmutableMap.copyOf(faces);
        ImmutableList.Builder<ITPolygon<Texture>> builder = ImmutableList.builder();
        for (ITGroup<Texture> g : faces.values()) {
            builder.addAll(g.getFaces());
        }
        this.allFaces = builder.build();
    }

    public static <Texture> ITSplitObjModel<Texture> union(ITSplitObjModel<Texture> a, ITSplitObjModel<Texture> b) {
        List<ITGroup<Texture>> groups = new ArrayList<>();
        if (a != null) groups.addAll(a.faces.values());
        if (b != null) groups.addAll(b.faces.values());
        return new ITSplitObjModel<>(groups.stream().flatMap(g -> g.getFaces().stream()).collect(Collectors.toList()));
    }

    public Map<ITEpsilonMath.Sign, ITSplitObjModel<Texture>> split(ITPlane splitPlane) {
        Map<ITEpsilonMath.Sign, ITGroup<Texture>> result = new EnumMap<>(ITEpsilonMath.Sign.class);
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
}
