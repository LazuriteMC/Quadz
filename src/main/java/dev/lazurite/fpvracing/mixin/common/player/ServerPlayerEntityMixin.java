package dev.lazurite.fpvracing.mixin.common.player;

import com.mojang.authlib.GameProfile;
import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.util.access.ServerPlayerAccess;
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

    /* Ughhhh */
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    /**
     * This mixin prevents the server log from getting spammed with messages
     * about the player moving too quickly.
     * @param info required by every mixin injection
     */
    @Inject(at = @At("HEAD"), method = "isInTeleportationState()Z", cancellable = true)
    public void isInTeleportationState(CallbackInfoReturnable<Boolean> info) {
        if (isInGoggles()) {
            info.setReturnValue(true);
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

        if (prevEntity instanceof QuadcopterEntity || nextEntity instanceof QuadcopterEntity) {
            networkHandler.sendPacket(new SetCameraEntityS2CPacket(nextEntity));
            cameraEntity = nextEntity;
            // requestTeleport is gone now
            info.cancel();
        }
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

    @Override
    public boolean isInGoggles() {
        return getCameraEntity() instanceof QuadcopterEntity;
    }

    @Override
    public boolean isInGoggles(ServerPlayerEntity player) {
        return player.getCameraEntity() instanceof QuadcopterEntity;
    }

    @Override
    public void setView(Entity entity) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        ShouldRenderPlayerS2C.send(player, !player.equals(entity));
        player.setCameraEntity(entity);

        if (entity instanceof QuadcopterEntity) {
            QuadcopterEntity quadcopter = (QuadcopterEntity) entity;

            for (int i = 0; i < 9; i++) {
                ItemStack stack = player.inventory.getStack(i);

                if (stack.getItem() instanceof TransmitterItem) {
                    if (quadcopter.isBound(FPVRacing.TRANSMITTER_CONTAINER.get(stack))) {
                        player.inventory.selectedSlot = i;
                        SelectedSlotS2C.send(player, i);
                    }
                }
            }
        }
    }
}
