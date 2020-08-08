package bluevista.fpvracingmod.config;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.server.ServerInitializer;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class ConfigReader {

    private static final String CONFIG_NAME = ServerInitializer.MODID + ".cfg";

    private final File file;
    private final HashMap<String, Number> values;

    public ConfigReader() {
        this.file = new File("config/" + CONFIG_NAME);
        this.values = new HashMap<>();
        copyFileIfNotFound();
        readValues();
    }

    private void copyFileIfNotFound() {
        try {
            if(!file.exists()) {
                InputStream in = this.getClass().getResourceAsStream("/" + CONFIG_NAME);
                OutputStream out = new FileOutputStream("config/" + CONFIG_NAME);
                IOUtils.copy(in, out);
            }
        } catch (Exception e) {
            System.err.println("Error copying config file");
            e.printStackTrace();
        }
    }

    private void readValues() {
        if (file.length() != 0) {
            try {
                Scanner scanner = new Scanner(file);
                String line;

                while (scanner.hasNextLine()) {
                    line = scanner.nextLine();

                    if(line.contains("#") || line.isEmpty()) {
                        // this handles a case where the line is a comment or a newline
                        continue;
                    }

                    if (line.split("=").length == 2) {
                        // this handles a case where the value of a key is defined in the config

                        String key = line.split("=")[0].trim();
                        String value = line.split("=")[1].trim();

                        if (Config.INT_KEYS.contains(key)) {
                            values.put(key, Integer.parseInt(value));
                        } else if (Config.FLOAT_KEYS.contains(key)) {
                            values.put(key, Float.parseFloat(value));
                        }
                    } else if (!line.startsWith("=") && line.contains("=")) {
                        // this handles a case where the value of a key is removed, leaving the line ending with an '='

                        String key = line.split("=")[0].trim();

                        if (Config.INT_KEYS.contains(key)) {
                            values.put(key, 0);
                        } else if (Config.FLOAT_KEYS.contains(key)) {
                            values.put(key, 0.0F);
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                System.err.println("Error reading config file");
                e.printStackTrace();
            }
        }
    }

    public void writeValues() {
        if (file.length() != 0) {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                Scanner scanner = new Scanner(file);
                String line;

                while (scanner.hasNextLine()) {
                    line = scanner.nextLine();

                    if (line.startsWith("#") || line.isEmpty()) {
                        // this handles a case where the line is a comment or a newline

                        stringBuilder.append(line).append(System.lineSeparator());
                    } else {
                        // this handles a case where the line is a key & value pair

                        for (String key : Config.ALL_OPTIONS) {
                            if (line.split("=")[0].equals(key)) {
                                stringBuilder.append(key).append("=").append(ClientInitializer.getConfig().getOption(key)).append(System.lineSeparator());
                            }
                        }
                    }
                }

                OutputStream out = new FileOutputStream("config/" + CONFIG_NAME);

                IOUtils.write(stringBuilder, out, Charset.defaultCharset());
            } catch (IOException e) {
                System.err.println("Error writing config values");
                e.printStackTrace();
            }
        }
    }

    public Number getValue(String key) {
        return values.get(key);
    }
}
