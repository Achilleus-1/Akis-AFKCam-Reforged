package com.ji.afkcinematic.cinematic;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CameraCollisionHelper {
    public static Vec3 resolveCollision(Vec3 startPos, Vec3 targetPos) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return targetPos;
        }
        ClipContext context = new ClipContext(startPos, targetPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)client.player);
        BlockHitResult hit = client.level.clip(context);
        if (hit.getType() != HitResult.Type.MISS) {
            Vec3 hitPos = hit.getLocation();
            Vec3 direction = startPos.subtract(hitPos).normalize();
            return hitPos.add(direction.scale(0.25));
        }
        return targetPos;
    }
}
