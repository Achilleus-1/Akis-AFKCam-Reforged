package com.ji.afkcinematic.render;

import com.ji.afkcinematic.config.ModConfig;
import com.ji.afkcinematic.render.HUDController;
import com.ji.afkcinematic.render.LetterboxRenderer;
import net.minecraft.client.Minecraft;

public class CinematicHUDManager {
    private static boolean captured = false;
    private static boolean previousHideGui = false;

    public static void activate(ModConfig config) {
        if (config.enableLetterbox) {
            LetterboxRenderer.fadeIn();
        }
        HUDController.setHidden(true);
        Minecraft client = Minecraft.getInstance();
        if (!captured) {
            previousHideGui = client.options.hideGui;
            captured = true;
        }
        client.options.hideGui = true;
    }

    public static void deactivate() {
        LetterboxRenderer.fadeOut();
        HUDController.setHidden(false);
        if (captured) {
            Minecraft.getInstance().options.hideGui = previousHideGui;
            captured = false;
        }
    }

    public static boolean shouldRenderPassiveChat(ModConfig config) {
        return config.chatVisibility != null && config.chatVisibility.isVisible(config.persistentMode);
    }

    public static void forceRestore() {
        if (captured) {
            Minecraft.getInstance().options.hideGui = previousHideGui;
            captured = false;
        }
    }

    public static boolean isHUDHidden() {
        return HUDController.isHidden();
    }
}
