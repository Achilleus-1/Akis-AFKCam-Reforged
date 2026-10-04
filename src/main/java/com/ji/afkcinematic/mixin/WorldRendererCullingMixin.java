package com.ji.afkcinematic.mixin;

import com.ji.afkcinematic.cinematic.CinematicManager;
import com.ji.afkcinematic.cinematic.CinematicState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={LevelRenderer.class})
public class WorldRendererCullingMixin {
    @Redirect(method={"setupRender"}, at=@At(value="FIELD", target="Lnet/minecraft/client/Minecraft;smartCull:Z"), require=1)
    private boolean disableOcclusionCullingInCinematic(Minecraft client) {
        return CinematicManager.getState() == CinematicState.CINEMATIC_ACTIVE ? false : client.smartCull;
    }
}
