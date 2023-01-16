package dev.lazurite.quadz.client.event;

import dev.lazurite.toolbox.api.network.PacketRegistry;
import net.minecraft.client.Minecraft;

public class ClientNetworkEventHooks {

    public static void onJoystickInput(PacketRegistry.ClientboundContext context) {
        var buf = context.byteBuf();
        var id = buf.readUUID();
        var axisCount = buf.readInt();
        var player = Minecraft.getInstance().level.getPlayerByUUID(id);

        if (player != null) {
            for (int i = 0; i < axisCount; i++) {
                var axis = buf.readResourceLocation();
                var value = buf.readFloat();
                player.quadz$setJoystickValue(axis, value);
            }
        }
    }

}
