package dev.lazurite.quadz.common.mixin;

import dev.lazurite.quadz.common.hooks.PlayerHooks;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;wantsToStopRiding()Z"
            )
    )
    public boolean tick$wantsToStopRiding(ServerPlayer player) {
        return PlayerHooks.onWantsToStopRiding(player);
    }

}
