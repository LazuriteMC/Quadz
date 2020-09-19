package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPositionS2CPacket.class)
public class EntityPositionS2CPacketMixin {
    @Shadow double x;
    @Shadow double y;
    @Shadow double z;
    @Shadow boolean onGround;

    @Inject(method = "<init>(Lnet/minecraft/entity/Entity;)V", at = @At("RETURN"))
    public void init(Entity entity, CallbackInfo info) {
        if(entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) entity;

            if(player.getCameraEntity() instanceof DroneEntity) {
                Vec3d pos = ((DroneEntity) player.getCameraEntity()).getPlayerStartPos().get(player);

                this.x = pos.x;
                this.y = pos.y;
                this.z = pos.z;
                this.onGround = true;
            }
        }
    }
}
