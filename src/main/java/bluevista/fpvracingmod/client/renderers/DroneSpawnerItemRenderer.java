package bluevista.fpvracingmod.client.renderers;

import bluevista.fpvracingmod.client.models.DroneModel;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public class DroneSpawnerItemRenderer {
    private static final DroneModel model = new DroneModel();

    public static void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5, -2.45, 0.5);
        matrices.scale(2.0f, 2.0f, 2.0f);
        model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntitySolid(DroneRenderer.droneTexture)), light, overlay, 1f, 1f, 1f, 1f);
        matrices.pop();
    }

}
