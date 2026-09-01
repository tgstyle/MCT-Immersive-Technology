package mctmods.immersivetechnology.common.multiblocks.metal.process;

public class RotationInertiaProcess {
    private final int speedUpRate;
    private final int speedDownRate;
    private final double inertia;
    private final double torque;
    private final double fixedFriction;
    private final double drag;

    public RotationInertiaProcess(double mass, double torque, double friction, int maxRpm) {
        this.inertia = mass;
        this.torque = torque;
        this.fixedFriction = friction;
        this.drag = Math.max(0, torque - fixedFriction) / maxRpm;
        this.speedUpRate = (int)Math.round(torque / inertia);
        this.speedDownRate = (int)Math.round((fixedFriction + drag * maxRpm) / inertia);
    }

    public int getSpeedUpRate() { return speedUpRate; }

    public int getSpeedDownRate() { return speedDownRate; }

    public double getAlpha(float ratio, int currentSpeed) {
        double opposing = fixedFriction + drag * currentSpeed;
        double netTorque = torque * ratio - opposing;
        return netTorque / inertia;
    }

    public int getDelta(float ratio, int currentSpeed) { return (int)Math.round(getAlpha(ratio, currentSpeed)); }
}
