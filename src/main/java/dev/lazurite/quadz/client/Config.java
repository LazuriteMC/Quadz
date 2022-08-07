package dev.lazurite.quadz.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import dev.lazurite.quadz.Quadz;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {
    public static int pitch = 1;
    public static int yaw = 3;
    public static int roll = 0;
    public static int throttle = 2;

    public static boolean pitchInverted = false;
    public static boolean yawInverted = true;
    public static boolean rollInverted = false;
    public static boolean throttleInverted = true;
    public static boolean throttleInCenter = false;

    public static float rate = 0.7f;
    public static float superRate = 0.8f;
    public static float expo = 0.0f;

    public static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("quadz.json");
    }

    public static void save() {
        final var path = getConfigPath();
        final var config = new JsonObject();

        config.add("pitch", new JsonPrimitive(pitch));
        config.add("yaw", new JsonPrimitive(yaw));
        config.add("roll", new JsonPrimitive(roll));
        config.add("throttle", new JsonPrimitive(throttle));
        config.add("pitchInverted", new JsonPrimitive(pitchInverted));
        config.add("yawInverted", new JsonPrimitive(yawInverted));
        config.add("rollInverted", new JsonPrimitive(rollInverted));
        config.add("throttleInverted", new JsonPrimitive(throttleInverted));
        config.add("throttleInCenter", new JsonPrimitive(throttleInCenter));
        config.add("rate", new JsonPrimitive(rate));
        config.add("superRate", new JsonPrimitive(superRate));
        config.add("expo", new JsonPrimitive(expo));

        try {
            Files.writeString(path, config.toString());
        } catch(IOException e) {
            Quadz.LOGGER.error(e);
        }
    }

    public static void load() {
        final var path = getConfigPath();

        if (!Files.exists(path)) {
            save();
            return;
        }

        try {
            final var config = JsonParser.parseReader(new InputStreamReader(Files.newInputStream(path))).getAsJsonObject();
            pitch = config.get("pitch").getAsInt();
            yaw = config.get("yaw").getAsInt();
            roll = config.get("roll").getAsInt();
            throttle = config.get("throttle").getAsInt();
            pitchInverted = config.get("pitchInverted").getAsBoolean();
            yawInverted = config.get("yawInverted").getAsBoolean();
            rollInverted = config.get("rollInverted").getAsBoolean();
            throttleInverted = config.get("throttleInverted").getAsBoolean();
            throttleInCenter = config.get("throttleInCenter").getAsBoolean();
            rate = config.get("rate").getAsFloat();
            superRate = config.get("superRate").getAsFloat();
            expo = config.get("expo").getAsFloat();
        } catch(IOException e) {
            Quadz.LOGGER.error(e);
        }
    }
}