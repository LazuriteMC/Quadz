package bluevista.fpvracingmod.client.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class Config {

    private final File file;
    private final HashMap<String, String> values;

    public Config(String configName, String[] keys) {
        this.file = new File("config/" + configName + ".cfg");
        this.values = new HashMap<>();
        createFileIfNotFound();
        safeWriteValues(file.getPath(), keys);
        readValues();
    }

    public Config(String filename) {
        this.file = new File(filename);
        this.values = new HashMap<>();
        createFileIfNotFound();
        readValues();
    }

    private void createFileIfNotFound() {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readValues() {
        if (file.length() > 0) {
            try {
                Scanner scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.split("=").length == 2) {
                        values.put(line.split("=")[0], line.split("=")[1]);
                    } else {
                        values.put(line.split("=")[0], "0"); // default value, works for ints and floats
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void safeWriteValues(String filename, String[] keys) {
        if (file.length() == 0) {
            try {
                FileWriter fileWriter = new FileWriter(filename);
                for (String key : keys) {
                    fileWriter.write(key + "=\n");
                }
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public HashMap<String, String> getValues() {
        return values;
    }

    public String getValue(String key) {
        try {
            return values.get(key);
        } catch (NullPointerException e) {
            return null;
        }
    }
}
