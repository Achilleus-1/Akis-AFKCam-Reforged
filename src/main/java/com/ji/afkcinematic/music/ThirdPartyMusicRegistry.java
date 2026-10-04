package com.ji.afkcinematic.music;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ji.afkcinematic.JiAFKCinematic;
import com.ji.afkcinematic.music.CinematicMusicManager;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;

public final class ThirdPartyMusicRegistry {
    private static final String MANIFEST_PATH = "ji_afk_cinematic/music.json";
    private static volatile List<SoundEvent> tracks = List.of();

    private ThirdPartyMusicRegistry() {
    }

    public static void init() {}

    public static List<SoundEvent> getTracks() {
        return tracks;
    }

    public static void reloadFrom(ResourceManager manager) {
        LinkedHashSet<ResourceLocation> merged = new LinkedHashSet<>();
        Map<ResourceLocation, List<Resource>> manifests = manager.listResourceStacks("ji_afk_cinematic", id -> id.getPath().equals(MANIFEST_PATH));
        manifests.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            for (Resource resource : entry.getValue()) {
                try {
                    BufferedReader reader = resource.openAsReader();
                    try {
                        JsonObject json = JsonParser.parseReader((Reader)reader).getAsJsonObject();
                        if (json.has("replace") && json.get("replace").getAsBoolean()) {
                            merged.clear();
                        }
                        if (!json.has("tracks") || !json.get("tracks").isJsonArray()) continue;
                        for (JsonElement element : json.getAsJsonArray("tracks")) {
                            String sound = element.isJsonPrimitive() && element.getAsJsonPrimitive().isString() ? element.getAsString() : (element.isJsonObject() && element.getAsJsonObject().has("sound") ? element.getAsJsonObject().get("sound").getAsString() : null);
                            if (sound == null) continue;
                            ResourceLocation id = ResourceLocation.tryParse((String)sound);
                            if (id != null) {
                                merged.add(id);
                                continue;
                            }
                            JiAFKCinematic.LOGGER.warn("Ignoring invalid cinematic music id '{}' from {}", (Object)element, (Object)resource.sourcePackId());
                        }
                    }
                    finally {
                        if (reader == null) continue;
                        ((Reader)reader).close();
                    }
                }
                catch (Exception e) {
                    JiAFKCinematic.LOGGER.warn("Failed to read cinematic music manifest {} from {}", new Object[]{entry.getKey(), resource.sourcePackId(), e});
                }
            }
        });
        ArrayList<SoundEvent> loaded = new ArrayList<SoundEvent>(merged.size());
        for (ResourceLocation id2 : merged) {
            loaded.add(SoundEvent.createVariableRangeEvent((ResourceLocation)id2));
        }
        tracks = List.copyOf(loaded);
        CinematicMusicManager.onThirdPartyMusicReloaded();
        JiAFKCinematic.LOGGER.info("Loaded {} declared third-party cinematic music event(s)", (Object)tracks.size());
    }
}
