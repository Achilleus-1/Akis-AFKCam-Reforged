package com.ji.afkcinematic.config;

import com.ji.afkcinematic.config.CinematicChatVisibility;
import com.ji.afkcinematic.config.DamageAction;
import com.ji.afkcinematic.config.MusicMode;
import com.ji.afkcinematic.config.PersistentCinematicMode;

public class ModConfig {
    public static final int CURRENT_CONFIG_VERSION = 9;
    public static final int UNLIMITED_CYCLES = -1;
    public int configVersion = 9;
    public int shotDurationSeconds = 10;
    public int afkThresholdSeconds = 30;
    public int maxCycles = 3;
    public float cameraSpeed = 0.5f;
    @Deprecated
    public transient boolean useEasing = false;
    @Deprecated
    public transient float easingIntensity = 0.0f;
    public int characterShotPercentage = 30;
    public PersistentCinematicMode persistentMode = PersistentCinematicMode.NORMAL;
    public boolean cameraRotationEnabled = false;
    public DamageAction damageAction = DamageAction.CANCEL_CINEMATIC;
    @Deprecated
    public transient boolean cancelOnFallDamage = false;
    @Deprecated
    public transient boolean cancelOnFire = false;
    @Deprecated
    public transient float lowHealthThreshold = 0.0f;
    public boolean extendedMusic = true;
    public boolean modEnabled = true;
    public boolean enableLetterbox = true;
    public CinematicChatVisibility chatVisibility = CinematicChatVisibility.VISIBLE;
    public boolean enableMusic = true;
    public MusicMode musicMode = MusicMode.VANILLA;
    @Deprecated
    public transient boolean thirdPartyMusic = false;
    public float cinematicMusicVolume = 0.5f;
    public transient int shotDurationTicks = 200;
    public transient int afkThresholdTicks = 1200;

    public void recalculate() {
        this.shotDurationSeconds = ModConfig.clamp(this.shotDurationSeconds, 5, 60);
        this.afkThresholdSeconds = ModConfig.clamp(this.afkThresholdSeconds, 10, 600);
        if (this.maxCycles != -1) {
            this.maxCycles = ModConfig.clamp(this.maxCycles, 1, 20);
        }
        this.cameraSpeed = ModConfig.clampFloat(this.cameraSpeed, 0.1f, 3.0f);
        this.useEasing = false;
        this.easingIntensity = 0.0f;
        if (this.persistentMode == null) {
            this.persistentMode = PersistentCinematicMode.NORMAL;
        }
        if (this.chatVisibility == null) {
            this.chatVisibility = CinematicChatVisibility.VISIBLE;
        }
        if (this.musicMode == null) {
            this.musicMode = MusicMode.VANILLA;
        }
        this.characterShotPercentage = Math.round((float)ModConfig.clamp(this.characterShotPercentage, 0, 100) / 10.0f) * 10;
        this.cancelOnFallDamage = false;
        this.cancelOnFire = false;
        this.lowHealthThreshold = 0.0f;
        this.cinematicMusicVolume = ModConfig.clampFloat(this.cinematicMusicVolume, 0.0f, 1.0f);
        this.shotDurationTicks = this.shotDurationSeconds * 20;
        this.afkThresholdTicks = this.afkThresholdSeconds * 20;
    }

    public boolean isUnlimitedCycles() {
        return this.maxCycles == -1;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clampFloat(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
