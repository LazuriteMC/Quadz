package io.lazurite.fpvracing.network.tracker;

import io.lazurite.fpvracing.network.tracker.generic.GenericType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * The main config handling class for not only reading and writing
 * from the file, but also being passed between the client and
 * the server.
 */
public class Config extends Properties {

    /**
     * Empty constructor :/
     */
    public Config() {

    }

    /**
     * The client-side config creation constructor. This
     * will read in the config from the file when called.
     * @param filename
     */
    @Environment(EnvType.CLIENT)
    public Config(String filename) {
        this();
        this.readConfig(filename);
    }

    public <T> void put(String key, GenericType<T> type, T value) {
        if (type.fromConfig(this, key) == null) {
            type.toConfig(this, key, value);
        }
    }

    /**
     * Read a boolean from the config.
     * @param key the key to get
     * @return a boolean value
     */
    public boolean getBool(String key) {
        return Boolean.parseBoolean(getProperty(key));
    }

    /**
     * Read an integer from the config.
     * @param key the key to get
     * @return an int value
     */
    public int getInt(String key) {
        return Integer.parseInt(getProperty(key));
    }

    /**
     * Read a float from the config.
     * @param key the key to get
     * @return a float value
     */
    public float getFloat(String key) {
        return Float.parseFloat(getProperty(key));
    }

    @Environment(EnvType.CLIENT)
    public void readConfig(String filename) {
        try {
            load(new FileInputStream("config/" + filename));
        } catch (IOException e) {
            System.err.println("Error reading " + filename);
            e.printStackTrace();
        }
    }

    @Environment(EnvType.CLIENT)
    public void writeConfig(String filename) {
        try {
            store(new FileOutputStream("config/" + filename), "");
        } catch (IOException e) {
            System.err.println("Error reading " + filename);
            e.printStackTrace();
        }
    }
}
