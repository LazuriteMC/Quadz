package dev.lazurite.fpvracing.mixin.client.render;

import dev.lazurite.fpvracing.client.renderer.QuadcopterItemRenderer;
import dev.lazurite.fpvracing.server.item.QuadcopterItem;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Renders the {@link QuadcopterItem} properly in the player's hand/toolbar.
 * @see QuadcopterItemRenderer
 */
@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Inject(
        method = "renderItem(" +
                "Lnet/minecraft/item/ItemStack;" +
                "Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;" +
                "Z" +
                "Lnet/minecraft/client/util/math/MatrixStack;" +
                "Lnet/minecraft/client/render/VertexConsumerProvider;" +
                "I" +
                "I" +
                "Lnet/minecraft/client/render/model/BakedModel;" +
                ")V",
        at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/util/math/MatrixStack;translate(DDD)V",
                shift = At.Shift.AFTER
        )
    )
    private void renderItem(ItemStack stack, Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo info) {
        if(stack.getItem() instanceof QuadcopterItem) {
            QuadcopterItemRenderer.render(matrices, vertexConsumers, light, overlay);
        }
    }
}
