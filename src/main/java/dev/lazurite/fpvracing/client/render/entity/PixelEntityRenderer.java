package dev.lazurite.fpvracing.client.render.entity;

import com.jme3.math.Quaternion;
import dev.lazurite.fpvracing.client.render.model.PixelModel;
import dev.lazurite.fpvracing.common.entity.quads.PixelEntity;
import dev.lazurite.rayon.impl.util.math.QuaternionHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib3.renderer.geo.GeoEntityRenderer;

/**
 * The renderer for pixel.
 * @see PixelEntity
 */
@Environment(EnvType.CLIENT)
public class PixelEntityRenderer extends GeoEntityRenderer<PixelEntity> {
    public PixelEntityRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher, new PixelModel<>());
        this.shadowRadius = 0.05F;
    }

    @Override
    public void render(PixelEntity pixel, float entityYaw, float tickDelta, MatrixStack stack, VertexConsumerProvider bufferIn, int packedLightIn) {
        float temp = pixel.bodyYaw;
        pixel.bodyYaw = 0;
        pixel.prevBodyYaw = 0;

        stack.push();
        stack.multiply(QuaternionHelper.bulletToMinecraft(pixel.getPhysicsRotation(new Quaternion(), tickDelta)));
        stack.translate(0, -pixel.getBoundingBox().getYLength() / 2, 0);
        super.render(pixel, 0, tickDelta, stack, bufferIn, packedLightIn);
        stack.pop();

        pixel.bodyYaw = temp;
    }
}
