package com.ji.afkcinematic.mixin;

import com.ji.afkcinematic.music.CinematicMusicManager;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={SoundEngine.class})
public class SoundSystemMixin {
    @Inject(method={"getVolume"}, at={@At(value="RETURN")}, cancellable=true, require=1)
    private void onGetSoundVolume(SoundSource category, CallbackInfoReturnable<Float> cir) {
        if (category == SoundSource.MUSIC) {
            float original = ((Float)cir.getReturnValue()).floatValue();
            cir.setReturnValue(Float.valueOf(original * CinematicMusicManager.vanillaMusicVolumeMultiplier));
        }
    }
}
