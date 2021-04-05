package dev.lazurite.quadz.common.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.data.model.Settings;
import dev.lazurite.quadz.common.data.model.Template;
import dev.lazurite.quadz.common.item.group.ItemGroupHandler;
import dev.lazurite.quadz.common.state.QuadcopterState;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This is the main housing for all of the quadcopter templates loaded
 * into the game. It handles loading from template zip files and contains
 * a map of all {@link Template} objects which can be referenced at any
 * time throughout execution. As such, templates aren't stored anywhere
 * except here - even {@link QuadcopterEntity} objects only store the ID
 * of the template so that it may look it up here.
 */
public class DataDriver {
    private static Map<String, Template> templates;

    public static void initialize() {
        templates = new ConcurrentHashMap<>();
        readFromDisk();
    }

    public static void load(Template template) {
        if (templates.containsKey(template.getId())) {
            Quadz.LOGGER.info("{} template already exists! Skipping...", template.getId());
            return;
        }

        Quadz.LOGGER.info("Loading {} template...", template.getId());
        templates.put(template.getId(), template);

        /*
         * Only add to inventory if the template originated on the server or your own client. Other
         * players' templates are not included in your inventory, but can be obtained from them.
         */
        if (template.getOriginDistance() < 2) {
            ItemStack stack = new ItemStack(Quadz.QUADCOPTER_ITEM);
            QuadcopterState.get(stack).ifPresent(state -> state.setTemplate(template.getId()));
            ItemGroupHandler.getInstance().register(stack);
        }
    }

    private static void readFromDisk() {
        try {
            Quadz.LOGGER.info("Reading templates...");
            Path source = FabricLoader.getInstance().getGameDir().normalize().resolve(Paths.get("quadz"));
            Path internal = FabricLoader.getInstance().getModContainer("quadz").get().getRootPath().resolve("assets").resolve("quadz").resolve("templates");

            if (!Files.exists(Paths.get("voyager.zip"))) {
                for (Path path : Files.walk(internal).collect(Collectors.toList())) {
                    Path file = source.resolve(internal.relativize(path).getFileName().toString());

                    if (!Files.exists(file)) {
                        if (!Files.exists(file.getParent())) {
                            Files.createDirectories(file.getParent());
                        }

                        Files.copy(path, file);
                    }
                }
            }

            for (Path path : Files.walk(source).collect(Collectors.toList())) {
                if (path.getFileName().toString().contains(".zip")) {
                    ZipFile zip = new ZipFile(path.toFile());
                    Settings settings = null;
                    JsonObject geo = null;
                    JsonObject animation = null;
                    byte[] texture = null;

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
                        load(new Template(settings, geo, animation, texture, 0));
                    } catch (NoSuchElementException e) {
                        Quadz.LOGGER.error(e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to load quadcopter templates.");
        } finally {
            Quadz.LOGGER.info("Done!");
        }
    }

    public static Template getTemplate(String id) {
        return templates.get(id);
    }

    public static List<Template> getTemplates() {
        return new ArrayList<>(templates.values());
    }

    /**
     * Removes any templates that are from anywhere besides this instance of the game.
     */
    public static void clearRemoteTemplates() {
        templates.values().removeIf(template -> template.getOriginDistance() > 0);
    }
}
