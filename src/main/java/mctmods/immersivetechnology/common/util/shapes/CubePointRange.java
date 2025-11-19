package mctmods.immersivetechnology.common.util.shapes;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;

public class CubePointRange extends AbstractDoubleList {
    private final int parts;

    CubePointRange(int pParts) {
        if (pParts <= 0) { throw new IllegalArgumentException("Need at least 1 part"); }
        else { this.parts = pParts; }
    }

    public double getDouble(int pValue) { return (double)pValue / (double)this.parts; }

    public int size() { return this.parts + 1; }
}
