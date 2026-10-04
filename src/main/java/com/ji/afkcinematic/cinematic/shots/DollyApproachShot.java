package com.ji.afkcinematic.cinematic.shots;

import com.ji.afkcinematic.cinematic.AbstractCameraShot;
import com.ji.afkcinematic.cinematic.ShotRandomizer;
import net.minecraft.world.phys.Vec3;

public class DollyApproachShot
extends AbstractCameraShot {
    private float startAngle;
    private static final float START_DIST = 8.0f;
    private static final float END_DIST = 3.0f;
    private static final float HEIGHT = 3.0f;

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
        float currentDist = this.lerp(scaledProgress, 8.0f, 3.0f);
        Vec3 offset = this.getCircularOffset(this.startAngle, currentDist);
        return this.getPlayerPos(tickDelta).add(offset.x, 3.0, offset.z);
    }

    @Override
    public float updatePitch(float progress, float speedMultiplier, float tickDelta) {
        return 10.0f;
    }

    @Override
    public float updateYaw(float progress, float speedMultiplier, float tickDelta) {
        return this.startAngle + 90.0f + 45.0f;
    }
}
