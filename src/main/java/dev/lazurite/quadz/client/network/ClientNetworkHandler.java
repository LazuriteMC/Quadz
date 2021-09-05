package dev.lazurite.quadz.client.network;

import dev.lazurite.quadz.client.input.Mode;
import dev.lazurite.quadz.client.render.QuadzRendering;
import dev.lazurite.quadz.common.data.DataDriver;
import dev.lazurite.quadz.common.data.model.Template;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.util.input.InputFrame;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;

import java.util.concurrent.CompletableFuture;

public class ClientNetworkHandler {
    public static void onInputFrameReceived(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int entityId = buf.readInt();
        InputFrame frame = new InputFrame(
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readEnumConstant(Mode.class));

        client.execute(() -> {
            Entity entity = client.world.getEntityById(entityId);

            if (entity instanceof QuadcopterEntity) {
                ((QuadcopterEntity) entity).getInputFrame().set(frame);
            }
        });
    }

    public static void onTemplateReceived(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        Template template = Template.deserialize(buf);

        client.execute(() -> {
            DataDriver.load(template);
            CompletableFuture.runAsync(() -> QuadzRendering.templateLoader.load(template));
        });
    }
}
