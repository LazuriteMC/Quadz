package bluevista.fpvracingmod.server;

import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.minecraft.server.MinecraftServer;

public class ServerTick {

    public static void tick(MinecraftServer mc) {

    }

    public static void register() {
        ServerTickCallback.EVENT.register(ServerTick::tick);
    }
}
