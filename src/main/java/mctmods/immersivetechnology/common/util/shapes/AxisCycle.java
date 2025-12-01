package mctmods.immersivetechnology.common.util.shapes;

import mctmods.immersivetechnology.common.util.ITMth;
import net.minecraft.util.EnumFacing;

public enum AxisCycle {
    NONE {
        public int cycle(int p_121810_, int p_121811_, int p_121812_, EnumFacing.Axis p_121813_) {
            return ITMth.choose(p_121813_, p_121810_, p_121811_, p_121812_);
        }
        public EnumFacing.Axis cycle(EnumFacing.Axis p_121815_) {
            return p_121815_;
        }
        public AxisCycle inverse() {
            return this;
        }
    },
    FORWARD {
        public int cycle(int p_121821_, int p_121822_, int p_121823_, EnumFacing.Axis p_121824_) {
            return ITMth.choose(p_121824_, p_121823_, p_121821_, p_121822_);
        }
        public EnumFacing.Axis cycle(EnumFacing.Axis p_121826_) {
            return AXIS_VALUES[(p_121826_.ordinal() + 1) % 3];
        }
        public AxisCycle inverse() {
            return BACKWARD;
        }
    },
    BACKWARD {
        public int cycle(int p_121832_, int p_121833_, int p_121834_, EnumFacing.Axis p_121835_) {
            return ITMth.choose(p_121835_, p_121833_, p_121834_, p_121832_);
        }
        public EnumFacing.Axis cycle(EnumFacing.Axis p_121837_) {
            return AXIS_VALUES[(p_121837_.ordinal() + 2) % 3];
        }
        public AxisCycle inverse() {
            return FORWARD;
        }
    };

    public static final EnumFacing.Axis[] AXIS_VALUES = EnumFacing.Axis.values();
    public static final AxisCycle[] VALUES = values();

    public abstract int cycle(int pX, int pY, int pZ, EnumFacing.Axis pAxis);

    public abstract EnumFacing.Axis cycle(EnumFacing.Axis pAxis);

    public abstract AxisCycle inverse();

    public static AxisCycle between(EnumFacing.Axis pTo, EnumFacing.Axis pAxis2) {
        return VALUES[(pAxis2.ordinal() - pTo.ordinal() + 3) % 3];
    }
}
