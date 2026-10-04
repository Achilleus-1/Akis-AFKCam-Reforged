package com.ji.afkcinematic.config;

import com.ji.afkcinematic.config.CinematicChatVisibility;
import com.ji.afkcinematic.config.ConfigManager;
import com.ji.afkcinematic.config.DamageAction;
import com.ji.afkcinematic.config.ModConfig;
import com.ji.afkcinematic.config.MusicMode;
import com.ji.afkcinematic.config.PersistentCinematicMode;
import com.ji.afkcinematic.input.KeySequenceTracker;
import com.ji.afkcinematic.music.LocalMusicPackManager;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.lwjgl.glfw.GLFW;

public class ConfigScreen
extends Screen {
    private final Screen parent;
    private ModConfig editConfig;
    private RebindState rebindState = RebindState.IDLE;
    private int backupMenu1;
    private int backupMenu2;
    private int backupToggle1;
    private int backupToggle2;
    private int backupImmediate1;
    private int backupImmediate2;
    private long rebindStartedMs = 0L;
    private Button menuKeyButton;
    private Button toggleKeyButton;
    private Button immediateKeyButton;
    private Button openMusicFolderButton;
    private Button refreshMusicButton;
    private Button reportButton;
    private Button modEnabledButton;
    private final List<AbstractWidget> scrollableWidgets = new ArrayList<AbstractWidget>();
    private final Map<AbstractWidget, Integer> scrollableBaseY = new IdentityHashMap<AbstractWidget, Integer>();
    private int contentScroll;
    private int maxContentScroll;
    private static final int NORMAL_CONTENT_TOP = 80;
    private Button pendingLabelTarget;
    private Component pendingLabelOriginal;
    private int pendingLabelTicksRemaining;

    public ConfigScreen(Screen parent) {
        super((Component)Component.translatable((String)"config.ji_afkcinematic.title"));
        this.parent = parent;
        this.cloneConfig();
    }

    private void cloneConfig() {
        ModConfig current = ConfigManager.getConfig();
        this.editConfig = new ModConfig();
        this.editConfig.shotDurationSeconds = current.shotDurationSeconds;
        this.editConfig.afkThresholdSeconds = current.afkThresholdSeconds;
        this.editConfig.maxCycles = current.maxCycles;
        this.editConfig.cameraSpeed = current.cameraSpeed;
        this.editConfig.characterShotPercentage = current.characterShotPercentage;
        this.editConfig.persistentMode = current.persistentMode;
        this.editConfig.cameraRotationEnabled = current.cameraRotationEnabled;
        this.editConfig.damageAction = current.damageAction;
        this.editConfig.extendedMusic = current.extendedMusic;
        this.editConfig.modEnabled = current.modEnabled;
        this.editConfig.enableLetterbox = current.enableLetterbox;
        this.editConfig.chatVisibility = current.chatVisibility;
        this.editConfig.enableMusic = current.enableMusic;
        this.editConfig.musicMode = current.musicMode;
        this.editConfig.menuKey1 = current.menuKey1;
        this.editConfig.menuKey2 = current.menuKey2;
        this.editConfig.toggleKey1 = current.toggleKey1;
        this.editConfig.toggleKey2 = current.toggleKey2;
        this.editConfig.immediateKey1 = current.immediateKey1;
        this.editConfig.immediateKey2 = current.immediateKey2;
        this.editConfig.cinematicMusicVolume = current.cinematicMusicVolume;
    }

    protected void init() {
        this.scrollableWidgets.clear();
        this.scrollableBaseY.clear();
        int centerX = this.width / 2;
        int yLeft = this.getContentTop() + 5;
        int yRight = this.getContentTop() + 5;
        int widgetWidth = 135;
        int entryHeight = 26;
        int col1X = centerX - 140;
        int col2X = centerX + 5;
        this.addRenderableWidget(new AbstractSliderButton(col1X, yLeft, widgetWidth, 20, (Component)Component.translatable((String)"config.ji_afkcinematic.music_volume", (Object[])new Object[]{(int)(this.editConfig.cinematicMusicVolume * 100.0f)}), this.editConfig.cinematicMusicVolume){
            {
                this.setTooltip(Tooltip.create((Component)Component.translatable((String)"config.ji_afkcinematic.tooltip.music_volume")));
            }

            protected void updateMessage() {
                int val = (int)(this.value * 100.0);
                this.setMessage((Component)Component.translatable((String)"config.ji_afkcinematic.music_volume", (Object[])new Object[]{val}));
            }

            protected void applyValue() {
                ConfigScreen.this.editConfig.cinematicMusicVolume = (float)this.value;
            }
        });
        this.addRenderableWidget(new AbstractSliderButton(col1X, yLeft += entryHeight, widgetWidth, 20, (Component)Component.translatable((String)"config.ji_afkcinematic.shot_duration", (Object[])new Object[]{this.editConfig.shotDurationSeconds}), ((double)this.editConfig.shotDurationSeconds - 5.0) / 55.0){
            {
                this.setTooltip(Tooltip.create((Component)Component.translatable((String)"config.ji_afkcinematic.tooltip.shot_duration")));
            }

            protected void updateMessage() {
                int val = 5 + (int)(this.value * 55.0);
                this.setMessage((Component)Component.translatable((String)"config.ji_afkcinematic.shot_duration", (Object[])new Object[]{val}));
            }

            protected void applyValue() {
                ConfigScreen.this.editConfig.shotDurationSeconds = 5 + (int)(this.value * 55.0);
            }
        });
        this.addRenderableWidget(new AbstractSliderButton(col1X, yLeft += entryHeight, widgetWidth, 20, (Component)Component.translatable((String)"config.ji_afkcinematic.afk_threshold", (Object[])new Object[]{this.editConfig.afkThresholdSeconds}), ((double)this.editConfig.afkThresholdSeconds - 10.0) / 590.0){
            {
                this.setTooltip(Tooltip.create((Component)Component.translatable((String)"config.ji_afkcinematic.tooltip.afk_threshold")));
            }

            protected void updateMessage() {
                int val = 10 + (int)(this.value * 590.0);
                this.setMessage((Component)Component.translatable((String)"config.ji_afkcinematic.afk_threshold", (Object[])new Object[]{val}));
            }

            protected void applyValue() {
                ConfigScreen.this.editConfig.afkThresholdSeconds = 10 + (int)(this.value * 590.0);
            }
        });
        this.addRenderableWidget(new AbstractSliderButton(col1X, yLeft += entryHeight, widgetWidth, 20, this.getMaxCyclesText(), (this.editConfig.isUnlimitedCycles() ? 20.0 : (double)this.editConfig.maxCycles - 1.0) / 20.0){
            {
                this.setTooltip(Tooltip.create((Component)Component.translatable((String)"config.ji_afkcinematic.tooltip.max_cycles")));
            }

            private int selectedIndex() {
                return (int)Math.round(this.value * 20.0);
            }

            protected void updateMessage() {
                int index = this.selectedIndex();
                this.setMessage((Component)Component.translatable((String)"config.ji_afkcinematic.max_cycles", (Object[])new Object[]{index == 20 ? Component.translatable((String)"config.ji_afkcinematic.unlimited") : Integer.toString(index + 1)}));
            }

            protected void applyValue() {
                int index = this.selectedIndex();
                this.value = (double)index / 20.0;
                ConfigScreen.this.editConfig.maxCycles = index == 20 ? -1 : index + 1;
                this.updateMessage();
            }
        });
        this.addRenderableWidget(new AbstractSliderButton(col1X, yLeft += entryHeight, widgetWidth, 20, (Component)Component.translatable((String)"config.ji_afkcinematic.camera_speed", (Object[])new Object[]{String.format("%.1f", Float.valueOf(this.editConfig.cameraSpeed))}), ((double)this.editConfig.cameraSpeed - 0.1) / 2.9){
            {
                this.setTooltip(Tooltip.create((Component)Component.translatable((String)"config.ji_afkcinematic.tooltip.camera_speed")));
            }

            protected void updateMessage() {
                float val = 0.1f + (float)(this.value * 2.9);
                this.setMessage((Component)Component.translatable((String)"config.ji_afkcinematic.camera_speed", (Object[])new Object[]{String.format("%.1f", Float.valueOf(val))}));
            }

            protected void applyValue() {
                ConfigScreen.this.editConfig.cameraSpeed = 0.1f + (float)(this.value * 2.9);
            }
        });
        this.addRenderableWidget(new AbstractSliderButton(col1X, yLeft += entryHeight, widgetWidth, 20, (Component)Component.translatable((String)"config.ji_afkcinematic.shot_mix", (Object[])new Object[]{this.editConfig.characterShotPercentage, 100 - this.editConfig.characterShotPercentage}), (double)this.editConfig.characterShotPercentage / 100.0){
            {
                this.setTooltip(Tooltip.create((Component)Component.translatable((String)"config.ji_afkcinematic.tooltip.shot_mix")));
            }

            private int snappedValue() {
                return (int)Math.round(this.value * 10.0) * 10;
            }

            protected void updateMessage() {
                int val = this.snappedValue();
                this.setMessage((Component)Component.translatable((String)"config.ji_afkcinematic.shot_mix", (Object[])new Object[]{val, 100 - val}));
            }

            protected void applyValue() {
                int val = this.snappedValue();
                this.value = (double)val / 100.0;
                ConfigScreen.this.editConfig.characterShotPercentage = val;
                this.updateMessage();
            }
        });
        yLeft += entryHeight;
        this.addRenderableWidget(Button.builder((Component)this.getDamageActionText(), button -> {
            int nextOrdinal = (this.editConfig.damageAction.ordinal() + 1) % DamageAction.values().length;
            this.editConfig.damageAction = DamageAction.values()[nextOrdinal];
            button.setMessage(this.getDamageActionText());
        }).tooltip(Tooltip.create((Component)Component.translatable((String)"config.ji_afkcinematic.tooltip.damage_action"))).bounds(col2X, yRight, widgetWidth, 20).build());
        this.addRenderableWidget(Button.builder((Component)this.getPersistentModeText(), button -> {
            this.editConfig.persistentMode = this.editConfig.persistentMode.next();
            button.setMessage(this.getPersistentModeText());
        }).tooltip(Tooltip.create((Component)Component.translatable((String)"config.ji_afkcinematic.tooltip.persistent_cinematics").append("\n").append((Component)Component.literal((String)"[BETA]").withStyle(ChatFormatting.YELLOW)))).bounds(col2X, yRight += entryHeight, widgetWidth, 20).build());
        this.addRenderableWidget(Button.builder((Component)this.getChatVisibilityText(), button -> {
            this.editConfig.chatVisibility = this.editConfig.chatVisibility.next();
            button.setMessage(this.getChatVisibilityText());
        }).tooltip(Tooltip.create((Component)Component.translatable((String)"config.ji_afkcinematic.tooltip.chat_visibility"))).bounds(col2X, yRight += entryHeight, widgetWidth, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.translatable((String)"config.ji_afkcinematic.camera_rotation").append(": ").append(this.getOnOffText(this.editConfig.cameraRotationEnabled)), button -> {
            this.editConfig.cameraRotationEnabled = !this.editConfig.cameraRotationEnabled;
            button.setMessage((Component)Component.translatable((String)"config.ji_afkcinematic.camera_rotation").append(": ").append(this.getOnOffText(this.editConfig.cameraRotationEnabled)));
        }).tooltip(Tooltip.create((Component)Component.translatable((String)"config.ji_afkcinematic.tooltip.camera_rotation"))).bounds(col2X, yRight += entryHeight, widgetWidth, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.translatable((String)"config.ji_afkcinematic.letterbox").append(": ").append(this.getOnOffText(this.editConfig.enableLetterbox)), button -> {
            this.editConfig.enableLetterbox = !this.editConfig.enableLetterbox;
            button.setMessage((Component)Component.translatable((String)"config.ji_afkcinematic.letterbox").append(": ").append(this.getOnOffText(this.editConfig.enableLetterbox)));
        }).tooltip(Tooltip.create((Component)Component.translatable((String)"config.ji_afkcinematic.tooltip.letterbox"))).bounds(col2X, yRight += entryHeight, widgetWidth, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.translatable((String)"config.ji_afkcinematic.music").append(": ").append(this.getOnOffText(this.editConfig.enableMusic)), button -> {
            this.editConfig.enableMusic = !this.editConfig.enableMusic;
            button.setMessage((Component)Component.translatable((String)"config.ji_afkcinematic.music").append(": ").append(this.getOnOffText(this.editConfig.enableMusic)));
        }).tooltip(Tooltip.create((Component)Component.translatable((String)"config.ji_afkcinematic.tooltip.music"))).bounds(col2X, yRight += entryHeight, widgetWidth, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.translatable((String)"config.ji_afkcinematic.extended_music").append(": ").append(this.getOnOffText(this.editConfig.extendedMusic)), button -> {
            this.editConfig.extendedMusic = !this.editConfig.extendedMusic;
            button.setMessage((Component)Component.translatable((String)"config.ji_afkcinematic.extended_music").append(": ").append(this.getOnOffText(this.editConfig.extendedMusic)));
        }).tooltip(Tooltip.create((Component)Component.translatable((String)"config.ji_afkcinematic.tooltip.extended_music"))).bounds(col2X, yRight += entryHeight, widgetWidth, 20).build());
        this.addRenderableWidget(Button.builder((Component)this.getMusicModeText(), button -> {
            this.editConfig.musicMode = this.editConfig.musicMode.next();
            button.setMessage(this.getMusicModeText());
            this.updateMusicFolderVisibility();
        }).tooltip(Tooltip.create((Component)Component.translatable((String)"config.ji_afkcinematic.tooltip.music_mode"))).bounds(col1X, yLeft, widgetWidth, 20).build());
        int centerStartY = Math.max(yLeft += entryHeight, yRight += entryHeight) + 5;
        this.openMusicFolderButton = Button.builder((Component)Component.translatable((String)"config.ji_afkcinematic.open_music_folder"), button -> LocalMusicPackManager.openMusicFolder()).tooltip(Tooltip.create((Component)Component.translatable((String)"config.ji_afkcinematic.tooltip.open_music_folder"))).bounds(col1X, centerStartY, widgetWidth, 20).build();
        this.addRenderableWidget(this.openMusicFolderButton);
        this.refreshMusicButton = Button.builder((Component)Component.translatable((String)"config.ji_afkcinematic.refresh_music"), button -> {
            LocalMusicPackManager.rebuildAndReload();
            button.setMessage((Component)Component.translatable((String)"config.ji_afkcinematic.refresh_music_done"));
        }).tooltip(Tooltip.create((Component)Component.translatable((String)"config.ji_afkcinematic.tooltip.refresh_music"))).bounds(col2X, centerStartY, widgetWidth, 20).build();
        this.addRenderableWidget(this.refreshMusicButton);
        this.menuKeyButton = Button.builder((Component)this.getMenuKeysText(), button -> this.startMenuRebind()).tooltip(Tooltip.create((Component)Component.translatable((String)"config.ji_afkcinematic.tooltip.menu_keys"))).bounds(col1X, centerStartY += entryHeight + 5, widgetWidth, 20).build();
        this.addRenderableWidget(this.menuKeyButton);
        this.toggleKeyButton = Button.builder((Component)this.getToggleKeysText(), button -> this.startToggleRebind()).tooltip(Tooltip.create((Component)Component.translatable((String)"config.ji_afkcinematic.tooltip.toggle_keys"))).bounds(col2X, centerStartY, widgetWidth, 20).build();
        this.addRenderableWidget(this.toggleKeyButton);
        this.immediateKeyButton = Button.builder((Component)this.getImmediateKeysText(), button -> this.startImmediateRebind()).tooltip(Tooltip.create((Component)Component.translatable((String)"config.ji_afkcinematic.tooltip.immediate_keys"))).bounds(col1X, centerStartY += entryHeight, widgetWidth, 20).build();
        this.addRenderableWidget(this.immediateKeyButton);
        this.modEnabledButton = Button.builder((Component)Component.translatable((String)"config.ji_afkcinematic.enabled").append(": ").append(this.getActiveDisabledText(this.editConfig.modEnabled)), button -> {
            this.editConfig.modEnabled = !this.editConfig.modEnabled;
            button.setMessage((Component)Component.translatable((String)"config.ji_afkcinematic.enabled").append(": ").append(this.getActiveDisabledText(this.editConfig.modEnabled)));
        }).tooltip(Tooltip.create((Component)Component.translatable((String)"config.ji_afkcinematic.tooltip.enabled"))).bounds(col2X, centerStartY, widgetWidth, 20).build();
        this.addRenderableWidget(this.modEnabledButton);
        this.captureScrollableWidgets();
        this.updateMusicFolderVisibility();
        this.reportButton = Button.builder((Component)Component.literal((String)"\u00a7e\u26a0"), (Button.OnPress)ConfirmLinkScreen.confirmLink((Screen)this, (String)"https://github.com/Achilleus-1/Akis-AFKCam-Reforged/issues")).tooltip(Tooltip.create((Component)Component.translatable((String)"config.ji_afkcinematic.tooltip.report"))).bounds(this.width - 35, this.height - 35, 30, 30).build();
        this.addRenderableWidget(this.reportButton);
        int bottomY = this.height - 35;
        this.addRenderableWidget(Button.builder((Component)Component.translatable((String)"config.ji_afkcinematic.reset_defaults"), button -> {
            this.editConfig = new ModConfig();
            this.rebuildWidgets();
        }).bounds(centerX - 155, bottomY, 100, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.translatable((String)"config.ji_afkcinematic.save"), button -> {
            ModConfig current = ConfigManager.getConfig();
            boolean musicSettingsChanged = current.musicMode != this.editConfig.musicMode || current.enableMusic != this.editConfig.enableMusic || current.extendedMusic != this.editConfig.extendedMusic || LocalMusicPackManager.hasSourceChanges();
            this.editConfig.recalculate();
            ConfigManager.setConfig(this.editConfig);
            if (musicSettingsChanged) {
                LocalMusicPackManager.rebuildAndReload();
            }
            this.onClose();
        }).bounds(centerX - 50, bottomY, 100, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.translatable((String)"config.ji_afkcinematic.cancel"), button -> this.onClose()).bounds(centerX + 55, bottomY, 100, 20).build());
        this.configureScrolling();
    }

    private void captureScrollableWidgets() {
        for (Object child : this.children()) {
            if (!(child instanceof AbstractWidget)) continue;
            AbstractWidget widget = (AbstractWidget)child;
            this.scrollableWidgets.add(widget);
            this.scrollableBaseY.put(widget, widget.getY());
        }
    }

    private void configureScrolling() {
        int viewportBottom = this.height - 45;
        int contentBottom = this.getContentTop();
        for (AbstractWidget widget : this.scrollableWidgets) {
            contentBottom = Math.max(contentBottom, this.scrollableBaseY.get(widget) + widget.getHeight());
        }
        this.maxContentScroll = Math.max(0, contentBottom - viewportBottom + 4);
        this.contentScroll = Math.max(0, Math.min(this.contentScroll, this.maxContentScroll));
        this.applyScrolling();
    }

    private void applyScrolling() {
        int viewportBottom = this.height - 45;
        for (AbstractWidget widget : this.scrollableWidgets) {
            int y = this.scrollableBaseY.get(widget) - this.contentScroll;
            widget.setY(y);
            boolean inside = y >= this.getContentTop() && y + widget.getHeight() <= viewportBottom;
            boolean customMusicControl = widget == this.openMusicFolderButton || widget == this.refreshMusicButton;
            widget.active = widget.visible = inside && (!customMusicControl || this.editConfig.musicMode.includesCustom());
        }
    }

    private int getContentTop() {
        return this.height < 360 ? 46 : 80;
    }

    private void updateMusicFolderVisibility() {
        if (this.openMusicFolderButton == null) {
            return;
        }
        Integer baseY = this.scrollableBaseY.get(this.openMusicFolderButton);
        int y = baseY == null ? this.openMusicFolderButton.getY() : baseY - this.contentScroll;
        boolean inside = y >= this.getContentTop() && y + this.openMusicFolderButton.getHeight() <= this.height - 45;
        this.openMusicFolderButton.active = this.openMusicFolderButton.visible = this.editConfig.musicMode.includesCustom() && inside;
        if (this.refreshMusicButton != null) {
            this.refreshMusicButton.active = this.refreshMusicButton.visible = this.editConfig.musicMode.includesCustom() && inside;
        }
    }

    public void scrollContent(double vertical) {
        if (this.maxContentScroll <= 0) {
            return;
        }
        this.contentScroll = Math.max(0, Math.min(this.maxContentScroll, this.contentScroll - (int)Math.round(vertical * 22.0)));
        this.applyScrolling();
    }

    private Component getDamageActionText() {
        return Component.translatable((String)"config.ji_afkcinematic.damage_action").append(": ").append((Component)Component.translatable((String)("config.ji_afkcinematic.damage_action." + this.editConfig.damageAction.name().toLowerCase())));
    }

    private Component getPersistentModeText() {
        ChatFormatting color = switch (this.editConfig.persistentMode) {
            default -> throw new MatchException(null, null);
            case PersistentCinematicMode.NORMAL -> ChatFormatting.GRAY;
            case PersistentCinematicMode.INTERACTIVE -> ChatFormatting.YELLOW;
            case PersistentCinematicMode.PERSISTENT -> ChatFormatting.RED;
        };
        String key = "config.ji_afkcinematic.persistent_mode." + this.editConfig.persistentMode.name().toLowerCase();
        return Component.translatable((String)"config.ji_afkcinematic.persistent_cinematics").append(": ").append((Component)Component.translatable((String)key).withStyle(color));
    }

    private Component getMaxCyclesText() {
        Object value = this.editConfig.isUnlimitedCycles() ? Component.translatable((String)"config.ji_afkcinematic.unlimited") : Integer.toString(this.editConfig.maxCycles);
        return Component.translatable((String)"config.ji_afkcinematic.max_cycles", (Object[])new Object[]{value});
    }

    private Component getChatVisibilityText() {
        String key = "config.ji_afkcinematic.chat_visibility." + this.editConfig.chatVisibility.name().toLowerCase();
        ChatFormatting color = this.editConfig.chatVisibility == CinematicChatVisibility.VISIBLE ? ChatFormatting.YELLOW : ChatFormatting.RED;
        return Component.translatable((String)"config.ji_afkcinematic.chat_visibility").append(": ").append((Component)Component.translatable((String)key).withStyle(color));
    }

    private Component getMenuKeysText() {
        if (this.isMenuDisabled()) {
            return Component.empty().append((Component)Component.translatable((String)"config.ji_afkcinematic.menu_keys.label").withStyle(ChatFormatting.WHITE)).append((Component)Component.translatable((String)"config.ji_afkcinematic.keybind_disabled_label"));
        }
        String keys = ConfigScreen.formatKeys(this.editConfig.menuKey1, this.editConfig.menuKey2);
        return Component.empty().append((Component)Component.translatable((String)"config.ji_afkcinematic.menu_keys.label").withStyle(ChatFormatting.WHITE)).append((Component)Component.literal((String)keys).withStyle(ChatFormatting.YELLOW));
    }

    private Component getToggleKeysText() {
        if (this.isToggleDisabled()) {
            return Component.empty().append((Component)Component.translatable((String)"config.ji_afkcinematic.toggle_keys.label").withStyle(ChatFormatting.WHITE)).append((Component)Component.translatable((String)"config.ji_afkcinematic.keybind_disabled_label"));
        }
        String keys = ConfigScreen.formatKeys(this.editConfig.toggleKey1, this.editConfig.toggleKey2);
        return Component.empty().append((Component)Component.translatable((String)"config.ji_afkcinematic.toggle_keys.label").withStyle(ChatFormatting.WHITE)).append((Component)Component.literal((String)keys).withStyle(ChatFormatting.YELLOW));
    }

    private Component getImmediateKeysText() {
        return Component.empty().append((Component)Component.translatable((String)"config.ji_afkcinematic.immediate_keys.label").withStyle(ChatFormatting.WHITE)).append((Component)(this.isImmediateDisabled() ? Component.translatable((String)"config.ji_afkcinematic.keybind_disabled_label") : Component.literal((String)ConfigScreen.formatKeys(this.editConfig.immediateKey1, this.editConfig.immediateKey2)).withStyle(ChatFormatting.YELLOW)));
    }

    private Component getMusicModeText() {
        ChatFormatting color = switch (this.editConfig.musicMode) {
            default -> throw new MatchException(null, null);
            case MusicMode.VANILLA -> ChatFormatting.GREEN;
            case MusicMode.MIXED -> ChatFormatting.GOLD;
            case MusicMode.CUSTOM -> ChatFormatting.YELLOW;
        };
        return Component.translatable((String)"config.ji_afkcinematic.music_mode").append(": ").append((Component)Component.translatable((String)("config.ji_afkcinematic.music_mode." + this.editConfig.musicMode.name().toLowerCase())).withStyle(color));
    }

    private Component getActiveDisabledText(boolean value) {
        if (value) {
            return Component.literal((String)"\u00a7a").append((Component)Component.translatable((String)"config.ji_afkcinematic.active"));
        }
        return Component.literal((String)"\u00a7c").append((Component)Component.translatable((String)"config.ji_afkcinematic.disabled"));
    }

    private Component getOnOffText(boolean value) {
        if (value) {
            return Component.literal((String)"\u00a7a").append((Component)Component.translatable((String)"config.ji_afkcinematic.on"));
        }
        return Component.literal((String)"\u00a7c").append((Component)Component.translatable((String)"config.ji_afkcinematic.off"));
    }

    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (this.maxContentScroll > 0) {
            int top = this.getContentTop();
            int bottom = this.height - 45;
            int trackX = this.width / 2 + 150;
            int trackHeight = bottom - top;
            int thumbHeight = Math.max(18, trackHeight * trackHeight / (trackHeight + this.maxContentScroll));
            int thumbY = top + (trackHeight - thumbHeight) * this.contentScroll / this.maxContentScroll;
            context.fill(trackX, top, trackX + 3, bottom, 0x55333333);
            context.fill(trackX, thumbY, trackX + 3, thumbY + thumbHeight, -5592406);
        }
        if (this.reportButton != null) {
            long time = System.currentTimeMillis() / 800L;
            int phase = (int)(time % 3L);
            if (phase == 0) {
                this.reportButton.setMessage((Component)Component.literal((String)"\u00a7e\u26a0"));
            } else if (phase == 1) {
                this.reportButton.setMessage((Component)Component.literal((String)"\u00a7b\u2666"));
            } else {
                this.reportButton.setMessage((Component)Component.literal((String)"\u00a7a\u2709"));
            }
        }
        int titleY = this.height < 360 ? 17 : 55;
        context.drawCenteredString(this.font, (Component)Component.literal((String)"\u00a76\u00a7lAki AFK Cam Reforged"), this.width / 2, titleY, -1);
        context.drawCenteredString(this.font, (Component)Component.literal((String)"\u00a75Port: Achilleus | Original: jiory_"), this.width / 2, titleY + 10, -1);
    }

    public void tick() {
        super.tick();
        if (this.pendingLabelTicksRemaining > 0 && this.pendingLabelTarget != null) {
            --this.pendingLabelTicksRemaining;
            if (this.pendingLabelTicksRemaining == 0 && this.pendingLabelOriginal != null) {
                this.pendingLabelTarget.setMessage(this.pendingLabelOriginal);
                this.pendingLabelTarget = null;
                this.pendingLabelOriginal = null;
            }
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.rebindState != RebindState.IDLE) {
            int[] out;
            int result;
            if (keyCode == 256) {
                if (this.rebindState == RebindState.MENU_WAITING_FIRST || this.rebindState == RebindState.TOGGLE_WAITING_FIRST || this.rebindState == RebindState.IMMEDIATE_WAITING_FIRST) {
                    if (this.rebindState == RebindState.MENU_WAITING_FIRST) {
                        this.editConfig.menuKey1 = -1;
                        this.editConfig.menuKey2 = -1;
                    } else if (this.rebindState == RebindState.TOGGLE_WAITING_FIRST) {
                        this.editConfig.toggleKey1 = -1;
                        this.editConfig.toggleKey2 = -1;
                    } else {
                        this.editConfig.immediateKey1 = -1;
                        this.editConfig.immediateKey2 = -1;
                    }
                    this.rebindState = RebindState.IDLE;
                    KeySequenceTracker.resetRebind();
                    this.refreshKeyButtonLabels();
                } else {
                    this.cancelRebind();
                }
                return true;
            }
            if (this.rebindState == RebindState.MENU_WAITING_FIRST || this.rebindState == RebindState.TOGGLE_WAITING_FIRST || this.rebindState == RebindState.IMMEDIATE_WAITING_FIRST) {
                if (System.currentTimeMillis() - this.rebindStartedMs > 1500L) {
                    this.cancelRebind();
                    return true;
                }
            } else if (KeySequenceTracker.getRebindRemainingMs() <= 0L) {
                this.cancelRebind();
                return true;
            }
            if ((result = KeySequenceTracker.processRebindKey(keyCode, out = new int[2])) == -1) {
                this.cancelRebind();
                return true;
            }
            if (result == 3) {
                Button target;
                this.pendingLabelTarget = target = this.rebindState == RebindState.MENU_WAITING_FIRST || this.rebindState == RebindState.MENU_WAITING_SECOND ? this.menuKeyButton : (this.rebindState == RebindState.TOGGLE_WAITING_FIRST || this.rebindState == RebindState.TOGGLE_WAITING_SECOND ? this.toggleKeyButton : this.immediateKeyButton);
                this.pendingLabelOriginal = target.getMessage();
                target.setMessage((Component)Component.literal((String)"Unsupported key").withStyle(ChatFormatting.RED));
                this.pendingLabelTicksRemaining = 30;
                return true;
            }
            if (this.rebindState == RebindState.MENU_WAITING_FIRST || this.rebindState == RebindState.MENU_WAITING_SECOND) {
                if (result == 1) {
                    this.rebindState = RebindState.MENU_WAITING_SECOND;
                    this.menuKeyButton.setMessage((Component)Component.translatable((String)"config.ji_afkcinematic.key_waiting_second"));
                } else if (result == 2) {
                    this.editConfig.menuKey1 = out[0];
                    this.editConfig.menuKey2 = out[1];
                    this.rebindState = RebindState.IDLE;
                    KeySequenceTracker.resetRebind();
                    this.refreshKeyButtonLabels();
                }
            } else if (this.rebindState == RebindState.TOGGLE_WAITING_FIRST || this.rebindState == RebindState.TOGGLE_WAITING_SECOND) {
                if (result == 1) {
                    this.rebindState = RebindState.TOGGLE_WAITING_SECOND;
                    this.toggleKeyButton.setMessage((Component)Component.translatable((String)"config.ji_afkcinematic.key_waiting_second"));
                } else if (result == 2) {
                    this.editConfig.toggleKey1 = out[0];
                    this.editConfig.toggleKey2 = out[1];
                    this.rebindState = RebindState.IDLE;
                    KeySequenceTracker.resetRebind();
                    this.refreshKeyButtonLabels();
                }
            } else if (result == 1) {
                this.rebindState = RebindState.IMMEDIATE_WAITING_SECOND;
                this.immediateKeyButton.setMessage((Component)Component.translatable((String)"config.ji_afkcinematic.key_waiting_second"));
            } else if (result == 2) {
                this.editConfig.immediateKey1 = out[0];
                this.editConfig.immediateKey2 = out[1];
                this.rebindState = RebindState.IDLE;
                KeySequenceTracker.resetRebind();
                this.refreshKeyButtonLabels();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }

    private static String getKeyName(int keyCode) {
        if (keyCode == -1) {
            return "NONE";
        }
        if (!KeySequenceTracker.isBindableKeyCode(keyCode)) {
            return "Unsupported Key #" + keyCode;
        }
        String name = GLFW.glfwGetKeyName((int)keyCode, (int)0);
        if (name != null) {
            return name.toUpperCase();
        }
        return switch (keyCode) {
            case 290 -> "F1";
            case 291 -> "F2";
            case 292 -> "F3";
            case 293 -> "F4";
            case 294 -> "F5";
            case 295 -> "F6";
            case 296 -> "F7";
            case 297 -> "F8";
            case 298 -> "F9";
            case 299 -> "F10";
            case 300 -> "F11";
            case 301 -> "F12";
            case 340 -> "Shift";
            case 344 -> "Shift";
            case 341 -> "Ctrl";
            case 345 -> "Ctrl";
            case 342 -> "Alt";
            case 346 -> "Alt";
            case 258 -> "Tab";
            case 32 -> "Space";
            case 257 -> "Enter";
            case 259 -> "Backspace";
            case 260 -> "Insert";
            case 261 -> "Delete";
            case 268 -> "Home";
            case 269 -> "End";
            case 266 -> "Page Up";
            case 267 -> "Page Down";
            case 263 -> "\u2190";
            case 262 -> "\u2192";
            case 265 -> "\u2191";
            case 264 -> "\u2193";
            case 65 -> "A";
            case 66 -> "B";
            case 67 -> "C";
            case 68 -> "D";
            case 69 -> "E";
            case 70 -> "F";
            case 71 -> "G";
            case 72 -> "H";
            case 73 -> "I";
            case 74 -> "J";
            case 75 -> "K";
            case 76 -> "L";
            case 77 -> "M";
            case 78 -> "N";
            case 79 -> "O";
            case 80 -> "P";
            case 81 -> "Q";
            case 82 -> "R";
            case 83 -> "S";
            case 84 -> "T";
            case 85 -> "U";
            case 86 -> "V";
            case 87 -> "W";
            case 88 -> "X";
            case 89 -> "Y";
            case 90 -> "Z";
            case 48 -> "0";
            case 49 -> "1";
            case 50 -> "2";
            case 51 -> "3";
            case 52 -> "4";
            case 53 -> "5";
            case 54 -> "6";
            case 55 -> "7";
            case 56 -> "8";
            case 57 -> "9";
            default -> "Key " + keyCode;
        };
    }

    private static String formatKeys(int k1, int k2) {
        boolean isNone2;
        String n1 = ConfigScreen.getKeyName(k1);
        String n2 = ConfigScreen.getKeyName(k2);
        boolean isNone1 = k1 == -1;
        boolean bl = isNone2 = k2 == -1;
        if (isNone1 && isNone2) {
            return "NONE";
        }
        if (isNone1) {
            return n2 + " + NONE";
        }
        if (isNone2) {
            return n1 + " + NONE";
        }
        return n1 + " + " + n2;
    }

    private void startMenuRebind() {
        this.backupMenu1 = this.editConfig.menuKey1;
        this.backupMenu2 = this.editConfig.menuKey2;
        this.rebindState = RebindState.MENU_WAITING_FIRST;
        KeySequenceTracker.startRebind();
        this.rebindStartedMs = System.currentTimeMillis();
        this.menuKeyButton.setMessage((Component)Component.translatable((String)"config.ji_afkcinematic.key_waiting_first"));
    }

    private void startToggleRebind() {
        this.backupToggle1 = this.editConfig.toggleKey1;
        this.backupToggle2 = this.editConfig.toggleKey2;
        this.rebindState = RebindState.TOGGLE_WAITING_FIRST;
        KeySequenceTracker.startRebind();
        this.rebindStartedMs = System.currentTimeMillis();
        this.toggleKeyButton.setMessage((Component)Component.translatable((String)"config.ji_afkcinematic.key_waiting_first"));
    }

    private void startImmediateRebind() {
        this.backupImmediate1 = this.editConfig.immediateKey1;
        this.backupImmediate2 = this.editConfig.immediateKey2;
        this.rebindState = RebindState.IMMEDIATE_WAITING_FIRST;
        KeySequenceTracker.startRebind();
        this.rebindStartedMs = System.currentTimeMillis();
        this.immediateKeyButton.setMessage((Component)Component.translatable((String)"config.ji_afkcinematic.key_waiting_first"));
    }

    private void cancelRebind() {
        if (this.rebindState == RebindState.MENU_WAITING_FIRST || this.rebindState == RebindState.MENU_WAITING_SECOND) {
            this.editConfig.menuKey1 = this.backupMenu1;
            this.editConfig.menuKey2 = this.backupMenu2;
        } else if (this.rebindState == RebindState.TOGGLE_WAITING_FIRST || this.rebindState == RebindState.TOGGLE_WAITING_SECOND) {
            this.editConfig.toggleKey1 = this.backupToggle1;
            this.editConfig.toggleKey2 = this.backupToggle2;
        } else {
            this.editConfig.immediateKey1 = this.backupImmediate1;
            this.editConfig.immediateKey2 = this.backupImmediate2;
        }
        this.rebindState = RebindState.IDLE;
        KeySequenceTracker.resetRebind();
        this.refreshKeyButtonLabels();
    }

    private void refreshKeyButtonLabels() {
        this.menuKeyButton.setMessage(this.getMenuKeysText());
        this.toggleKeyButton.setMessage(this.getToggleKeysText());
        this.immediateKeyButton.setMessage(this.getImmediateKeysText());
    }

    private boolean isMenuDisabled() {
        return this.editConfig.menuKey1 == -1 && this.editConfig.menuKey2 == -1;
    }

    private boolean isToggleDisabled() {
        return this.editConfig.toggleKey1 == -1 && this.editConfig.toggleKey2 == -1;
    }

    private boolean isImmediateDisabled() {
        return this.editConfig.immediateKey1 == -1 && this.editConfig.immediateKey2 == -1;
    }

    private static enum RebindState {
        IDLE,
        MENU_WAITING_FIRST,
        MENU_WAITING_SECOND,
        TOGGLE_WAITING_FIRST,
        TOGGLE_WAITING_SECOND,
        IMMEDIATE_WAITING_FIRST,
        IMMEDIATE_WAITING_SECOND;

    }
}
