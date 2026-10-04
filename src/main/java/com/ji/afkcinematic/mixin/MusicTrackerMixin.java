package com.ji.afkcinematic.mixin;

import com.ji.afkcinematic.music.CinematicMusicManager;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MusicManager.class})
public class MusicTrackerMixin {
    @Inject(method={"tick"}, at={@At(value="HEAD")}, cancellable=true, require=1)
    private void onTick(CallbackInfo ci) {
        if (CinematicMusicManager.isOurMusicPlaying) {
            ci.cancel();
        }
    }
}
