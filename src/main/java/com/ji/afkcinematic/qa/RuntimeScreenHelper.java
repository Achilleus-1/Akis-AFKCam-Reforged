package com.ji.afkcinematic.qa;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;

final class RuntimeScreenHelper {
    private RuntimeScreenHelper() {
    }

    static void openEmptyChat(Minecraft client) {
        client.setScreen((Screen)new ChatScreen(""));
    }
}
