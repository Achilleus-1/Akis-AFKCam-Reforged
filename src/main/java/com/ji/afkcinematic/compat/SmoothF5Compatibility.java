package com.ji.afkcinematic.compat;

import java.lang.reflect.Field;

public final class SmoothF5Compatibility {
    private static volatile boolean snapOnNextCameraUpdate;
    private static volatile boolean targetDetached;
    private static volatile boolean targetMirrored;

    private SmoothF5Compatibility() {
    }

    public static void prepareImmediateReturn(boolean detached, boolean mirrored) {
        targetDetached = detached;
        targetMirrored = mirrored;
        snapOnNextCameraUpdate = true;
    }

    public static void onCameraUpdate(Object camera) {
        if (!snapOnNextCameraUpdate) {
            return;
        }
        snapOnNextCameraUpdate = false;
        SmoothF5Compatibility.resetState(camera, 0);
    }

    private static void resetState(Object target, int depth) {
        if (target == null || depth > 2) {
            return;
        }
        for (Field field : target.getClass().getDeclaredFields()) {
            String name = field.getName();
            try {
                field.setAccessible(true);
                if (name.endsWith("wasDetached")) {
                    field.setBoolean(target, targetDetached);
                    continue;
                }
                if (name.endsWith("wasMirrored")) {
                    field.setBoolean(target, targetMirrored);
                    continue;
                }
                if (name.endsWith("isTransitioning") || name.endsWith("transDeltaReady") || name.endsWith("shouldSnapNextTail")) {
                    field.setBoolean(target, false);
                    continue;
                }
                if (!name.endsWith("smoother") && !name.equals("state")) continue;
                SmoothF5Compatibility.resetState(field.get(target), depth + 1);
            }
            catch (ReflectiveOperationException | RuntimeException exception) {
                // empty catch block
            }
        }
    }
}
