package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.network.config.ConfigC2S;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.client.physics.PhysicsWorld;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow ClientWorld world;

    @Inject(at = @At("TAIL"), method = "onGameJoin")
    public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo info) {
        ConfigC2S.send(ClientInitializer.getConfig());
        ClientInitializer.physicsWorld = new PhysicsWorld();
    }

    @Inject(
            method = "onEntityPosition(Lnet/minecraft/network/packet/s2c/play/EntityPositionS2CPacket;)V",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/world/ClientWorld;getEntityById(I)Lnet/minecraft/entity/Entity;"),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void onEntityPosition(EntityPositionS2CPacket packet, CallbackInfo info, Entity entity) {
        if (entity instanceof DroneEntity) {
            info.cancel();
        }
    }

    @Inject(
        method = "onEntityUpdate(Lnet/minecraft/network/packet/s2c/play/EntityS2CPacket;)V",
        at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/network/packet/s2c/play/EntityS2CPacket;getEntity(Lnet/minecraft/world/World;)Lnet/minecraft/entity/Entity;"),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void onEntityUpdate(EntityS2CPacket packet, CallbackInfo info, Entity entity) {
        if (entity instanceof DroneEntity) {
            info.cancel();
        }
    }

    @Inject(
            method = "onEntitySpawn(Lnet/minecraft/network/packet/s2c/play/EntitySpawnS2CPacket;)V",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/network/packet/s2c/play/EntitySpawnS2CPacket;getEntityTypeId()Lnet/minecraft/entity/EntityType;"),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo info, double x, double y, double z, EntityType<?> type) {
        Entity entity = null;
        if (type == ServerInitializer.DRONE_ENTITY) {
            float yaw = (float)(packet.getYaw() * 360) / 256.0F;
            entity = DroneEntity.create(world, DroneEntity.NULL_UUID, new Vec3d(x, y, z), yaw);
        }

        if (entity != null) {
            int i = packet.getId();
            entity.setVelocity(Vec3d.ZERO);
            entity.updatePosition(x, y, z);
            entity.updateTrackedPosition(x, y, z);
            entity.setEntityId(i);
            entity.setUuid(packet.getUuid());
            this.world.addEntity(i, entity);
            info.cancel();
        }
    }
}
