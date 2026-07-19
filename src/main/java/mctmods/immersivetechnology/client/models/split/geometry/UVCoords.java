package mctmods.immersivetechnology.client.models.split.geometry;

public record UVCoords(double u, double v) {
    public static UVCoords interpolate(UVCoords a, UVCoords b, double lambda) {
        return new UVCoords(a.u() * lambda + b.u() * (1 - lambda), a.v() * lambda + b.v() * (1 - lambda));
    }
}
