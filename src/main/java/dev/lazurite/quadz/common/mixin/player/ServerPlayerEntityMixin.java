package dev.lazurite.quadz.common.mixin.player;

import com.mojang.authlib.GameProfile;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    /* Ughhhh */
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    /**
     * Changes the behavior of sneaking as the player so that when the player
     * is flying a drone, they cannot exit from the goggles at this point in the code.
     * @param player the player on which this method was originally called
     * @return whether or not the player should dismount
     */
    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;shouldDismount()Z")
    )
    public boolean shouldDismount(ServerPlayerEntity player) {
        if (player.getCameraEntity() instanceof QuadcopterEntity) {
            return false;
        } else {
            return player.isSneaking();
        }
    }
}
