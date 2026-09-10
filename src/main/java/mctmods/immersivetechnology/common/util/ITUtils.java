package mctmods.immersivetechnology.common.util;

import net.minecraftforge.fluids.IFluidTank;

public class ITUtils {
    public static IFluidTank[] emptyIFluidTankList = new IFluidTank[0];

    public static final int[] EMPTY_INT_ARRAY = new int[0];

    public static float remapRange(float inMin, float inMax, float outMin, float outMax, float value) { return outMin + ((value - inMin) / inMax) * (outMax - outMin); }
}
