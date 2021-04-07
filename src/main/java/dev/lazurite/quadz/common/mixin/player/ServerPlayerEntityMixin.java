package dev.lazurite.quadz.common.mixin.player;

import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.util.Frequency;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Changes the behavior of sneaking as the player so that when the player
 * is flying a drone, they cannot exit from the goggles at this point in the code.
 */
@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements Frequency.Storage {
    @Unique private Frequency frequency;

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;shouldDismount()Z"
            )
    )
    public boolean shouldDismount(ServerPlayerEntity player) {
        if (player.getCameraEntity() instanceof QuadcopterEntity) {
            return false;
        } else {
            return player.isSneaking();
        }
    }

    @Override
    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    @Override
    public Frequency getFrequency() {
        return this.frequency;
    }
}
