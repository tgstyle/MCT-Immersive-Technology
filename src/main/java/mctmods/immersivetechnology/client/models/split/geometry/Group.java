package mctmods.immersivetechnology.client.models.split.geometry;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public record Group<Texture>(List<Polygon<Texture>> faces) {
    public Group(List<Polygon<Texture>> faces) {
        this.faces = ImmutableList.copyOf(faces);
    }

    public List<Polygon<Texture>> getFaces() {
        return this.faces;
    }

    public Stream<Map.Entry<EpsilonMath.Sign, Group<Texture>>> split(ModPlane p) {
        Map<EpsilonMath.Sign, List<Polygon<Texture>>> splitFaces = new java.util.EnumMap<>(EpsilonMath.Sign.class);
        for (Polygon<Texture> f : this.getFaces()) {
            Map<EpsilonMath.Sign, Polygon<Texture>> splitResult = f.splitAlong(p);
            for (Map.Entry<EpsilonMath.Sign, Polygon<Texture>> e : splitResult.entrySet()) {
                splitFaces.computeIfAbsent(e.getKey(), k -> new ArrayList<>(4)).add(e.getValue());
            }
        }
        return splitFaces.entrySet().stream().map(e -> Map.entry(e.getKey(), new Group<>(e.getValue())));
    }

    public Group<Texture> merge(Group<Texture> other) {
        ImmutableList.Builder<Polygon<Texture>> builder = ImmutableList.builderWithExpectedSize(this.faces.size() + other.faces.size());
        builder.addAll(this.faces);
        builder.addAll(other.faces);
        return new Group<>(builder.build());
    }

    public Group<Texture> translate(int axis, double amount) { return new Group<>(this.faces.stream().map(p -> p.translate(axis, amount)).toList()); }

    public Group<Texture> translate(Vec3d offset) { return new Group<>(this.faces.stream().map(p -> p.translate(offset)).toList()); }
}
