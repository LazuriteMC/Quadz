package dev.lazurite.quadz.common.util.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class BetterServerJoinEvents {
    public static final Event<Join> JOIN = EventFactory.createArrayBacked(Join.class, (callbacks) -> (player, server) -> {
        for (Join event : callbacks) {
            event.onJoin(player, server);
        }
    });

    @FunctionalInterface
    public interface Join {
        void onJoin(ServerPlayerEntity player, MinecraftServer server);
    }
}
