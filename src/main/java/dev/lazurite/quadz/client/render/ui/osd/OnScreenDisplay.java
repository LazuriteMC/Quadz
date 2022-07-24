package dev.lazurite.quadz.client.render.ui.osd;

import com.jme3.math.Vector3f;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.common.quadcopter.entity.QuadcopterEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public final class OnScreenDisplay {
    private static final int white = 14737632;
    private static final int spacing = 25;

    public static void render(QuadcopterEntity quadcopter, Font font, PoseStack poseStack, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight() - spacing;

        Component callSign = Component.literal(Config.getInstance().callSign); // TODO
        int callSignWidth = font.width(callSign);

        VelocityUnit unit = Config.getInstance().velocityUnit;
        float vel = Math.round(quadcopter.getRigidBody().getLinearVelocity(new Vector3f()).length() * unit.getFactor() * 10) / 10f;
        Component velocity = Component.literal(vel + " " + unit.getAbbreviation());

        font.drawShadow(poseStack, callSign, width * 0.5f - callSignWidth * 0.5f, height, white);
        font.drawShadow(poseStack, velocity, spacing, height, white);
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
