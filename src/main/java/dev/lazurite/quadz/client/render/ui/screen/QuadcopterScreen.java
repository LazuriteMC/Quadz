package dev.lazurite.quadz.client.render.ui.screen;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.data.template.TemplateLoader;
import dev.lazurite.quadz.common.quadcopter.entity.QuadcopterEntity;
import dev.lazurite.toolbox.api.network.ClientNetworking;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class QuadcopterScreen {
    public static void show(QuadcopterEntity entity) {
        final var settings = TemplateLoader.getTemplate(entity.getTemplate()).getSettings();

        final var builder = ConfigBuilder.create()
                .setTitle(Component.literal(settings.getName() + " by " + settings.getAuthor()))
                .setSavingRunnable(() -> {
                    ClientNetworking.send(Quadz.QUADCOPTER_SETTINGS_C2S, buf -> {
                        buf.writeInt(entity.getId());
                        buf.writeInt(entity.getCameraAngle());
                    });
                });

        final var category = builder.getOrCreateCategory(Component.literal(""));

        category.addEntry(builder.entryBuilder().startIntSlider(
                Component.literal("config.quadz.entry.camera_angle"), entity.getCameraAngle(), 0, 90)
                .setDefaultValue(settings.getCameraAngle())
                .setSaveConsumer(entity::setCameraAngle)
                .build());

        Minecraft.getInstance().setScreen(builder.setFallbackCategory(category).build());
    }
}
