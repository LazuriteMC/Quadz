package dev.lazurite.quadz.client.render.screen.osd;

import com.jme3.math.Vector3f;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.common.entity.Quadcopter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

public final class OnScreenDisplay {

    private static final int white = 14737632;
    private static final int spacing = 25;

    public static void render(Quadcopter quadcopter, Font font, PoseStack poseStack, float tickDelta) {
        var client = Minecraft.getInstance();
        var width = client.getWindow().getGuiScaledWidth();
        var height = client.getWindow().getGuiScaledHeight() - spacing;
        var unit = Config.velocityUnit;
        final var vel = Math.round(quadcopter.getRigidBody().getLinearVelocity(new Vector3f()).length() * unit.getFactor() * 10) / 10f;
        final var velocity = Component.literal(vel + " " + unit.getAbbreviation());
        font.drawShadow(poseStack, velocity, spacing, height, white);
    }

}
