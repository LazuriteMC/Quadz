package dev.lazurite.quadz.common.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.data.model.Settings;
import dev.lazurite.quadz.common.data.model.Template;
import dev.lazurite.quadz.common.item.group.ItemGroupHandler;
import dev.lazurite.quadz.common.state.QuadcopterState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DataDriver {
    public static final Path QUADZ_DIR = Paths.get("quadz");
    private static Map<String, Template> templates;

    public static void initialize() {
        templates = new HashMap<>();
        readFromDisk();
    }

    public static void load(Template template) {
        if (templates.containsKey(template.getId())) {
            Quadz.LOGGER.info("Quadcopter template already exists! Skipping...");
            return;
        }

        templates.put(template.getId(), template);

        ItemStack stack = new ItemStack(Quadz.QUADCOPTER_ITEM);
        QuadcopterState.get(stack).ifPresent(state -> state.setTemplate(template.getId()));
        ItemGroupHandler.getInstance().register(stack);
    }

    private static void readFromDisk() {
        try {
            Quadz.LOGGER.info("Reading quadz templates...");
            Path source = FabricLoader.getInstance().getGameDir().normalize().resolve(QUADZ_DIR);

            if (!Files.exists(source)) {
                Files.createDirectory(source);
            }

            for (Path path : Files.walk(source).collect(Collectors.toList())) {
                if (path.getFileName().toString().contains(".zip")) {
                    ZipFile zip = new ZipFile(path.toFile());
                    Settings settings = null;
                    JsonObject geo = null;
                    JsonObject animation = null;
                    byte[] texture = null;

                    Quadz.LOGGER.info("Loading {} template...", zip.getName());

                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();

                        if (!entry.isDirectory()) {
                            /* Settings */
                            if (entry.getName().contains(".settings.json")) {
                                settings = new Gson().fromJson(new InputStreamReader(zip.getInputStream(entry)), Settings.class);

                            /* Geo Model */
                            } else if (entry.getName().contains(".geo.json")) {
                                geo = new JsonParser().parse(new InputStreamReader(zip.getInputStream(entry))).getAsJsonObject();

                            /* Animation */
                            } else if (entry.getName().contains(".animation.json")) {
                                animation = new JsonParser().parse(new InputStreamReader(zip.getInputStream(entry))).getAsJsonObject();

                            /* Texture */
                            } else if (entry.getName().contains(".png")) {
                                InputStream stream = zip.getInputStream(entry);
                                texture = new byte[stream.available()];
                                stream.read(texture);
                            }
                        }
                    }

                    try {
                        load(new Template(settings, geo, animation, texture, true));
                    } catch (RuntimeException e) {
                        Quadz.LOGGER.error(e.getMessage());
                    }
                }
            }
        } catch (IOException | NoSuchElementException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to load quadcopter templates.");
        } finally {
            Quadz.LOGGER.info("Done!");
        }
    }

    public static Template getTemplate(String id) {
        Template out = templates.get(id);
        return out == null ? templates.get("missing") : out;
    }

    public static List<Template> getTemplates() {
        return new ArrayList<>(templates.values());
    }

    public static void clearRemoteTemplates() {
        templates.values().removeIf(template -> !template.isOriginal());
    }
}
