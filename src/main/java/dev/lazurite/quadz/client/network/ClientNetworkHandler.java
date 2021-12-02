package dev.lazurite.quadz.client.network;

import dev.lazurite.quadz.client.input.Mode;
import dev.lazurite.quadz.client.render.QuadzRendering;
import dev.lazurite.quadz.common.data.DataDriver;
import dev.lazurite.quadz.common.data.model.Template;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.util.input.InputFrame;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

import java.util.concurrent.CompletableFuture;

public class ClientNetworkHandler {
    public static void onInputFrameReceived(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender sender) {
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
                buf.readEnum(Mode.class));

        client.execute(() -> {
            Entity entity = client.level.getEntity(entityId);

            if (entity instanceof QuadcopterEntity) {
                ((QuadcopterEntity) entity).getInputFrame().set(frame);
            }
        });
    }

    public static void onTemplateReceived(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender sender) {
        Template template = Template.deserialize(buf);

        client.execute(() -> {
            DataDriver.load(template);
            CompletableFuture.runAsync(() -> QuadzRendering.templateLoader.load(template));
        });
    }
}
