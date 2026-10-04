package com.ji.afkcinematic.cinematic.shots;

import com.ji.afkcinematic.cinematic.AbstractCameraShot;
import com.ji.afkcinematic.cinematic.ShotRandomizer;
import net.minecraft.world.phys.Vec3;

public class LowOrbitShot
extends AbstractCameraShot {
    private float startAngle;
    private boolean clockwise;
    private static final float DISTANCE = 6.0f;
    private static final float HEIGHT = 2.0f;
    private static final float ORBIT_DEGREES = 50.0f;

    @Override
    public void start() {
        this.startAngle = ShotRandomizer.getRandomStartAngle();
        this.clockwise = ShotRandomizer.getRandomDirection();
    }

    @Override
    public Vec3 updatePosition(float progress, float speedMultiplier, float tickDelta) {
        if (!this.isPlayerAvailable()) {
            return Vec3.ZERO;
        }
        float currentAngle = this.startAngle + progress * 50.0f * speedMultiplier * (float)(this.clockwise ? 1 : -1);
        Vec3 offset = this.getCircularOffset(currentAngle, 6.0f);
        return this.getPlayerPos(tickDelta).add(offset.x, 2.0, offset.z);
    }

    @Override
    public float updatePitch(float progress, float speedMultiplier, float tickDelta) {
        return -5.0f;
    }

    @Override
    public float updateYaw(float progress, float speedMultiplier, float tickDelta) {
        float currentAngle = this.startAngle + progress * 50.0f * speedMultiplier * (float)(this.clockwise ? 1 : -1);
        return currentAngle + 90.0f;
    }
}
