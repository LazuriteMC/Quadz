package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.network.config.ConfigC2S;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.physics.PhysicsWorld;
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
public class ClientPlayNetworkManagerMixin {
    @Shadow private ClientWorld world;

    @Inject(at = @At("TAIL"), method = "onGameJoin")
    public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo info) {
        ConfigC2S.send(ClientInitializer.getConfig());
        ClientInitializer.physicsWorld = new PhysicsWorld();
    }

    @Inject(
            method = "onEntitySpawn(Lnet/minecraft/network/packet/s2c/play/EntitySpawnS2CPacket;)V",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/network/packet/s2c/play/EntitySpawnS2CPacket;getEntityTypeId()Lnet/minecraft/entity/EntityType;"),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo ci, double x, double y, double z, EntityType<?> type) {
        Entity entity = null;
        if (type == ServerInitializer.DRONE_ENTITY)
            entity = DroneEntity.create(world, new Vec3d(x, y, z), 0);

        if (entity != null) {
            int i = packet.getId();
            entity.setVelocity(Vec3d.ZERO);
            entity.updatePosition(x, y, z);
            entity.updateTrackedPosition(x, y, z);
            entity.setEntityId(i);
            entity.setUuid(packet.getUuid());
            this.world.addEntity(i, entity);
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "onEntityPosition", cancellable = true)
    public void onEntityPosition(EntityPositionS2CPacket packet, CallbackInfo info) {
//        Entity entity = this.world.getEntityById(packet.getId());
//        if (entity instanceof DroneEntity) {
//            info.cancel();
//        }
    }

    @Inject(at = @At("HEAD"), method = "onVelocityUpdate", cancellable = true)
    public void onVelocityUpdate(EntityVelocityUpdateS2CPacket packet, CallbackInfo info) {
//        Entity entity = this.world.getEntityById(packet.getId());
//        if (entity instanceof DroneEntity) {
//            info.cancel();
//        }
    }

    @Inject(at = @At("HEAD"), method = "onEntityTrackerUpdate", cancellable = true)
    public void onEntityTrackerUpdate(EntityTrackerUpdateS2CPacket packet, CallbackInfo info) {
//        Entity entity = this.world.getEntityById(packet.id());
//        if (entity instanceof DroneEntity) {
//            info.cancel();
//        }
    }
}
