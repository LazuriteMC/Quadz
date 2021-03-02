package dev.lazurite.quadz.client.render.ui;

import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.client.input.Mode;
import dev.lazurite.quadz.client.input.InputTick;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import java.util.Map;

/**
 * Class for housing the methods which returns a new config screen made using Cloth Config.
 * @see ConfigScreen#getModConfigScreenFactory()
 * @see Config
 */
public class ConfigScreen implements ModMenuApi {
    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(new TranslatableText("config.quadz.title"))
                .setSavingRunnable(Config.getInstance()::save);

        return builder.setFallbackCategory(getControllerSettings(builder)).build();
    }

    public static ConfigCategory getControllerSettings(ConfigBuilder builder) {
        ConfigCategory controllerSetup = builder.getOrCreateCategory(new LiteralText(""));
        Map <Integer, String> joysticks = InputTick.getInstance().getJoysticks();

        SubCategoryBuilder controllerAxes = builder.entryBuilder().startSubCategory(new TranslatableText("config.quadz.category.controller_axes"));
        SubCategoryBuilder preferences = builder.entryBuilder().startSubCategory(new TranslatableText("config.quadz.category.preferences"));

        preferences.add(builder.entryBuilder().startEnumSelector(
                new TranslatableText("config.quadz.entry.mode"), Mode.class, Config.getInstance().mode)
                .setEnumNameProvider(value -> new TranslatableText(((Mode) value).getTranslation()))
                .setSaveConsumer(value -> Config.getInstance().mode = value)
                .setDefaultValue(Config.getInstance().mode)
                .build());

        preferences.add(builder.entryBuilder().startIntSlider(
                new TranslatableText("config.quadz.entry.max_angle"), Config.getInstance().maxAngle, 0, 60)
                .setSaveConsumer(value -> Config.getInstance().maxAngle = value)
                .setDefaultValue(Config.getInstance().maxAngle)
                .build());

        preferences.add(builder.entryBuilder().startBooleanToggle(
                new TranslatableText("config.quadz.entry.follow_los"), Config.getInstance().followLOS)
                .setDefaultValue(Config.getInstance().followLOS)
                .setSaveConsumer(value -> Config.getInstance().followLOS = value)
                .build());

        preferences.add(builder.entryBuilder().startFloatField(
                new TranslatableText("config.quadz.entry.rate"), Config.getInstance().rate)
                .setDefaultValue(Config.getInstance().rate)
                .setSaveConsumer(value -> Config.getInstance().rate = value)
                .setMin(0).build());

        preferences.add(builder.entryBuilder().startFloatField(
                new TranslatableText("config.quadz.entry.expo"), Config.getInstance().expo)
                .setDefaultValue(Config.getInstance().expo)
                .setSaveConsumer(value -> Config.getInstance().expo = value)
                .setMin(0).build());

        preferences.add(builder.entryBuilder().startFloatField(
                new TranslatableText("config.quadz.entry.super_rate"), Config.getInstance().superRate)
                .setDefaultValue(Config.getInstance().superRate)
                .setSaveConsumer(value -> Config.getInstance().superRate = value)
                .setMin(0).build());

        preferences.add(builder.entryBuilder().startFloatField(
                new TranslatableText("config.quadz.entry.super_rate"), Config.getInstance().superRate)
                .setDefaultValue(Config.getInstance().superRate)
                .setSaveConsumer(value -> Config.getInstance().superRate = value)
                .setMin(0).build());

        preferences.add(builder.entryBuilder().startFloatField(
                new TranslatableText("config.quadz.entry.deadzone"), Config.getInstance().deadzone)
                .setDefaultValue(Config.getInstance().deadzone)
                .setSaveConsumer(value -> Config.getInstance().deadzone = value)
                .setMin(0).build());

        controllerAxes.add(builder.entryBuilder().startSelector(
                new TranslatableText("config.quadz.entry.controller_id"), joysticks.keySet().toArray(), Config.getInstance().controllerId)
                .setDefaultValue(Config.getInstance().controllerId)
                .setNameProvider(value -> {
                    String name = joysticks.get((int) value);

                    if ((int) value == -1) {
                        return new TranslatableText("config.quadz.entry.controller_id.keyboard");
                    } else if (name.length() > 15) {
                        return new LiteralText(name.substring(0, 15) + "...");
                    } else {
                        return new LiteralText(name);
                    }
                })
                .setSaveConsumer(value -> Config.getInstance().controllerId = (int) value)
                .build());

        controllerAxes.add(builder.entryBuilder().startIntField(
                new TranslatableText("config.quadz.entry.pitch_axis"), Config.getInstance().pitch)
                .setDefaultValue(Config.getInstance().pitch)
                .setSaveConsumer(value -> Config.getInstance().pitch = value)
                .setMin(0).build());

        controllerAxes.add(builder.entryBuilder().startIntField(
                new TranslatableText("config.quadz.entry.yaw_axis"), Config.getInstance().yaw)
                .setDefaultValue(Config.getInstance().yaw)
                .setSaveConsumer(value -> Config.getInstance().yaw = value)
                .setMin(0).build());

        controllerAxes.add(builder.entryBuilder().startIntField(
                new TranslatableText("config.quadz.entry.roll_axis"), Config.getInstance().roll)
                .setDefaultValue(Config.getInstance().roll)
                .setSaveConsumer(value -> Config.getInstance().roll = value)
                .setMin(0).build());

        controllerAxes.add(builder.entryBuilder().startIntField(
                new TranslatableText("config.quadz.entry.throttle_axis"), Config.getInstance().throttle)
                .setDefaultValue(Config.getInstance().throttle)
                .setSaveConsumer(value -> Config.getInstance().throttle = value)
                .setMin(0).build());

        controllerAxes.add(builder.entryBuilder().startBooleanToggle(
                new TranslatableText("config.quadz.entry.invert_pitch"), Config.getInstance().invertPitch)
                .setDefaultValue(Config.getInstance().invertPitch)
                .setSaveConsumer(value -> Config.getInstance().invertPitch = value)
                .build());

        controllerAxes.add(builder.entryBuilder().startBooleanToggle(
                new TranslatableText("config.quadz.entry.invert_yaw"), Config.getInstance().invertYaw)
                .setDefaultValue(Config.getInstance().invertYaw)
                .setSaveConsumer(value -> Config.getInstance().invertYaw = value)
                .build());

        controllerAxes.add(builder.entryBuilder().startBooleanToggle(
                new TranslatableText("config.quadz.entry.invert_roll"), Config.getInstance().invertRoll)
                .setDefaultValue(Config.getInstance().invertRoll)
                .setSaveConsumer(value -> Config.getInstance().invertRoll = value)
                .build());

        controllerAxes.add(builder.entryBuilder().startBooleanToggle(
                new TranslatableText("config.quadz.entry.invert_throttle"), Config.getInstance().invertThrottle)
                .setDefaultValue(Config.getInstance().invertThrottle)
                .setSaveConsumer(value -> Config.getInstance().invertThrottle = value)
                .build());

        controllerAxes.add(builder.entryBuilder().startBooleanToggle(
                new TranslatableText("config.quadz.entry.throttle_in_center"), Config.getInstance().throttleInCenter)
                .setDefaultValue(Config.getInstance().throttleInCenter)
                .setSaveConsumer(value -> Config.getInstance().throttleInCenter = value)
                .build());

        controllerSetup.addEntry(preferences.setExpanded(true).build());
        controllerSetup.addEntry(controllerAxes.build());
        return controllerSetup;
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreen::create;
    }
}
