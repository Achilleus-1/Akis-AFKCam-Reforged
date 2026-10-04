package com.ji.afkcinematic.config;

import com.ji.afkcinematic.JiAFKCinematic;
import com.ji.afkcinematic.config.CinematicChatVisibility;
import com.ji.afkcinematic.config.DamageAction;
import com.ji.afkcinematic.config.MusicMode;
import com.ji.afkcinematic.config.PersistentCinematicMode;
import com.ji.afkcinematic.input.KeySequenceTracker;

public class ModConfig {
    public static final int CURRENT_CONFIG_VERSION = 8;
    public static final int UNLIMITED_CYCLES = -1;
    public int configVersion = 8;
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
    public int menuKey1 = 296;
    public int menuKey2 = 72;
    public int toggleKey1 = 341;
    public int toggleKey2 = 72;
    public int immediateKey1 = 296;
    public int immediateKey2 = 73;
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
        this.menuKey1 = ModConfig.clamp(this.menuKey1, -1, 65535);
        this.menuKey2 = ModConfig.clamp(this.menuKey2, -1, 65535);
        this.toggleKey1 = ModConfig.clamp(this.toggleKey1, -1, 65535);
        this.toggleKey2 = ModConfig.clamp(this.toggleKey2, -1, 65535);
        this.immediateKey1 = ModConfig.clamp(this.immediateKey1, -1, 65535);
        this.immediateKey2 = ModConfig.clamp(this.immediateKey2, -1, 65535);
        if (this.menuKey1 == this.toggleKey1 && this.menuKey2 == this.toggleKey2 && !ModConfig.isDisabledShortcut(this.menuKey1, this.menuKey2)) {
            JiAFKCinematic.LOGGER.warn("Menu and toggle keybinds collide ({}, {}); resetting to defaults", (Object)this.menuKey1, (Object)this.menuKey2);
            this.menuKey1 = 296;
            this.menuKey2 = 72;
            this.toggleKey1 = 341;
            this.toggleKey2 = 72;
        }
        this.shotDurationTicks = this.shotDurationSeconds * 20;
        this.afkThresholdTicks = this.afkThresholdSeconds * 20;
        if (!(ModConfig.isDisabledShortcut(this.menuKey1, this.menuKey2) || KeySequenceTracker.isBindableKeyCode(this.menuKey1) && KeySequenceTracker.isBindableKeyCode(this.menuKey2))) {
            JiAFKCinematic.LOGGER.warn("Invalid menu shortcut ({}, {}); resetting to F7 + H", (Object)this.menuKey1, (Object)this.menuKey2);
            this.menuKey1 = 296;
            this.menuKey2 = 72;
        }
        if (!(ModConfig.isDisabledShortcut(this.toggleKey1, this.toggleKey2) || KeySequenceTracker.isBindableKeyCode(this.toggleKey1) && KeySequenceTracker.isBindableKeyCode(this.toggleKey2))) {
            JiAFKCinematic.LOGGER.warn("Invalid toggle shortcut ({}, {}); resetting to Ctrl + H", (Object)this.toggleKey1, (Object)this.toggleKey2);
            this.toggleKey1 = 341;
            this.toggleKey2 = 72;
        }
        if (!(ModConfig.isDisabledShortcut(this.immediateKey1, this.immediateKey2) || KeySequenceTracker.isBindableKeyCode(this.immediateKey1) && KeySequenceTracker.isBindableKeyCode(this.immediateKey2))) {
            JiAFKCinematic.LOGGER.warn("Invalid immediate shortcut ({}, {}); resetting to F7 + I", (Object)this.immediateKey1, (Object)this.immediateKey2);
            this.immediateKey1 = 296;
            this.immediateKey2 = 73;
        }
    }

    public boolean isUnlimitedCycles() {
        return this.maxCycles == -1;
    }

    private static boolean isDisabledShortcut(int firstKey, int secondKey) {
        return firstKey == -1 && secondKey == -1;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clampFloat(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
