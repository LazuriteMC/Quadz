package dev.lazurite.quadz.client.render.ui.screen;

import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.client.input.Mode;
import dev.lazurite.quadz.client.input.InputTick;
import dev.lazurite.quadz.client.render.ui.osd.OnScreenDisplay;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.gui.entries.*;
import me.shedaniel.clothconfig2.impl.builders.SelectorBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class for housing the methods which returns a new config screen made using Cloth Config.
 * @see Config
 */
@Environment(EnvType.CLIENT)
public class ConfigScreen {
    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setTitle(new TranslatableText("config.quadz.title"))
                .setSavingRunnable(Config.getInstance()::save)
                .setParentScreen(parent);

        ConfigCategory controllerSetup = builder.getOrCreateCategory(new TranslatableText("config.quadz.category.setup"));
        ConfigCategory controllerPreferences = builder.getOrCreateCategory(new TranslatableText("config.quadz.category.stick_feel"));
        ConfigCategory cameraPreferences = builder.getOrCreateCategory(new TranslatableText("config.quadz.category.camera"));
        ConfigCategory osdPreferences = builder.getOrCreateCategory(new TranslatableText("config.quadz.category.osd"));
        Map <Integer, String> joysticks = InputTick.getInstance().getJoysticks();

        // region camera preferences

        cameraPreferences.addEntry(builder.entryBuilder().startBooleanToggle(
                new TranslatableText("config.quadz.entry.follow_los"), Config.getInstance().followLOS)
                .setDefaultValue(Config.getInstance().followLOS)
                .setSaveConsumer(value -> Config.getInstance().followLOS = value)
                .build());

        cameraPreferences.addEntry(builder.entryBuilder().startBooleanToggle(
                new TranslatableText("config.quadz.entry.render_first_person"), Config.getInstance().renderFirstPerson)
                .setDefaultValue(Config.getInstance().renderFirstPerson)
                .setSaveConsumer(value -> Config.getInstance().renderFirstPerson = value)
                .build());

        cameraPreferences.addEntry(builder.entryBuilder().startBooleanToggle(
                new TranslatableText("config.quadz.entry.render_camera_in_center"), Config.getInstance().renderCameraInCenter)
                .setDefaultValue(Config.getInstance().renderCameraInCenter)
                .setSaveConsumer(value -> Config.getInstance().renderCameraInCenter = value)
                .build());

        cameraPreferences.addEntry(builder.entryBuilder().startIntSlider(
                new TranslatableText("config.quadz.entry.first_person_fov"), Config.getInstance().firstPersonFOV, 30, 135)
                .setTextGetter(value -> value == 30 ? new LiteralText("Match Player") : new LiteralText(value + "°"))
                .setDefaultValue(30)
                .setSaveConsumer(value -> Config.getInstance().firstPersonFOV = value)
                .build());

        // endregion camera preferences

        // region osd preferences

        osdPreferences.addEntry(builder.entryBuilder().startBooleanToggle(
                new TranslatableText("config.quadz.entry.osd_enabled"), Config.getInstance().osdEnabled)
                .setDefaultValue(Config.getInstance().osdEnabled)
                .setSaveConsumer(value -> Config.getInstance().osdEnabled = value)
                .build());

        osdPreferences.addEntry(builder.entryBuilder().startTextField(
                new TranslatableText("config.quadz.entry.call_sign"), Config.getInstance().callSign)
                .setDefaultValue(Config.getInstance().callSign)
                .setSaveConsumer(value -> Config.getInstance().callSign = value)
                .build());

        osdPreferences.addEntry(builder.entryBuilder().startEnumSelector(
                new TranslatableText("config.quadz.entry.velocity_unit"), OnScreenDisplay.VelocityUnit.class, Config.getInstance().velocityUnit)
                .setDefaultValue(Config.getInstance().velocityUnit)
                .setEnumNameProvider(value -> ((OnScreenDisplay.VelocityUnit) value).getTranslation())
                .setSaveConsumer(value -> Config.getInstance().velocityUnit = value)
                .build());

        // endregion osd preferences

        // region controller preferences

        SelectorBuilder controllerSelector = builder.entryBuilder().startSelector(
                new TranslatableText("config.quadz.entry.controller_id"), joysticks.keySet().toArray(), Config.getInstance().controllerId)
                .setDefaultValue(Config.getInstance().controllerId)
                .setSaveConsumer(value -> Config.getInstance().controllerId = (int) value);

        // hyper concern and gross
        AtomicReference<SelectionListEntry> controllerSelectorBuilt = new AtomicReference<>();

        IntegerSliderEntry maxAngleEntry = builder.entryBuilder().startIntSlider(
                new TranslatableText("config.quadz.entry.max_angle"), Config.getInstance().maxAngle, 10, 45)
                .setSaveConsumer(value -> Config.getInstance().maxAngle = value)
                .setDefaultValue(Config.getInstance().maxAngle)
                .build();

        EnumListEntry modeSelector = builder.entryBuilder().startEnumSelector(
                new TranslatableText("config.quadz.entry.mode"), Mode.class, Config.getInstance().mode)
                .setEnumNameProvider(value -> {
                    maxAngleEntry.setEditable(value == Mode.ANGLE || (int) controllerSelectorBuilt.get().getValue() == -1);
                    return new TranslatableText(((Mode) value).getTranslation());
                })
                .setSaveConsumer(value -> Config.getInstance().mode = value)
                .setDefaultValue(Config.getInstance().mode)
                .build();

        controllerPreferences.addEntry(modeSelector);
        controllerPreferences.addEntry(maxAngleEntry);

        controllerPreferences.addEntry(builder.entryBuilder().startFloatField(
                new TranslatableText("config.quadz.entry.rate"), Config.getInstance().rate)
                .setDefaultValue(Config.getInstance().rate)
                .setSaveConsumer(value -> Config.getInstance().rate = value)
                .setMin(0).build());

        controllerPreferences.addEntry(builder.entryBuilder().startFloatField(
                new TranslatableText("config.quadz.entry.expo"), Config.getInstance().expo)
                .setDefaultValue(Config.getInstance().expo)
                .setSaveConsumer(value -> Config.getInstance().expo = value)
                .setMin(0).build());

        controllerPreferences.addEntry(builder.entryBuilder().startFloatField(
                new TranslatableText("config.quadz.entry.super_rate"), Config.getInstance().superRate)
                .setDefaultValue(Config.getInstance().superRate)
                .setSaveConsumer(value -> Config.getInstance().superRate = value)
                .setMin(0).build());

        controllerPreferences.addEntry(builder.entryBuilder().startFloatField(
                new TranslatableText("config.quadz.entry.deadzone"), Config.getInstance().deadzone)
                .setDefaultValue(Config.getInstance().deadzone)
                .setSaveConsumer(value -> Config.getInstance().deadzone = value)
                .setMin(0).build());

        // endregion controller preferences

        // region controller setup

        IntegerListEntry pitchEntry = builder.entryBuilder().startIntField(
                new TranslatableText("config.quadz.entry.pitch_axis"), Config.getInstance().pitch)
                .setDefaultValue(Config.getInstance().pitch)
                .setSaveConsumer(value -> Config.getInstance().pitch = value)
                .setMin(0).build();

        IntegerListEntry yawEntry = builder.entryBuilder().startIntField(
                new TranslatableText("config.quadz.entry.yaw_axis"), Config.getInstance().yaw)
                .setDefaultValue(Config.getInstance().yaw)
                .setSaveConsumer(value -> Config.getInstance().yaw = value)
                .setMin(0).build();

        IntegerListEntry rollEntry = builder.entryBuilder().startIntField(
                new TranslatableText("config.quadz.entry.roll_axis"), Config.getInstance().roll)
                .setDefaultValue(Config.getInstance().roll)
                .setSaveConsumer(value -> Config.getInstance().roll = value)
                .setMin(0).build();

        IntegerListEntry throttleEntry = builder.entryBuilder().startIntField(
                new TranslatableText("config.quadz.entry.throttle_axis"), Config.getInstance().throttle)
                .setDefaultValue(Config.getInstance().throttle)
                .setSaveConsumer(value -> Config.getInstance().throttle = value)
                .setMin(0).build();

        BooleanListEntry invertPitchEntry = builder.entryBuilder().startBooleanToggle(
                new TranslatableText("config.quadz.entry.invert_pitch"), Config.getInstance().invertPitch)
                .setDefaultValue(Config.getInstance().invertPitch)
                .setSaveConsumer(value -> Config.getInstance().invertPitch = value)
                .build();

        BooleanListEntry invertYawEntry = builder.entryBuilder().startBooleanToggle(
                new TranslatableText("config.quadz.entry.invert_yaw"), Config.getInstance().invertYaw)
                .setDefaultValue(Config.getInstance().invertYaw)
                .setSaveConsumer(value -> Config.getInstance().invertYaw = value)
                .build();

        BooleanListEntry invertRollEntry = builder.entryBuilder().startBooleanToggle(
                new TranslatableText("config.quadz.entry.invert_roll"), Config.getInstance().invertRoll)
                .setDefaultValue(Config.getInstance().invertRoll)
                .setSaveConsumer(value -> Config.getInstance().invertRoll = value)
                .build();

        BooleanListEntry invertThrottleEntry = builder.entryBuilder().startBooleanToggle(
                new TranslatableText("config.quadz.entry.invert_throttle"), Config.getInstance().invertThrottle)
                .setDefaultValue(Config.getInstance().invertThrottle)
                .setSaveConsumer(value -> Config.getInstance().invertThrottle = value)
                .build();

        BooleanListEntry throttleInCenterEntry = builder.entryBuilder().startBooleanToggle(
                new TranslatableText("config.quadz.entry.throttle_in_center"), Config.getInstance().throttleInCenter)
                .setDefaultValue(Config.getInstance().throttleInCenter)
                .setSaveConsumer(value -> Config.getInstance().throttleInCenter = value)
                .build();

        // hyper concern
        controllerSelectorBuilt.set(controllerSelector.setNameProvider(value -> {
            String name = joysticks.get((int) value);
            modeSelector.setEditable((int) value != -1);
            maxAngleEntry.setEditable((int) value == -1 || modeSelector.getValue() == Mode.ANGLE);
            pitchEntry.setEditable((int) value != -1);
            yawEntry.setEditable((int) value != -1);
            rollEntry.setEditable((int) value != -1);
            throttleEntry.setEditable((int) value != -1);
            invertPitchEntry.setEditable((int) value != -1);
            invertYawEntry.setEditable((int) value != -1);
            invertRollEntry.setEditable((int) value != -1);
            invertThrottleEntry.setEditable((int) value != -1);
            throttleInCenterEntry.setEditable((int) value != -1);

            if ((int) value == -1) {
                return new TranslatableText("config.quadz.entry.controller_id.keyboard");
            } else if (name.length() > 15) {
                return new LiteralText(name.substring(0, 15) + "...");
            } else {
                return new LiteralText(name);
            }
        }).build());

        controllerSetup.addEntry(controllerSelectorBuilt.get());
        controllerSetup.addEntry(pitchEntry);
        controllerSetup.addEntry(yawEntry);
        controllerSetup.addEntry(rollEntry);
        controllerSetup.addEntry(throttleEntry);
        controllerSetup.addEntry(invertPitchEntry);
        controllerSetup.addEntry(invertYawEntry);
        controllerSetup.addEntry(invertRollEntry);
        controllerSetup.addEntry(invertThrottleEntry);
        controllerSetup.addEntry(throttleInCenterEntry);

        // endregion controller setup

        return builder.setFallbackCategory(controllerSetup).build();
    }
}
