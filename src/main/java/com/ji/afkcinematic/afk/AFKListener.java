package com.ji.afkcinematic.afk;

public interface AFKListener {
    public void onAFKTriggered();

    public void onActivityDetected();

    public void onReset();
}
