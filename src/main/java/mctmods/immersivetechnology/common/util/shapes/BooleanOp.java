package mctmods.immersivetechnology.common.util.shapes;

public interface BooleanOp {
    BooleanOp OR = (p_82705_, p_82706_) -> p_82705_ || p_82706_;

    boolean apply(boolean pPrimaryBool, boolean pSecondaryBool);
}
