package com.ji.afkcinematic.config;

public enum MusicMode {
    VANILLA(true, false),
    MIXED(true, true),
    CUSTOM(false, true);

    private final boolean vanilla;
    private final boolean custom;

    private MusicMode(boolean vanilla, boolean custom) {
        this.vanilla = vanilla;
        this.custom = custom;
    }

    public boolean includesVanilla() {
        return this.vanilla;
    }

    public boolean includesCustom() {
        return this.custom;
    }

    public MusicMode next() {
        MusicMode[] values = MusicMode.values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
