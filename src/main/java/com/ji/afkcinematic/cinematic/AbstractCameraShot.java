package com.ji.afkcinematic.cinematic;

import com.ji.afkcinematic.cinematic.CameraShot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractCameraShot
implements CameraShot {
    protected final Minecraft client = Minecraft.getInstance();

    @Override
    public void start() {
    }

    protected Vec3 getPlayerPos(float tickDelta) {
        LocalPlayer player = this.client.player;
        return player != null ? player.getPosition(tickDelta) : Vec3.ZERO;
    }

    protected boolean isPlayerAvailable() {
        return this.client.player != null;
    }

    protected float lerp(float progress, float start, float end) {
        return Mth.lerp((float)progress, (float)start, (float)end);
    }

    protected Vec3 getCircularOffset(float angleDegrees, float distance) {
        float rads = angleDegrees * ((float)Math.PI / 180);
        return new Vec3((double)(Mth.cos((float)rads) * distance), 0.0, (double)(Mth.sin((float)rads) * distance));
    }

    @Override
    public abstract Vec3 updatePosition(float var1, float var2, float var3);

    @Override
    public abstract float updatePitch(float var1, float var2, float var3);

    @Override
    public abstract float updateYaw(float var1, float var2, float var3);
}
