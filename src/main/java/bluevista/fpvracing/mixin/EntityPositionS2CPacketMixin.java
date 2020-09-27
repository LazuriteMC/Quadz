package bluevista.fpvracing.mixin;

import bluevista.fpvracing.server.ServerTick;
import bluevista.fpvracing.server.entities.DroneEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin class modifies the values in the packet before they are sent.
 * @author Ethan Johnson
 */
@Mixin(EntityPositionS2CPacket.class)
public class EntityPositionS2CPacketMixin {
    @Shadow double x;
    @Shadow double y;
    @Shadow double z;
    @Shadow boolean onGround;

    /**
     * This mixin modifies the constructor of {@link EntityPositionS2CPacket} in
     * order to keep the player position in the same place when the given {@link ServerPlayerEntity}
     * is flying a {@link DroneEntity}.
     * @param entity the given {@link Entity}. In this case, the {@link ServerPlayerEntity}
     * @param info required by every mixin injection
     */
    @Inject(method = "<init>(Lnet/minecraft/entity/Entity;)V", at = @At("RETURN"))
    public void init(Entity entity, CallbackInfo info) {
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) entity;

            if (ServerTick.isInGoggles(player)) {
                Vec3d pos = ((DroneEntity) player.getCameraEntity()).getPlayerStartPos().get(player);

                this.x = pos.x;
                this.y = pos.y;
                this.z = pos.z;
                this.onGround = true;
            }
        }
    }
}
