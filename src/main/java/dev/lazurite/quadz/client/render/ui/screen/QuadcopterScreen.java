package dev.lazurite.quadz.client.render.ui.screen;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.common.data.DataDriver;
import dev.lazurite.quadz.common.data.model.Template;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
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
public class QuadcopterScreen {
    public static void show(QuadcopterEntity entity) {
        Template.Settings settings = DataDriver.getTemplate(entity.getTemplate()).getSettings();

        ConfigBuilder builder = ConfigBuilder.create()
                .setTitle(new LiteralText(settings.getName() + " by " + settings.getAuthor()))
                .setSavingRunnable(() -> {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeInt(entity.getId());
                    buf.writeInt(entity.getCameraAngle());
                    ClientPlayNetworking.send(Quadz.QUADCOPTER_SETTINGS_C2S, buf);
                });

        ConfigCategory category = builder.getOrCreateCategory(new LiteralText(""));

        category.addEntry(builder.entryBuilder().startIntSlider(
                new TranslatableText("config.quadz.entry.camera_angle"), entity.getCameraAngle(), 0, 90)
                .setDefaultValue(settings.getCameraAngle())
                .setSaveConsumer(entity::setCameraAngle)
                .build());

        MinecraftClient.getInstance().setScreen(builder.setFallbackCategory(category).build());
    }
}
