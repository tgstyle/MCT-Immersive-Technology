package mctmods.immersivetechnology.common.util;

import java.util.function.IntPredicate;
import net.minecraft.util.EnumFacing;

public class ITMth {
    public static int binarySearch(int pMin, int pMax, IntPredicate pIsTargetBeforeOrAt) {
        int i = pMax - pMin;
        while (i > 0) {
            int j = i / 2;
            int k = pMin + j;
            if (pIsTargetBeforeOrAt.test(k)) {
                i = j;
            } else {
                pMin = k + 1;
                i -= j + 1;
            }
        }
        return pMin;
    }

    public static int clamp(int pValue, int pMin, int pMax) {
        return Math.max(pMin, Math.min(pValue, pMax));
    }

    public static double clamp(double pValue, double pMin, double pMax) {
        return Math.max(pMin, Math.min(pValue, pMax));
    }

    public static int floor(double pValue) {
        int i = (int)pValue;
        return pValue < (double)i ? i - 1 : i;
    }

    public static int gcd(int a, int b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            int t = b;
            b = a % b;
            a = t;
        }
        return a;
    }

    public static long lcm(int a, int b) {
        if (a == 0 || b == 0) return 0;
        return Math.abs((long) a * (long) b) / gcd(a, b);
    }

    public static boolean fuzzyEquals(double a, double b, double tolerance) {
        return Math.abs(a - b) < tolerance;
    }

    public static int choose(EnumFacing.Axis axis, int x, int y, int z) {
        switch (axis) {
            case X: return x;
            case Y: return y;
            case Z: return z;
            default: throw new IllegalStateException("Invalid axis: " + axis);
        }
    }
}
