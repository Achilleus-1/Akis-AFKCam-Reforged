package com.ji.afkcinematic.mixin;

import com.ji.afkcinematic.JiAFKCinematic;
import com.ji.afkcinematic.cinematic.CameraController;
import com.ji.afkcinematic.cinematic.CinematicManager;
import com.ji.afkcinematic.cinematic.CinematicState;
import com.ji.afkcinematic.compat.SmoothF5Compatibility;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Camera.class})
public abstract class CameraMixin {
    @Shadow
    @Final
    private static Vector3f FORWARDS;
    @Shadow
    @Final
    private static Vector3f UP;
    @Shadow
    @Final
    private static Vector3f LEFT;
    @Shadow
    @Final
    private Quaternionf rotation;
    @Shadow
    @Final
    private Vector3f forwards;
    @Shadow
    @Final
    private Vector3f up;
    @Shadow
    @Final
    private Vector3f left;

    @Shadow
    protected abstract void setPosition(double var1, double var3, double var5);

    @Shadow
    protected abstract void setRotation(float var1, float var2);

    @Inject(method={"setup"}, at={@At(value="HEAD")}, order=500, require=1)
    private void beforeCameraUpdate(BlockGetter area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        SmoothF5Compatibility.onCameraUpdate(this);
    }

    @Inject(method={"setup"}, at={@At(value="TAIL")}, order=2000, require=1)
    private void onCameraUpdate(BlockGetter area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        CinematicState state = CinematicManager.getState();
        if (state == CinematicState.CINEMATIC_ACTIVE) {
            int ticksLeft = CinematicManager.getTicksLeftInCurrentShot();
            int durationTicks = CinematicManager.getShotDurationTicks();
            if (durationTicks <= 0) {
                return;
            }
            float ticksPassed = durationTicks - ticksLeft;
            float frameProgress = (ticksPassed + tickDelta) / (float)durationTicks;
            CameraController.evaluateFrame(frameProgress, tickDelta);
            Vec3 pos = CameraController.getFramePos();
            float pitch = CameraController.getFramePitch();
            float yaw = CameraController.getFrameYaw();
            float roll = CameraController.getFrameRoll();
            if (!(Float.isFinite(pitch) && Float.isFinite(yaw) && Float.isFinite(roll) && Double.isFinite(pos.x) && Double.isFinite(pos.y) && Double.isFinite(pos.z))) {
                JiAFKCinematic.LOGGER.warn("Skipping non-finite camera frame (pos={}, pitch={}, yaw={})", new Object[]{pos, Float.valueOf(pitch), Float.valueOf(yaw)});
                return;
            }
            this.setPosition(pos.x, pos.y, pos.z);
            this.setRotation(yaw, pitch);
            if (roll != 0.0f) {
                this.rotation.rotateZ(roll * ((float)Math.PI / 180));
                FORWARDS.rotate((Quaternionfc)this.rotation, this.forwards);
                UP.rotate((Quaternionfc)this.rotation, this.up);
                LEFT.rotate((Quaternionfc)this.rotation, this.left);
            }
        }
    }

    @Inject(method={"isDetached"}, at={@At(value="HEAD")}, cancellable=true, require=1)
    private void overrideThirdPerson(CallbackInfoReturnable<Boolean> cir) {
        if (CinematicManager.getState() == CinematicState.CINEMATIC_ACTIVE) {
            cir.setReturnValue(true);
        }
    }
}
