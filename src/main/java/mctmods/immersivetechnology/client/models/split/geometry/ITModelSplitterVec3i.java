package mctmods.immersivetechnology.client.models.split.geometry;

public record ITModelSplitterVec3i(int x, int y, int z) {
    public int distanceSq(ITModelSplitterVec3i other) {
        return this.subtract(other).lengthSq();
    }

    public ITModelSplitterVec3i subtract(ITModelSplitterVec3i other) {
        return new ITModelSplitterVec3i(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public int lengthSq() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }
}
