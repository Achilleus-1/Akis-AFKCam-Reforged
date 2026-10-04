package com.ji.afkcinematic.cinematic;

import com.ji.afkcinematic.afk.AFKDetector;
import com.ji.afkcinematic.afk.AFKListener;
import com.ji.afkcinematic.cinematic.CameraController;
import com.ji.afkcinematic.cinematic.CinematicCameraManager;
import com.ji.afkcinematic.cinematic.CinematicState;
import com.ji.afkcinematic.cinematic.ShotRandomizer;
import com.ji.afkcinematic.config.ConfigManager;
import com.ji.afkcinematic.config.DamageAction;
import com.ji.afkcinematic.config.ModConfig;
import com.ji.afkcinematic.config.PersistentCinematicMode;
import com.ji.afkcinematic.music.CinematicMusicManager;
import com.ji.afkcinematic.render.CinematicHUDManager;
import com.ji.afkcinematic.render.LetterboxRenderer;
import com.ji.afkcinematic.platform.ClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;

public class CinematicManager
implements AFKListener {
    private static final CinematicManager INSTANCE = new CinematicManager();
    private static CinematicState state = CinematicState.IDLE;
    private static int cinematicTicks = 0;
    private static int currentCycle = 0;
    private static int ticksLeftInCurrentShot = 0;
    private static int currentShotIndex = 0;

    public static void init() {
        CameraController.init();
        AFKDetector.addListener(INSTANCE);
        ClientEvents.onTick(client -> CinematicManager.tick());
    }

    @Override
    public void onAFKTriggered() {
        CinematicManager.onAFKDetected();
    }

    @Override
    public void onActivityDetected() {
        if (state == CinematicState.CINEMATIC_ACTIVE) {
            CinematicManager.deactivateCinematic();
        }
    }

    @Override
    public void onReset() {
        if (state != CinematicState.IDLE) {
            CinematicManager.reset();
        }
    }

    public static void forceDeactivate() {
        CinematicManager.fullTeardown();
        AFKDetector.setLockedOut(false);
    }

    public static void toggleImmediate() {
        if (state == CinematicState.CINEMATIC_ACTIVE) {
            CinematicManager.forceDeactivate();
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.level != null) {
            AFKDetector.setLockedOut(false);
            CinematicManager.onAFKDetected();
        }
    }

    private static void tick() {
        Minecraft client = Minecraft.getInstance();
        if (state == CinematicState.CINEMATIC_ACTIVE && client.screen instanceof PauseScreen) {
            CinematicManager.forceDeactivate();
            return;
        }
        if (client.player == null || client.level == null || client.isPaused()) {
            return;
        }
        if (state == CinematicState.CINEMATIC_ACTIVE) {
            CinematicManager.handleCinematicTick(client);
        }
    }

    private static void handleCinematicTick(Minecraft client) {
        boolean lockedMode;
        if (client.player.deathTime > 0 || client.player.getHealth() <= 0.0f) {
            CinematicManager.fullTeardown();
            AFKDetector.setLockedOut(true);
            return;
        }
        ModConfig config = ConfigManager.getConfig();
        boolean tookDamage = client.player.hurtTime > 0;
        boolean bl = lockedMode = config.persistentMode == PersistentCinematicMode.PERSISTENT;
        if (tookDamage && (lockedMode || config.damageAction != DamageAction.IGNORE)) {
            DamageAction dAction = lockedMode ? DamageAction.CANCEL_CINEMATIC : config.damageAction;
            CinematicManager.deactivateCinematic();
            AFKDetector.setLockedOut(true);
            if (dAction == DamageAction.PAUSE_GAME) {
                client.setScreen((Screen)new PauseScreen(true));
            }
            return;
        }
        ++cinematicTicks;
        if (--ticksLeftInCurrentShot <= 0) {
            CinematicManager.advanceShot();
        }
    }

    private static void advanceShot() {
        ModConfig config;
        int totalShots = CameraController.getShotCount();
        if ((currentShotIndex = (currentShotIndex + 1) % totalShots) == 0 && cinematicTicks > 0 && !(config = ConfigManager.getConfig()).isUnlimitedCycles() && ++currentCycle >= config.maxCycles) {
            CinematicManager.deactivateCinematic();
            AFKDetector.setLockedOut(true);
            return;
        }
        CameraController.startShot(currentShotIndex);
        ticksLeftInCurrentShot = ConfigManager.getConfig().shotDurationTicks;
    }

    public static void onAFKDetected() {
        if (state == CinematicState.CINEMATIC_ACTIVE) {
            return;
        }
        ModConfig config = ConfigManager.getConfig();
        state = CinematicState.CINEMATIC_ACTIVE;
        cinematicTicks = 0;
        currentCycle = 0;
        currentShotIndex = 0;
        ticksLeftInCurrentShot = config.shotDurationTicks;
        ShotRandomizer.reset();
        CameraController.reset();
        CameraController.prepareSequence(config.characterShotPercentage);
        CameraController.startShot(0);
        CinematicHUDManager.activate(config);
        CinematicCameraManager.activate();
        if (config.enableMusic) {
            CinematicMusicManager.checkAndPlayMusic();
        }
    }

    public static void deactivateCinematic() {
        if (state != CinematicState.CINEMATIC_ACTIVE) {
            return;
        }
        CinematicMusicManager.stopMusic();
        CinematicHUDManager.deactivate();
        CinematicCameraManager.deactivate();
        LetterboxRenderer.reset();
        state = CinematicState.IDLE;
    }

    public static void fullTeardown() {
        CinematicMusicManager.forceStop();
        CinematicHUDManager.deactivate();
        CinematicCameraManager.deactivate();
        LetterboxRenderer.reset();
        state = CinematicState.IDLE;
    }

    @Deprecated
    public static void reset() {
        CinematicManager.fullTeardown();
    }

    public static CinematicState getState() {
        return state;
    }

    public static void setState(CinematicState newState) {
        state = newState;
    }

    public static int getTicksLeftInCurrentShot() {
        return ticksLeftInCurrentShot;
    }

    public static int getShotDurationTicks() {
        return ConfigManager.getConfig().shotDurationTicks;
    }
}
