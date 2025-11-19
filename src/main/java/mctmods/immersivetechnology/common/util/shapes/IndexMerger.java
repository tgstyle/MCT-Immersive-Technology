package mctmods.immersivetechnology.common.util.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;

interface IndexMerger {
    DoubleList getList();

    void forMergedIndexes(IndexConsumer consumer);

    int size();

    interface IndexConsumer {
        boolean merge(int firstValue, int secondValue, int thirdValue);
    }
}
