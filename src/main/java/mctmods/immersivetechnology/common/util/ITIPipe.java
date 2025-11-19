package mctmods.immersivetechnology.common.util;

@SuppressWarnings("unused")
public interface ITIPipe {
    boolean hasCover();
    void toggleSide(int side);
    int[] getSideConfig();
}
