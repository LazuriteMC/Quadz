package dev.lazurite.quadz.client.render.screen.osd;

import com.jme3.math.Vector3f;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.common.entity.Quadcopter;
import dev.lazurite.quadz.common.util.Search;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class OnScreenDisplay extends GuiComponent {

    private static final OnScreenDisplay instance = new OnScreenDisplay();

    public static OnScreenDisplay getInstance() {
        return instance;
    }

    public void render(Quadcopter quadcopter, Font font, PoseStack poseStack, float tickDelta) {
        renderVelocityText(quadcopter, font, poseStack);
        renderStickInput(quadcopter, poseStack);
    }

    private void renderVelocityText(Quadcopter quadcopter, Font font, PoseStack poseStack) {
        var client = Minecraft.getInstance();
        var height = client.getWindow().getGuiScaledHeight() - 25;
        var unit = Config.velocityUnit;
        final var vel = Math.round(quadcopter.getRigidBody().getLinearVelocity(new Vector3f()).length() * unit.getFactor() * 10) / 10f;
        final var velocity = Component.literal(vel + " " + unit.getAbbreviation());
        font.drawShadow(poseStack, velocity, 25, height, 0xFFFFFFFF);
    }

    private void renderStickInput(Quadcopter quadcopter, PoseStack poseStack) {
        Search.forPlayer(quadcopter).ifPresent(player -> {
            var pitch = player.getJoystickValue(new ResourceLocation(Quadz.MODID, "pitch")) * (Config.pitchInverted ? -1 : 1);
            var yaw = player.getJoystickValue(new ResourceLocation(Quadz.MODID, "yaw")) * (Config.yawInverted ? -1 : 1);
            var roll = player.getJoystickValue(new ResourceLocation(Quadz.MODID, "roll")) * (Config.rollInverted ? -1 : 1);
            var throttle = (player.getJoystickValue(new ResourceLocation(Quadz.MODID, "throttle")) + 1.0f) * (Config.throttleInverted ? -1 : 1);

            var width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            var height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            var centerX = width / 2;

            var leftX = centerX - 30;
            var rightX = centerX + 30;
            var topY = height - 75;
            var middleY = height - 50;
            var bottomY = height - 25;

            // Draw crosses
            vLine(poseStack, leftX, bottomY, topY, 0xFFFFFFFF);
            vLine(poseStack, rightX, bottomY, topY, 0xFFFFFFFF);
            hLine(poseStack, leftX - 25, leftX + 25, middleY, 0xFFFFFFFF);
            hLine(poseStack, rightX - 25, rightX + 25, middleY, 0xFFFFFFFF);

            // Draw stick positions
            int dotSize = 2;
            int yawAdjusted = (int) (yaw * 25);
            int throttleAdjusted = (int) (throttle * 25) - 25;
            int rollAdjusted = (int) (roll * 25);
            int pitchAdjusted = (int) (pitch * 25);
            fill(poseStack, leftX + yawAdjusted - dotSize, middleY - throttleAdjusted - dotSize, leftX + yawAdjusted + dotSize, middleY - throttleAdjusted + dotSize, 0xFFFFFFFF);
            fill(poseStack, rightX + rollAdjusted - dotSize, middleY - pitchAdjusted - dotSize, rightX + rollAdjusted + dotSize, middleY - pitchAdjusted + dotSize, 0xFFFFFFFF);
        });
    }

}
