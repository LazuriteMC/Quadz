package dev.lazurite.quadz.client.render.renderer;

import com.jme3.math.Quaternion;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.lazurite.form.impl.client.render.renderer.TemplatedEntityRenderer;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class QuadcopterRenderer extends TemplatedEntityRenderer<QuadcopterEntity> {
    public QuadcopterRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(QuadcopterEntity quadcopterEntity, float entityYaw, float tickDelta, PoseStack stack, MultiBufferSource bufferIn, int packedLightIn) {
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
