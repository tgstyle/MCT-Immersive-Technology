package mctmods.immersivetechnology.common.blocks.helper;

public interface ITITickableBase {
    default boolean canTickAny() {
        return true;
    }
}
