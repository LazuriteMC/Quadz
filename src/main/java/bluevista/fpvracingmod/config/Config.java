package bluevista.fpvracingmod.config;

import net.minecraft.network.PacketByteBuf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Config {

    public static final String ALL = "all"; // used to represent all config options
    public static final String WRITE = "write"; // used to write config options to local config file
    public static final String REVERT = "revert"; // used to revert the config to its initial state

    public static final String CONTROLLER_ID = "controllerID";
    public static final String THROTTLE = "throttle";
    public static final String PITCH = "pitch";
    public static final String YAW = "yaw";
    public static final String ROLL = "roll";
    public static final String DEADZONE = "deadzone";
    public static final String THROTTLE_CENTER_POSITION = "throttleCenterPosition";
    public static final String RATE = "rate";
    public static final String SUPER_RATE = "superRate";
    public static final String EXPO = "expo";
    public static final String INVERT_THROTTLE = "invertThrottle";
    public static final String INVERT_PITCH = "invertPitch";
    public static final String INVERT_YAW = "invertYaw";
    public static final String INVERT_ROLL = "invertRoll";
    public static final String CAMERA_ANGLE = "cameraAngle";
    public static final String BAND = "band";
    public static final String CHANNEL = "channel";

    public static final List<String> ALL_OPTIONS = Arrays.asList(
            CONTROLLER_ID,
            THROTTLE,
            PITCH,
            YAW,
            ROLL,
            DEADZONE,
            THROTTLE_CENTER_POSITION,
            RATE,
            SUPER_RATE,
            EXPO,
            INVERT_THROTTLE,
            INVERT_PITCH,
            INVERT_YAW,
            INVERT_ROLL,
            CAMERA_ANGLE,
            BAND,
            CHANNEL
    );

    public static final List<String> INT_KEYS = Arrays.asList(
            CONTROLLER_ID,
            THROTTLE,
            PITCH,
            YAW,
            ROLL,
            THROTTLE_CENTER_POSITION,
            INVERT_THROTTLE,
            INVERT_PITCH,
            INVERT_YAW,
            INVERT_ROLL,
            CAMERA_ANGLE,
            BAND,
            CHANNEL
    );

    public static final List<String> FLOAT_KEYS = Arrays.asList(
            DEADZONE,
            RATE,
            SUPER_RATE,
            EXPO
    );

    private HashMap<String, Number> options;
    private ConfigReader configReader;

    /* CONSTRUCTORS */

    public Config() {
        options = new HashMap<>();
        configReader = new ConfigReader();
    }

    public Config(PacketByteBuf buf) {
        this();
        this.deserializeAndSet(buf);
    }

    /* SETTERS */

    public void setOption(String key, Number value) {
        options.remove(key);
        options.put(key, value);
    }

    /* GETTERS */

    public HashMap<String, Number> getOptions() {
        return this.options;
    }

    public Number getOption(String key) {
        return options.get(key);
    }

    // Convenience method
    public int getIntOption(String key) {
        return getOption(key).intValue();
    }

    // Convenience method
    public float getFloatOption(String key) {
        return getOption(key).floatValue();
    }

    /* OTHER METHODS */

    public void loadConfig() {
        for (String key : ALL_OPTIONS) {
            try {
                this.getOptions().put(key, configReader.getValue(key));
            } catch (Exception e) {
                System.err.println("Error loading config file");
                e.printStackTrace();
            }
        }
    }

    public void writeConfig() {
        configReader.writeValues();
    }

    public void serialize(PacketByteBuf buf) {
        String key = buf.readString(32767); // majik
        buf.writeString(key); // put it back?
        if (key.equals(ALL)) {
            for (String option : ALL_OPTIONS) {
                if (INT_KEYS.contains(option)) {
                    buf.writeInt(getIntOption(option));
                } else if (FLOAT_KEYS.contains(option)) {
                    buf.writeFloat(getFloatOption(option));
                }
            }
        } else if (INT_KEYS.contains(key)) {
            buf.writeInt(getIntOption(key));
        } else if (FLOAT_KEYS.contains(key)) {
            buf.writeFloat(getFloatOption(key));
        }
    }

    public void deserializeAndSet(PacketByteBuf buf) {
        String key = buf.readString(32767); // majik
        if (key.equals(ALL)) {
            for (String option : ALL_OPTIONS) {
                if (INT_KEYS.contains(option)) {
                    setOption(option, buf.readInt());
                } else if (FLOAT_KEYS.contains(option)) {
                    setOption(option, buf.readFloat());
                }
            }
        } else if (INT_KEYS.contains(key)) {
            setOption(key, buf.readInt());
        } else if (FLOAT_KEYS.contains(key)) {
            setOption(key, buf.readFloat());
        }
    }
}
