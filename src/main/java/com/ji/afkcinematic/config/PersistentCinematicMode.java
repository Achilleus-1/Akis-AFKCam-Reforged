package com.ji.afkcinematic.config;

public enum PersistentCinematicMode {
    NORMAL,
    INTERACTIVE,
    PERSISTENT;


    public PersistentCinematicMode next() {
        PersistentCinematicMode[] modes = PersistentCinematicMode.values();
        return modes[(this.ordinal() + 1) % modes.length];
    }
}
