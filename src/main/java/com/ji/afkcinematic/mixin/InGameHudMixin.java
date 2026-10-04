package com.ji.afkcinematic.mixin;

import com.ji.afkcinematic.config.ConfigManager;
import com.ji.afkcinematic.render.CinematicHUDManager;
import com.ji.afkcinematic.render.HUDController;
import com.ji.afkcinematic.render.LetterboxRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Gui.class})
public abstract class InGameHudMixin {
    @Invoker(value="renderChat")
    protected abstract void jiAfk$renderChat(GuiGraphics var1, DeltaTracker var2);

    @Inject(method={"render"}, at={@At(value="HEAD")}, cancellable=true, require=1)
    private void onRender(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (HUDController.isHidden()) {
            LetterboxRenderer.renderFromHud(context, tickCounter.getGameTimeDeltaPartialTick(false));
            if (CinematicHUDManager.shouldRenderPassiveChat(ConfigManager.getConfig())) {
                this.jiAfk$renderChat(context, tickCounter);
            }
            ci.cancel();
        }
    }
}
