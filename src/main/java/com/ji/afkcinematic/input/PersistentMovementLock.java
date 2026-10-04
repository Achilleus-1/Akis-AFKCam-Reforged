package com.ji.afkcinematic.input;

import com.ji.afkcinematic.cinematic.CinematicManager;
import com.ji.afkcinematic.cinematic.CinematicState;
import com.ji.afkcinematic.config.ConfigManager;
import com.ji.afkcinematic.config.PersistentCinematicMode;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;

public final class PersistentMovementLock {
    private PersistentMovementLock() {
    }

    public static boolean isLocked() {
        return CinematicManager.getState() == CinematicState.CINEMATIC_ACTIVE && ConfigManager.getConfig().persistentMode == PersistentCinematicMode.PERSISTENT;
    }

    public static void clear(Object input) {
        if (!PersistentMovementLock.isLocked() || input == null) {
            return;
        }
        for (Class<?> type = input.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                try {
                    field.setAccessible(true);
                    Class<?> fieldType = field.getType();
                    if (fieldType == Float.TYPE) {
                        field.setFloat(input, 0.0f);
                        continue;
                    }
                    if (fieldType == Boolean.TYPE) {
                        field.setBoolean(input, false);
                        continue;
                    }
                    if (fieldType.isRecord()) {
                        Object empty = PersistentMovementLock.emptyBooleanRecord(fieldType);
                        if (empty == null) continue;
                        field.set(input, empty);
                        continue;
                    }
                    Object zeroVector = PersistentMovementLock.zeroFloatPair(fieldType);
                    if (zeroVector == null) continue;
                    field.set(input, zeroVector);
                }
                catch (ReflectiveOperationException | RuntimeException exception) {
                    // empty catch block
                }
            }
        }
    }

    private static Object emptyBooleanRecord(Class<?> type) throws ReflectiveOperationException {
        RecordComponent[] components = type.getRecordComponents();
        Class[] parameterTypes = new Class[components.length];
        Object[] values = new Object[components.length];
        for (int i = 0; i < components.length; ++i) {
            parameterTypes[i] = components[i].getType();
            if (parameterTypes[i] != Boolean.TYPE) {
                return null;
            }
            values[i] = false;
        }
        Constructor<?> constructor = type.getDeclaredConstructor(parameterTypes);
        constructor.setAccessible(true);
        return constructor.newInstance(values);
    }

    private static Object zeroFloatPair(Class<?> type) {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor(Float.TYPE, Float.TYPE);
            constructor.setAccessible(true);
            return constructor.newInstance(Float.valueOf(0.0f), Float.valueOf(0.0f));
        }
        catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }
}
