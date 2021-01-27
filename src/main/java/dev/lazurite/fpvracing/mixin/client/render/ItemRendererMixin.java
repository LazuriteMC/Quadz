package dev.lazurite.fpvracing.mixin.client.render;

import dev.lazurite.fpvracing.client.render.item.VoxelRacerOneItemRenderer;
import dev.lazurite.fpvracing.client.render.item.VoyagerItemRenderer;
import dev.lazurite.fpvracing.common.item.quads.VoxelRacerOneItem;
import dev.lazurite.fpvracing.common.item.quads.VoyagerItem;
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
 * Renders the {@link VoxelRacerOneItem} properly in the player's hand/toolbar.
 * @see VoxelRacerOneItemRenderer
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
        if(stack.getItem() instanceof VoxelRacerOneItem) {
            VoxelRacerOneItemRenderer.render(matrices, vertexConsumers, light, overlay);
        } else if (stack.getItem() instanceof VoyagerItem) {
            VoyagerItemRenderer.render(matrices, vertexConsumers, light, overlay);
        }
    }
}
