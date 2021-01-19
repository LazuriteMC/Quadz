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
import java.nio.file.Path;

@Settings(onlyAnnotated = true)
public final class Config {
    public static final Config INSTANCE = new Config();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("fpv.json");
    private static final ConfigTree CONFIG_TREE = ConfigTree.builder()
            .applyFromPojo(INSTANCE, AnnotatedSettings.builder().build())
            .build();

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
        if (Files.exists(PATH)) {
            try {
                FiberSerialization.deserialize(
                        CONFIG_TREE,
                        Files.newInputStream(PATH),
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
                    CONFIG_TREE,
                    Files.newOutputStream(PATH),
                    new JanksonValueSerializer(false)
            );
        } catch (IOException e) {
            FPVRacing.LOGGER.error("Error saving config.");
            e.printStackTrace();
        }
    }
}
