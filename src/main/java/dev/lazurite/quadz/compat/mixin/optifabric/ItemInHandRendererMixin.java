package dev.lazurite.quadz.compat.mixin.optifabric;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(
            method = "renderItem",
            at = @At("HEAD"),
            cancellable = true
    )
    public void renderItem_HEAD(LivingEntity livingEntity, ItemStack itemStack, ItemTransforms.TransformType transformType, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo info) {
        if (minecraft.getCameraEntity() instanceof QuadcopterEntity) {
            info.cancel();
        }
    }
}
