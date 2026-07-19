package mctmods.immersivetechnology.client.models.split.geometry;

import com.google.common.base.Preconditions;

public record Vec3d(double x, double y, double z) {
    public static final Vec3d ZERO = new Vec3d(0.0, 0.0, 0.0);

    public Vec3d {
        Preconditions.checkArgument(Double.isFinite(x));
        Preconditions.checkArgument(Double.isFinite(y));
        Preconditions.checkArgument(Double.isFinite(z));
    }

    public Vec3d(double[] coords) {
        this(coords[0], coords[1], coords[2]);
    }

    public Vec3d(ModelSplitterVec3i vec) {
        this(vec.x(), vec.y(), vec.z());
    }

    public double dotProduct(Vec3d other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public double get(int index) {
        return switch (index) {
            case 0 -> this.x;
            case 1 -> this.y;
            case 2 -> this.z;
            default -> throw new IllegalStateException("Unexpected index in Vec3d: " + index);
        };
    }

    public Vec3d normalize() {
        double length = this.length();
        return length < 1.0E-4 ? this : this.scale(1.0 / length);
    }

    public double length() {
        return Math.sqrt(this.lengthSquared());
    }

    public double lengthSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public Vec3d scale(double lambda) {
        return new Vec3d(this.x * lambda, this.y * lambda, this.z * lambda);
    }

    public Vec3d add(Vec3d other) {
        return new Vec3d(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vec3d subtract(Vec3d other) {
        return new Vec3d(this.x - other.x, this.y - other.y, this.z - other.z);
    }
}
