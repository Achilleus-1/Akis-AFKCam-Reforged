package com.ji.afkcinematic.cinematic.shots;

import com.ji.afkcinematic.cinematic.AbstractCameraShot;
import com.ji.afkcinematic.cinematic.ShotRandomizer;
import net.minecraft.world.phys.Vec3;

public class OverShoulderShot
extends AbstractCameraShot {
    private float startAngle;

    @Override
    public void start() {
        this.startAngle = ShotRandomizer.getRandomStartAngle();
    }

    @Override
    public Vec3 updatePosition(float progress, float speedMultiplier, float tickDelta) {
        if (!this.isPlayerAvailable()) {
            return Vec3.ZERO;
        }
        float scaledProgress = progress * speedMultiplier;
        float currentAngle = this.startAngle + scaledProgress * 5.0f;
        float currentDist = 3.0f + scaledProgress * 1.5f;
        Vec3 offset = this.getCircularOffset(currentAngle, currentDist);
        return this.getPlayerPos(tickDelta).add(offset.x, 2.5, offset.z);
    }

    @Override
    public float updatePitch(float progress, float speedMultiplier, float tickDelta) {
        return 5.0f;
    }

    @Override
    public float updateYaw(float progress, float speedMultiplier, float tickDelta) {
        return this.startAngle - 45.0f;
    }
}
