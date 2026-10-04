package com.ji.afkcinematic.cinematic;

import com.ji.afkcinematic.compat.SmoothF5Compatibility;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

public class CinematicCameraManager {
    private static CameraType previousPerspective;
    private static boolean active;

    public static void activate() {
        if (active) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        previousPerspective = client.options.getCameraType();
        active = true;
        client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
    }

    public static void deactivate() {
        if (!active) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        CameraType restore = previousPerspective;
        previousPerspective = null;
        active = false;
        if (restore != null) {
            client.options.setCameraType(restore);
            SmoothF5Compatibility.prepareImmediateReturn(restore != CameraType.FIRST_PERSON, restore == CameraType.THIRD_PERSON_FRONT);
        }
    }
}
