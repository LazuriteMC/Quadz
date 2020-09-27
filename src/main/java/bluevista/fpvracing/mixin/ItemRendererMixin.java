package bluevista.fpvracing.mixin;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.client.renderers.DroneSpawnerItemRenderer;
import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.server.items.DroneSpawnerItem;
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
 * Mixin class for {@link ItemRenderer}.
 * @author Ethan Johnson
 */
@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    /**
     * Injects item renderer code for {@link bluevista.fpvracing.server.items.DroneSpawnerItem}.
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
        if(stack.getItem() instanceof DroneSpawnerItem) {
            int size = DroneSpawnerItem.getTagValue(stack, Config.SIZE) != null ? DroneSpawnerItem.getTagValue(stack, Config.SIZE).intValue() : ClientInitializer.getConfig().getIntOption(Config.SIZE);
            DroneSpawnerItemRenderer.render(matrices, vertexConsumers, light, overlay, size);
        }
    }
}
