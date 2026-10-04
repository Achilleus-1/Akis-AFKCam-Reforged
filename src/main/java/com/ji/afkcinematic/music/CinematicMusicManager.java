package com.ji.afkcinematic.music;

import com.ji.afkcinematic.JiAFKCinematic;
import com.ji.afkcinematic.config.ConfigManager;
import com.ji.afkcinematic.config.ModConfig;
import com.ji.afkcinematic.config.MusicMode;
import com.ji.afkcinematic.music.BalancedShuffleBag;
import com.ji.afkcinematic.music.CinematicMusicInstance;
import com.ji.afkcinematic.music.LocalMusicPackManager;
import com.ji.afkcinematic.music.ThirdPartyMusicRegistry;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import com.ji.afkcinematic.platform.ClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class CinematicMusicManager {
    public static boolean isOurMusicPlaying = false;
    private static CinematicMusicInstance currentInstance;
    private static FadeState state;
    public static float vanillaMusicVolumeMultiplier;
    private static float originalMusicVolume;
    private static float currentFade;
    private static final float FADE_SPEED = 0.01f;
    private static int missingTrackRetryTicks;
    private static final List<Object> trackPool;
    private static final BalancedShuffleBag<Object> shuffleBag;
    private static Method cachedUpdateSoundVolume;
    private static boolean updateSoundVolumeMissing;

    public static void init() {
        LocalMusicPackManager.initialize();
        ThirdPartyMusicRegistry.init();
        ClientEvents.onTick(client -> CinematicMusicManager.tick(client));
    }

    public static void checkAndPlayMusic() {
        Minecraft client = Minecraft.getInstance();
        if (isOurMusicPlaying) {
            CinematicMusicManager.retryMissingTrack(client);
            return;
        }
        state = FadeState.FADE_OUT_GAME;
        originalMusicVolume = CinematicMusicManager.getMusicOptionVolume(client);
        currentFade = 1.0f;
        isOurMusicPlaying = true;
        missingTrackRetryTicks = 0;
        CinematicMusicManager.playCinematicMusicSafe(client);
        missingTrackRetryTicks = 20;
    }

    public static void stopMusic() {
        if (isOurMusicPlaying) {
            state = FadeState.FADE_OUT_CINEMATIC;
            if (currentInstance != null) {
                currentInstance.fadeOutAndStop();
            } else {
                isOurMusicPlaying = false;
                currentInstance = null;
                state = FadeState.IDLE;
                if (originalMusicVolume != -1.0f) {
                    CinematicMusicManager.setMusicOptionVolume(Minecraft.getInstance(), originalMusicVolume);
                    originalMusicVolume = -1.0f;
                }
            }
        }
    }

    public static void forceStop() {
        if (currentInstance != null) {
            currentInstance.forceStop();
            currentInstance = null;
        }
        isOurMusicPlaying = false;
        missingTrackRetryTicks = 0;
        state = FadeState.IDLE;
        if (originalMusicVolume != -1.0f) {
            CinematicMusicManager.setMusicOptionVolume(Minecraft.getInstance(), originalMusicVolume);
            originalMusicVolume = -1.0f;
        }
    }

    private static void tick(Minecraft client) {
        if (missingTrackRetryTicks > 0) {
            --missingTrackRetryTicks;
        }
        if (isOurMusicPlaying && state != FadeState.FADE_OUT_CINEMATIC) {
            CinematicMusicManager.retryMissingTrack(client);
        }
        if (state == FadeState.IDLE) {
            return;
        }
        if (state == FadeState.FADE_OUT_GAME) {
            if ((currentFade -= 0.01f) <= 0.0f) {
                currentFade = 0.0f;
                CinematicMusicManager.setMusicOptionVolume(client, 0.0f);
                CinematicMusicManager.stopVanillaMusic(client);
                state = FadeState.FADE_IN_CINEMATIC;
            } else {
                CinematicMusicManager.setMusicOptionVolume(client, originalMusicVolume * currentFade);
            }
        } else if (state == FadeState.FADE_IN_CINEMATIC) {
            if ((currentFade += 0.01f) >= 1.0f) {
                currentFade = 1.0f;
                state = FadeState.IDLE;
            }
            CinematicMusicManager.setMusicOptionVolume(client, originalMusicVolume * currentFade);
        } else if (state == FadeState.FADE_OUT_CINEMATIC && (currentInstance == null || currentInstance.isStopped())) {
            if (originalMusicVolume != -1.0f) {
                CinematicMusicManager.setMusicOptionVolume(client, originalMusicVolume);
                originalMusicVolume = -1.0f;
            }
            state = FadeState.IDLE;
            isOurMusicPlaying = false;
            currentInstance = null;
        }
    }

    private static float getMusicOptionVolume(Minecraft client) {
        try {
            return ((Double)client.options.getSoundSourceOptionInstance(SoundSource.MUSIC).get()).floatValue();
        }
        catch (Exception e) {
            return 1.0f;
        }
    }

    private static void setMusicOptionVolume(Minecraft client, float volume) {
        if (!Float.isFinite(volume)) {
            return;
        }
        try {
            client.options.getSoundSourceOptionInstance(SoundSource.MUSIC).set((double)volume);
            if (client.getSoundManager() != null && !updateSoundVolumeMissing) {
                Method m = cachedUpdateSoundVolume;
                if (m == null) {
                    for (Method candidate : client.getSoundManager().getClass().getMethods()) {
                        if (!candidate.getName().equals("updateSoundVolume")) continue;
                        cachedUpdateSoundVolume = candidate;
                        m = candidate;
                        break;
                    }
                    if (m == null) {
                        updateSoundVolumeMissing = true;
                    }
                }
                if (m != null) {
                    if (m.getParameterCount() == 1) {
                        m.invoke((Object)client.getSoundManager(), SoundSource.MUSIC);
                    } else if (m.getParameterCount() == 2) {
                        m.invoke((Object)client.getSoundManager(), SoundSource.MUSIC, Float.valueOf(volume));
                    }
                }
            }
        }
        catch (Exception e) {
            JiAFKCinematic.LOGGER.warn("Failed to set music option volume to {}", (Object)Float.valueOf(volume), (Object)e);
        }
    }

    private static void stopVanillaMusic(Minecraft client) {
        if (client.getMusicManager() != null) {
            client.getMusicManager().stopPlaying();
        }
    }

    private static void fillShuffleBag() {
        trackPool.clear();
        ModConfig config = ConfigManager.getConfig();
        MusicMode mode = config.musicMode;
        if (mode.includesVanilla()) {
            CinematicMusicManager.addTrackSafe(() -> SoundEvents.MUSIC_GAME);
            CinematicMusicManager.addTrackSafe(() -> SoundEvents.MUSIC_CREATIVE);
            CinematicMusicManager.addTrackSafe(() -> SoundEvents.MUSIC_MENU);
            CinematicMusicManager.addTrackSafe(() -> SoundEvents.MUSIC_END);
            CinematicMusicManager.addTrackSafe(() -> SoundEvents.MUSIC_CREDITS);
        }
        if (mode.includesVanilla() && config.extendedMusic) {
            CinematicMusicManager.addTrackSafe(() -> SoundEvents.MUSIC_DISC_CAT);
            CinematicMusicManager.addTrackSafe(() -> SoundEvents.MUSIC_DISC_BLOCKS);
            CinematicMusicManager.addTrackSafe(() -> SoundEvents.MUSIC_DISC_CHIRP);
            CinematicMusicManager.addTrackSafe(() -> SoundEvents.MUSIC_DISC_FAR);
            CinematicMusicManager.addTrackSafe(() -> SoundEvents.MUSIC_DISC_MALL);
            CinematicMusicManager.addTrackSafe(() -> SoundEvents.MUSIC_DISC_MELLOHI);
            CinematicMusicManager.addTrackSafe(() -> SoundEvents.MUSIC_DISC_STAL);
            CinematicMusicManager.addTrackSafe(() -> SoundEvents.MUSIC_DISC_STRAD);
            CinematicMusicManager.addTrackSafe(() -> SoundEvents.MUSIC_DISC_WARD);
            CinematicMusicManager.addTrackSafe(() -> SoundEvents.MUSIC_DISC_WAIT);
            CinematicMusicManager.addTrackSafe(() -> SoundEvents.MUSIC_DISC_PIGSTEP);
            CinematicMusicManager.addTrackSafe(() -> SoundEvents.MUSIC_DISC_OTHERSIDE);
            CinematicMusicManager.addTrackSafe(() -> SoundEvents.MUSIC_DISC_RELIC);
            CinematicMusicManager.addTrackSafe(() -> SoundEvents.MUSIC_DISC_CREATOR);
            CinematicMusicManager.addTrackSafe(() -> SoundEvents.MUSIC_DISC_CREATOR_MUSIC_BOX);
            CinematicMusicManager.addTrackSafe(() -> SoundEvents.MUSIC_DISC_PRECIPICE);
        }
        if (mode.includesCustom()) {
            for (SoundEvent track : ThirdPartyMusicRegistry.getTracks()) {
                CinematicMusicManager.addTrackSafe(() -> track);
            }
        }
        shuffleBag.replace(trackPool);
    }

    private static void addTrackSafe(Supplier<Object> getter) {
        try {
            Object track = getter.get();
            if (track != null && !CinematicMusicManager.isForbiddenTrack(track)) {
                trackPool.add(track);
            }
        }
        catch (Throwable e) {
            JiAFKCinematic.LOGGER.warn("Failed to resolve a cinematic music track", e);
        }
    }

    private static boolean isForbiddenTrack(Object track) {
        return Objects.equals(track, SoundEvents.MUSIC_DISC_5) || Objects.equals(track, SoundEvents.MUSIC_DISC_11) || Objects.equals(track, SoundEvents.MUSIC_DISC_13);
    }

    private static Object getNextTrack() {
        if (shuffleBag.isCycleComplete()) {
            CinematicMusicManager.fillShuffleBag();
        }
        return shuffleBag.next();
    }

    static void onThirdPartyMusicReloaded() {
        shuffleBag.replace(List.of());
        missingTrackRetryTicks = 0;
    }

    static void onResourcesReloaded() {
        CinematicMusicManager.onThirdPartyMusicReloaded();
        if (isOurMusicPlaying) {
            if (currentInstance != null) {
                currentInstance.forceStop();
            }
            currentInstance = null;
        }
    }

    private static void retryMissingTrack(Minecraft client) {
        boolean missing;
        boolean bl = missing = currentInstance == null || currentInstance.isStopped() || client.getSoundManager() != null && !client.getSoundManager().isActive((SoundInstance)currentInstance);
        if (missing && missingTrackRetryTicks <= 0) {
            CinematicMusicManager.playCinematicMusicSafe(client);
            missingTrackRetryTicks = 20;
        }
    }

    private static void playCinematicMusicSafe(Minecraft client) {
        try {
            Object track = CinematicMusicManager.getNextTrack();
            if (track == null) {
                return;
            }
            SoundEvent soundEvent = track instanceof SoundEvent ? (SoundEvent)track : (SoundEvent)((Holder)track).value();
            if (currentInstance != null) {
                currentInstance.forceStop();
                currentInstance = null;
            }
            currentInstance = new CinematicMusicInstance(soundEvent);
            if (client.getSoundManager() != null) {
                client.getSoundManager().play((SoundInstance)currentInstance);
            }
        }
        catch (Exception e) {
            JiAFKCinematic.LOGGER.warn("Failed to play cinematic music", (Throwable)e);
        }
    }

    static {
        state = FadeState.IDLE;
        vanillaMusicVolumeMultiplier = 1.0f;
        originalMusicVolume = -1.0f;
        currentFade = 1.0f;
        trackPool = new ArrayList<Object>();
        shuffleBag = new BalancedShuffleBag();
        updateSoundVolumeMissing = false;
    }

    public static enum FadeState {
        IDLE,
        FADE_OUT_GAME,
        FADE_IN_CINEMATIC,
        FADE_OUT_CINEMATIC;

    }
}
