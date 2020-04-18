package mctmods.immersivetechnology.common.util;

public interface IPipe {

    boolean hasCover();
    void toggleSide(int side);
    int[] getSideConfig();

}