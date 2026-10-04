package com.ji.afkcinematic.cinematic.shots;

import com.ji.afkcinematic.cinematic.AbstractCameraShot;
import com.ji.afkcinematic.cinematic.ShotMotion;
import net.minecraft.world.phys.Vec3;

public final class EnvironmentalShot
extends AbstractCameraShot {
    private static final float NOMINAL_TRAVEL_SECONDS = 12.0f;
    private final Preset preset;
    private float startAngle;

    public EnvironmentalShot(Preset preset) {
        this.preset = preset;
    }

    @Override
    public void start() {
        float playerYaw = this.client.player != null ? this.client.player.getYRot() : 0.0f;
        this.startAngle = playerYaw + this.preset.angleOffset;
    }

    private float travel(float elapsedSeconds, float speedMultiplier) {
        return ShotMotion.travel(elapsedSeconds, speedMultiplier, 12.0f);
    }

    private float angle(float phase) {
        return this.startAngle + phase * this.preset.sweep * this.preset.direction;
    }

    @Override
    public Vec3 updatePosition(float elapsedSeconds, float speedMultiplier, float tickDelta) {
        if (!this.isPlayerAvailable()) {
            return Vec3.ZERO;
        }
        float travel = this.travel(elapsedSeconds, speedMultiplier);
        float p = Math.min(1.0f, travel);
        Vec3 offset = this.getCircularOffset(this.angle(travel), this.lerp(p, this.preset.startDistance, this.preset.endDistance));
        return this.getPlayerPos(tickDelta).add(offset.x, (double)this.lerp(p, this.preset.startHeight, this.preset.endHeight), offset.z);
    }

    @Override
    public float updatePitch(float elapsedSeconds, float speedMultiplier, float tickDelta) {
        float p = Math.min(1.0f, this.travel(elapsedSeconds, speedMultiplier));
        return this.lerp(p, this.preset.startPitch, this.preset.endPitch);
    }

    @Override
    public float updateYaw(float elapsedSeconds, float speedMultiplier, float tickDelta) {
        float travel = this.travel(elapsedSeconds, speedMultiplier);
        float p = Math.min(1.0f, travel);
        return this.angle(travel) + this.lerp(p, this.preset.startYawOffset, this.preset.endYawOffset) * this.preset.direction;
    }

    @Override
    public String getId() {
        return "environment/" + this.preset.id;
    }

    public static enum Preset {
        HORIZON_REVEAL("horizon_reveal", 8.0f, 14.0f, 2.0f, 7.0f, 8.0f, 15.0f, 1.0f, -90.0f, -84.0f, -4.0f, -10.0f),
        GROUND_SKIM("ground_skim", 5.0f, 10.0f, 0.35f, 0.8f, 12.0f, 105.0f, -1.0f, -85.0f, -70.0f, 2.0f, -2.0f),
        CANOPY_GLIDE("canopy_glide", 9.0f, 13.0f, 11.0f, 14.0f, 18.0f, 195.0f, 1.0f, -100.0f, -75.0f, 18.0f, 8.0f),
        DISTANT_VISTA("distant_vista", 12.0f, 12.0f, 5.0f, 5.0f, 4.0f, 285.0f, -1.0f, -82.0f, -74.0f, 6.0f, 6.0f),
        PARALLAX_TRUCK("parallax_truck", 7.0f, 9.0f, 3.0f, 4.0f, 28.0f, 45.0f, 1.0f, -100.0f, -62.0f, 4.0f, 8.0f),
        SKYWARD_TILT("skyward_tilt", 6.0f, 8.0f, 2.0f, 6.0f, 5.0f, 135.0f, -1.0f, -90.0f, -88.0f, -8.0f, -32.0f),
        VALLEY_CRANE("valley_crane", 6.0f, 15.0f, 3.0f, 16.0f, 10.0f, 225.0f, 1.0f, -92.0f, -78.0f, 8.0f, 24.0f),
        WIDE_ESTABLISHING("wide_establishing", 16.0f, 18.0f, 10.0f, 12.0f, 14.0f, 315.0f, -1.0f, -100.0f, -80.0f, 12.0f, 9.0f),
        HORIZON_ARC("horizon_arc", 11.0f, 13.0f, 6.0f, 8.0f, 35.0f, 75.0f, 1.0f, -90.0f, -55.0f, 6.0f, 10.0f),
        GRAND_LANDSCAPE("grand_landscape", 10.0f, 12.0f, 4.0f, 5.5f, 9.0f, 165.0f, 1.0f, -90.0f, -76.0f, 5.0f, 2.0f),
        RIVERLINE_DRIFT("riverline_drift", 7.0f, 11.0f, 1.2f, 2.0f, 22.0f, 255.0f, -1.0f, -96.0f, -72.0f, 1.0f, 4.0f),
        RIDGELINE_SWEEP("ridgeline_sweep", 15.0f, 17.0f, 9.0f, 11.0f, 11.0f, 345.0f, 1.0f, -88.0f, -70.0f, 10.0f, 7.0f),
        CAVE_MOUTH_REVEAL("cave_mouth_reveal", 5.0f, 9.0f, 1.0f, 2.5f, 16.0f, 30.0f, -1.0f, -108.0f, -76.0f, 3.0f, -6.0f),
        CLOUDLINE_ASCENT("cloudline_ascent", 10.0f, 13.0f, 8.0f, 17.0f, 9.0f, 120.0f, 1.0f, -92.0f, -84.0f, -4.0f, -22.0f),
        FOREGROUND_REVEAL("foreground_reveal", 6.0f, 8.0f, 2.0f, 3.6f, 13.0f, 210.0f, 1.0f, -84.0f, -66.0f, 2.0f, 6.0f);

        final String id;
        final float startDistance;
        final float endDistance;
        final float startHeight;
        final float endHeight;
        final float sweep;
        final float angleOffset;
        final float direction;
        final float startYawOffset;
        final float endYawOffset;
        final float startPitch;
        final float endPitch;

        private Preset(String id, float startDistance, float endDistance, float startHeight, float endHeight, float sweep, float angleOffset, float direction, float startYawOffset, float endYawOffset, float startPitch, float endPitch) {
            this.id = id;
            this.startDistance = startDistance;
            this.endDistance = endDistance;
            this.startHeight = startHeight;
            this.endHeight = endHeight;
            this.sweep = sweep;
            this.angleOffset = angleOffset;
            this.direction = direction;
            this.startYawOffset = startYawOffset;
            this.endYawOffset = endYawOffset;
            this.startPitch = startPitch;
            this.endPitch = endPitch;
        }
    }
}
