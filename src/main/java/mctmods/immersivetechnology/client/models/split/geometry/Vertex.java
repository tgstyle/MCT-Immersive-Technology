package mctmods.immersivetechnology.client.models.split.geometry;

public record Vertex(Vec3d position, Vec3d normal, UVCoords uv) {
    public static Vertex interpolate(Vertex a, Vertex b, double lambda) { return new Vertex(a.position().scale(lambda).add(b.position().scale(1.0 - lambda)), a.normal().scale(lambda).add(b.normal().scale(1.0 - lambda)), UVCoords.interpolate(a.uv(), b.uv(), lambda)); }

    public Vertex translate(int axis, double amount) {
        double[] offsetData = new double[3];
        offsetData[axis] = amount;
        return this.translate(new Vec3d(offsetData));
    }

    public Vertex translate(Vec3d offset) {
        return new Vertex(this.position.add(offset), this.normal, this.uv);
    }
}
