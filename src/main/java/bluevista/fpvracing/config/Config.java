package bluevista.fpvracing.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Config {

    // For dealing with commands
    public static final String CONFIG = "config";
    public static final String DRONE = "drone";
    public static final String GOGGLES = "goggles";
    public static final String HELP = "help";
    public static final String SUCCESS = "success";
    public static final String ERROR = "error";
    public static final String WRITE = "write";
    public static final String REVERT = "revert";

    // For dealing with item tags for item specific configuration
    public static final String ON = "on";
    public static final String BIND = "bind";
    public static final String PLAYER_ID = "playerID";
    public static final String NO_CLIP = "noClip";
    public static final String GOD_MODE = "godMode";

    // For dealing with the config file items
    public static final String ALL = "all"; // used to represent all config options

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
    public static final String FIELD_OF_VIEW = "fieldOfView";
    public static final String BAND = "band";
    public static final String CHANNEL = "channel";
    public static final String MASS = "mass";
    public static final String SIZE = "size";
    public static final String THRUST = "thrust";
    public static final String THRUST_CURVE = "thrustCurve";
    public static final String GRAVITY = "gravity";
    public static final String AIR_DENSITY = "airDensity";
    public static final String DRAG_COEFFICIENT = "dragCoefficient";
    public static final String BLOCK_RADIUS = "blockRadius";
    public static final String DAMAGE_COEFFICIENT = "damageCoefficient";
//    public static final String CRASH_MOMENTUM_THRESHOLD = "crashMomentumThreshold";

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
            CHANNEL,
            BLOCK_RADIUS,
//            CRASH_MOMENTUM_THRESHOLD,
            SIZE
    );

    public static final List<String> FLOAT_KEYS = Arrays.asList(
            DEADZONE,
            RATE,
            SUPER_RATE,
            EXPO,
            FIELD_OF_VIEW,
            MASS,
            DRAG_COEFFICIENT,
            AIR_DENSITY,
            THRUST,
            THRUST_CURVE,
            GRAVITY,
            DAMAGE_COEFFICIENT
    );

    public static final List<String> ALL_OPTIONS = new ArrayList<String>() {{
        addAll(INT_KEYS);
        addAll(FLOAT_KEYS);
    }};

    private HashMap<String, Number> options;
    private ConfigReader configReader;

    /* CONSTRUCTORS */

    public Config() {
        options = new HashMap<>();
        configReader = new ConfigReader();
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

}
