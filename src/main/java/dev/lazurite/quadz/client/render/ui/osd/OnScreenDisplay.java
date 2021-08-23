package dev.lazurite.quadz.client.render.ui.osd;

import com.jme3.math.Vector3f;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public final class OnScreenDisplay {
    private static final int white = 14737632;
    private static final int spacing = 25;

    public static void render(QuadcopterEntity quadcopter, TextRenderer textRenderer, MatrixStack matrixStack, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight() - spacing;

        Text callSign = new LiteralText(quadcopter.getCallSign());
        int callSignWidth = textRenderer.getWidth(callSign);

        VelocityUnit unit = Config.getInstance().velocityUnit;
        float vel = Math.round(quadcopter.getRigidBody().getLinearVelocity(new Vector3f()).length() * unit.getFactor() * 10) / 10f;
        Text velocity = new LiteralText(vel + " " + unit.getAbbreviation());

        textRenderer.drawWithShadow(matrixStack, callSign, width * 0.5f - callSignWidth * 0.5f, height, white);
        textRenderer.drawWithShadow(matrixStack, velocity, width * 0.5f + callSignWidth * 0.5f + spacing, height, white);
    }

    public enum VelocityUnit {
        METERS_PER_SECOND(new TranslatableText("config.quadz.velocity.meters_per_second"), "m/s", 1.0f),
        KILOMETERS_PER_HOUR(new TranslatableText("config.quadz.velocity.kilometers_per_hour"), "kph", 3.6f),
        MILES_PER_HOUR(new TranslatableText("config.quadz.velocity.miles_per_hour"), "mph", 2.237f);

        private final TranslatableText translation;
        private final String abbreviation;
        private final float factor;

        VelocityUnit(TranslatableText translation, String abbreviation, float factor) {
            this.translation = translation;
            this.abbreviation = abbreviation;
            this.factor = factor;
        }

        public TranslatableText getTranslation() {
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
