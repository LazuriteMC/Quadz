package dev.lazurite.fpvracing.client.render.ui.config;

import com.google.common.collect.Lists;
import dev.lazurite.fpvracing.client.config.Config;
import dev.lazurite.fpvracing.client.config.ModMenuEntry;
import dev.lazurite.fpvracing.client.input.InputTick;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;

import java.util.List;

/**
 * Class for housing the methods which returns a new config screen made using Cloth Config.
 * @see ModMenuEntry#getModConfigScreenFactory()
 * @see Config
 */
public class ConfigScreen {
    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(new TranslatableText("config.fpvracing.title"))
//                .setTransparentBackground(true)
                .setSavingRunnable(Config.getInstance()::save);

        return builder.setFallbackCategory(getControllerSetup(builder)).build();
    }

    public static ConfigCategory getControllerSetup(ConfigBuilder builder) {
        ConfigCategory controllerSetup = builder.getOrCreateCategory(new TranslatableText("config.rayon.category.controller_setup")); // category name is ignored
        List<String> joysticks = Lists.newArrayList(InputTick.getInstance().getJoysticks());

        if (joysticks.isEmpty()) {
            joysticks.add("");
        }

        /* List of Controllers */
        controllerSetup.addEntry(builder.entryBuilder().startSelector(
        new TranslatableText("config.fpvracing.entry.controller_id"),
        joysticks.toArray(),
        joysticks.get(0))
                .setTooltip(new TranslatableText("config.fpvracing.entry.controller_id.tooltip"))
                .setSaveConsumer(value -> {
                    for (int i = 0; i < joysticks.toArray().length; i++) {
                        if (value.equals(joysticks.toArray()[i])) {
                            Config.getInstance().controllerId = i;
                        }
                    }
                })
                .build());

        return controllerSetup;
    }
}
