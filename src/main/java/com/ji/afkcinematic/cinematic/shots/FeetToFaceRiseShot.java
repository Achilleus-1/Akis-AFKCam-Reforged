package com.ji.afkcinematic.cinematic.shots;

import com.ji.afkcinematic.cinematic.AbstractCameraShot;
import com.ji.afkcinematic.cinematic.ShotRandomizer;
import net.minecraft.world.phys.Vec3;

public class FeetToFaceRiseShot
extends AbstractCameraShot {
    private float startAngle;
    private static final float START_DIST = 6.0f;
    private static final float END_DIST = 4.0f;
    private static final float START_HEIGHT = 0.5f;
    private static final float END_HEIGHT = 4.0f;

    @Override
    public void start() {
        this.startAngle = ShotRandomizer.getRandomStartAngle();
    }

    @Override
    public Vec3 updatePosition(float progress, float speedMultiplier, float tickDelta) {
        if (!this.isPlayerAvailable()) {
            return Vec3.ZERO;
        }
        float scaledProgress = Math.min(1.0f, progress * speedMultiplier);
        float currentDist = this.lerp(scaledProgress, 6.0f, 4.0f);
        float currentHeight = this.lerp(scaledProgress, 0.5f, 4.0f);
        Vec3 offset = this.getCircularOffset(this.startAngle, currentDist);
        return this.getPlayerPos(tickDelta).add(offset.x, (double)currentHeight, offset.z);
    }

    @Override
    public float updatePitch(float progress, float speedMultiplier, float tickDelta) {
        float scaledProgress = Math.min(1.0f, progress * speedMultiplier);
        return this.lerp(scaledProgress, -20.0f, 0.0f);
    }

    @Override
    public float updateYaw(float progress, float speedMultiplier, float tickDelta) {
        return this.startAngle + 90.0f;
    }
}
