package dev.lazurite.quadz.client.render.renderer;

import com.jme3.math.Quaternion;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.lazurite.quadz.client.render.model.QuadcopterModel;
import dev.lazurite.quadz.common.data.DataDriver;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.rayon.core.impl.bullet.math.Converter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

/**
 * Responsible for rendering the {@link QuadcopterEntity} based on
 * which template it has. If it doesn't have a template, it just
 * doesn't render until it's assigned one.
 * @see QuadcopterEntity
 * @see QuadcopterModel
 */
@Environment(EnvType.CLIENT)
public class QuadcopterEntityRenderer extends GeoEntityRenderer<QuadcopterEntity> {
    public QuadcopterEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new QuadcopterModel());
        this.shadowRadius = 0.1f;
    }

    @Override
    public void render(QuadcopterEntity quadcopter, float entityYaw, float tickDelta, PoseStack stack, MultiBufferSource bufferIn, int packedLightIn) {
        if (DataDriver.getTemplate(quadcopter.getTemplate()) != null) {
            this.shadowRadius = quadcopter.dimensions.width * quadcopter.dimensions.height * 2;

            float temp = quadcopter.bodyYaw;
            quadcopter.bodyYaw = 0;
            quadcopter.prevBodyYaw = 0;

            stack.pushPose();
            stack.mulPose(Converter.toMinecraft(quadcopter.getPhysicsRotation(new Quaternion(), tickDelta)));
            stack.translate(0, -quadcopter.getBoundingBox().getYsize() / 2, 0);
            super.render(quadcopter, 0, tickDelta, stack, bufferIn, packedLightIn);
            stack.popPose();

            quadcopter.bodyYaw = temp;
        }
    }
}
