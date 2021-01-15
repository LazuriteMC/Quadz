package dev.lazurite.fpvracing.common.util.config;

public final class Config {
    public static final Config INSTANCE = new Config();

    private Config() {
    }

    public boolean shouldRenderPlayer = false;
}
