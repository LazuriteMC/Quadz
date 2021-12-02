package dev.lazurite.quadz.client.mixin;

import dev.lazurite.quadz.client.util.ClientTick;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "handleSetCamera", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onSetCameraEntity(ClientboundSetCameraPacket packet, CallbackInfo ci, Entity entity) {
        if (entity == null) {
            ClientTick.desiredCameraEntity = packet.entityId; // TODO: Access restricted :(
        }
    }
}
