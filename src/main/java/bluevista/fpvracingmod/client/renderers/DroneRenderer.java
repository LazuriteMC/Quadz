package bluevista.fpvracingmod.client.renderers;

import bluevista.fpvracingmod.client.ClientTick;
import bluevista.fpvracingmod.client.models.DroneModel;
import bluevista.fpvracingmod.server.ServerInitializer;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;

@Environment(EnvType.CLIENT)
public class DroneRenderer extends EntityRenderer<DroneEntity> {

    private static final Identifier droneTexture = new Identifier(ServerInitializer.MODID, "textures/entity/drone.png");
    private static final DroneModel model = new DroneModel();

    public DroneRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
        this.shadowRadius = 0.2F;
    }

    public void render(DroneEntity droneEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        if(ClientTick.shouldRender()) {
            this.shadowRadius = 0.2F;
            matrixStack.push();

            matrixStack.scale(-1.0F, -1.0F, -1.0F);

            Matrix4f newMat = new Matrix4f(droneEntity.getOrientation());
            Matrix4f screenMat = matrixStack.peek().getModel();
            newMat.transpose();
            newMat.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180F));
            newMat.invert();
            screenMat.multiply(newMat);

            matrixStack.translate(0.0D, -1.5D, 0.0D);

            // this literally does nothing???
            //this.model.setAngles(droneEntity, g, 0.0F, -0.1F, 0.0F, 0.0F);
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(this.model.getLayer(this.getTexture(droneEntity)));
            this.model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
            matrixStack.pop();
        } else {
            this.shadowRadius = 0;
        }
        super.render(droneEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public Identifier getTexture(DroneEntity entity) {
        return droneTexture;
    }

    @Override
    protected int getBlockLight(DroneEntity entity, BlockPos blockPos) {
        return 15;
    }

    @Override
    protected int method_27950(DroneEntity entity, BlockPos blockPos) {
        return 15;
    }
}
