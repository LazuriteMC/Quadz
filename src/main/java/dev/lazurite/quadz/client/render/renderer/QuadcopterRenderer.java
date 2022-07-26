package dev.lazurite.quadz.client.render.renderer;

import com.jme3.math.Quaternion;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.lazurite.quadz.client.render.model.RemoteControllableEntityModel;
import dev.lazurite.quadz.common.data.template.TemplateLoader;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class QuadcopterRenderer extends GeoEntityRenderer<QuadcopterEntity> {
    public QuadcopterRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new RemoteControllableEntityModel<>());
        this.shadowRadius = 0.1f;
    }

    @Override
    public void render(QuadcopterEntity quadcopterEntity, float entityYaw, float tickDelta, PoseStack stack, MultiBufferSource bufferIn, int packedLightIn) {
        if (TemplateLoader.getTemplate(quadcopterEntity.getTemplate()) != null) {
            this.shadowRadius = quadcopterEntity.dimensions.width * quadcopterEntity.dimensions.height * 2;

            float temp = quadcopterEntity.yBodyRot;
            quadcopterEntity.yBodyRot = 0;
            quadcopterEntity.yBodyRotO = 0;

            stack.pushPose();
            stack.mulPose(Convert.toMinecraft(quadcopterEntity.getPhysicsRotation(new Quaternion(), tickDelta)));
            stack.translate(0, -quadcopterEntity.getBoundingBox().getYsize() / 2, 0);
            super.render(quadcopterEntity, 0, tickDelta, stack, bufferIn, packedLightIn);
            stack.popPose();

            quadcopterEntity.yBodyRot = temp;
        }
    }

    @Override
    public ResourceLocation getTextureLocation(QuadcopterEntity quadcopterEntity) {
        return this.getGeoModelProvider().getTextureResource(quadcopterEntity);
    }

}
