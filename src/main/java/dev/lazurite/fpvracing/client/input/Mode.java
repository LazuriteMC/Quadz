package dev.lazurite.fpvracing.client.input;

public enum Mode {
    RATE("mode.fpvracing.rate"),
    ANGLE("mode.fpvracing.angle");

    private final String translation;

    Mode(String translation) {
        this.translation = translation;
    }

    public String getTranslation() {
        return this.translation;
    }
}