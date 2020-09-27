package bluevista.fpvracing.mixin;

import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.entities.DroneEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin class modifies the values in the packet before they are sent.
 * @author Ethan Johnson
 */
@Mixin(EntityS2CPacket.RotateAndMoveRelative.class)
public class RotateAndMoveRelativeMixin extends EntityS2CPacket {

    /**
     * Similar to {@link MoveRelativeMixin}, this mixin modifies the
     * constructor of {@link RotateAndMoveRelative} so that deltaX, deltaY,
     * and deltaZ are always zero when it is given a {@link ServerPlayerEntity}
     * who is flying a drone. Pitch and yaw are always passed through normally.
     * @param entityId the entity id
     * @param deltaX the change in x position
     * @param deltaY the change in y position
     * @param deltaZ the change in z position
     * @param yaw the yaw rotation
     * @param pitch the pitch rotation
     * @param onGround whether or not the entity is on the ground
     * @param info required by every mixin injection
     */
    @Inject(method = "<init>(ISSSBBZ)V", at = @At("RETURN"))
    public void init(int entityId, short deltaX, short deltaY, short deltaZ, byte yaw, byte pitch, boolean onGround, CallbackInfo info) {
        MinecraftServer server = ServerInitializer.server;
        Entity entity = server.getOverworld().getEntityById(entityId);

        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) entity;

            if (player.getCameraEntity() instanceof DroneEntity) {
                this.deltaX = 0;
                this.deltaY = 0;
                this.deltaZ = 0;
                this.onGround = true;
                this.positionChanged = false;
            }
        }
    }
}
