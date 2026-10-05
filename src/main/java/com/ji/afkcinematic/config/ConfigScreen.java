package com.ji.afkcinematic.config;

import com.ji.afkcinematic.config.CinematicChatVisibility;
import com.ji.afkcinematic.config.ConfigManager;
import com.ji.afkcinematic.config.DamageAction;
import com.ji.afkcinematic.config.ModConfig;
import com.ji.afkcinematic.config.MusicMode;
import com.ji.afkcinematic.config.PersistentCinematicMode;
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
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen
extends Screen {
    private final Screen parent;
    private ModConfig editConfig;
    private Button openMusicFolderButton;
    private Button refreshMusicButton;
    private Button reportButton;
    private Button modEnabledButton;
    private final List<AbstractWidget> scrollableWidgets = new ArrayList<AbstractWidget>();
    private final Map<AbstractWidget, Integer> scrollableBaseY = new IdentityHashMap<AbstractWidget, Integer>();
    private int contentScroll;
    private int maxContentScroll;
    private static final int NORMAL_CONTENT_TOP = 80;

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
        this.addRenderableWidget(Button.builder(Component.translatable("config.ji_afkcinematic.controls"),
            button -> this.minecraft.setScreen(new net.minecraft.client.gui.screens.options.controls.KeyBindsScreen(this, this.minecraft.options)))
            .tooltip(Tooltip.create(Component.translatable("config.ji_afkcinematic.tooltip.controls")))
            .bounds(col1X, centerStartY += entryHeight + 5, widgetWidth, 20).build());
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
        context.drawCenteredString(this.font, (Component)Component.literal((String)"\u00a75Product of Achilleus"), this.width / 2, titleY + 10, -1);
    }

    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }

}
