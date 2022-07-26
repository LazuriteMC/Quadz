package dev.lazurite.quadz.client.render.ui.screen;

import dev.lazurite.quadz.api.InputHandler;
import dev.lazurite.quadz.api.JoystickRegistry;
import dev.lazurite.quadz.common.data.Config;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Class for housing the methods which returns a new config screen made using Cloth Config.
 * @see Config
 */
public class ConfigScreen {
    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setTitle(Component.translatable("config.remote.title"))
                .setSavingRunnable(Config::save)
                .setParentScreen(parent);

        final var controller = builder.getOrCreateCategory(Component.translatable("config.remote.category.controller"));
        final var camera = builder.getOrCreateCategory(Component.translatable("config.remote.category.camera"));
        final var joysticks = InputHandler.JOYSTICKS;

        JoystickRegistry.getInstance().registerControllableParameter(new IntegerParameter(new ResourceLocation(MODID, "controllerId"), Component.translatable("config.remote.controller.controllerId"), Config.Category.CONTROLLER, -1, true, -1, 100, false));
        JoystickRegistry.getInstance().registerControllableParameter(new FloatParameter(new ResourceLocation(MODID, "deadzone"), Component.translatable("config.remote.controller.deadzone"), Config.Category.CONTROLLER, 0.05f, true, 0.0f, 1.0f, true));
        JoystickRegistry.getInstance().registerControllableParameter(new BooleanParameter(new ResourceLocation(MODID, "followLOS"), Component.translatable("config.remote.camera.followLOS"), Config.Category.CAMERA, true, true));
        JoystickRegistry.getInstance().registerControllableParameter(new BooleanParameter(new ResourceLocation(MODID, "renderFirstPerson"), Component.translatable("config.remote.camera.renderFirstPerson"), Config.Category.CAMERA, true, true));
        JoystickRegistry.getInstance().registerControllableParameter(new BooleanParameter(new ResourceLocation(MODID, "renderCameraInCenter"), Component.translatable("config.remote.camera.renderCameraInCenter"), Config.Category.CAMERA, true, true));
        JoystickRegistry.getInstance().registerControllableParameter(new BooleanParameter(new ResourceLocation(MODID, "osdEnabled"), Component.translatable("config.remote.camera.osdEnabled"), Config.Category.CAMERA, true, true));
        JoystickRegistry.getInstance().registerControllableParameter(new IntegerParameter(new ResourceLocation(MODID, "firstPersonFOV"), Component.translatable("config.remote.camera.firstPersonFOV"), Config.Category.CAMERA, 30, true, 10, 150, true));
        JoystickRegistry.getInstance().registerControllableParameter(new EnumParameter<>(new ResourceLocation(MODID, "velocityUnit"), Component.translatable("config.remote.camera.velocityUnit"), Config.Category.CAMERA, OnScreenDisplay.VelocityUnit.METERS_PER_SECOND, OnScreenDisplay.VelocityUnit.class, true));

//        IntegerSliderEntry maxAngleEntry = builder.entryBuilder().startIntSlider(
//                Component.translatable("config.quadz.entry.max_angle"), OldConfig.getInstance().maxAngle, 10, 45)
//                .setSaveConsumer(value -> OldConfig.getInstance().maxAngle = value)
//                .setDefaultValue(OldConfig.getInstance().maxAngle)
//                .build();
//
//        EnumListEntry modeSelector = builder.entryBuilder().startEnumSelector(
//                Component.translatable("config.quadz.entry.mode"), Mode.class, OldConfig.getInstance().mode)
//                .setEnumNameProvider(value -> {
//                    maxAngleEntry.setEditable(value == Mode.ANGLE || (int) controllerSelectorBuilt.get().getValue() == -1);
//                    return Component.translatable(((Mode) value).getTranslation());
//                })
//                .setSaveConsumer(value -> OldConfig.getInstance().mode = value)
//                .setDefaultValue(OldConfig.getInstance().mode)
//                .build();

        JoystickRegistry.getInstance().getJoystickAxes().forEach(joystickAxis -> {
            controller.addEntry(builder.entryBuilder().startIntField(joystickAxis.getName(), joystickAxis.getAxis())
                    .setDefaultValue(joystickAxis.getAxis())
                    .setSaveConsumer(joystickAxis::setAxis)
                    .setMin(0)
                    .build());

            controller.addEntry(builder.entryBuilder().startBooleanToggle(joystickAxis.getName(), joystickAxis.isInverted())
                    .setDefaultValue(joystickAxis.isInverted())
                    .setSaveConsumer(joystickAxis::setInverted)
                    .build());
        });

        controller.addEntry(builder.entryBuilder().startSelector(
                Component.translatable("config.remote.controller.controller_id"), joysticks.keySet().toArray(), Config.controllerId)
                .setDefaultValue(Config.controllerId)
                .setSaveConsumer(value -> Config.controllerId = (int) value)
                .build());

        return builder.setFallbackCategory(controller).build();
    }
}
