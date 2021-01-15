package dev.lazurite.fpvracing.client.config;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Settings;

@Settings(onlyAnnotated = true)
public final class Config {
    public static final Config INSTANCE = new Config();

    private Config() {
    }

    public boolean shouldRenderPlayer = false;

    @Setting
    public int controllerId;

    @Setting
    public int throttle;

    @Setting
    public int pitch;

    @Setting
    public int roll;

    @Setting
    public int yaw;

    @Setting
    public float deadzone;

    @Setting
    public boolean invertThrottle;

    @Setting
    public boolean invertPitch;

    @Setting
    public boolean invertRoll;

    @Setting
    public boolean invertYaw;

    @Setting
    public boolean throttleInCenter;
}
