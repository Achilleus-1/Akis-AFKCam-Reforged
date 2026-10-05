package com.ji.afkcinematic.input;

import com.ji.afkcinematic.afk.AFKDetector;
import com.ji.afkcinematic.cinematic.CinematicManager;
import com.ji.afkcinematic.cinematic.CinematicState;
import com.ji.afkcinematic.config.ConfigManager;
import com.ji.afkcinematic.input.CinematicInputPolicy;
import com.ji.afkcinematic.platform.ClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.Input;
import net.minecraft.world.phys.Vec2;

public final class GameplayActivityMonitor {
    private static final float MOVE_EPSILON_SQUARED = 1.0E-4f;
    private static Object previousPlayer;
    private static float previousYaw;
    private static float previousPitch;
    private static boolean previousChatOpen;

    private GameplayActivityMonitor() {
    }

    public static void init() {
        ClientEvents.onTick(GameplayActivityMonitor::tick);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(GameplayActivityMonitor::onKeyboardActivity);
    }

    private static void onKeyboardActivity(net.neoforged.neoforge.client.event.InputEvent.Key event) {
        if (event.getAction() != org.lwjgl.glfw.GLFW.GLFW_PRESS) return;
        Minecraft client = Minecraft.getInstance();
        if (ModKeyMappings.matches(com.mojang.blaze3d.platform.InputConstants.getKey(event.getKey(), event.getScanCode()))) return;
        boolean chatOpen = client.screen instanceof ChatScreen;
        CinematicInputPolicy.Event activity = event.getKey() == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
            ? CinematicInputPolicy.Event.ESCAPE
            : (client.options.keyChat.matches(event.getKey(), event.getScanCode())
                || client.options.keyCommand.matches(event.getKey(), event.getScanCode()))
                ? CinematicInputPolicy.Event.CHAT_OPEN
                : chatOpen ? CinematicInputPolicy.Event.CHAT_INPUT : CinematicInputPolicy.Event.GAMEPLAY_ACTION;
        register(activity, chatOpen);
    }

    private static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            GameplayActivityMonitor.reset();
            return;
        }
        boolean chatOpen = client.screen instanceof ChatScreen;
        if (previousPlayer != client.player) {
            previousPlayer = client.player;
            previousYaw = client.player.getYRot();
            previousPitch = client.player.getXRot();
            previousChatOpen = chatOpen;
            return;
        }
        if (chatOpen && !previousChatOpen) {
            GameplayActivityMonitor.register(CinematicInputPolicy.Event.CHAT_OPEN, false);
        }
        previousChatOpen = chatOpen;
        if (!chatOpen) {
            Vec2 movement = client.player.input.getMoveVector();
            Input keys = client.player.input;
            if (movement.lengthSquared() > 1.0E-4f) {
                GameplayActivityMonitor.register(CinematicInputPolicy.Event.MOVE, false);
            }
            if (keys.jumping || keys.shiftKeyDown || client.options.keySprint.isDown()) {
                GameplayActivityMonitor.register(CinematicInputPolicy.Event.JUMP_SNEAK, false);
            }
            float yaw = client.player.getYRot();
            float pitch = client.player.getXRot();
            if (Float.compare(yaw, previousYaw) != 0 || Float.compare(pitch, previousPitch) != 0) {
                GameplayActivityMonitor.register(CinematicInputPolicy.Event.LOOK, false);
            }
            previousYaw = yaw;
            previousPitch = pitch;
        }
    }

    private static void register(CinematicInputPolicy.Event event, boolean chatOpen) {
        if (CinematicInputPolicy.shouldRegisterActivity(CinematicManager.getState() == CinematicState.CINEMATIC_ACTIVE, ConfigManager.getConfig().persistentMode, chatOpen, event)) {
            AFKDetector.registerActivity();
        }
    }

    private static void reset() {
        previousPlayer = null;
        previousChatOpen = false;
    }
}
