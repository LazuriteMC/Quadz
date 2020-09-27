package bluevista.fpvracing.client.renderers;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.client.models.DroneModel;
import bluevista.fpvracing.config.Config;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public class DroneSpawnerItemRenderer {
    private static DroneModel model;

    public static void register() {
        model = new DroneModel(ClientInitializer.getConfig().getIntOption(Config.SIZE));
    }

    public static void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, int size) {
        matrices.push();
        matrices.translate(0.5, 0.45, 0.5);
        matrices.scale(2.0f, 2.0f, 2.0f);
        model.setSize(size);
        model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntitySolid(DroneRenderer.droneTexture)), light, overlay, 1f, 1f, 1f, 1f);
        matrices.pop();
    }

}
