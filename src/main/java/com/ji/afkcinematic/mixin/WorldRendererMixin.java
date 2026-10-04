package com.ji.afkcinematic.mixin;

import com.ji.afkcinematic.cinematic.CinematicManager;
import com.ji.afkcinematic.cinematic.CinematicState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LevelRenderer.class})
public class WorldRendererMixin {
    @Inject(method={"isSectionCompiled"}, at={@At(value="HEAD")}, cancellable=true, require=1)
    private void forcePlayerRenderingReady(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (CinematicManager.getState() == CinematicState.CINEMATIC_ACTIVE) {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null && client.player.distanceToSqr(Vec3.atCenterOf((Vec3i)pos)) < 4096.0) {
                cir.setReturnValue(true);
            }
        }
    }
}
