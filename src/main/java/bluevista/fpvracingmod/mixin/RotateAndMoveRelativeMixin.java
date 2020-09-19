package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityS2CPacket.RotateAndMoveRelative.class)
public class RotateAndMoveRelativeMixin extends EntityS2CPacket {
    @Inject(method = "<init>(ISSSBBZ)V", at = @At("RETURN"))
    public void init(int entityId, short deltaX, short deltaY, short deltaZ, byte yaw, byte pitch, boolean onGround, CallbackInfo info) {
        MinecraftServer server = ServerInitializer.server;
        Entity entity = server.getOverworld().getEntityById(entityId);

        if(entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) entity;

            if(player.getCameraEntity() instanceof DroneEntity) {
                this.deltaX = 0; //(short) (int) EntityS2CPacket.encodePacketCoordinate(0);
                this.deltaY = 0; //(short) (int) EntityS2CPacket.encodePacketCoordinate(0);
                this.deltaZ = 0; //(short) (int) EntityS2CPacket.encodePacketCoordinate(0);
                this.onGround = true;
                this.positionChanged = false;
            }
        }
    }
}
