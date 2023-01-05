package dev.lazurite.quadz.client.render.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {

    public ConfigScreen() {
        super(Component.translatable("quadz.config.title"));
    }

}
