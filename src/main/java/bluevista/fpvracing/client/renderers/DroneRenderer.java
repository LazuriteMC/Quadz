package bluevista.fpvracing.client.renderers;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.client.models.DroneModel;
import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.client.math.QuaternionHelper;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.entities.DroneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class DroneRenderer extends EntityRenderer<DroneEntity> {
    public static final Identifier droneTexture = new Identifier(ServerInitializer.MODID, "textures/entity/drone.png");
    private DroneModel model;

    public DroneRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
        this.shadowRadius = 0.2F;
        this.model = new DroneModel(ClientInitializer.getConfig().getIntOption(Config.SIZE));
    }

    public void render(DroneEntity droneEntity, float yaw, float delta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        this.shadowRadius = 0.2F;

        matrixStack.push();
        if (droneEntity.isActive()) {
            matrixStack.peek().getModel().multiply(QuaternionHelper.quat4fToQuaternion(droneEntity.getOrientation()));
        } else {
            matrixStack.peek().getModel().multiply(QuaternionHelper.quat4fToQuaternion(
                    QuaternionHelper.slerp(droneEntity.getPrevOrientation(), droneEntity.getOrientation(), delta)
            ));
        }

        this.model.setSize(droneEntity.getConfigValues(Config.SIZE).intValue());
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(model.getLayer(this.getTexture(droneEntity)));
        model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.pop();

        super.render(droneEntity, yaw, delta, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public boolean shouldRender(DroneEntity drone, Frustum frustum, double x, double y, double z) {
        return drone.shouldRender(x, y, z);
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
