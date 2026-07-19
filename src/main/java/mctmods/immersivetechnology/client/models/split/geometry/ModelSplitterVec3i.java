package mctmods.immersivetechnology.client.models.split.geometry;

public record ModelSplitterVec3i(int x, int y, int z) {
    public int distanceSq(ModelSplitterVec3i other) {
        return this.subtract(other).lengthSq();
    }

    public ModelSplitterVec3i subtract(ModelSplitterVec3i other) {
        return new ModelSplitterVec3i(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public int lengthSq() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }
}
