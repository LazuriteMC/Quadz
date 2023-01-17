package dev.lazurite.quadz.client.render.screen;

import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.client.render.screen.osd.VelocityUnit;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public interface MainConfigScreen {

    static Screen get(Screen parent) {
        var builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("quadz.config.title"))
            .setSavingRunnable(Config::save);

        var entryBuilder = builder.entryBuilder();
        var controllerCategory = builder.getOrCreateCategory(Component.translatable("quadz.config.controller.title"));
        var visualsCategory = builder.getOrCreateCategory(Component.translatable("quadz.config.visuals.title"));

        controllerCategory.addEntry(
                entryBuilder.startFloatField(Component.translatable("quadz.config.controller.rate"), Config.rate)
                        .setDefaultValue(Config.rate)
                        .setSaveConsumer(value -> Config.rate = value)
                        .build()
        );

        controllerCategory.addEntry(
                entryBuilder.startFloatField(Component.translatable("quadz.config.controller.superRate"), Config.superRate)
                        .setDefaultValue(Config.superRate)
                        .setSaveConsumer(value -> Config.superRate = value)
                        .build()
        );

        controllerCategory.addEntry(
                entryBuilder.startFloatField(Component.translatable("quadz.config.controller.expo"), Config.expo)
                        .setDefaultValue(Config.expo)
                        .setSaveConsumer(value -> Config.expo = value)
                        .build()
        );

        controllerCategory.addEntry(
                entryBuilder.startFloatField(Component.translatable("quadz.config.controller.deadzone"), Config.deadzone)
                        .setDefaultValue(Config.deadzone)
                        .setSaveConsumer(value -> Config.deadzone = value)
                        .build()
        );

        visualsCategory.addEntry(
                entryBuilder.startBooleanToggle(Component.translatable("quadz.config.visuals.osd_toggle"), Config.osdEnabled)
                        .setDefaultValue(Config.osdEnabled)
                        .setSaveConsumer(value -> Config.osdEnabled = value)
                        .build()
        );

        visualsCategory.addEntry(
                entryBuilder.startEnumSelector(Component.translatable("quadz.config.visuals.velocity_unit"), VelocityUnit.class, Config.velocityUnit)
                        .setEnumNameProvider(unit -> ((VelocityUnit) unit).getTranslation())
                        .setDefaultValue(Config.velocityUnit)
                        .setSaveConsumer(value -> Config.velocityUnit = value)
                        .build()
        );

        visualsCategory.addEntry(
                entryBuilder.startBooleanToggle(Component.translatable("quadz.config.visuals.follow_los_toggle"), Config.followLOS)
                        .setDefaultValue(Config.followLOS)
                        .setSaveConsumer(value -> Config.followLOS = value)
                        .build()
        );

        visualsCategory.addEntry(
                entryBuilder.startBooleanToggle(Component.translatable("quadz.config.visuals.static_toggle"), Config.videoInterferenceEnabled)
                    .setDefaultValue(Config.videoInterferenceEnabled)
                    .setSaveConsumer(value -> Config.videoInterferenceEnabled = value)
                    .build()
        );

        visualsCategory.addEntry(
                entryBuilder.startBooleanToggle(Component.translatable("quadz.config.visuals.fisheye_toggle"), Config.fisheyeEnabled)
                        .setDefaultValue(Config.fisheyeEnabled)
                        .setSaveConsumer(value -> Config.fisheyeEnabled = value)
                        .build()
        );

        visualsCategory.addEntry(
                entryBuilder.startIntSlider(Component.translatable("quadz.config.visuals.fisheye_amount"), (int) (Config.fisheyeAmount * 100), 0, 100)
                        .setDefaultValue((int) (Config.fisheyeAmount * 100))
                        .setSaveConsumer(value -> Config.fisheyeAmount = value * 0.01f)
                        .build()
        );

        visualsCategory.addEntry(
                entryBuilder.startBooleanToggle(Component.translatable("quadz.config.visuals.camera_in_center_toggle"), Config.renderCameraInCenter)
                        .setDefaultValue(Config.renderCameraInCenter)
                        .setSaveConsumer(value -> Config.renderCameraInCenter = value)
                        .build()
        );

        visualsCategory.addEntry(
                entryBuilder.startBooleanToggle(Component.translatable("quadz.config.visuals.first_person_render_toggle"), Config.renderFirstPerson)
                        .setDefaultValue(Config.renderFirstPerson)
                        .setSaveConsumer(value -> Config.renderFirstPerson = value)
                        .build()
        );

        builder.setGlobalized(true);
        var screen = builder.build();
        screen.addRenderableWidget(Button.builder(Component.literal("TEST"), button -> {}).pos(100, 100).size(60, 20).build());
        return screen;
    }

}
