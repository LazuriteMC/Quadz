package dev.lazurite.fpvracing.client.ui.config;

import com.google.common.collect.Lists;
import dev.lazurite.fpvracing.client.config.Config;
import dev.lazurite.fpvracing.client.config.ModMenuEntry;
import dev.lazurite.fpvracing.client.input.InputTick;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
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
                .setSavingRunnable(Config.INSTANCE::save);

        return builder.setFallbackCategory(buildControllerSetup(builder)).build();
    }

    public static ConfigCategory buildControllerSetup(ConfigBuilder builder) {
        ConfigCategory controllerSetup = builder.getOrCreateCategory(new TranslatableText("config.rayon.category.controller_setup")); // category name is ignored

        List<String> joysticks = Lists.newArrayList(InputTick.INSTANCE.getJoysticks());
        System.out.println(joysticks);

        /* List of Controllers */
//        controllerSetup.addEntry(builder.entryBuilder().startStrList(new TranslatableText("config.fpvracing.entry.controller_id"), joysticks).build());
        controllerSetup.addEntry(builder.entryBuilder().startStringDropdownMenu(
                new TranslatableText("config.fpvracing.entry.controller_id"),
                "test",
                (value) ->  new LiteralText("AAAA"))
                .build());

        return controllerSetup;
    }
}
