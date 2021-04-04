package dev.lazurite.quadz.common.mixin.player;

import dev.lazurite.quadz.common.util.event.BetterServerJoinEvents;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Shadow @Final private MinecraftServer server;

    @Inject(
            method = "onPlayerConnect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V",
                    shift = At.Shift.AFTER
            )
    )
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
        BetterServerJoinEvents.JOIN.invoker().onJoin(player, server);
    }
}
