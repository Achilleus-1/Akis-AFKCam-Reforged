package com.ji.afkcinematic.mixin;

import com.ji.afkcinematic.input.PersistentMovementLock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LocalPlayer.class})
public class ClientPlayerMovementMixin {
    @Inject(method={"aiStep"}, at={@At(value="HEAD")}, require=1)
    private void beforeMovement(CallbackInfo ci) {
        ClientPlayerMovementMixin.clear();
    }

    @Inject(method={"aiStep"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/Input;tick(ZF)V", shift=At.Shift.AFTER)}, require=1)
    private void afterLegacyInput(CallbackInfo ci) {
        ClientPlayerMovementMixin.clear();
    }

    private static void clear() {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            PersistentMovementLock.clear(client.player.input);
        }
    }
}
