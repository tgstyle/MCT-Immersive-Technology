package mctmods.immersivetechnology.common.util.shapes;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class NonOverlappingMerger extends AbstractDoubleList implements IndexMerger {
    private final DoubleList lower;
    private final DoubleList upper;
    private final boolean swap;

    protected NonOverlappingMerger(DoubleList lower, DoubleList upper, boolean swap) {
        this.lower = lower;
        this.upper = upper;
        this.swap = swap;
    }

    public int size() { return this.lower.size() + this.upper.size(); }

    public void forMergedIndexes(IndexConsumer consumer) {
        if (this.swap) {this.forNonSwappedIndexes((p1, p2, p3) -> consumer.merge(p2, p1, p3));} else {this.forNonSwappedIndexes(consumer);}
    }

    private void forNonSwappedIndexes(IndexConsumer consumer) {
        int lowerSize = this.lower.size();
        for (int j = 0; j < lowerSize; j++) { if (!consumer.merge(j, -1, j)) { return; } }
        int upperSize = this.upper.size() - 1;
        for (int k = 0; k < upperSize; k++) { if (!consumer.merge(lowerSize - 1, k, lowerSize + k)) { return; } }
    }

    public double getDouble(int index) { return index < this.lower.size() ? this.lower.getDouble(index) : this.upper.getDouble(index - this.lower.size()); }

    public DoubleList getList() { return this; }
}
