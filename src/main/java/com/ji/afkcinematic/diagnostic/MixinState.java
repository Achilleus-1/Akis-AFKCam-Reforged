package com.ji.afkcinematic.diagnostic;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class MixinState {
    private static final Set<String> APPLIED = new LinkedHashSet<String>();

    private MixinState() {
    }

    public static void markApplied(String mixinClassName) {
        if (mixinClassName == null) {
            return;
        }
        String simple = mixinClassName;
        int dot = mixinClassName.lastIndexOf(46);
        if (dot >= 0) {
            simple = mixinClassName.substring(dot + 1);
        }
        APPLIED.add(simple);
    }

    public static Set<String> getApplied() {
        return Collections.unmodifiableSet(APPLIED);
    }

    public static boolean didApply(String simpleName) {
        return APPLIED.contains(simpleName);
    }
}
