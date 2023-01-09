package dev.lazurite.quadz.client.hooks;

import dev.lazurite.quadz.client.render.screen.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.GridWidget;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;

public class ScreenHooks {

    public static void addQuadzButton(PauseScreen screen, GridWidget gridWidget, GridWidget.RowHelper rowHelper) {
        screen.addRenderableWidget(Button.builder(
                Component.literal("Q"),
                button -> {
                    Minecraft.getInstance().setScreen(new ConfigScreen());
                }
        )
        .size(20, 20)
        .pos(screen.disconnectButton.getX() + screen.disconnectButton.getWidth() + 5, screen.disconnectButton.getY())
        .build());
    }

}
