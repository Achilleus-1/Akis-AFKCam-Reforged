package com.ji.afkcinematic.cinematic.shots;

import com.ji.afkcinematic.cinematic.AbstractCameraShot;
import com.ji.afkcinematic.cinematic.ShotMotion;
import net.minecraft.world.phys.Vec3;

public final class ComposedCharacterShot
extends AbstractCameraShot {
    private static final float NOMINAL_TRAVEL_SECONDS = 12.0f;
    private final Preset preset;
    private float startAngle;

    public ComposedCharacterShot(Preset preset) {
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
        return this.startAngle + this.preset.sweep * this.preset.direction * phase;
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
        float distance = this.lerp(p, this.preset.startDistance, this.preset.endDistance);
        float height = this.lerp(p, this.preset.startHeight, this.preset.endHeight);
        return (float)Math.toDegrees(Math.atan2(height - 1.35f, Math.max(0.1f, distance)));
    }

    @Override
    public float updateYaw(float elapsedSeconds, float speedMultiplier, float tickDelta) {
        return this.angle(this.travel(elapsedSeconds, speedMultiplier)) - 90.0f;
    }

    @Override
    public String getId() {
        return "character/" + this.preset.id;
    }

    public static enum Preset {
        EYE_LEVEL_ARC("eye_level_arc", 4.2f, 4.2f, 1.7f, 1.7f, 28.0f, 35.0f, 1.0f),
        LOW_HERO_ARC("low_hero_arc", 4.8f, 4.8f, 0.45f, 0.65f, 24.0f, 145.0f, -1.0f),
        HIGH_PORTRAIT("high_portrait", 4.5f, 4.2f, 3.1f, 2.8f, 18.0f, 215.0f, 1.0f),
        LEFT_SHOULDER("left_shoulder", 2.6f, 2.8f, 1.75f, 1.75f, 9.0f, 120.0f, 1.0f),
        RIGHT_SHOULDER("right_shoulder", 2.6f, 2.8f, 1.75f, 1.75f, 9.0f, 240.0f, -1.0f),
        SLOW_PUSH_IN("slow_push_in", 7.5f, 3.8f, 1.8f, 1.8f, 4.0f, 20.0f, 1.0f),
        SLOW_PULL_BACK("slow_pull_back", 3.5f, 8.5f, 1.6f, 2.2f, 5.0f, 200.0f, -1.0f),
        CRANE_RISE("crane_rise", 5.5f, 7.5f, 1.0f, 6.5f, 12.0f, 310.0f, 1.0f),
        CRANE_DESCENT("crane_descent", 7.5f, 5.2f, 6.0f, 1.4f, 10.0f, 70.0f, -1.0f),
        AERIAL_THREE_QUARTER("aerial_three_quarter", 7.0f, 7.0f, 7.5f, 7.0f, 22.0f, 155.0f, 1.0f),
        DISTANT_SILHOUETTE("distant_silhouette", 13.0f, 14.0f, 3.0f, 3.4f, 12.0f, 265.0f, -1.0f),
        GROUND_TO_PORTRAIT("ground_to_portrait", 4.0f, 4.0f, 0.2f, 1.9f, 8.0f, 330.0f, 1.0f),
        PROFILE_TRUCK("profile_truck", 5.5f, 5.5f, 1.65f, 1.65f, 34.0f, 90.0f, -1.0f),
        TIGHT_PORTRAIT("tight_portrait", 3.0f, 3.2f, 1.8f, 1.8f, 12.0f, 225.0f, 1.0f),
        HEROIC_THREE_QUARTER("heroic_three_quarter", 4.6f, 5.2f, 1.8f, 2.4f, 9.0f, 45.0f, 1.0f);

        final String id;
        final float startDistance;
        final float endDistance;
        final float startHeight;
        final float endHeight;
        final float sweep;
        final float angleOffset;
        final float direction;

        private Preset(String id, float startDistance, float endDistance, float startHeight, float endHeight, float sweep, float angleOffset, float direction) {
            this.id = id;
            this.startDistance = startDistance;
            this.endDistance = endDistance;
            this.startHeight = startHeight;
            this.endHeight = endHeight;
            this.sweep = sweep;
            this.angleOffset = angleOffset;
            this.direction = direction;
        }
    }
}
