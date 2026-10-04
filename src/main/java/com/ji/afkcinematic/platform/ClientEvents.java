package com.ji.afkcinematic.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import com.ji.afkcinematic.music.ThirdPartyMusicRegistry;

/** Native NeoForge lifecycle hooks, preserving the original callback order. */
public final class ClientEvents {
    private static final List<Consumer<Minecraft>> ticks = new ArrayList<>();
    private static final List<Consumer<Minecraft>> stopping = new ArrayList<>();
    private static final List<BiConsumer<GuiGraphics, DeltaTracker>> hud = new ArrayList<>();
    public static void onTick(Consumer<Minecraft> callback) { ticks.add(callback); }
    public static void onStopping(Consumer<Minecraft> callback) { stopping.add(callback); }
    public static void onHud(BiConsumer<GuiGraphics, DeltaTracker> callback) { hud.add(callback); }
    public static void install(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> ticks.forEach(callback -> callback.accept(Minecraft.getInstance())));
        NeoForge.EVENT_BUS.addListener((GameShuttingDownEvent event) -> stopping.forEach(callback -> callback.accept(Minecraft.getInstance())));
        NeoForge.EVENT_BUS.addListener((RenderGuiEvent.Post event) -> hud.forEach(callback -> callback.accept(event.getGuiGraphics(), event.getPartialTick())));
        modBus.addListener((RegisterClientReloadListenersEvent event) -> event.registerReloadListener((ResourceManagerReloadListener) ThirdPartyMusicRegistry::reloadFrom));
    }
}
