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

    private final Quadcopter quadcopter;
    private final Font font;

    public OnScreenDisplay(Quadcopter quadcopter) {
        this.quadcopter = quadcopter;
        this.font = Minecraft.getInstance().font;
    }

    public void renderVelocity(PoseStack poseStack, float tickDelta) {
        var client = Minecraft.getInstance();
        var height = client.getWindow().getGuiScaledHeight() - 25;
        var unit = Config.velocityUnit;
        final var vel = Math.round(quadcopter.getRigidBody().getLinearVelocity(new Vector3f()).length() * unit.getFactor() * 10) / 10f;
        final var velocity = Component.literal(vel + " " + unit.getAbbreviation());
        font.drawShadow(poseStack, velocity, 25, height, 0xFFFFFFFF);
    }

    public void renderSticks(PoseStack poseStack, float tickDelta) {
        Search.forPlayer(quadcopter).ifPresent(player -> {
            var pitch = player.quadz$getJoystickValue(new ResourceLocation(Quadz.MODID, "pitch"));
            var yaw = player.quadz$getJoystickValue(new ResourceLocation(Quadz.MODID, "yaw"));
            var roll = player.quadz$getJoystickValue(new ResourceLocation(Quadz.MODID, "roll"));
            var throttle = (player.quadz$getJoystickValue(new ResourceLocation(Quadz.MODID, "throttle")) + 1.0f);
            var width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            var height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            renderSticks(poseStack, tickDelta, width / 2, height - 75, 25, 5, pitch, yaw, roll, throttle);
        });
    }

    public static void renderSticks(PoseStack poseStack, float tickDelta, int x, int y, int scale, int spacing, float pitch, float yaw, float roll, float throttle) {
        var leftX = x - scale - spacing;
        var rightX = x + scale + spacing;
        var topY = y + scale;
        var bottomY = y - scale;

        // Draw crosses
        fill(poseStack, leftX, bottomY + 1, leftX + 1, topY, 0xFFFFFFFF);
        fill(poseStack, rightX, bottomY + 1, rightX + 1, topY, 0xFFFFFFFF);
        fill(poseStack, leftX - scale, y, leftX + scale + 1, y + 1, 0xFFFFFFFF);
        fill(poseStack, rightX - scale, y, rightX + scale + 1, y + 1, 0xFFFFFFFF);

        // Draw stick positions
        int dotSize = 2;
        int yawAdjusted = (int) (yaw * scale);
        int throttleAdjusted = (int) (throttle * scale) - scale;
        int rollAdjusted = (int) (roll * scale);
        int pitchAdjusted = (int) (pitch * scale);
        fill(poseStack, leftX + yawAdjusted - dotSize, y - throttleAdjusted - dotSize, leftX + yawAdjusted + dotSize, y - throttleAdjusted + dotSize, 0xFFFFFFFF);
        fill(poseStack, rightX + rollAdjusted - dotSize, y - pitchAdjusted - dotSize, rightX + rollAdjusted + dotSize, y - pitchAdjusted + dotSize, 0xFFFFFFFF);
    }

}
