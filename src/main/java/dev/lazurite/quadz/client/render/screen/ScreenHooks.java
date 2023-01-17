package dev.lazurite.quadz.client.render.screen;

import dev.lazurite.quadz.Quadz;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ScreenHooks {

    public static void addQuadzButtonToPauseScreen(PauseScreen screen) {
        screen.addRenderableWidget(getButton(
                screen.disconnectButton.getX() + screen.disconnectButton.getWidth() + 5,
                screen.disconnectButton.getY()
        ));
    }

    public static void addQuadzButtonToTitleScreen(TitleScreen screen) {
        screen.addRenderableWidget(getButton(screen.width / 2 + 128, screen.height / 4 + 132));
    }

    private static ImageButton getButton(int x, int y) {
        return new ImageButton(
                x, y, 20, 20, 0, 0, 20,
                new ResourceLocation(Quadz.MODID, "textures/gui/quadz.png"),
                32, 64,
                button -> Minecraft.getInstance().setScreen(new ConfigScreen()),
                Component.translatable("quadz.config.title")
        );
    }

}
