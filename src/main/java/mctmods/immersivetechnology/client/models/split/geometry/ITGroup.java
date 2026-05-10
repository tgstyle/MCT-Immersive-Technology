package mctmods.immersivetechnology.client.models.split.geometry;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public record ITGroup<Texture>(List<ITPolygon<Texture>> faces) {

    public ITGroup(List<ITPolygon<Texture>> faces) {
        this.faces = ImmutableList.copyOf(faces);
    }

    public List<ITPolygon<Texture>> getFaces() {
        return this.faces;
    }

    public Stream<Map.Entry<ITEpsilonMath.Sign, ITGroup<Texture>>> split(ITPlane p) {
        Map<ITEpsilonMath.Sign, List<ITPolygon<Texture>>> splitFaces = new java.util.EnumMap<>(ITEpsilonMath.Sign.class);
        for (ITPolygon<Texture> f : this.getFaces()) {
            Map<ITEpsilonMath.Sign, ITPolygon<Texture>> splitResult = f.splitAlong(p);
            for (Map.Entry<ITEpsilonMath.Sign, ITPolygon<Texture>> e : splitResult.entrySet()) {
                splitFaces.computeIfAbsent(e.getKey(), k -> new ArrayList<>(4)).add(e.getValue());
            }
        }
        return splitFaces.entrySet().stream().map(e -> Map.entry(e.getKey(), new ITGroup<>(e.getValue())));
    }

    public ITGroup<Texture> merge(ITGroup<Texture> other) {
        ImmutableList.Builder<ITPolygon<Texture>> builder = ImmutableList.builderWithExpectedSize(this.faces.size() + other.faces.size());
        builder.addAll(this.faces);
        builder.addAll(other.faces);
        return new ITGroup<>(builder.build());
    }

    public ITGroup<Texture> translate(int axis, double amount) {
        return new ITGroup<>(this.faces.stream().map(p -> p.translate(axis, amount)).toList());
    }

    public ITGroup<Texture> translate(ITVec3d offset) {
        return new ITGroup<>(this.faces.stream().map(p -> p.translate(offset)).toList());
    }
}
