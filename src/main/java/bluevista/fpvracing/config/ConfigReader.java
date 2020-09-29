package bluevista.fpvracing.config;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.server.ServerInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

@Environment(EnvType.CLIENT)
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
            if (!file.exists()) {
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

                    if (line.contains("#") || line.isEmpty()) {
                        // this handles a case where the line is a comment or a blank line (line separator)
                        continue;
                    }

                    if (line.split("=").length == 2) {
                        // this handles a case where the value of a key is defined in the config

                        String key = line.split("=")[0].trim();
                        String value = line.split("=")[1].trim();

                        try {
                            values.put(key, NumberFormat.getInstance().parse(value));
                        } catch (ParseException e) {
                            values.put(key, 0); // default
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

                Config config = ClientInitializer.getConfig();
                int lastWrittenKeyIndex = -1; // no value yet written

                while (scanner.hasNextLine()) {
                    line = scanner.nextLine();

                    if (line.startsWith("#") || line.isEmpty()) {
                        // this handles a case where the line is a comment or a newline

                        Scanner scanner2 = new Scanner(new File("config/" + CONFIG_NAME)); // prevents scanner from being updated by using scanner2
                        scanner2.useDelimiter(scanner.delimiter());

                        String pattern = scanner2.findWithinHorizon(Config.ALL_OPTIONS.get(lastWrittenKeyIndex + 1) + "=", 0);

                        while (true) {
                            if (pattern != null && pattern.startsWith("#")) {
                                pattern = scanner2.findWithinHorizon(Config.ALL_OPTIONS.get(lastWrittenKeyIndex + 1) + "=", 0);
                            } else {
                                break;
                            }
                        }

                        if (pattern == null) {
                            ++lastWrittenKeyIndex;
                            String value = Config.ALL_OPTIONS.get(lastWrittenKeyIndex);
                            stringBuilder.append(value).append("=").append(config.getOption(value)).append(System.lineSeparator());
                        }

                        stringBuilder.append(line).append(System.lineSeparator());
                    } else {
                        // this handles a case where the line is a key & value pair, hopefully

                        for (String key : Config.ALL_OPTIONS) {
                            if (line.split("=")[0].equals(key)) {
                                lastWrittenKeyIndex = Config.ALL_OPTIONS.indexOf(key);
                                stringBuilder.append(key).append("=").append(config.getOption(key)).append(System.lineSeparator());
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
        if (values.get(key) == null) {
            return 0;
        }

        return values.get(key);
    }
}
