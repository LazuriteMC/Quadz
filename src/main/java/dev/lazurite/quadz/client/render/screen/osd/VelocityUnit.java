package dev.lazurite.quadz.client.render.screen.osd;

import net.minecraft.network.chat.Component;

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
