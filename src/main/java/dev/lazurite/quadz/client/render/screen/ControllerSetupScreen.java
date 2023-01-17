package dev.lazurite.quadz.client.render.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.client.render.screen.osd.OnScreenDisplay;
import dev.lazurite.quadz.common.util.FloatBufferUtil;
import dev.lazurite.quadz.common.util.JoystickOutput;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.function.Consumer;

public class ControllerSetupScreen extends Screen {

    private final Screen parent;

    private final ResourceLocation pitchLocation = new ResourceLocation(Quadz.MODID, "pitch");
    private final ResourceLocation yawLocation = new ResourceLocation(Quadz.MODID, "yaw");
    private final ResourceLocation rollLocation = new ResourceLocation(Quadz.MODID, "roll");
    private final ResourceLocation throttleLocation = new ResourceLocation(Quadz.MODID, "throttle");

    private Button saveButton;
    private Button cancelButton;
    private Button configButton;
    private Button pitchButton;
    private Button yawButton;
    private Button rollButton;
    private Button throttleButton;
    private Button invertPitchButton;
    private Button invertYawButton;
    private Button invertRollButton;
    private Button invertThrottleButton;
    private Consumer<Integer> axisConsumer;

    private FloatBuffer axes;
    private List<Float> previousAxes;

    public ControllerSetupScreen(Screen parent) {
        super(Component.translatable("quadz.config.controller_setup.title"));
        this.parent = parent;
    }

    @Override
    public void init() {
        var spacing = 10;
        var bWidth = 60;
        var bHeight = 20;

        this.saveButton = Button.builder(Component.translatable("quadz.config.controller_setup.save"), this::onSaveButton)
            .pos(spacing, height - bHeight - spacing)
            .size(bWidth, bHeight)
            .build();

        this.cancelButton = Button.builder(Component.translatable("quadz.config.controller_setup.cancel"), this::onCancelButton)
                .pos(spacing + bWidth + spacing / 2, height - bHeight - spacing)
                .size(bWidth, bHeight)
                .build();

        this.configButton = Button.builder(Component.translatable("quadz.config.controller_setup.config"), this::onConfigButton)
                .pos(width - spacing - bWidth, height - bHeight - spacing)
                .size(bWidth, bHeight)
                .build();

        this.pitchButton = Button.builder(Component.translatable("quadz.config.controller_setup.pitch"), button -> onAxisButton(axis -> Config.pitch = axis))
            .pos(width / 2 + bWidth + spacing, bHeight + spacing)
            .size(bWidth, bHeight)
            .build();

        this.yawButton = Button.builder(Component.translatable("quadz.config.controller_setup.yaw"), button -> onAxisButton(axis -> Config.yaw = axis))
            .pos(width / 2 - bWidth - spacing / 3, bHeight + spacing)
            .size(bWidth, bHeight)
            .build();

        this.rollButton = Button.builder(Component.translatable("quadz.config.controller_setup.roll"), button -> onAxisButton(axis -> Config.roll = axis))
            .pos(width / 2 + spacing / 3, bHeight + spacing)
            .size(bWidth, bHeight)
            .build();

        this.throttleButton = Button.builder(Component.translatable("quadz.config.controller_setup.throttle"), button -> onAxisButton(axis -> Config.throttle = axis))
            .pos(width / 2 - bWidth * 2 - spacing, bHeight + spacing)
            .size(bWidth, bHeight)
            .build();

        this.invertPitchButton = Button.builder(Component.translatable("quadz.config.controller_setup.invert"), button -> Config.pitchInverted = !Config.pitchInverted)
                .pos(width / 2 + bWidth + spacing, bHeight * 2 + spacing)
                .size(bWidth, bHeight)
                .build();

        this.invertYawButton = Button.builder(Component.translatable("quadz.config.controller_setup.invert"), button -> Config.yawInverted = !Config.yawInverted)
                .pos(width / 2 - bWidth - spacing / 3, bHeight * 2 + spacing)
                .size(bWidth, bHeight)
                .build();

        this.invertRollButton = Button.builder(Component.translatable("quadz.config.controller_setup.invert"), button -> Config.rollInverted = !Config.rollInverted)
                .pos(width / 2 + spacing / 3, bHeight * 2 + spacing)
                .size(bWidth, bHeight)
                .build();

        this.invertThrottleButton = Button.builder(Component.translatable("quadz.config.controller_setup.invert"), button -> Config.throttleInverted = !Config.throttleInverted)
                .pos(width / 2 - bWidth * 2 - spacing, bHeight * 2 + spacing)
                .size(bWidth, bHeight)
                .build();

        this.addRenderableWidget(this.saveButton);
        this.addRenderableWidget(this.cancelButton);
        this.addRenderableWidget(this.configButton);
        this.addRenderableWidget(this.pitchButton);
        this.addRenderableWidget(this.yawButton);
        this.addRenderableWidget(this.rollButton);
        this.addRenderableWidget(this.throttleButton);
        this.addRenderableWidget(this.invertPitchButton);
        this.addRenderableWidget(this.invertYawButton);
        this.addRenderableWidget(this.invertRollButton);
        this.addRenderableWidget(this.invertThrottleButton);
    }

    private void onSaveButton(Button button) {
        Config.save();
        this.minecraft.setScreen(this.parent);
    }

    private void onCancelButton(Button button) {
        this.minecraft.setScreen(this.parent);
    }

    private void onConfigButton(Button button) {
        this.minecraft.setScreen(MainConfigScreen.get(this));
    }

    private void onAxisButton(Consumer<Integer> axisConsumer) {
        this.axisConsumer = axisConsumer;
        this.pitchButton.active = false;
        this.yawButton.active = false;
        this.rollButton.active = false;
        this.throttleButton.active = false;
        this.saveButton.active = false;
        this.invertPitchButton.active = false;
        this.invertYawButton.active = false;
        this.invertRollButton.active = false;
        this.invertThrottleButton.active = false;
    }

    @Override
    public void tick() {
        while (this.axisConsumer != null && this.axes != null && this.axes.hasRemaining() && this.previousAxes != null) {
            var currentAxis = Math.round(this.axes.get() * 10f) / 10f;
            var previousAxis = Math.round(this.previousAxes.get(this.axes.position() - 1) * 10f) / 10f;

            if (currentAxis != previousAxis) {
                this.axisConsumer.accept(this.axes.position() - 1);
                this.axisConsumer = null;
                this.pitchButton.active = true;
                this.yawButton.active = true;
                this.rollButton.active = true;
                this.throttleButton.active = true;
                this.saveButton.active = true;
                this.invertPitchButton.active = true;
                this.invertYawButton.active = true;
                this.invertRollButton.active = true;
                this.invertThrottleButton.active = true;
                break;
            }
        }

        if (this.axes != null && this.axes.remaining() > 0)
            this.previousAxes = FloatBufferUtil.toArray(this.axes);
        this.axes = JoystickOutput.getAllAxisValues();
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        super.renderDirtBackground(0);
        super.render(poseStack, i, j, f);

        var pitch = JoystickOutput.getAxisValue(null, Config.pitch, this.pitchLocation, Config.pitchInverted, false);
        var yaw = JoystickOutput.getAxisValue(null, Config.yaw, this.yawLocation, Config.yawInverted, false);
        var roll = JoystickOutput.getAxisValue(null, Config.roll, this.rollLocation, Config.rollInverted, false);
        var throttle = JoystickOutput.getAxisValue(null, Config.throttle, this.throttleLocation, Config.throttleInverted, Config.throttleInCenter) + 1.0f;
        OnScreenDisplay.renderSticks(poseStack, f, width / 2, height / 2 + 20, 40, 10, pitch, yaw, roll, throttle);

        // An axis has been selected. Time to listen...
        if (this.axisConsumer != null) {
            drawCenteredString(poseStack, this.font, Component.translatable("quadz.config.controller_setup.prompt"), this.width / 2, 85, 0x00FF00);
        }
    }

}
