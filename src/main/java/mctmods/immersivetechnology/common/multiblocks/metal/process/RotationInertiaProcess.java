package mctmods.immersivetechnology.common.multiblocks.metal.process;

public class RotationInertiaProcess {
    private final int speedUpRate;
    private final int speedDownRate;

    public RotationInertiaProcess(double inertia, double torque, double friction) {
        this.speedUpRate = (int) Math.round(torque / inertia);
        this.speedDownRate = (int) Math.round(friction / inertia);
    }

    public int getSpeedUpRate() { return speedUpRate; }
    public int getSpeedDownRate() { return speedDownRate; }
}
