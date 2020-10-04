package bluevista.fpvracing.mixin;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.client.ClientTick;
import bluevista.fpvracing.server.entities.DroneEntity;
import bluevista.fpvracing.server.items.GogglesItem;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.network.Packet;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Redirect(
            method = "sendMovementPackets()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V",
                    ordinal = 4
            )
    )
    public void sendPositionOnly(ClientPlayNetworkHandler networkHandler, Packet<?> packet) {
        if (ClientTick.isServerModded || !(ClientInitializer.client.getCameraEntity() instanceof DroneEntity)) {
            networkHandler.sendPacket(packet);
        }
    }

    @Redirect(
            method = "sendMovementPackets()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V",
                    ordinal = 5
            )
    )
    public void sendBoth(ClientPlayNetworkHandler networkHandler, Packet<?> packet) {
        if (ClientTick.isServerModded || !(ClientInitializer.client.getCameraEntity() instanceof DroneEntity)) {
            networkHandler.sendPacket(packet);
        }
    }

    @Inject(at = @At("HEAD"), method = "move", cancellable = true)
    public void move(MovementType type, Vec3d movement, CallbackInfo info) {
        if (GogglesItem.isInGoggles()) {
            info.cancel();
        }
    }
}
