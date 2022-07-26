package dev.lazurite.quadz.common.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.api.JoystickRegistry;
import dev.lazurite.quadz.client.render.ui.osd.OnScreenDisplay;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {
    private static boolean flag;
    public static int controllerId = -1;
    public static float deadzone = 0.05f;
    public static boolean followLOS = true;
    public static boolean renderFirstPerson = true;
    public static boolean renderCameraInCenter = true;
    public static int firstPersonFOV = 30;
    public static boolean osdEnabled = true;
    public static OnScreenDisplay.VelocityUnit velocityUnit = OnScreenDisplay.VelocityUnit.METERS_PER_SECOND;

    public static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("remote.json");
    }

    public static void save() {
        final var joystickAxes = JoystickRegistry.getInstance().getJoystickAxes();
        final var path = getConfigPath();
        final var config = new JsonObject();

        joystickAxes.forEach(joystickAxis -> {
            final var joystickObject = new JsonObject();
            joystickObject.add("axis", new JsonPrimitive(joystickAxis.getAxis()));
            joystickObject.add("inverted", new JsonPrimitive(joystickAxis.isInverted()));
            config.add(joystickAxis.getResourceLocation().toString(), joystickObject);
        });

        config.add("controllerId", new JsonPrimitive(controllerId));
        config.add("deadzone", new JsonPrimitive(deadzone));
        config.add("followLOS", new JsonPrimitive(followLOS));
        config.add("renderFirstPerson", new JsonPrimitive(renderFirstPerson));
        config.add("renderCameraInCenter", new JsonPrimitive(renderCameraInCenter));
        config.add("firstPersonFOV", new JsonPrimitive(firstPersonFOV));
        config.add("osdEnabled", new JsonPrimitive(osdEnabled));
        config.add("velocityUnit", new JsonPrimitive(velocityUnit.toString()));

        try {
            Files.writeString(path, config.toString());
        } catch(IOException e) {
            Quadz.LOGGER.error(e);
        }
    }

    public static void load() {
        if (flag) return;

        final var path = getConfigPath();

        if (!Files.exists(path)) {
            save();
            return;
        }

        try {
            final var config = JsonParser.parseReader(new InputStreamReader(Files.newInputStream(path))).getAsJsonObject();

            JoystickRegistry.getInstance().getJoystickAxes().forEach(joystickAxis -> {
                final var joystickName = joystickAxis.getResourceLocation().toString();
                joystickAxis.setAxis(config.getAsJsonObject(joystickName).get("axis").getAsInt());
            });

            controllerId = config.get("controllerId").getAsInt();
            deadzone = config.get("deadzone").getAsFloat();
            followLOS = config.get("followLOS").getAsBoolean();
            renderFirstPerson = config.get("renderFirstPerson").getAsBoolean();
            renderCameraInCenter = config.get("renderCameraInCenter").getAsBoolean();
            firstPersonFOV = config.get("firstPersonFOV").getAsInt();
            osdEnabled = config.get("osdEnabled").getAsBoolean();
            velocityUnit = OnScreenDisplay.VelocityUnit.valueOf(config.get("velocityUnit").getAsString());
        } catch(IOException e) {
            Quadz.LOGGER.error(e);
        }

        flag = true;
    }
}
