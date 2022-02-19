package dev.lazurite.quadz.client.network;

import dev.lazurite.quadz.client.input.Mode;
import dev.lazurite.quadz.client.render.QuadzRendering;
import dev.lazurite.quadz.common.data.template.TemplateLoader;
import dev.lazurite.quadz.common.data.template.model.Template;
import dev.lazurite.quadz.common.quadcopter.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.util.input.InputFrame;
import dev.lazurite.toolbox.api.network.PacketRegistry;
import net.minecraft.client.Minecraft;

import java.util.concurrent.CompletableFuture;

public class ClientNetworkHandler {
    public static void onInputFrameReceived(PacketRegistry.ClientboundContext context) {
        final var client = Minecraft.getInstance();
        final var buf = context.byteBuf();
        final var entityId = buf.readInt();

        final var frame = new InputFrame(
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readEnum(Mode.class));

        client .execute(() -> {
            final var entity = client.level.getEntity(entityId);

            if (entity instanceof QuadcopterEntity quadcopterEntity) {
                quadcopterEntity.getInputFrame().set(frame);
            }
        });
    }

    public static void onTemplateReceived(PacketRegistry.ClientboundContext context) {
        final var client = Minecraft.getInstance();
        final var buf = context.byteBuf();
        final var template = Template.deserialize(buf);

        client.execute(() -> {
            TemplateLoader.load(template);
            CompletableFuture.runAsync(() -> QuadzRendering.templateLoader.load(template));
        });
    }
}