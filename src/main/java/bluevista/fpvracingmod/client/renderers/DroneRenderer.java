package bluevista.fpvracingmod.client.renderers;

import bluevista.fpvracingmod.client.models.DroneModel;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

@Environment(EnvType.CLIENT)
public class DroneRenderer extends EntityRenderer<DroneEntity> {

    private static final Identifier droneTexture = new Identifier("fpvracing", "textures/entity/drone.png");
    private static final DroneModel model = new DroneModel();

    public DroneRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
        this.shadowRadius = 0.2F;
    }

    public void render(DroneEntity droneEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(270.0F - f)); // rotates 90 CCW

        matrixStack.scale(-1.0F, -1.0F, -1.0F); // makes a chonky cow (change -3.0 to -1.0 to revert chonkiness)
        matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90.0F));

        Matrix4f newMat = new Matrix4f(droneEntity.getOrientation());
        Matrix4f screenMat = matrixStack.peek().getModel();
        newMat.transpose();
        screenMat.multiply(newMat);

        matrixStack.translate(0.0D, -1.5D, 0.0D);
        this.model.setAngles(droneEntity, g, 0.0F, -0.1F, 0.0F, 0.0F);
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(this.model.getLayer(this.getTexture(droneEntity)));
        this.model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.pop();
        super.render(droneEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public Identifier getTexture(DroneEntity entity) {
        return droneTexture;
    }
}
