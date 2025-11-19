package mctmods.immersivetechnology.common.util.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;

public class IndirectMerger implements IndexMerger {
    private static final DoubleList EMPTY = DoubleLists.unmodifiable(DoubleArrayList.wrap(new double[]{0.0D}));
    private final double[] result;
    private final int[] firstIndices;
    private final int[] secondIndices;
    private final int resultLength;

    public IndirectMerger(DoubleList pLower, DoubleList pUpper, boolean pExcludeUpper, boolean pExcludeLower) {
        double d0 = Double.NaN;
        int i = pLower.size();
        int j = pUpper.size();
        int k = i + j;
        this.result = new double[k];
        this.firstIndices = new int[k];
        this.secondIndices = new int[k];
        boolean flag = !pExcludeUpper;
        boolean flag1 = !pExcludeLower;
        int l = 0;
        int i1 = 0;
        int j1 = 0;

        while (true) {
            boolean flag2 = i1 >= i;
            boolean flag3 = j1 >= j;
            if (flag2 && flag3) {
                this.resultLength = Math.max(1, l);
                return;
            }

            boolean flag4 = !flag2 && (flag3 || pLower.getDouble(i1) < pUpper.getDouble(j1) + 1.0E-7D);
            if (flag4) {
                ++i1;
                if (flag && (j1 == 0 || flag3)) { continue; }
            } else {
                ++j1;
                if (flag1 && (i1 == 0 || flag2)) { continue; }
            }

            int k1 = i1 - 1;
            int l1 = j1 - 1;
            double d1 = flag4 ? pLower.getDouble(k1) : pUpper.getDouble(l1);
            if (!(d0 >= d1 - 1.0E-7D)) {
                this.firstIndices[l] = k1;
                this.secondIndices[l] = l1;
                this.result[l] = d1;
                ++l;
                d0 = d1;
            } else {
                this.firstIndices[l - 1] = k1;
                this.secondIndices[l - 1] = l1;
            }
        }
    }

    public void forMergedIndexes(IndexConsumer pConsumer) {
        int i = this.resultLength - 1;
        for (int j = 0; j < i; j++) { if (!pConsumer.merge(this.firstIndices[j], this.secondIndices[j], j)) { return; } }
    }

    public int size() { return this.resultLength; }

    public DoubleList getList() { return this.resultLength <= 1 ? EMPTY : DoubleArrayList.wrap(this.result, this.resultLength); }
}
