package com.ji.afkcinematic.afk;

import com.ji.afkcinematic.afk.AFKListener;
import com.ji.afkcinematic.config.ConfigManager;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import com.ji.afkcinematic.platform.ClientEvents;
import net.minecraft.client.Minecraft;

public class AFKDetector {
    private static int ticksSinceLastActivity = 0;
    private static boolean isLockedOut = false;
    private static boolean triggered = false;
    private static final List<AFKListener> LISTENERS = new ArrayList<AFKListener>();

    public static void init() {
        ClientEvents.onTick(client -> AFKDetector.tick());
    }

    public static void addListener(AFKListener listener) {
        if (!LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    private static void dispatch(Consumer<AFKListener> action) {
        new ArrayList<AFKListener>(LISTENERS).forEach(action);
    }

    private static void tick() {
        if (!ConfigManager.getConfig().modEnabled) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) {
            ticksSinceLastActivity = 0;
            triggered = false;
            AFKDetector.dispatch(AFKListener::onReset);
            return;
        }
        if (client.isPaused() || isLockedOut) {
            return;
        }
        int threshold = ConfigManager.getConfig().afkThresholdTicks;
        if (++ticksSinceLastActivity >= threshold && !triggered) {
            triggered = true;
            AFKDetector.dispatch(AFKListener::onAFKTriggered);
        }
    }

    public static void registerActivity() {
        if (ticksSinceLastActivity == 0 && !isLockedOut && !triggered) {
            return;
        }
        ticksSinceLastActivity = 0;
        isLockedOut = false;
        triggered = false;
        AFKDetector.dispatch(AFKListener::onActivityDetected);
    }

    public static void setLockedOut(boolean lockedOut) {
        isLockedOut = lockedOut;
    }
}
