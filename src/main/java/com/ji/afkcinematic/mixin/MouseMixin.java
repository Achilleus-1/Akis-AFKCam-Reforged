package com.ji.afkcinematic.mixin;

import com.ji.afkcinematic.afk.AFKDetector;
import com.ji.afkcinematic.cinematic.CinematicManager;
import com.ji.afkcinematic.cinematic.CinematicState;
import com.ji.afkcinematic.config.ConfigManager;
import com.ji.afkcinematic.config.ConfigScreen;
import com.ji.afkcinematic.input.CinematicInputPolicy;
import com.ji.afkcinematic.input.KeySequenceTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MouseHandler.class})
public class MouseMixin {
    private double lastX = 0.0;
    private double lastY = 0.0;

    @Inject(method={"onMove"}, at={@At(value="HEAD")}, require=1)
    private void onCursorMove(long window, double x, double y, CallbackInfo ci) {
        this.registerMouseActivity(CinematicInputPolicy.Event.LOOK);
        this.lastX = x;
        this.lastY = y;
    }

    @Inject(method={"onPress"}, at={@At(value="HEAD")}, require=1)
    private void onMouseClick(long window, int button, int action, int modifiers, CallbackInfo ci) {
        if (action == 1) {
            this.registerMouseActivity(CinematicInputPolicy.Event.GAMEPLAY_ACTION);
        }
        KeySequenceTracker.resetAll();
    }

    @Inject(method={"onScroll"}, at={@At(value="HEAD")}, require=1)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof ConfigScreen) {
            ConfigScreen screen2 = (ConfigScreen)screen;
            screen2.scrollContent(vertical);
            return;
        }
        this.registerMouseActivity(CinematicInputPolicy.Event.GAMEPLAY_ACTION);
    }

    private void registerMouseActivity(CinematicInputPolicy.Event event) {
        Minecraft client = Minecraft.getInstance();
        boolean chatOpen = client.screen instanceof ChatScreen;
        if (CinematicInputPolicy.shouldRegisterActivity(CinematicManager.getState() == CinematicState.CINEMATIC_ACTIVE, ConfigManager.getConfig().persistentMode, chatOpen, event)) {
            AFKDetector.registerActivity();
        }
    }
}
