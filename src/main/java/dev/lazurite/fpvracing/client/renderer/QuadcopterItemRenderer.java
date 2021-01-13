package dev.lazurite.fpvracing.client.renderer;

import dev.lazurite.fpvracing.client.model.QuadcopterModel;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public class QuadcopterItemRenderer {
    private final static QuadcopterModel model = new QuadcopterModel();

    public static void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5, 0.45, 0.5);
        matrices.scale(2.0f, 2.0f, 2.0f);
        model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntitySolid(QuadcopterRenderer.quadTexture)), light, overlay, 1f, 1f, 1f, 1f);
        matrices.pop();
    }

}
