package com.ji.afkcinematic.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ji.afkcinematic.JiAFKCinematic;
import com.ji.afkcinematic.config.CinematicChatVisibility;
import com.ji.afkcinematic.config.ModConfig;
import com.ji.afkcinematic.config.MusicMode;
import com.ji.afkcinematic.config.PersistentCinematicMode;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.ModList;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("ji-afk-cinematic.json");
    private static ModConfig config = new ModConfig();

    public static void loadConfig() {
        block17: {
            try {
                if (Files.exists(CONFIG_PATH, new LinkOption[0])) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_PATH.toFile()));){
                        ModConfig loaded;
                        String legacyChat;
                        JsonObject raw = JsonParser.parseReader((Reader)reader).getAsJsonObject();
                        if (raw.has("chatVisibility") && ("PERSISTENT_MODES".equals(legacyChat = raw.get("chatVisibility").getAsString()) || "ALWAYS".equals(legacyChat))) {
                            raw.addProperty("chatVisibility", "VISIBLE");
                        }
                        if ((loaded = (ModConfig)GSON.fromJson((JsonElement)raw, ModConfig.class)) != null) {
                            if (!raw.has("characterShotPercentage")) {
                                loaded.characterShotPercentage = 30;
                            }
                            if (!raw.has("persistentMode")) {
                                boolean legacyPersistent = raw.has("persistentCinematics") && raw.get("persistentCinematics").getAsBoolean();
                                PersistentCinematicMode persistentCinematicMode = loaded.persistentMode = legacyPersistent ? PersistentCinematicMode.INTERACTIVE : PersistentCinematicMode.NORMAL;
                            }
                            if (!raw.has("cameraRotationEnabled")) {
                                loaded.cameraRotationEnabled = false;
                            }
                            if (!raw.has("cinematicMusicVolume")) {
                                loaded.cinematicMusicVolume = 0.5f;
                            }
                            if (!raw.has("chatVisibility")) {
                                loaded.chatVisibility = CinematicChatVisibility.VISIBLE;
                            }
                            if (!raw.has("musicMode")) {
                                loaded.musicMode = raw.has("thirdPartyMusic") && raw.get("thirdPartyMusic").getAsBoolean() ? MusicMode.MIXED : MusicMode.VANILLA;
                            }
                            config = loaded;
                        }
                    }
                    JiAFKCinematic.LOGGER.info("Configuration loaded from {}", (Object)CONFIG_PATH);
                    break block17;
                }
                ConfigManager.saveConfig();
                JiAFKCinematic.LOGGER.info("Default configuration created at {}", (Object)CONFIG_PATH);
            }
            catch (Exception e) {
                JiAFKCinematic.LOGGER.error("Failed to load config, using defaults", (Throwable)e);
                config = new ModConfig();
            }
        }
        ConfigManager.migrateIfNeeded();
        config.recalculate();
    }

    private static void migrateIfNeeded() {
        if (ConfigManager.config.configVersion >= 8) {
            return;
        }
        JiAFKCinematic.LOGGER.info("Migrating config v{} -> v{}", (Object)ConfigManager.config.configVersion, (Object)8);
        if (ConfigManager.config.configVersion < 4) {
            if (ConfigManager.config.characterShotPercentage == 50) {
                ConfigManager.config.characterShotPercentage = 30;
            }
            ConfigManager.config.cameraRotationEnabled = false;
        }
        if (ConfigManager.config.configVersion < 5 && ConfigManager.config.persistentMode == null) {
            ConfigManager.config.persistentMode = PersistentCinematicMode.NORMAL;
        }
        if (ConfigManager.config.configVersion < 6) {
            if (ConfigManager.config.chatVisibility == null) {
                ConfigManager.config.chatVisibility = CinematicChatVisibility.VISIBLE;
            }
            ConfigManager.config.thirdPartyMusic = false;
        }
        if (ConfigManager.config.configVersion < 7) {
            if (ConfigManager.config.chatVisibility == null) {
                ConfigManager.config.chatVisibility = CinematicChatVisibility.VISIBLE;
            }
            if (ConfigManager.config.toggleKey1 == 341 && ConfigManager.config.toggleKey2 == 72) {
                ConfigManager.config.toggleKey1 = 296;
                ConfigManager.config.toggleKey2 = 73;
            }
        }
        if (ConfigManager.config.configVersion < 8) {
            if (ConfigManager.config.toggleKey1 == 296 && ConfigManager.config.toggleKey2 == 73) {
                ConfigManager.config.toggleKey1 = 341;
                ConfigManager.config.toggleKey2 = 72;
            }
            ConfigManager.config.immediateKey1 = 296;
            ConfigManager.config.immediateKey2 = 73;
            if (ConfigManager.config.musicMode == null) {
                ConfigManager.config.musicMode = ConfigManager.config.thirdPartyMusic ? MusicMode.MIXED : MusicMode.VANILLA;
            }
        }
        ConfigManager.config.configVersion = 8;
        ConfigManager.saveConfig();
    }

    public static void saveConfig() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent(), new FileAttribute[0]);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_PATH.toFile()));){
                GSON.toJson((Object)config, (Appendable)writer);
            }
            JiAFKCinematic.LOGGER.info("Configuration saved to {}", (Object)CONFIG_PATH);
        }
        catch (IOException e) {
            JiAFKCinematic.LOGGER.error("Failed to save config", (Throwable)e);
        }
    }

    public static ModConfig getConfig() {
        return config;
    }

    public static void setConfig(ModConfig newConfig) {
        config = newConfig;
        config.recalculate();
        ConfigManager.saveConfig();
    }
}
