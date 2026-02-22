package mctmods.immersivetechnology.client.models.split.geometry;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.EnumMap;
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
        Map<ITEpsilonMath.Sign, List<ITPolygon<Texture>>> splitFaces = new EnumMap<>(ITEpsilonMath.Sign.class);
        for (ITPolygon<Texture> f : this.getFaces()) {
            Map<ITEpsilonMath.Sign, ITPolygon<Texture>> splitResult = f.splitAlong(p);
            for (Map.Entry<ITEpsilonMath.Sign, ITPolygon<Texture>> e : splitResult.entrySet()) {
                splitFaces.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).add(e.getValue());
            }
        }
        return splitFaces.entrySet().stream().map(e -> Map.entry(e.getKey(), new ITGroup<>(e.getValue())));
    }

    public ITGroup<Texture> merge(ITGroup<Texture> other) {
        return new ITGroup<>(ImmutableList.<ITPolygon<Texture>>builder()
                .addAll(this.getFaces())
                .addAll(other.getFaces())
                .build());
    }

    public ITGroup<Texture> translate(int axis, double amount) {
        return new ITGroup<>(this.faces.stream().map(p -> p.translate(axis, amount)).toList());
    }

    public ITGroup<Texture> translate(ITVec3d offset) {
        return new ITGroup<>(this.faces.stream().map(p -> p.translate(offset)).toList());
    }
}
