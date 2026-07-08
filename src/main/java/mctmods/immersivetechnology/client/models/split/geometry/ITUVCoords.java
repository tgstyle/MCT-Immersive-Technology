package mctmods.immersivetechnology.client.models.split.geometry;

public record ITUVCoords(double u, double v) {
    public static ITUVCoords interpolate(ITUVCoords a, ITUVCoords b, double lambda) { return new ITUVCoords(a.u() * lambda + b.u() * (1 - lambda), a.v() * lambda + b.v() * (1 - lambda)); }
}
