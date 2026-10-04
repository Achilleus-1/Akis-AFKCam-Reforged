package com.ji.afkcinematic.input;

public final class KeySequenceTracker {
    public static final long SEQUENCE_TIMEOUT_MS = 1500L;
    public static final int UNSUPPORTED_KEY_REJECTED = 3;
    private static int menuFirstKey = -1;
    private static long menuFirstKeyTime = 0L;
    private static int toggleFirstKey = -1;
    private static long toggleFirstKeyTime = 0L;
    private static int immediateFirstKey = -1;
    private static long immediateFirstKeyTime = 0L;
    private static int rebindFirstKey = -1;
    private static long rebindFirstTimeMs = 0L;

    private KeySequenceTracker() {
    }

    public static boolean isBindableKeyCode(int keyCode) {
        return keyCode >= 32 && keyCode <= 348;
    }

    public static int[] acceptedFirstKeys(int configuredKey) {
        if (configuredKey == 341 || configuredKey == 345) {
            return new int[]{341, 345};
        }
        if (configuredKey == 340 || configuredKey == 344) {
            return new int[]{340, 344};
        }
        if (configuredKey == 342 || configuredKey == 346) {
            return new int[]{342, 346};
        }
        return new int[]{configuredKey};
    }

    private static boolean matchesAny(int keyCode, int[] candidates) {
        for (int k : candidates) {
            if (k != keyCode) continue;
            return true;
        }
        return false;
    }

    public static boolean checkMenu(int keyCode, int[] acceptedFirstKeys, int secondKey) {
        return KeySequenceTracker.check(keyCode, acceptedFirstKeys, secondKey, true);
    }

    public static boolean checkToggle(int keyCode, int[] acceptedFirstKeys, int secondKey) {
        return KeySequenceTracker.check(keyCode, acceptedFirstKeys, secondKey, false);
    }

    public static boolean checkImmediate(int keyCode, int[] acceptedFirstKeys, int secondKey) {
        return KeySequenceTracker.checkImmediateInternal(keyCode, acceptedFirstKeys, secondKey);
    }

    public static boolean isToggleSequenceStep(int keyCode, int configuredFirst, int secondKey) {
        int[] firstKeys = KeySequenceTracker.acceptedFirstKeys(configuredFirst);
        if (toggleFirstKey == -1) {
            return KeySequenceTracker.matchesAny(keyCode, firstKeys);
        }
        if (System.currentTimeMillis() - toggleFirstKeyTime > 1500L) {
            return false;
        }
        return keyCode == secondKey;
    }

    public static boolean isImmediateSequenceStep(int keyCode, int configuredFirst, int secondKey) {
        int[] firstKeys = KeySequenceTracker.acceptedFirstKeys(configuredFirst);
        if (immediateFirstKey == -1) {
            return KeySequenceTracker.matchesAny(keyCode, firstKeys);
        }
        if (System.currentTimeMillis() - immediateFirstKeyTime > 1500L) {
            return false;
        }
        return keyCode == secondKey;
    }

    public static void onKeyReleased(int keyCode) {
        if (keyCode < 0) {
            KeySequenceTracker.resetAll();
            return;
        }
        if (menuFirstKey == keyCode) {
            KeySequenceTracker.resetSequence(true);
        }
        if (toggleFirstKey == keyCode) {
            KeySequenceTracker.resetSequence(false);
        }
        if (immediateFirstKey == keyCode) {
            KeySequenceTracker.resetImmediateSequence();
        }
    }

    private static boolean checkImmediateInternal(int keyCode, int[] acceptedFirstKeys, int secondKey) {
        long now = System.currentTimeMillis();
        if (acceptedFirstKeys.length == 1 && acceptedFirstKeys[0] == secondKey) {
            if (keyCode == secondKey) {
                KeySequenceTracker.resetImmediateSequence();
                return true;
            }
            return false;
        }
        if (immediateFirstKey == -1) {
            if (KeySequenceTracker.matchesAny(keyCode, acceptedFirstKeys)) {
                immediateFirstKey = keyCode;
                immediateFirstKeyTime = now;
            }
            return false;
        }
        if (now - immediateFirstKeyTime > 1500L) {
            KeySequenceTracker.resetImmediateSequence();
            if (KeySequenceTracker.matchesAny(keyCode, acceptedFirstKeys)) {
                immediateFirstKey = keyCode;
                immediateFirstKeyTime = now;
            }
            return false;
        }
        if (keyCode == secondKey && KeySequenceTracker.matchesAny(immediateFirstKey, acceptedFirstKeys)) {
            KeySequenceTracker.resetImmediateSequence();
            return true;
        }
        KeySequenceTracker.resetImmediateSequence();
        if (KeySequenceTracker.matchesAny(keyCode, acceptedFirstKeys)) {
            immediateFirstKey = keyCode;
            immediateFirstKeyTime = now;
        }
        return false;
    }

    private static boolean check(int keyCode, int[] acceptedFirstKeys, int secondKey, boolean isMenu) {
        long firstTime;
        long now = System.currentTimeMillis();
        int firstKey = isMenu ? menuFirstKey : toggleFirstKey;
        long l = firstTime = isMenu ? menuFirstKeyTime : toggleFirstKeyTime;
        if (acceptedFirstKeys.length == 1 && acceptedFirstKeys[0] == secondKey) {
            if (keyCode == secondKey) {
                KeySequenceTracker.resetSequence(isMenu);
                return true;
            }
            return false;
        }
        if (firstKey == -1) {
            if (KeySequenceTracker.matchesAny(keyCode, acceptedFirstKeys)) {
                KeySequenceTracker.setFirst(isMenu, keyCode, now);
            }
            return false;
        }
        if (now - firstTime > 1500L) {
            KeySequenceTracker.resetSequence(isMenu);
            if (KeySequenceTracker.matchesAny(keyCode, acceptedFirstKeys)) {
                KeySequenceTracker.setFirst(isMenu, keyCode, now);
            }
            return false;
        }
        if (keyCode == secondKey) {
            if (KeySequenceTracker.matchesAny(firstKey, acceptedFirstKeys)) {
                KeySequenceTracker.resetSequence(isMenu);
                return true;
            }
            KeySequenceTracker.resetSequence(isMenu);
            return false;
        }
        KeySequenceTracker.resetSequence(isMenu);
        if (KeySequenceTracker.matchesAny(keyCode, acceptedFirstKeys)) {
            KeySequenceTracker.setFirst(isMenu, keyCode, now);
        }
        return false;
    }

    private static void setFirst(boolean isMenu, int keyCode, long now) {
        if (isMenu) {
            menuFirstKey = keyCode;
            menuFirstKeyTime = now;
        } else {
            toggleFirstKey = keyCode;
            toggleFirstKeyTime = now;
        }
    }

    public static void resetSequence(boolean isMenu) {
        if (isMenu) {
            menuFirstKey = -1;
            menuFirstKeyTime = 0L;
        } else {
            toggleFirstKey = -1;
            toggleFirstKeyTime = 0L;
        }
    }

    public static void resetAll() {
        menuFirstKey = -1;
        menuFirstKeyTime = 0L;
        toggleFirstKey = -1;
        toggleFirstKeyTime = 0L;
        immediateFirstKey = -1;
        immediateFirstKeyTime = 0L;
    }

    public static void resetImmediateSequence() {
        immediateFirstKey = -1;
        immediateFirstKeyTime = 0L;
    }

    public static void startRebind() {
        rebindFirstKey = -1;
        rebindFirstTimeMs = 0L;
    }

    public static boolean hasRebindFirst() {
        return rebindFirstKey != -1;
    }

    public static int getRebindFirst() {
        return rebindFirstKey;
    }

    public static long getRebindRemainingMs() {
        if (rebindFirstKey == -1) {
            return 0L;
        }
        return Math.max(0L, 1500L - (System.currentTimeMillis() - rebindFirstTimeMs));
    }

    public static int processRebindKey(int keyCode, int[] outKeys) {
        long now = System.currentTimeMillis();
        if (keyCode == 256) {
            KeySequenceTracker.resetRebind();
            return -1;
        }
        if (!KeySequenceTracker.isBindableKeyCode(keyCode)) {
            return 3;
        }
        if (rebindFirstKey == -1) {
            rebindFirstKey = keyCode;
            rebindFirstTimeMs = now;
            outKeys[0] = keyCode;
            return 1;
        }
        if (now - rebindFirstTimeMs > 1500L) {
            KeySequenceTracker.resetRebind();
            return -1;
        }
        outKeys[0] = rebindFirstKey;
        outKeys[1] = keyCode;
        KeySequenceTracker.resetRebind();
        return 2;
    }

    public static void resetRebind() {
        rebindFirstKey = -1;
        rebindFirstTimeMs = 0L;
    }
}
