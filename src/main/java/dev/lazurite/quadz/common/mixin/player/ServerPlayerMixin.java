package dev.lazurite.quadz.common.mixin.player;

import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.util.PlayerData;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements PlayerData {
    @Unique private String callSign;

    /**
     * Changes the behavior of sneaking as the player so that when the player
     * is flying a drone, they cannot exit from the goggles at this point in the code.
     */
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;wantsToStopRiding()Z"
            )
    )
    public boolean tick_wantsToStopRiding(ServerPlayer serverPlayer) {
        if (serverPlayer.getCamera() instanceof QuadcopterEntity) {
            return false;
        } else {
            return serverPlayer.isCrouching();
        }
    }

    @Override
    public void setCallSign(String callSign) {
        this.callSign = callSign;
    }

    @Override
    public String getCallSign() {
        return this.callSign;
    }
}
