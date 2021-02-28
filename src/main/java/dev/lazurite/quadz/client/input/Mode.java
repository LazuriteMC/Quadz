package dev.lazurite.quadz.client.input;

public enum Mode {
    RATE("mode.quadz.rate"),
    ANGLE("mode.quadz.angle");

    private final String translation;

    Mode(String translation) {
        this.translation = translation;
    }

    public String getTranslation() {
        return this.translation;
    }
}