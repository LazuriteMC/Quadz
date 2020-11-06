package dev.lazurite.fpvracing.mixin;

import dev.lazurite.fpvracing.client.ClientInitializer;
import dev.lazurite.fpvracing.client.ClientTick;
import dev.lazurite.fpvracing.network.packet.ConfigC2S;
import dev.lazurite.fpvracing.physics.PhysicsWorld;
import dev.lazurite.fpvracing.server.ServerInitializer;
import dev.lazurite.fpvracing.server.entity.FlyableEntity;
import dev.lazurite.fpvracing.server.entity.flyable.QuadcopterEntity;
import dev.lazurite.fpvracing.server.entity.flyable.FixedWingEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.vecmath.Vector3f;

/**
 * Contains mixins mostly relating to {@link Entity} spawning, movement, and positioning.
 * Also handles game join operations.
 * @author Ethan Johnson
 */
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow ClientWorld world;

    /**
     * This mixin sends the client's config file to the server and
     * initializes a new physics world whenever a game is joined.
     * @param packet
     * @param info required by every mixin injection
     */
    @Inject(at = @At("TAIL"), method = "onGameJoin")
    public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo info) {
        ClientTick.isServerModded = false;
        ConfigC2S.send(ClientInitializer.getConfig());
        ClientInitializer.physicsWorld = new PhysicsWorld();
    }

    /**
     * This mixin cancels all {@link Entity} position updates from the server if it receives
     * information for a {@link FlyableEntity}.
     * @param packet
     * @param info required by every mixin injection
     * @param entity the {@link Entity} on which the injection point was originally called
     */
    @Inject(
            method = "onEntityPosition(Lnet/minecraft/network/packet/s2c/play/EntityPositionS2CPacket;)V",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/world/ClientWorld;getEntityById(I)Lnet/minecraft/entity/Entity;"),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void onEntityPosition(EntityPositionS2CPacket packet, CallbackInfo info, Entity entity) {
        if (entity instanceof FlyableEntity) {
            info.cancel();
        }
    }

    /**
     * This mixin cancels all {@link Entity} movement updates from the server if it receives
     * information for a {@link FlyableEntity}.
     * @param packet
     * @param info required by every mixin injection
     * @param entity the {@link Entity} on which the injection point was originally called
     */
    @Inject(
        method = "onEntityUpdate(Lnet/minecraft/network/packet/s2c/play/EntityS2CPacket;)V",
        at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/network/packet/s2c/play/EntityS2CPacket;getEntity(Lnet/minecraft/world/World;)Lnet/minecraft/entity/Entity;"),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void onEntityUpdate(EntityS2CPacket packet, CallbackInfo info, Entity entity) {
        if (entity instanceof FlyableEntity) {
            info.cancel();
        }
    }

    /**
     * This mixin is necessary since the game hard codes all of the entity types into
     * this method. This mixin just adds another one.
     * @param packet
     * @param info required by every mixin injection
     * @param x
     * @param y
     * @param z
     * @param type
     */
    @Inject(
            method = "onEntitySpawn(Lnet/minecraft/network/packet/s2c/play/EntitySpawnS2CPacket;)V",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/network/packet/s2c/play/EntitySpawnS2CPacket;getEntityTypeId()Lnet/minecraft/entity/EntityType;"),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo info, double x, double y, double z, EntityType<?> type) {
        FlyableEntity entity = null;

        if (type == ServerInitializer.QUADCOPTER_ENTITY) {
            entity = new QuadcopterEntity(type, world);
        } else if (type == ServerInitializer.FIXED_WING_ENTITY) {
            entity = new FixedWingEntity(type, world);
        }

        if (entity != null) {
            int i = packet.getId();
            entity.updatePositionAndAngles(new Vector3f((float) x, (float) y, (float) z), (float)(packet.getYaw() * 360) / 256.0F, 0);
            entity.setEntityId(i);
            entity.setUuid(packet.getUuid());
            this.world.addEntity(i, entity);
            info.cancel();
        }
    }
}
