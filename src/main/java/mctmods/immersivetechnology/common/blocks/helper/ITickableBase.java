package mctmods.immersivetechnology.common.blocks.helper;

public interface ITickableBase {
    default boolean canTickAny() {
        return true;
    }
}
