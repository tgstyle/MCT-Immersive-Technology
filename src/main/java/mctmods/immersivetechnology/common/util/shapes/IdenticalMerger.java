package mctmods.immersivetechnology.common.util.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;

public class IdenticalMerger implements IndexMerger {
    private final DoubleList coords;

    public IdenticalMerger(DoubleList coords) { this.coords = coords; }

    public void forMergedIndexes(IndexConsumer consumer) {
        int size = this.coords.size() - 1;
        for (int j = 0; j < size; j++) { if (!consumer.merge(j, j, j)) { return; } }
    }

    public int size() { return this.coords.size(); }

    public DoubleList getList() { return this.coords; }
}
