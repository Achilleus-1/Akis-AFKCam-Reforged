package com.ji.afkcinematic.cinematic;

import net.minecraft.world.phys.Vec3;

public interface CameraShot {
    public void start();

    public Vec3 updatePosition(float var1, float var2, float var3);

    public float updatePitch(float var1, float var2, float var3);

    public float updateYaw(float var1, float var2, float var3);

    default public float updateRoll(float progress, float speedMultiplier, float tickDelta) {
        return 0.0f;
    }

    default public String getId() {
        return this.getClass().getSimpleName();
    }
}
