package bluevista.fpvracingmod.client.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class Config {

    private File file;

    public Config(String filename, String[] keys) {
        this(filename);
        // write keys=
    }

    public Config(String filename) {
        this.file = new File(filename);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Config file doesn't exist");
            e.printStackTrace();
        }
    }

    public String getFilename() {
        return file.getName();
    }

    public String getConfigValue(String key) {
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.split("=")[0].equals(key)) {
                    return line.split("=")[1];
                }
                System.out.println(line.split("=")[1]);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Config file doesn't exist");
            e.printStackTrace();
        }
        return null;
    }
}
