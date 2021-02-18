package dev.lazurite.fpvracing.client.render.ui.config;

import com.google.common.collect.Lists;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.lazurite.fpvracing.client.config.Config;
import dev.lazurite.fpvracing.client.input.InputTick;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import java.util.List;

/**
 * Class for housing the methods which returns a new config screen made using Cloth Config.
 * @see ConfigScreen#getModConfigScreenFactory()
 * @see Config
 */
public class ConfigScreen implements ModMenuApi {
    /*public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(new TranslatableText("config.fpvracing.title"))
//                .setTransparentBackground(true)
                .setSavingRunnable(Config.getInstance()::save);

        return builder.setFallbackCategory(getControllerSettings(builder)).build();
    }

    public static ConfigCategory getControllerSettings(ConfigBuilder builder) {
        ConfigCategory controllerSetup = builder.getOrCreateCategory(new TranslatableText("config.rayon.category.controller_setup")); // category name is ignored
        List<String> joysticks = Lists.newArrayList(InputTick.getInstance().getJoysticks());

        if (joysticks.isEmpty()) {
            joysticks.add("");
        }

        /* List of Controllers *//*
        controllerSetup.addEntry(builder.entryBuilder().startSelector(
        new TranslatableText("config.fpvracing.entry.controller_id"),
        joysticks.toArray(),
        joysticks.get(0))
                .setTooltip(new TranslatableText("config.fpvracing.entry.controller_id.tooltip"))
                .setNameProvider(value -> {
                    String string = (String) value;

                    if (string.length() > 15) {
                        return new LiteralText(string.substring(0, 15) + "...");
                    } else {
                        return new LiteralText(string);
                    }
                })
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

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreen::create;
    } */
}
