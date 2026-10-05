package com.ji.afkcinematic.input;

import com.google.common.base.Suppliers;
import com.ji.afkcinematic.cinematic.CinematicManager;
import com.ji.afkcinematic.cinematic.CinematicState;
import com.ji.afkcinematic.config.ConfigManager;
import com.ji.afkcinematic.config.ConfigScreen;
import com.ji.afkcinematic.platform.ClientEvents;
import com.ji.afkcinematic.render.ToggleToastManager;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Supplier;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

/** Ordinary Minecraft bindings: user assignments and conflicts belong to Controls. */
public final class ModKeyMappings {
    private static final String CATEGORY = "key.categories.ji_afk_cinematic";
    public static final Supplier<KeyMapping> OPEN_SETTINGS = binding("open_settings");
    public static final Supplier<KeyMapping> TOGGLE_ENABLED = binding("toggle_enabled");
    public static final Supplier<KeyMapping> TOGGLE_CINEMATIC = binding("toggle_cinematic");

    private ModKeyMappings() {}

    private static Supplier<KeyMapping> binding(String action) {
        return Suppliers.memoize(() -> new KeyMapping("key.ji_afk_cinematic." + action,
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(), CATEGORY));
    }

    public static void install(IEventBus modBus) {
        modBus.addListener((RegisterKeyMappingsEvent event) -> {
            event.register(OPEN_SETTINGS.get());
            event.register(TOGGLE_ENABLED.get());
            event.register(TOGGLE_CINEMATIC.get());
        });
        ClientEvents.onTick(ModKeyMappings::tick);
    }

    public static boolean matches(InputConstants.Key key) {
        return OPEN_SETTINGS.get().isActiveAndMatches(key)
            || TOGGLE_ENABLED.get().isActiveAndMatches(key)
            || TOGGLE_CINEMATIC.get().isActiveAndMatches(key);
    }

    private static void tick(Minecraft client) {
        while (OPEN_SETTINGS.get().consumeClick()) {
            if (client.player != null && client.screen == null) {
                client.setScreen(new ConfigScreen(null));
            }
        }
        while (TOGGLE_ENABLED.get().consumeClick()) {
            if (client.player == null || client.screen != null) continue;
            var config = ConfigManager.getConfig();
            config.modEnabled = !config.modEnabled;
            ConfigManager.saveConfig();
            ToggleToastManager.show(config.modEnabled);
            if (!config.modEnabled) CinematicManager.forceDeactivate();
        }
        while (TOGGLE_CINEMATIC.get().consumeClick()) {
            if (client.player == null || client.screen != null) continue;
            if (CinematicManager.getState() == CinematicState.CINEMATIC_ACTIVE) {
                CinematicManager.forceDeactivate();
            } else if (ConfigManager.getConfig().modEnabled) {
                CinematicManager.toggleImmediate();
            }
        }
    }
}
