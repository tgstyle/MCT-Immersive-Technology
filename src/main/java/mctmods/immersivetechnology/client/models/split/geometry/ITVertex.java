package mctmods.immersivetechnology.client.models.split.geometry;

public record ITVertex(ITVec3d position, ITVec3d normal, ITUVCoords uv) {
    public static ITVertex interpolate(ITVertex a, ITVertex b, double lambda) { return new ITVertex(a.position().scale(lambda).add(b.position().scale(1.0 - lambda)), a.normal().scale(lambda).add(b.normal().scale(1.0 - lambda)), ITUVCoords.interpolate(a.uv(), b.uv(), lambda)); }

    public ITVertex translate(int axis, double amount) {
        double[] offsetData = new double[3];
        offsetData[axis] = amount;
        return this.translate(new ITVec3d(offsetData));
    }

    public ITVertex translate(ITVec3d offset) {
        return new ITVertex(this.position.add(offset), this.normal, this.uv);
    }
}
