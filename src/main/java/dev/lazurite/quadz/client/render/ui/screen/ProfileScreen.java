package dev.lazurite.quadz.client.render.ui.screen;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.common.data.DataDriver;
import dev.lazurite.quadz.common.data.model.Settings;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.util.Frequency;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class ProfileScreen {
    public static void show(QuadcopterEntity entity) {
        Settings settings = DataDriver.getTemplate(entity.getTemplate()).getSettings();
        TempSettings temp = new TempSettings(entity);

        ConfigBuilder builder = ConfigBuilder.create()
                .setTitle(new LiteralText(settings.getName() + " by " + settings.getAuthor()))
                .setSavingRunnable(() -> {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeInt(entity.getEntityId());
                    buf.writeInt(temp.cameraAngle);
                    buf.writeFloat(temp.mass);
                    buf.writeFloat(temp.dragCoefficient);
                    buf.writeFloat(temp.thrust);
                    buf.writeFloat(temp.thrustCurve);
                    buf.writeFloat(temp.width);
                    buf.writeFloat(temp.height);
                    buf.writeInt(temp.band);
                    buf.writeInt(temp.channel);
                    ClientPlayNetworking.send(Quadz.QUADCOPTER_SETTINGS_C2S, buf);

                    entity.setWidth(temp.width);
                    entity.setHeight(temp.height);
                    entity.calculateDimensions();
                });

        ConfigCategory category = builder.getOrCreateCategory(new LiteralText(""));

        category.addEntry(builder.entryBuilder().startFloatField(
                new TranslatableText("quadconfig.quadz.entry.mass"), entity.getRigidBody().getMass())
                .setSaveConsumer(value -> temp.mass = value)
                .setDefaultValue(settings.getMass()).setMin(0.0f)
                .build());

        category.addEntry(builder.entryBuilder().startFloatField(
                new TranslatableText("quadconfig.quadz.entry.drag_coefficient"), entity.getRigidBody().getDragCoefficient())
                .setSaveConsumer(value -> temp.dragCoefficient = value)
                .setDefaultValue(settings.getDragCoefficient()).setMin(0.0f)
                .build());

        category.addEntry(builder.entryBuilder().startFloatField(
                new TranslatableText("quadconfig.quadz.entry.thrust"), entity.getThrust())
                .setSaveConsumer(value -> temp.thrust = value)
                .setDefaultValue(settings.getThrust())
                .build());

        category.addEntry(builder.entryBuilder().startFloatField(
                new TranslatableText("quadconfig.quadz.entry.thrust_curve"), entity.getThrustCurve())
                .setSaveConsumer(value -> temp.thrustCurve = value)
                .setDefaultValue(settings.getThrustCurve()).setMin(0.0f)
                .build());

        category.addEntry(builder.entryBuilder().startFloatField(
                new TranslatableText("quadconfig.quadz.entry.width"), entity.getWidth())
                .setSaveConsumer(value -> temp.width = value)
                .setDefaultValue(settings.getWidth()).setMin(0.0f)
                .build());

        category.addEntry(builder.entryBuilder().startFloatField(
                new TranslatableText("quadconfig.quadz.entry.height"), entity.getHeight())
                .setSaveConsumer(value -> temp.height = value)
                .setDefaultValue(settings.getHeight()).setMin(0.0f)
                .build());

        category.addEntry(builder.entryBuilder().startIntSlider(
                new TranslatableText("config.quadz.entry.band"), Frequency.getBandIndex((char) temp.band), 0, 4)
                .setTextGetter(value -> new LiteralText(String.valueOf(Frequency.BANDS[value])))
                .setSaveConsumer(value -> temp.band = Frequency.BANDS[value])
                .setDefaultValue(Frequency.getBandIndex(Config.getInstance().band))
                .build());

        category.addEntry(builder.entryBuilder().startIntSlider(
                new TranslatableText("config.quadz.entry.channel"), temp.channel, 1, 8)
                .setSaveConsumer(value -> temp.channel = value)
                .setDefaultValue(Config.getInstance().channel)
                .build());

        category.addEntry(builder.entryBuilder().startIntSlider(
                new TranslatableText("quadconfig.quadz.entry.camera_angle"), entity.getCameraAngle(), 0, 90)
                .setSaveConsumer(value -> temp.cameraAngle = value)
                .setDefaultValue(settings.getCameraAngle())
                .build());

        MinecraftClient.getInstance().openScreen(builder.setFallbackCategory(category).build());
    }

    static class TempSettings {
        public int cameraAngle;
        public float mass;
        public float dragCoefficient;
        public float thrust;
        public float thrustCurve;
        public float width;
        public float height;
        public int band;
        public int channel;

        TempSettings(QuadcopterEntity entity) {
            Frequency frequency = entity.getFrequency();
            this.cameraAngle = entity.getCameraAngle();
            this.mass = entity.getRigidBody().getMass();
            this.dragCoefficient = entity.getRigidBody().getDragCoefficient();
            this.thrust = entity.getThrust();
            this.thrustCurve = entity.getThrustCurve();
            this.width = entity.getWidth();
            this.height = entity.getHeight();
            this.band = frequency.getBand();
            this.channel = frequency.getChannel();
        }
    }
}
