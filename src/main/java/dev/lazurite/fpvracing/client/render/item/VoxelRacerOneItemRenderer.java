package dev.lazurite.fpvracing.client.render.item;

import dev.lazurite.fpvracing.client.render.entity.VoxelRacerOneEntityRenderer;
import dev.lazurite.fpvracing.client.render.model.VoxelRacerOneModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class VoxelRacerOneItemRenderer {
    private final static VoxelRacerOneModel model = new VoxelRacerOneModel();

    public static void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5, 0.45, 0.5);
        matrices.scale(2.0f, 2.0f, 2.0f);
        model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntitySolid(VoxelRacerOneEntityRenderer.texture)), light, overlay, 1f, 1f, 1f, 1f);
        matrices.pop();
    }

}
