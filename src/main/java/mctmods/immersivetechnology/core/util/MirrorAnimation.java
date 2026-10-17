package mctmods.immersivetechnology.core.util;

import net.minecraft.util.Mth;

public class MirrorAnimation {
    public static final int TO_IDLE = -1;
    public static final int IDLE = -4;
    public static final int ROTATE = 0;
    public static final int TILT = 1;
    public static final int TRACK = 2;
    private static final int TWEEN_TICKS = 60;
    public float supportRotation;
    public float tilt;
    public float baseRotation;
    public int phase;
    private int ticks;
    private float startRotation;
    private float deltaRotation;
    private float startTilt;
    private float deltaTilt;

    public MirrorAnimation(int phase) { this.phase = phase; }

    public boolean tweening() { return ticks > 0; }

    public void startTween(float rotationDelta, float tiltDelta, int newPhase) {
        startRotation = supportRotation;
        deltaRotation = rotationDelta;
        startTilt = tilt;
        deltaTilt = tiltDelta;
        ticks = TWEEN_TICKS;
        phase = newPhase;
    }

    public void stepTween() {
        float progress = (TWEEN_TICKS - ticks) / (float) TWEEN_TICKS;
        float eased = Mth.cos(progress * Mth.PI) * -0.5f + 0.5f;
        supportRotation = startRotation + deltaRotation * eased;
        tilt = startTilt + deltaTilt * eased;
        ticks--;
    }

    public void stop() { ticks = 0; }

    public void spin() {
        baseRotation = SunTracking.wrap360(baseRotation + 0.5f);
        supportRotation = baseRotation;
        tilt = 0;
    }

    public void track(boolean active, float targetRotation, float targetTilt) {
        if (active && phase == IDLE) { startTween(SunTracking.shortestTurn(supportRotation, targetRotation), 0, ROTATE); }
        else if (!active && phase != IDLE) {
            baseRotation = SunTracking.wrap360(supportRotation);
            startTween(0, -tilt, IDLE);
        }
        if (tweening()) {
            stepTween();
            if (!tweening()) {
                if (phase == ROTATE) { startTween(SunTracking.shortestTurn(supportRotation, targetRotation), targetTilt - tilt, TILT); }
                else if (phase == TILT) { phase = TRACK; }
            }
        }
        else if (phase == TRACK) {
            supportRotation = targetRotation;
            tilt = targetTilt;
        }
        else if (phase == IDLE) { spin(); }
    }
}
