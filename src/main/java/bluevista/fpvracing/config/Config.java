package bluevista.fpvracing.config;

import bluevista.fpvracing.network.datatracker.FlyableTrackerRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private final Properties properties;

    public Config() {
        properties = new Properties();
    }

    public Config(Properties properties) {
        this.properties = properties;
    }

    @Environment(EnvType.CLIENT)
    public Config(String filename) {
        this();
        this.readConfig(filename);
    }

    public Properties getProperties() {
        return this.properties;
    }

    public boolean getBool(String key) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }

    public int getInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    public float getFloat(String key) {
        return Float.parseFloat(properties.getProperty(key));
    }

    @Environment(EnvType.CLIENT)
    public void readConfig(String filename) {
        try {
            properties.load(new FileInputStream("config/" + filename));
        } catch (IOException e) {
            System.err.println("Error reading " + filename);
            e.printStackTrace();
        }
    }

    @Environment(EnvType.CLIENT)
    public void writeConfig(String filename) {
        try {
            properties.store(new FileOutputStream("config/" + filename), "");
        } catch (IOException e) {
            System.err.println("Error reading " + filename);
            e.printStackTrace();
        }
    }
}
