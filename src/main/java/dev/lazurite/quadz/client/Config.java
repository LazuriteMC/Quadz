package dev.lazurite.quadz.client;

import dev.lazurite.quadz.client.input.Mode;
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
    private static final Config instance = new Config();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("quadz.json");

    @Setting
    @Setting.Constrain.Range(min = -1)
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

    @Setting
    @Setting.Constrain.Range(min = 0, max = 60)
    public int maxAngle;

    @Setting
    public Mode mode;

    @Setting
    public boolean followLOS;

    @Setting
    public boolean renderFirstPerson;

    private Config() {
        /* Defaults */
        this.controllerId = -1;
        this.throttle = 0;
        this.pitch = 2;
        this.roll = 1;
        this.yaw = 3;
        this.deadzone = 0.05f;
        this.invertThrottle = false;
        this.invertPitch = false;
        this.invertRoll = false;
        this.invertYaw = true;
        this.throttleInCenter = false;
        this.rate = 0.7f;
        this.superRate = 0.9f;
        this.expo = 0.1f;
        this.maxAngle = 30;
        this.mode = Mode.RATE;
        this.followLOS = true;
        this.renderFirstPerson = false;
    }

    public static Config getInstance() {
        return instance;
    }

    public void load() {
        if (Files.exists(PATH)) {
            try {
                FiberSerialization.deserialize(
                        ConfigTree.builder().applyFromPojo(instance, AnnotatedSettings.builder().build()).build(),
                        Files.newInputStream(PATH),
                        new JanksonValueSerializer(false)
                );
            } catch (IOException | FiberException e) {
                e.printStackTrace();
            }
        } else {
            this.save();
        }
    }

    public void save() {
        try {
            FiberSerialization.serialize(
                    ConfigTree.builder().applyFromPojo(instance, AnnotatedSettings.builder().build()).build(),
                    Files.newOutputStream(PATH),
                    new JanksonValueSerializer(false)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}