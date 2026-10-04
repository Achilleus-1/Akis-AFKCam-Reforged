package com.ji.afkcinematic.music;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ji.afkcinematic.JiAFKCinematic;
import com.ji.afkcinematic.music.CinematicMusicManager;
import java.awt.Desktop;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.ModList;
import net.minecraft.client.Minecraft;

public final class LocalMusicPackManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String PACK_ID = "file/ji-afk-cinematic-local";
    private static final String NAMESPACE = "ji_afk_cinematic_local";
    private static final String FINGERPRINT_FILE = ".ji-afk-source.sha256";

    private LocalMusicPackManager() {
    }

    public static Path getMusicFolder() {
        return FMLPaths.CONFIGDIR.get().resolve("ji-afk-cinematic").resolve("music");
    }

    public static void initialize() {
        LocalMusicPackManager.rebuild(false);
    }

    public static void openMusicFolder() {
        try {
            Path folder = LocalMusicPackManager.getMusicFolder();
            Files.createDirectories(folder, new FileAttribute[0]);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(folder.toFile());
            }
        }
        catch (Exception e) {
            JiAFKCinematic.LOGGER.warn("Could not open local music folder", (Throwable)e);
        }
    }

    public static int rebuildAndReload() {
        return LocalMusicPackManager.rebuild(true);
    }

    public static boolean hasSourceChanges() {
        try {
            Path musicFolder = LocalMusicPackManager.getMusicFolder();
            Files.createDirectories(musicFolder, new FileAttribute[0]);
            Path marker = LocalMusicPackManager.getGeneratedPackFolder().resolve(FINGERPRINT_FILE);
            return !Files.exists(marker, new LinkOption[0]) || !Files.readString(marker, StandardCharsets.UTF_8).equals(LocalMusicPackManager.calculateSourceFingerprint(musicFolder));
        }
        catch (Exception e) {
            JiAFKCinematic.LOGGER.warn("Could not inspect the local music folder", (Throwable)e);
            return true;
        }
    }

    private static int rebuild(boolean reload) {
        try {
            Path musicFolder = LocalMusicPackManager.getMusicFolder();
            Files.createDirectories(musicFolder, new FileAttribute[0]);
            Path pack = LocalMusicPackManager.getGeneratedPackFolder();
            Path sounds = pack.resolve("assets").resolve(NAMESPACE).resolve("sounds").resolve("music");
            Files.createDirectories(sounds, new FileAttribute[0]);
            try (Stream<Path> oldFiles = Files.list(sounds);){
                for (Path old : oldFiles.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).toList()) {
                    Files.deleteIfExists(old);
                }
            }
            LinkedHashMap<String, Map<String, List<Map<String, Object>>>> soundDefinitions = new LinkedHashMap<>();
            ArrayList<String> manifestTracks = new ArrayList<>();
            int index = 0;
            try (Stream<Path> files = Files.list(musicFolder);){
                for (Path source : files.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".ogg")).sorted().toList()) {
                    String base = source.getFileName().toString().replaceFirst("(?i)\\.ogg$", "");
                    Object slug = base.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]+", "_");
                    if (((String)slug).isBlank()) {
                        slug = "track";
                    }
                    slug = (String)slug + "_" + index++;
                    Files.copy(source, sounds.resolve((String)slug + ".ogg"), StandardCopyOption.REPLACE_EXISTING);
                    Map<String, List<Map<String, Object>>> sound = Map.of("sounds", List.of(Map.of("name", "ji_afk_cinematic_local:music/" + (String)slug, "stream", true)));
                    soundDefinitions.put("local." + (String)slug, sound);
                    manifestTracks.add("ji_afk_cinematic_local:local." + (String)slug);
                }
            }
            Map<String, Map<String, Object>> packMeta = Map.of("pack", Map.of("pack_format", 34, "supported_formats", Map.of("min_inclusive", 34, "max_inclusive", 999), "description", "Aki AFK Cam Reforged local music"));
            Files.writeString(pack.resolve("pack.mcmeta"), (CharSequence)GSON.toJson(packMeta), StandardCharsets.UTF_8, new OpenOption[0]);
            Files.writeString(pack.resolve("assets").resolve(NAMESPACE).resolve("sounds.json"), (CharSequence)GSON.toJson(soundDefinitions), StandardCharsets.UTF_8, new OpenOption[0]);
            Path manifest = pack.resolve("assets").resolve(NAMESPACE).resolve("ji_afk_cinematic").resolve("music.json");
            Files.createDirectories(manifest.getParent(), new FileAttribute[0]);
            Files.writeString(manifest, (CharSequence)GSON.toJson(Map.of("replace", false, "tracks", manifestTracks)), StandardCharsets.UTF_8, new OpenOption[0]);
            Files.writeString(pack.resolve(FINGERPRINT_FILE), (CharSequence)LocalMusicPackManager.calculateSourceFingerprint(musicFolder), StandardCharsets.UTF_8, new OpenOption[0]);
            Minecraft client = Minecraft.getInstance();
            if (client != null && client.options != null) {
                if (!client.options.resourcePacks.contains(PACK_ID)) {
                    client.options.resourcePacks.add(PACK_ID);
                }
                client.options.incompatibleResourcePacks.remove(PACK_ID);
                client.options.save();
                client.getResourcePackRepository().reload();
                client.getResourcePackRepository().setSelected((Collection)client.options.resourcePacks);
                CinematicMusicManager.onThirdPartyMusicReloaded();
                if (reload) {
                    client.reloadResourcePacks().whenComplete((unused, error) -> {
                        if (error != null) {
                            JiAFKCinematic.LOGGER.warn("Could not reload resources after updating local music", error);
                        } else {
                            client.execute(CinematicMusicManager::onResourcesReloaded);
                        }
                    });
                }
            }
            JiAFKCinematic.LOGGER.info("Prepared {} local cinematic music track(s)", (Object)manifestTracks.size());
            return manifestTracks.size();
        }
        catch (Exception e) {
            JiAFKCinematic.LOGGER.warn("Could not rebuild local cinematic music pack", (Throwable)e);
            return 0;
        }
    }

    private static Path getGeneratedPackFolder() {
        return FMLPaths.GAMEDIR.get().resolve("resourcepacks").resolve("ji-afk-cinematic-local");
    }

    private static String calculateSourceFingerprint(Path musicFolder) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (Stream<Path> files = Files.list(musicFolder);){
            for (Path source : files.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".ogg")).sorted().toList()) {
                digest.update(source.getFileName().toString().getBytes(StandardCharsets.UTF_8));
                digest.update((byte)0);
                digest.update(Files.readAllBytes(source));
                digest.update((byte)0);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
