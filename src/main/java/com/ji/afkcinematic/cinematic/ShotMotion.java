package com.ji.afkcinematic.cinematic;

public final class ShotMotion {
    private ShotMotion() {
    }

    public static float elapsedSeconds(float frameProgress, int durationSeconds) {
        float safeProgress = Float.isFinite(frameProgress) ? Math.max(0.0f, Math.min(1.0f, frameProgress)) : 0.0f;
        return safeProgress * (float)Math.max(0, durationSeconds);
    }

    public static float phase(float elapsedSeconds, float speedMultiplier, float travelSeconds) {
        return Math.min(1.0f, ShotMotion.travel(elapsedSeconds, speedMultiplier, travelSeconds));
    }

    public static float travel(float elapsedSeconds, float speedMultiplier, float travelSeconds) {
        if (!Float.isFinite(elapsedSeconds) || !Float.isFinite(speedMultiplier) || !Float.isFinite(travelSeconds) || travelSeconds <= 0.0f) {
            return 0.0f;
        }
        return Math.max(0.0f, elapsedSeconds * Math.max(0.0f, speedMultiplier) / travelSeconds);
    }
}
