package dev.lazurite.quadz.client.render.osd;

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
//        final var vel = Math.round(quadcopterEntity.getRigidBody().getLinearVelocity(new Vector3f()).length() * unit.getFactor() * 10) / 10f;
//        final var velocity = Component.literal(vel + " " + unit.getAbbreviation());
//        font.drawShadow(poseStack, velocity, spacing, height, white);
        // TODO
    }

    public enum VelocityUnit {
        METERS_PER_SECOND(Component.translatable("config.quadz.velocity.meters_per_second"), "m/s", 1.0f),
        KILOMETERS_PER_HOUR(Component.translatable("config.quadz.velocity.kilometers_per_hour"), "kph", 3.6f),
        MILES_PER_HOUR(Component.translatable("config.quadz.velocity.miles_per_hour"), "mph", 2.237f);

        private final Component translation;
        private final String abbreviation;
        private final float factor;

        VelocityUnit(Component translation, String abbreviation, float factor) {
            this.translation = translation;
            this.abbreviation = abbreviation;
            this.factor = factor;
        }

        public Component getTranslation() {
            return this.translation;
        }

        public String getAbbreviation() {
            return this.abbreviation;
        }

        public float getFactor() {
            return this.factor;
        }

    }

}
