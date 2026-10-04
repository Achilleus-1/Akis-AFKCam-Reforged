package com.ji.afkcinematic.render;

import com.ji.afkcinematic.cinematic.EasingFunctions;
import com.ji.afkcinematic.config.ConfigManager;
import com.ji.afkcinematic.render.HUDController;
import com.ji.afkcinematic.platform.ClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class LetterboxRenderer {
    private static final float BAR_RATIO = 0.16f;
    private static volatile State letterboxState = State.HIDDEN;
    private static volatile float lastProgress = 0.0f;
    private static volatile float currentProgress = 0.0f;
    private static final float FADE_IN_TICKS = 60.0f;
    private static final float FADE_OUT_TICKS = 8.0f;

    public static void init() {
        ClientEvents.onHud((drawContext, renderTickCounter) -> {
            if (HUDController.isHidden()) {
                return;
            }
            LetterboxRenderer.render(drawContext, renderTickCounter.getGameTimeDeltaPartialTick(false));
        });
    }

    public static void fadeIn() {
        if (!ConfigManager.getConfig().enableLetterbox) {
            return;
        }
        letterboxState = State.FADING_IN;
    }

    public static void fadeOut() {
        letterboxState = State.FADING_OUT;
    }

    public static void reset() {
        letterboxState = State.HIDDEN;
        lastProgress = 0.0f;
        currentProgress = 0.0f;
    }

    public static void tick() {
        lastProgress = currentProgress;
        if (letterboxState == State.FADING_IN) {
            if ((currentProgress += 0.016666668f) >= 1.0f) {
                currentProgress = 1.0f;
                letterboxState = State.VISIBLE;
            }
        } else if (letterboxState == State.FADING_OUT && (currentProgress -= 0.125f) <= 0.0f) {
            currentProgress = 0.0f;
            letterboxState = State.HIDDEN;
        }
    }

    public static void renderFromHud(GuiGraphics drawContext, float tickDelta) {
        LetterboxRenderer.render(drawContext, tickDelta);
    }

    private static void render(GuiGraphics drawContext, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();
        int targetBarHeight = (int)((float)height * 0.16f);
        float lerpedProgress = lastProgress + (currentProgress - lastProgress) * tickDelta;
        if (lerpedProgress <= 0.0f && letterboxState == State.HIDDEN) {
            return;
        }
        float eased = EasingFunctions.easeInOutCubic(lerpedProgress);
        float currentBarHeight = (float)targetBarHeight * eased;
        int alpha = (int)(255.0f * eased);
        int color = alpha << 24 | 0;
        drawContext.pose().pushPose();
        drawContext.pose().translate(0.0f, currentBarHeight - (float)targetBarHeight, 0.0f);
        drawContext.fill(0, 0, width, targetBarHeight, color);
        drawContext.pose().popPose();
        drawContext.pose().pushPose();
        drawContext.pose().translate(0.0f, (float)targetBarHeight - currentBarHeight, 0.0f);
        drawContext.fill(0, height - targetBarHeight, width, height, color);
        drawContext.pose().popPose();
    }

    private static enum State {
        HIDDEN,
        FADING_IN,
        VISIBLE,
        FADING_OUT;

    }
}
