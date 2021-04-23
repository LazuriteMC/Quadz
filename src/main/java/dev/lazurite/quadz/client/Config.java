package dev.lazurite.quadz.client;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.input.Mode;
import dev.lazurite.quadz.client.render.ui.osd.OnScreenDisplay;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.AnnotatedSettings;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Settings;
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.FiberException;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.FiberSerialization;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.JanksonValueSerializer;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigTree;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Environment(EnvType.CLIENT)
@Settings(onlyAnnotated = true)
public final class Config {
    private static final Config instance = new Config();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("quadz.json");

    @Setting @Setting.Constrain.Range(min = -1) public int controllerId;
    @Setting @Setting.Constrain.Range(min = 0) public int throttle;
    @Setting @Setting.Constrain.Range(min = 0) public int pitch;
    @Setting @Setting.Constrain.Range(min = 0) public int roll;
    @Setting @Setting.Constrain.Range(min = 0) public int yaw;
    @Setting @Setting.Constrain.Range(min = 0, max = 1) public float deadzone;
    @Setting public boolean invertThrottle;
    @Setting public boolean invertPitch;
    @Setting public boolean invertRoll;
    @Setting public boolean invertYaw;
    @Setting public boolean throttleInCenter;

    @Setting @Setting.Constrain.Range(min = 0.0f) public float rate;
    @Setting @Setting.Constrain.Range(min = 0.0f) public float superRate;
    @Setting @Setting.Constrain.Range(min = 0.0f) public float expo;
    @Setting @Setting.Constrain.Range(min = 10, max = 45) public int maxAngle;
    @Setting public Mode mode;

    @Setting public boolean followLOS;
    @Setting public boolean renderFirstPerson;
    @Setting public int firstPersonFOV;
    @Setting public float thirdPersonOffsetX;
    @Setting public float thirdPersonOffsetY;
    @Setting public int thirdPersonAngle;
    @Setting public int channel;
    @Setting public char band;

    @Setting public boolean osdEnabled;
    @Setting public String callSign;
    @Setting public OnScreenDisplay.VelocityUnit velocityUnit;

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
        this.renderFirstPerson = true;
        this.firstPersonFOV = 30;
        this.thirdPersonOffsetX = 3.0f;
        this.thirdPersonOffsetY = 0.0f;
        this.thirdPersonAngle = 0;
        this.channel = 1;
        this.band = 'R';
        this.osdEnabled = true;
        this.callSign = "";
        this.velocityUnit = OnScreenDisplay.VelocityUnit.METERS_PER_SECOND;
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
        this.sendPlayerData();

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

    public void sendPlayerData() {
        if (MinecraftClient.getInstance().world != null) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(band);
            buf.writeInt(channel);
            buf.writeString(callSign);
            ClientPlayNetworking.send(Quadz.PLAYER_DATA_C2S, buf);
        }
    }
}