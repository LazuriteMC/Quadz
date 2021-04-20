package dev.lazurite.quadz.client.mixin;

import dev.lazurite.quadz.client.util.ClientTick;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.SetCameraEntityS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onSetCameraEntity", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onSetCameraEntity(SetCameraEntityS2CPacket packet, CallbackInfo info, Entity entity) {
        if (entity == null) {
            ClientTick.desiredCameraEntity = packet.entityId;
        }
    }
}
