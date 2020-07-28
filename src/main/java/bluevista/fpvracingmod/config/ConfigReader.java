package bluevista.fpvracingmod.config;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.server.ServerInitializer;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.*;

public class ConfigReader {

    private static final String CONFIG_NAME = ServerInitializer.MODID + ".cfg";

    private final File file;
    private final HashMap<String, String> values;

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

    // rework this to parse the values into their respective datatypes before storing them in values
    private void readValues() {
        if (file.length() != 0) {
            try {
                Scanner scanner = new Scanner(file);
                String line;

                while (scanner.hasNextLine()) {
                    line = scanner.nextLine();
                    if(line.contains("#")) {
                        continue;
                    }

                    if (line.split("=").length == 2) {
                        values.put(line.split("=")[0], line.split("=")[1]);
                    } else {
                        values.put(line.split("=")[0], "0"); // default value, works for ints and floats
                    }
                }
            } catch (FileNotFoundException e) {
                System.err.println("Error reading config file");
                e.printStackTrace();
            }
        }
    }

    public void writeChangedValues() {
        if (file.length() != 0) {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                Scanner scanner = new Scanner(file);
                String line;

                while (scanner.hasNextLine()) {
                    line = scanner.nextLine();
                    if (line.startsWith("#") || line.isEmpty()) {
                        stringBuilder.append(line).append("\n");
                    } else {
                        for (String key : Config.ALL_OPTIONS) {
                            if (line.split("=")[0].equals(key)) {
                                stringBuilder.append(key).append("=").append(ClientInitializer.getConfig().getOption(key)).append("\n");
                            }
                        }
                    }
                }

                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(stringBuilder.toString());
                fileWriter.close();
            } catch (IOException e) {
                System.err.println("Error writing config values");
                e.printStackTrace();
            }
        }
    }

    public String getValue(String key) {
        try {
            return values.get(key);
        } catch (NullPointerException e) {
            return null;
        }
    }
}
