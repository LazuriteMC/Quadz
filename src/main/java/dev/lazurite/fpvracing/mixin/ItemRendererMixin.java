package dev.lazurite.fpvracing.mixin;

import dev.lazurite.fpvracing.client.ClientInitializer;
import dev.lazurite.fpvracing.client.renderer.QuadcopterItemRenderer;
import dev.lazurite.fpvracing.server.ServerInitializer;
import dev.lazurite.fpvracing.server.entity.PhysicsEntity;
import dev.lazurite.fpvracing.server.item.QuadcopterItem;
import dev.lazurite.fpvracing.network.tracker.GenericDataTrackerRegistry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin class for {@link ItemRenderer}.
 * @author Ethan Johnson
 */
@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    /**
     * Injects item renderer code for {@link QuadcopterItem}.
     * @param stack
     * @param renderMode
     * @param leftHanded
     * @param matrices
     * @param vertexConsumers
     * @param light
     * @param overlay
     * @param model
     * @param info required by every mixin injection
     */
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
            CompoundTag tag = stack.getOrCreateSubTag(ServerInitializer.MODID);
            GenericDataTrackerRegistry.Entry<Integer> entry = PhysicsEntity.SIZE;
            int size = entry.getKey().getType().fromTag(tag, entry.getKey().getName());

            if (size == 0) {
                size = ClientInitializer.getConfig().getValue(entry.getKey());
            }

            QuadcopterItemRenderer.render(matrices, vertexConsumers, light, overlay, size);
        }
    }
}
