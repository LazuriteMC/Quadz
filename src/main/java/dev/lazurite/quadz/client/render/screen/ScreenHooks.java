package dev.lazurite.quadz.client.render.screen;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.render.screen.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.GridWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ScreenHooks {

    public static void addQuadzButton(PauseScreen screen) {
        screen.addRenderableWidget(new ImageButton(
                screen.disconnectButton.getX() + screen.disconnectButton.getWidth() + 5,
                screen.disconnectButton.getY(),
                20, 20, 0, 0, 20,
                new ResourceLocation(Quadz.MODID, "textures/gui/quadz.png"),
                32, 64,
                button -> Minecraft.getInstance().setScreen(new ConfigScreen()),
                Component.translatable("quadz.config.title")
        ));
    }

}
