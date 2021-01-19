package dev.lazurite.fpvracing.client.config;

import dev.lazurite.fpvracing.FPVRacing;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.AnnotatedSettings;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Settings;
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.FiberException;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.FiberSerialization;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.JanksonValueSerializer;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigTree;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;

@Settings(onlyAnnotated = true)
public final class Config {
    public static final Config INSTANCE = new Config();
    public static final String CONFIG_NAME = "fpv.json";

    public boolean shouldRenderPlayer = false;

    @Setting
    @Setting.Constrain.Range(min = 0)
    public int controllerId;

    @Setting
    @Setting.Constrain.Range(min = 0)
    public int throttle;

    @Setting
    @Setting.Constrain.Range(min = 0)
    public int pitch;

    @Setting
    @Setting.Constrain.Range(min = 0)
    public int roll;

    @Setting
    @Setting.Constrain.Range(min = 0)
    public int yaw;

    @Setting
    @Setting.Constrain.Range(min = 0, max = 1)
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

    @Setting
    @Setting.Constrain.Range(min = 0.0f)
    public float rate;

    @Setting
    @Setting.Constrain.Range(min = 0.0f)
    public float superRate;

    @Setting
    @Setting.Constrain.Range(min = 0.0f)
    public float expo;

    private Config() {
    }

    public void load() {
        if (Files.exists(FabricLoader.getInstance().getConfigDir().resolve(CONFIG_NAME))) {
            try {
                FiberSerialization.deserialize(
                        ConfigTree.builder()
                                .applyFromPojo(this, AnnotatedSettings.builder().build())
                                .build(),
                        Files.newInputStream(FabricLoader.getInstance().getConfigDir().resolve(CONFIG_NAME)),
                        new JanksonValueSerializer(false)
                );
            } catch (IOException | FiberException e) {
                FPVRacing.LOGGER.error("Error loading config.");
                e.printStackTrace();
            }
        } else {
            FPVRacing.LOGGER.info("Creating config.");
            this.save();
        }
    }

    public void save() {
        try {
            FiberSerialization.serialize(
                    ConfigTree.builder()
                            .applyFromPojo(this, AnnotatedSettings.builder().build())
                            .build(),
                    Files.newOutputStream(FabricLoader.getInstance().getConfigDir().resolve(CONFIG_NAME)),
                    new JanksonValueSerializer(false)
            );
        } catch (IOException e) {
            FPVRacing.LOGGER.error("Error saving config.");
            e.printStackTrace();
        }
    }
}
