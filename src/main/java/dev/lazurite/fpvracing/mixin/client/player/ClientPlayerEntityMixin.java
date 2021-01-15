package dev.lazurite.fpvracing.mixin.client.player;

import dev.lazurite.fpvracing.access.PlayerAccess;
import dev.lazurite.fpvracing.common.entity.FlyableEntity;
import dev.lazurite.fpvracing.common.item.GogglesItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * These mixin methods are for the {@link ClientPlayerEntity}. They're
 * mainly written to handle movement of the player between the client and
 * the server.
 * @author Ethan Johnson
 */
@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin implements PlayerAccess {
    @Shadow @Final protected MinecraftClient client;

//    /**
//     * Prevents a player movement packet from being sent by the game when the
//     * player is flying a client-side only drone. This one handles the
//     * {@link net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionOnly} class.
//     * @param networkHandler
//     * @param packet
//     */
//    @Redirect(
//            method = "sendMovementPackets()V",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V",
//                    ordinal = 4
//            )
//    )
//    public void sendPositionOnly(ClientPlayNetworkHandler networkHandler, Packet<?> packet) {
//        if (ClientTick.isServerModded || !(ClientInitializer.client.getCameraEntity() instanceof QuadcopterEntity)) {
//            networkHandler.sendPacket(packet);
//        }
//    }

//    /**
//     * Prevents a player movement packet from being sent by the game when the
//     * player is flying a client-side only drone. This one handles the
//     * {@link net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Both} class.
//     * @param networkHandler
//     * @param packet
//     */
//    @Redirect(
//            method = "sendMovementPackets()V",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V",
//                    ordinal = 5
//            )
//    )
//    public void sendBoth(ClientPlayNetworkHandler networkHandler, Packet<?> packet) {
//        if (ClientTick.isServerModded || !(ClientInitializer.client.getCameraEntity() instanceof QuadcopterEntity)) {
//            networkHandler.sendPacket(packet);
//        }
//    }

    /**
     * This mixin prevents the player's position from being changed while they're flying a drone.
     * @param type
     * @param movement
     * @param info required by every mixin injection
     */
    @Inject(at = @At("HEAD"), method = "move", cancellable = true)
    public void move(MovementType type, Vec3d movement, CallbackInfo info) {
        if (GogglesItem.isInGoggles()) {
            info.cancel();
        }
    }

    @Unique
    @Override
    public boolean isInGoggles() {
        return client.getCameraEntity() instanceof FlyableEntity;
    }
}
