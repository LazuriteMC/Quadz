package dev.lazurite.fpvracing.mixin;

import dev.lazurite.fpvracing.server.entity.FlyableEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin class only affects item/hand rendering.
 * @author Ethan Johnson
 */
@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Shadow @Final private MinecraftClient client;

    /**
     * This mixin injection controls whether or not the player's hand or
     * held item should be rendered. Works much better with mods like Optifine.
     * @param tickDelta
     * @param matrices
     * @param vertexConsumers
     * @param player
     * @param light
     * @param info required by every mixin injection
     */
    @Inject(at = @At("HEAD"), method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", cancellable = true)
    public void renderItem(float tickDelta, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, ClientPlayerEntity player, int light, CallbackInfo info) {
        if (client.getCameraEntity() instanceof FlyableEntity) {
            info.cancel();
        }
    }
}
