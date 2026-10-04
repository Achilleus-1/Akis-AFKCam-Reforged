package com.ji.afkcinematic.render;

import com.ji.afkcinematic.JiAFKCinematic;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class ToggleToastManager {
    private static final long DISPLAY_MS = 2000L;
    private static volatile SystemToast.SystemToastId cachedType;

    private ToggleToastManager() {
    }

    private static SystemToast.SystemToastId getType() {
        if (cachedType != null) {
            return cachedType;
        }
        try {
            cachedType = new SystemToast.SystemToastId(2000L);
        }
        catch (Throwable t) {
            JiAFKCinematic.LOGGER.warn("Could not init SystemToast.Type, toggle toast disabled", t);
        }
        return cachedType;
    }

    public static void show(boolean enabled) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return;
        }
        ToastComponent manager = ToggleToastManager.getToastComponent(client);
        if (manager == null) {
            return;
        }
        SystemToast.SystemToastId type = ToggleToastManager.getType();
        if (type == null) {
            return;
        }
        ChatFormatting color = enabled ? ChatFormatting.GREEN : ChatFormatting.RED;
        String key = enabled ? "on" : "off";
        MutableComponent title = Component.translatable((String)("overlay.ji_afkcinematic.toggle." + key)).copy().withStyle(color);
        try {
            SystemToast.add((ToastComponent)manager, (SystemToast.SystemToastId)type, (Component)title, null);
        }
        catch (Throwable t) {
            JiAFKCinematic.LOGGER.warn("Could not show toggle toast", t);
        }
    }

    private static ToastComponent getToastComponent(Minecraft client) {
        try {
            return client.getToasts();
        }
        catch (NoSuchFieldError | NoSuchMethodError e) {
            return null;
        }
    }
}
