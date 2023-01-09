package dev.lazurite.quadz.client.render.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {

    public ConfigScreen() {
        super(Component.translatable("quadz.config.title"));
    }

    @Override
    public void init() {
        this.addRenderableWidget(Button.builder(
                Component.literal("69420"),
                button -> {
                }
        )
        .pos(50, 50)
        .size(100, 20)
        .build());
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        super.renderDirtBackground(0);
        super.render(poseStack, i, j, f);
    }

}
