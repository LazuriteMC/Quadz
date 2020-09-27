package bluevista.fpvracing.mixin;

import bluevista.fpvracing.server.ServerTick;
import bluevista.fpvracing.server.entities.DroneEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.packet.s2c.play.SetCameraEntityS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A mixin class for {@link ServerPlayerEntity} which
 * serves multiple purposes.
 * @author Ethan Johnson
 */
@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Shadow ServerPlayNetworkHandler networkHandler;
    @Shadow Entity cameraEntity;

    /**
     * Designed to prevent a server player who is flying a drone from
     * becoming unloaded on clients observing the player.
     * @param entity the entity that the game wishes to stop tracking
     * @param info required by every mixin injection
     */
    @Inject(at = @At("HEAD"), method = "onStoppedTracking", cancellable = true)
    public void onStoppedTracking(Entity entity, CallbackInfo info) {
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) entity;

            if (ServerTick.isInGoggles(player)) {
                info.cancel();
            }
        }

        if (entity instanceof DroneEntity) {
            if (ServerTick.isInGoggles((ServerPlayerEntity) (Object) this) && !entity.removed) {
                info.cancel();
            }
        }
    }

    /**
     * Helps prevent the {@link ServerPlayerEntity} from taking damage while in teleported state.
     * @param source the damage source
     * @param amount the damage amount
     * @param info required by every mixin injection
     */
    @Inject(at = @At("HEAD"), method = "damage", cancellable = true)
    public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        if (ServerTick.isInGoggles(player)) {
            DroneEntity drone = (DroneEntity) player.getCameraEntity();

            if (!player.getPos().equals(drone.getPlayerStartPos().get(player))) {
                info.setReturnValue(false);
            }
        }
    }

    /**
     * Does the same things as {@link ServerPlayerEntity#setCameraEntity(Entity)} but excludes the
     * {@link ServerPlayerEntity#requestTeleport(double, double, double)} method call towards the end.
     * @param entity the entity that will be set as the player's camera entity
     * @param info required by every mixin injection
     */
    @Inject(at = @At("HEAD"), method = "setCameraEntity", cancellable = true)
    public void setCameraEntity(Entity entity, CallbackInfo info) {
        Entity prevEntity = cameraEntity;
        Entity nextEntity = (Entity) (entity == null ? this : entity);

        if (prevEntity instanceof DroneEntity || nextEntity instanceof DroneEntity) {
            networkHandler.sendPacket(new SetCameraEntityS2CPacket(nextEntity));
            cameraEntity = nextEntity;
            // requestTeleport is gone now
            info.cancel();
        }
    }

    /**
     * When the player disconnects from the server, run {@link ServerTick#resetView(ServerPlayerEntity)}
     * @param info required by every mixin injection
     */
    @Inject(at = @At("TAIL"), method = "onDisconnect")
    public void onDisconnect(CallbackInfo info) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        if (ServerTick.isInGoggles(player)) {
            ServerTick.resetView(player);
        }
    }

    /**
     * Inserts a {@link ServerTick#resetView(ServerPlayerEntity)} method call into tick
     * just before the second {@link ServerPlayerEntity#setCameraEntity(Entity)} is called.
     * @param info required by every mixin injection
     */
    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;setCameraEntity(Lnet/minecraft/entity/Entity;)V",
                    ordinal = 1
            )
    )
    public void tick(CallbackInfo info) {
        ServerTick.resetView((ServerPlayerEntity) (Object) this);
    }

    /**
     * Changes the behavior of sneaking as the player so that when the player
     * is flying a drone, they cannot exit from the goggles at this point in the code.
     * This is done later in {@link ServerTick#tick(MinecraftServer)}
     * @param player the player on which this method was originally called
     * @return whether or not the player should dismount
     */
    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;shouldDismount()Z")
    )
    public boolean shouldDismount(ServerPlayerEntity player) {
        if (ServerTick.isInGoggles(player)) {
            return false;
        } else {
            return player.isSneaking();
        }
    }


    /**
     * At this specific method call within {@link ServerPlayerEntity#tick()}, do not execute
     * the intended code (i.e. redirect to nothing).
     * @param entity the entity on which this method was originally called
     * @param x
     * @param y
     * @param z
     * @param yaw
     * @param pitch
     */
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;updatePositionAndAngles(DDDFF)V",
                    ordinal = 0
            )
    )
    public void updatePositionAndAngles(ServerPlayerEntity entity, double x, double y, double z, float yaw, float pitch) {
        // Don't move plz
    }
}
