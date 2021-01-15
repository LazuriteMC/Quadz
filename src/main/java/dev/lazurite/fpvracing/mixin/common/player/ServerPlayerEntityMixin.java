package dev.lazurite.fpvracing.mixin.common.player;

import com.mojang.authlib.GameProfile;
import dev.lazurite.fpvracing.access.ServerPlayerAccess;
import dev.lazurite.fpvracing.common.entity.FlyableEntity;
import dev.lazurite.fpvracing.common.item.GogglesItem;
import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;
import dev.lazurite.fpvracing.common.item.TransmitterItem;
import dev.lazurite.fpvracing.common.packet.SelectedSlotS2C;
import dev.lazurite.fpvracing.common.packet.ShouldRenderPlayerS2C;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.SetCameraEntityS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerPlayerAccess {
    @Shadow public ServerPlayNetworkHandler networkHandler;
    @Shadow private Entity cameraEntity;

    @Shadow public abstract Entity getCameraEntity();

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

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

            if (GogglesItem.isInGoggles(player)) {
                info.cancel();
            }
        }

//        if (entity instanceof DroneEntity) {
//            if (GogglesItem.isInGoggles((ServerPlayerEntity) (Object) this) && !entity.removed) {
//                info.cancel();
//            }
//        }
    }

    /**
     * This mixin prevents the server log from getting spammed with messages
     * about the player moving too quickly.
     * @param info required by every mixin injection
     */
    @Inject(at = @At("HEAD"), method = "isInTeleportationState()Z", cancellable = true)
    public void isInTeleportationState(CallbackInfoReturnable<Boolean> info) {
        if (GogglesItem.isInGoggles((ServerPlayerEntity) (Object) this)) {
            info.setReturnValue(true);
        }
    }

//    /**
//     * Helps prevent the {@link ServerPlayerEntity} from taking damage while in teleported state.
//     * @param source the damage source
//     * @param amount the damage amount
//     * @param info required by every mixin injection
//     */
//    @Inject(at = @At("HEAD"), method = "damage", cancellable = true)
//    public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
//        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
//
//        if (GogglesItem.isInGoggles(player)) {
//            if (!player.getPos().equals(ServerTick.playerPositionManager.getPos(player))) {
//                info.setReturnValue(false);
//            }
//        }
//    }

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

        if (prevEntity instanceof QuadcopterEntity || nextEntity instanceof QuadcopterEntity) {
            networkHandler.sendPacket(new SetCameraEntityS2CPacket(nextEntity));
            cameraEntity = nextEntity;
            // requestTeleport is gone now
            info.cancel();
        }
    }

//    /**
//     * When the player disconnects from the server, run {@link ServerTick#resetView(ServerPlayerEntity)}
//     * @param info required by every mixin injection
//     */
//    @Inject(at = @At("TAIL"), method = "onDisconnect")
//    public void onDisconnect(CallbackInfo info) {
//        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
//
//        if (GogglesItem.isInGoggles(player)) {
//            this.setView(this);
//        }
//    }

    /**
     * Inserts a {@link ServerPlayerAccess#setView} method call into tick
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
        this.setView((ServerPlayerEntity) (Object) this);
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
        if (isInGoggles()) {
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

    @Unique
    @Override
    public boolean isInGoggles() {
        return getCameraEntity() instanceof FlyableEntity;
    }

    @Unique
    @Override
    public boolean isInGoggles(ServerPlayerEntity player) {
        return player.getCameraEntity() instanceof FlyableEntity;
    }

    @Unique
    @Override
    public void setView(Entity entity) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        ShouldRenderPlayerS2C.send(player, !player.equals(entity));
        player.setCameraEntity(entity);

        if (entity instanceof FlyableEntity) {
            FlyableEntity flyable = (FlyableEntity) entity;

            for (int i = 0; i < 9; i++) {
                ItemStack itemStack = player.inventory.getStack(i);

                if (TransmitterItem.isBoundTransmitter(itemStack, flyable)) {
                    player.inventory.selectedSlot = i;
                    SelectedSlotS2C.send(player, i);
                }
            }
        }
    }
}
