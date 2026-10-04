package com.ji.afkcinematic.config;

import com.ji.afkcinematic.config.PersistentCinematicMode;

public enum CinematicChatVisibility {
    HIDDEN,
    VISIBLE;


    public CinematicChatVisibility next() {
        CinematicChatVisibility[] values = CinematicChatVisibility.values();
        return values[(this.ordinal() + 1) % values.length];
    }

    public boolean isVisible(PersistentCinematicMode mode) {
        return this == VISIBLE;
    }
}
