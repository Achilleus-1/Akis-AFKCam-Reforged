package com.ji.afkcinematic;

import com.ji.afkcinematic.afk.AFKDetector;
import com.ji.afkcinematic.cinematic.CinematicManager;
import com.ji.afkcinematic.config.ConfigManager;
import com.ji.afkcinematic.diagnostic.MixinState;
import com.ji.afkcinematic.input.GameplayActivityMonitor;
import com.ji.afkcinematic.music.CinematicMusicManager;
import com.ji.afkcinematic.qa.RuntimeProbe;
import com.ji.afkcinematic.render.CinematicHUDManager;
import com.ji.afkcinematic.render.LetterboxRenderer;
import java.util.Set;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import com.ji.afkcinematic.config.ConfigScreen;

import com.ji.afkcinematic.platform.ClientEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = "ji_afk_cinematic", dist = Dist.CLIENT)
public class JiAFKCinematic {
    public JiAFKCinematic(IEventBus modBus, ModContainer container) {
        ClientEvents.install(modBus);
        container.registerExtensionPoint(IConfigScreenFactory.class, (mc, parent) -> new ConfigScreen(parent));
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(this::onInitializeClient));
    }
    public static final String MOD_ID = "ji-afk-cinematic";
    public static final String MOD_NAME = "Aki AFK Cam Reforged";
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"ji-afk-cinematic");
    private static final int MIXIN_DIAGNOSTIC_DELAY_TICKS = 200;
    private static final Set<String> CRITICAL_MIXINS = Set.of("CameraMixin", "InGameHudMixin", "KeyboardMixin", "MouseMixin", "MinecraftClientMixin");
    private static int mixinDiagnosticTicks;
    private static boolean mixinDiagnosticComplete;

    public void onInitializeClient() {
        LOGGER.info("Initializing {} v2.3.1", (Object)MOD_NAME);
        ConfigManager.loadConfig();
        GameplayActivityMonitor.init();
        AFKDetector.init();
        CinematicManager.init();
        CinematicMusicManager.init();
        LetterboxRenderer.init();
        RuntimeProbe.initIfEnabled();
        ClientEvents.onStopping(client -> {
            CinematicManager.fullTeardown();
            CinematicHUDManager.forceRestore();
        });
        ClientEvents.onTick(client -> {
            LetterboxRenderer.tick();
            if (!mixinDiagnosticComplete && ++mixinDiagnosticTicks >= 200) {
                mixinDiagnosticComplete = true;
                JiAFKCinematic.reportMissingCriticalMixins();
            }
        });
    }

    private static void reportMissingCriticalMixins() {
        for (String critical : CRITICAL_MIXINS) {
            if (MixinState.didApply(critical)) continue;
            LOGGER.warn("Critical mixin '{}' did not apply \u2014 the '{}' feature will be broken. This usually means a Minecraft API change; please report this version combination.", (Object)critical, (Object)critical);
        }
    }
}
