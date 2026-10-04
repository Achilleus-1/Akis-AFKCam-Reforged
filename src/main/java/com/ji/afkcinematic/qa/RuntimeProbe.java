package com.ji.afkcinematic.qa;

import com.ji.afkcinematic.JiAFKCinematic;
import com.ji.afkcinematic.cinematic.CameraController;
import com.ji.afkcinematic.cinematic.CinematicManager;
import com.ji.afkcinematic.cinematic.CinematicState;
import com.ji.afkcinematic.config.ConfigManager;
import com.ji.afkcinematic.config.ConfigScreen;
import com.ji.afkcinematic.config.ModConfig;
import com.ji.afkcinematic.config.PersistentCinematicMode;
import com.ji.afkcinematic.diagnostic.MixinState;
import com.ji.afkcinematic.input.CinematicInputPolicy;
import com.ji.afkcinematic.qa.RuntimeScreenHelper;
import java.util.List;
import com.ji.afkcinematic.platform.ClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;

public final class RuntimeProbe {
    public static final String ENABLE_PROPERTY = "ji.afkcinematic.runtimeTest";
    public static final String PASS_MARKER = "JI_RUNTIME_TEST_PASS";
    private static final List<String> CRITICAL_MIXINS = List.of("CameraMixin", "InGameHudMixin", "KeyboardMixin", "MouseMixin", "MinecraftClientMixin");
    private static int waitingTicks;
    private static int phase;
    private static boolean worldRequested;
    private static boolean resourcesRequested;
    private static int frameTicks;
    private static net.minecraft.client.CameraType previousCamera;
    private static boolean previousHud;

    private RuntimeProbe() {
    }

    public static void initIfEnabled() {
        if (!Boolean.getBoolean(ENABLE_PROPERTY)) {
            return;
        }
        JiAFKCinematic.LOGGER.info("JI_RUNTIME_TEST_START");
        ClientEvents.onTick(RuntimeProbe::tick);
    }

    private static void tick(Minecraft client) {
        if (phase >= 5) {
            return;
        }
        if (++waitingTicks > 1200) {
            RuntimeProbe.fail("world/menu probe timed out");
        }
        if (client.player == null || client.level == null) {
            if (Boolean.getBoolean("ji.afkcinematic.autoSmokeTest") && !worldRequested
                    && client.screen instanceof net.minecraft.client.gui.screens.TitleScreen && client.getOverlay() == null) {
                if (Boolean.getBoolean("ji.afkcinematic.testLocalMusic") && !resourcesRequested) {
                    resourcesRequested = true;
                    RuntimeProbe.check(com.ji.afkcinematic.music.LocalMusicPackManager.rebuildAndReload() == 1,
                        "local OGG did not build into one music event");
                    return;
                }
                if (Boolean.getBoolean("ji.afkcinematic.testLocalMusic")) {
                    RuntimeProbe.check(com.ji.afkcinematic.music.ThirdPartyMusicRegistry.getTracks().size() == 1,
                        "local music manifest was not discovered after resource reload");
                }
                worldRequested = true;
                JiAFKCinematic.LOGGER.info("JI_RUNTIME_TEST_CREATE_WORLD");
                client.options.pauseOnLostFocus = false;
                client.options.renderDistance().set(4);
                client.options.simulationDistance().set(5);
                client.createWorldOpenFlows().createFreshLevel("port-smoke-" + System.currentTimeMillis(),
                    new net.minecraft.world.level.LevelSettings("Port smoke test", net.minecraft.world.level.GameType.CREATIVE,
                        false, net.minecraft.world.Difficulty.PEACEFUL, true, new net.minecraft.world.level.GameRules(),
                        net.minecraft.world.level.WorldDataConfiguration.DEFAULT),
                    new net.minecraft.world.level.levelgen.WorldOptions(1L, false, false),
                    registry -> registry.registryOrThrow(net.minecraft.core.registries.Registries.WORLD_PRESET)
                        .getHolderOrThrow(net.minecraft.world.level.levelgen.presets.WorldPresets.FLAT).value().createWorldDimensions(), client.screen);
            }
            return;
        }
        if (phase == 0) {
            for (String mixin : CRITICAL_MIXINS) {
                RuntimeProbe.check(MixinState.didApply(mixin), "critical mixin did not apply: " + mixin);
            }
            ModConfig config = ConfigManager.getConfig();
            previousCamera = client.options.getCameraType();
            previousHud = client.options.hideGui;
            config.characterShotPercentage = 30;
            config.persistentMode = PersistentCinematicMode.INTERACTIVE;
            config.enableMusic = true;
            config.enableLetterbox = true;
            if (Boolean.getBoolean("ji.afkcinematic.testLocalMusic")) config.musicMode = com.ji.afkcinematic.config.MusicMode.CUSTOM;
            config.recalculate();
            CinematicManager.onAFKDetected();
            RuntimeProbe.check(CinematicManager.getState() == CinematicState.CINEMATIC_ACTIVE, "cinematic did not enter active state");
            RuntimeProbe.check(CameraController.getShotCount() == 15, "visible sequence is not exactly 15 shots");
            RuntimeProbe.check(CameraController.getCharacterPresetCount() == 15, "character pool is not 15 shots");
            RuntimeProbe.check(CameraController.getEnvironmentPresetCount() == 15, "environment pool is not 15 shots");
            int characterShots = CameraController.getActiveCharacterShotCount();
            RuntimeProbe.check(characterShots == 4 || characterShots == 5, "30% mix did not select 4-5 character shots");
            RuntimeProbe.check(CameraController.getActiveEnvironmentShotCount() == 15 - characterShots, "environment mix did not complement character shots");
            RuntimeProbe.check(!RuntimeProbe.activity(true, CinematicInputPolicy.Event.CHAT_OPEN), "chat key incorrectly cancels persistent cinematic");
            RuntimeProbe.check(!RuntimeProbe.activity(false, CinematicInputPolicy.Event.LOOK), "mouse movement incorrectly cancels persistent cinematic");
            RuntimeProbe.check(!RuntimeProbe.activity(true, CinematicInputPolicy.Event.CHAT_INPUT), "chat click incorrectly cancels persistent cinematic");
            RuntimeProbe.check(RuntimeProbe.activity(false, CinematicInputPolicy.Event.ESCAPE), "escape does not cancel persistent cinematic");
            RuntimeScreenHelper.openEmptyChat(client);
            phase = 1;
            JiAFKCinematic.LOGGER.info("JI_RUNTIME_TEST_CINEMATIC_ACTIVE");
            return;
        }
        if (phase == 1) {
            RuntimeProbe.check(client.screen instanceof ChatScreen, "chat screen did not remain open for a frame");
            RuntimeProbe.check(CinematicManager.getState() == CinematicState.CINEMATIC_ACTIVE, "opening chat canceled the persistent cinematic");
            if (++frameTicks < 40) return;
            net.minecraft.world.phys.Vec3 position = client.gameRenderer.getMainCamera().getPosition();
            RuntimeProbe.check(Double.isFinite(position.x) && Double.isFinite(position.y) && Double.isFinite(position.z), "camera frame is not finite");
            RuntimeProbe.check(client.gameRenderer.getMainCamera().isDetached(), "cinematic camera is not detached");
            RuntimeProbe.check(com.ji.afkcinematic.render.HUDController.isHidden(), "cinematic HUD was not hidden");
            RuntimeProbe.check(com.ji.afkcinematic.music.CinematicMusicManager.isOurMusicPlaying, "cinematic music did not start");
            for (int mix : new int[] {0, 100}) {
                CameraController.prepareSequence(mix);
                for (int shot = 0; shot < 15; shot++) {
                    CameraController.startShot(shot);
                    for (float progress : new float[] {0, 0.25f, 0.5f, 0.75f, 1}) {
                        CameraController.evaluateFrame(progress, 0.5f);
                        net.minecraft.world.phys.Vec3 pos = CameraController.getFramePos();
                        RuntimeProbe.check(Double.isFinite(pos.x) && Double.isFinite(pos.y) && Double.isFinite(pos.z)
                            && Float.isFinite(CameraController.getFramePitch()) && Float.isFinite(CameraController.getFrameYaw()),
                            "invalid camera frame in " + CameraController.getCurrentShotId());
                    }
                }
            }
            CameraController.prepareSequence(30);
            CameraController.startShot(0);
            com.ji.afkcinematic.render.ToggleToastManager.show(true);
            // Keyboard activity intentionally cancels INTERACTIVE mode.
            // Exercise the menu shortcut while PERSISTENT mode keeps the camera active.
            ConfigManager.getConfig().persistentMode = PersistentCinematicMode.PERSISTENT;
            client.setScreen(null);
            long window = client.getWindow().getWindow();
            client.keyboardHandler.keyPress(window, 296, 0, 1, 0);
            client.keyboardHandler.keyPress(window, 72, 0, 1, 0);
            client.keyboardHandler.keyPress(window, 72, 0, 0, 0);
            client.keyboardHandler.keyPress(window, 296, 0, 0, 0);
            phase = 2;
            return;
        }
        if (phase == 2) {
            RuntimeProbe.check(client.screen instanceof ConfigScreen, "configuration screen did not remain open for a frame");
            if (++frameTicks < 60) return;
            client.setScreen(null);
            ConfigManager.getConfig().persistentMode = PersistentCinematicMode.PERSISTENT;
            client.player.input.forwardImpulse = 1;
            client.player.input.jumping = true;
            com.ji.afkcinematic.input.PersistentMovementLock.clear(client.player.input);
            RuntimeProbe.check(client.player.input.forwardImpulse == 0 && !client.player.input.jumping, "persistent movement lock did not clear legacy input");
            phase = 3;
            frameTicks = 0;
            return;
        }
        if (phase == 3) {
            if (++frameTicks < 20) return;
            CinematicManager.forceDeactivate();
            RuntimeProbe.check(CinematicManager.getState() == CinematicState.IDLE, "cinematic teardown did not restore idle state");
            RuntimeProbe.check(client.options.getCameraType() == previousCamera, "camera perspective was not restored");
            RuntimeProbe.check(client.options.hideGui == previousHud, "HUD option was not restored");
            CinematicManager.fullTeardown();
            phase = 5;
            JiAFKCinematic.LOGGER.info(PASS_MARKER);
            if (Boolean.getBoolean("ji.afkcinematic.autoSmokeTest")) client.stop();
        }
    }

    private static boolean activity(boolean chatOpen, CinematicInputPolicy.Event event) {
        return CinematicInputPolicy.shouldRegisterActivity(true, PersistentCinematicMode.INTERACTIVE, chatOpen, event);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            RuntimeProbe.fail(message);
        }
    }

    private static void fail(String message) {
        throw new IllegalStateException("JI_RUNTIME_TEST_FAIL: " + message);
    }
}
