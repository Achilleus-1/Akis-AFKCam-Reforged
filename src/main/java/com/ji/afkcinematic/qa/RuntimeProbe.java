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
import com.ji.afkcinematic.input.ModKeyMappings;
import com.ji.afkcinematic.qa.RuntimeScreenHelper;
import java.util.List;
import com.ji.afkcinematic.platform.ClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;

public final class RuntimeProbe {
    public static final String ENABLE_PROPERTY = "ji.afkcinematic.runtimeTest";
    public static final String PASS_MARKER = "JI_RUNTIME_TEST_PASS";
    private static final List<String> CRITICAL_MIXINS = List.of("CameraMixin", "InGameHudMixin", "MouseMixin", "MinecraftClientMixin");
    private static int waitingTicks;
    private static int phase;
    private static boolean worldRequested;
    private static boolean resourcesRequested;
    private static int frameTicks;
    private static net.minecraft.client.CameraType previousCamera;
    private static boolean previousHud;
    private static net.minecraft.client.KeyMapping testKeybinding;
    private static byte[] optionsBeforeMusicSetup;
    private static net.minecraft.world.phys.Vec3 movementStart;

    public static void captureOptionsBeforeMusicSetup(Minecraft client) {
        if (!Boolean.getBoolean(ENABLE_PROPERTY) || !Boolean.getBoolean("ji.afkcinematic.testKeybindings")) return;
        optionsBeforeMusicSetup = readOptions(client);
    }

    private static byte[] readOptions(Minecraft client) {
        try {
            return java.nio.file.Files.readAllBytes(client.gameDirectory.toPath().resolve("options.txt"));
        } catch (java.io.IOException e) {
            throw new IllegalStateException("JI_RUNTIME_TEST_FAIL: could not read options.txt", e);
        }
    }

    public static void registerTestKeybinding(net.neoforged.bus.api.IEventBus modBus) {
        if (!Boolean.getBoolean(ENABLE_PROPERTY) || !Boolean.getBoolean("ji.afkcinematic.testKeybindings")) return;
        modBus.addListener((net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event) -> {
            testKeybinding = new net.minecraft.client.KeyMapping("key.ji_afk_cinematic.persistence_probe",
                com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM, org.lwjgl.glfw.GLFW.GLFW_KEY_H,
                "key.categories.misc");
            event.register(testKeybinding);
        });
    }

    private static void checkSavedKeybinding(Minecraft client) {
        if (!Boolean.getBoolean("ji.afkcinematic.testKeybindings")) return;
        RuntimeProbe.check(testKeybinding != null, "test mod keybinding was not registered");
        RuntimeProbe.check(testKeybinding.getKey().getName().equals("key.keyboard.semicolon"),
            "custom mod keybinding was reset to " + testKeybinding.getKey().getName());
        try {
            List<String> savedOptions = java.nio.file.Files.readAllLines(client.gameDirectory.toPath().resolve("options.txt"));
            RuntimeProbe.check(savedOptions
                .contains("key_key.ji_afk_cinematic.persistence_probe:key.keyboard.semicolon"),
                "custom mod keybinding was overwritten in options.txt");
            RuntimeProbe.check(client.options.resourcePacks.contains("file/ji-afk-cinematic-local"),
                "local music pack was not selected for this session");
        } catch (java.io.IOException e) {
            throw new IllegalStateException("JI_RUNTIME_TEST_FAIL: could not read saved keybinding", e);
        }
        var mappings = List.of(ModKeyMappings.OPEN_SETTINGS.get(), ModKeyMappings.TOGGLE_ENABLED.get(),
            ModKeyMappings.TOGGLE_CINEMATIC.get());
        var expectedKeys = List.of("key.keyboard.f10", "key.keyboard.f8", "key.keyboard.f9");
        for (int i = 0; i < mappings.size(); i++) {
            RuntimeProbe.check(mappings.get(i).getDefaultKey().equals(com.mojang.blaze3d.platform.InputConstants.UNKNOWN),
                "a camera action claims a key by default");
            RuntimeProbe.check(mappings.get(i).getKey().getName().equals(expectedKeys.get(i)),
                "saved camera binding was not loaded from normal Controls");
        }
        JiAFKCinematic.LOGGER.info("JI_KEYBINDING_PERSISTENCE_PASS");
    }

    private RuntimeProbe() {
    }

    public static void initIfEnabled() {
        if (!Boolean.getBoolean(ENABLE_PROPERTY)) {
            return;
        }
        JiAFKCinematic.LOGGER.info("JI_RUNTIME_TEST_START");
        if (optionsBeforeMusicSetup != null) {
            RuntimeProbe.check(java.util.Arrays.equals(optionsBeforeMusicSetup, readOptions(Minecraft.getInstance())),
                "music-pack initialization wrote global options");
        }
        ClientEvents.onTick(RuntimeProbe::tick);
    }

    private static void tick(Minecraft client) {
        if (phase >= 9) {
            return;
        }
        if (++waitingTicks > 1200) {
            RuntimeProbe.fail("world/menu probe timed out");
        }
        if (client.player == null || client.level == null) {
            if (Boolean.getBoolean("ji.afkcinematic.autoSmokeTest") && !worldRequested
                    && client.screen instanceof net.minecraft.client.gui.screens.TitleScreen && client.getOverlay() == null) {
                RuntimeProbe.checkSavedKeybinding(client);
                if (Boolean.getBoolean("ji.afkcinematic.testLocalMusic") && !resourcesRequested) {
                    resourcesRequested = true;
                    byte[] optionsBeforeReload = readOptions(client);
                    RuntimeProbe.check(com.ji.afkcinematic.music.LocalMusicPackManager.rebuildAndReload() == 1,
                        "local OGG did not build into one music event");
                    RuntimeProbe.check(java.util.Arrays.equals(optionsBeforeReload, readOptions(client)),
                        "local music rebuild wrote global options");
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
            // The obsolete hardcoded sequence must have no effect.
            press(client, 296);
            press(client, 72);
            RuntimeProbe.check(client.screen == null, "legacy F7/H shortcut is still active");
            if (Boolean.getBoolean("ji.afkcinematic.testKeybindings")) {
                press(client, ModKeyMappings.OPEN_SETTINGS.get().getKey().getValue());
            } else {
                client.setScreen(new ConfigScreen(null));
            }
            phase = 2;
            return;
        }
        if (phase == 2) {
            RuntimeProbe.check(client.screen instanceof ConfigScreen, "configuration screen did not remain open for a frame");
            if (++frameTicks < 60) return;
            var controlsButton = client.screen.children().stream()
                .filter(child -> child instanceof net.minecraft.client.gui.components.Button)
                .map(child -> (net.minecraft.client.gui.components.Button) child)
                .filter(button -> button.getMessage().getString().equals("Key Bindings...")).findFirst().orElseThrow();
            controlsButton.onPress();
            RuntimeProbe.check(client.screen instanceof net.minecraft.client.gui.screens.options.controls.KeyBindsScreen,
                "camera settings did not open Minecraft's Key Binds screen");
            phase = 5;
            frameTicks = 0;
            return;
        }
        if (phase == 3) {
            if (++frameTicks < 20) return;
            RuntimeProbe.check(client.player.input.forwardImpulse > 0 && client.player.position().distanceToSqr(movementStart) > 0.01,
                "persistent cinematic suppressed normal movement controls");
            client.keyboardHandler.keyPress(client.getWindow().getWindow(), client.options.keyUp.getKey().getValue(), 0, 0, 0);
            CinematicManager.forceDeactivate();
            RuntimeProbe.check(CinematicManager.getState() == CinematicState.IDLE, "cinematic teardown did not restore idle state");
            RuntimeProbe.check(client.options.getCameraType() == previousCamera, "camera perspective was not restored");
            RuntimeProbe.check(client.options.hideGui == previousHud, "HUD option was not restored");
            CinematicManager.fullTeardown();
            if (Boolean.getBoolean("ji.afkcinematic.testKeybindings")) {
                press(client, ModKeyMappings.TOGGLE_ENABLED.get().getKey().getValue());
                phase = 4;
                return;
            }
            finish(client);
        } else if (phase == 5) {
            RuntimeProbe.check(client.screen instanceof net.minecraft.client.gui.screens.options.controls.KeyBindsScreen,
                "Key Binds screen did not remain open for a frame");
            if (++frameTicks < 20) return;
            client.screen.onClose();
            RuntimeProbe.check(client.screen instanceof ConfigScreen, "Key Binds did not return to camera settings");
            client.setScreen(null);
            ConfigManager.getConfig().persistentMode = PersistentCinematicMode.PERSISTENT;
            movementStart = client.player.position();
            client.keyboardHandler.keyPress(client.getWindow().getWindow(), client.options.keyUp.getKey().getValue(), 0, 1, 0);
            phase = 3;
            frameTicks = 0;
        } else if (phase == 4) {
            RuntimeProbe.check(!ConfigManager.getConfig().modEnabled, "registered enable/disable binding did not disable the mod");
            press(client, ModKeyMappings.TOGGLE_ENABLED.get().getKey().getValue());
            phase = 6;
        } else if (phase == 6) {
            RuntimeProbe.check(ConfigManager.getConfig().modEnabled, "registered enable/disable binding did not enable the mod");
            press(client, ModKeyMappings.TOGGLE_CINEMATIC.get().getKey().getValue());
            phase = 7;
        } else if (phase == 7) {
            RuntimeProbe.check(CinematicManager.getState() == CinematicState.CINEMATIC_ACTIVE,
                "registered cinematic binding did not start the camera");
            press(client, ModKeyMappings.TOGGLE_CINEMATIC.get().getKey().getValue());
            phase = 8;
        } else if (phase == 8) {
            RuntimeProbe.check(CinematicManager.getState() == CinematicState.IDLE,
                "registered cinematic binding did not stop the camera");
            finish(client);
        }
    }

    private static void finish(Minecraft client) {
        RuntimeProbe.checkSavedKeybinding(client);
        phase = 9;
        JiAFKCinematic.LOGGER.info(PASS_MARKER);
        if (Boolean.getBoolean("ji.afkcinematic.autoSmokeTest")) client.stop();
    }

    private static boolean activity(boolean chatOpen, CinematicInputPolicy.Event event) {
        return CinematicInputPolicy.shouldRegisterActivity(true, PersistentCinematicMode.INTERACTIVE, chatOpen, event);
    }

    private static void press(Minecraft client, int key) {
        long window = client.getWindow().getWindow();
        client.keyboardHandler.keyPress(window, key, 0, 1, 0);
        client.keyboardHandler.keyPress(window, key, 0, 0, 0);
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
