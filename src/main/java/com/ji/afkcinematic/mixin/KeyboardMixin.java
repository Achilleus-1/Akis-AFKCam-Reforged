package com.ji.afkcinematic.mixin;

import com.ji.afkcinematic.afk.AFKDetector;
import com.ji.afkcinematic.cinematic.CinematicManager;
import com.ji.afkcinematic.cinematic.CinematicState;
import com.ji.afkcinematic.config.ConfigManager;
import com.ji.afkcinematic.config.ConfigScreen;
import com.ji.afkcinematic.config.ModConfig;
import com.ji.afkcinematic.input.CinematicInputPolicy;
import com.ji.afkcinematic.input.KeySequenceTracker;
import com.ji.afkcinematic.render.ToggleToastManager;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={KeyboardHandler.class})
public class KeyboardMixin {
    @Inject(method={"keyPress"}, at={@At(value="HEAD")}, require=1)
    private void onKeyPress(long window, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        boolean immediateStep;
        if (action == 0) {
            if (KeySequenceTracker.isBindableKeyCode(key)) {
                KeySequenceTracker.onKeyReleased(key);
            }
            return;
        }
        if (action != 1) {
            return;
        }
        boolean cinematicActive = CinematicManager.getState() == CinematicState.CINEMATIC_ACTIVE;
        ModConfig shortcutConfig = ConfigManager.getConfig();
        boolean toggleStep = (shortcutConfig.toggleKey1 != -1 || shortcutConfig.toggleKey2 != -1) && KeySequenceTracker.isToggleSequenceStep(key, shortcutConfig.toggleKey1, shortcutConfig.toggleKey2);
        boolean bl = immediateStep = (shortcutConfig.immediateKey1 != -1 || shortcutConfig.immediateKey2 != -1) && KeySequenceTracker.isImmediateSequenceStep(key, shortcutConfig.immediateKey1, shortcutConfig.immediateKey2);
        if (!cinematicActive || !toggleStep && !immediateStep) {
            this.registerKeyboardActivity(key, scanCode);
        }
        if (!CinematicInputPolicy.shouldProcessModShortcuts(Minecraft.getInstance().screen instanceof ChatScreen, cinematicActive, ConfigManager.getConfig().persistentMode)) {
            KeySequenceTracker.resetAll();
            return;
        }
        if (!KeySequenceTracker.isBindableKeyCode(key)) {
            KeySequenceTracker.resetAll();
            return;
        }
        this.processShortcuts(window, key, cinematicActive);
    }

    private void registerKeyboardActivity(int keyCode, int scanCode) {
        CinematicInputPolicy.Event event;
        Minecraft client = Minecraft.getInstance();
        boolean chatOpen = client.screen instanceof ChatScreen;
        event = keyCode == 256 ? CinematicInputPolicy.Event.ESCAPE : (client.options.keyChat.matches(keyCode, scanCode) || client.options.keyCommand.matches(keyCode, scanCode) ? CinematicInputPolicy.Event.CHAT_OPEN : (chatOpen ? CinematicInputPolicy.Event.CHAT_INPUT : CinematicInputPolicy.Event.GAMEPLAY_ACTION));
        if (CinematicInputPolicy.shouldRegisterActivity(CinematicManager.getState() == CinematicState.CINEMATIC_ACTIVE, ConfigManager.getConfig().persistentMode, chatOpen, event)) {
            AFKDetector.registerActivity();
        }
    }

    private void processShortcuts(long window, int keyCode, boolean cinematicWasActive) {
        int[] immediateFirst;
        int[] toggleFirst;
        int[] menuFirst;
        ModConfig cfg = ConfigManager.getConfig();
        Minecraft client = Minecraft.getInstance();
        if ((cfg.menuKey1 != -1 || cfg.menuKey2 != -1) && KeySequenceTracker.checkMenu(keyCode, menuFirst = KeySequenceTracker.acceptedFirstKeys(cfg.menuKey1), cfg.menuKey2)) {
            if (client.screen == null) {
                client.setScreen((Screen)new ConfigScreen(client.screen));
            }
            KeySequenceTracker.resetSequence(true);
            return;
        }
        if ((cfg.toggleKey1 != -1 || cfg.toggleKey2 != -1) && KeySequenceTracker.checkToggle(keyCode, toggleFirst = KeySequenceTracker.acceptedFirstKeys(cfg.toggleKey1), cfg.toggleKey2)) {
            cfg.modEnabled = !cfg.modEnabled;
            ConfigManager.saveConfig();
            ToggleToastManager.show(cfg.modEnabled);
            if (!cfg.modEnabled && cinematicWasActive) {
                CinematicManager.forceDeactivate();
            }
            KeySequenceTracker.resetSequence(false);
            return;
        }
        if ((cfg.immediateKey1 != -1 || cfg.immediateKey2 != -1) && KeySequenceTracker.checkImmediate(keyCode, immediateFirst = KeySequenceTracker.acceptedFirstKeys(cfg.immediateKey1), cfg.immediateKey2)) {
            if (cinematicWasActive) {
                CinematicManager.forceDeactivate();
            } else if (cfg.modEnabled) {
                CinematicManager.toggleImmediate();
            }
            KeySequenceTracker.resetImmediateSequence();
        }
    }
}
