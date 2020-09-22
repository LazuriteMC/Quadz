package bluevista.fpvracingmod.client.renderers;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.client.models.DroneModel;
import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.client.math.QuaternionHelper;
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

    public void render(DroneEntity droneEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        this.shadowRadius = 0.2F;

        matrixStack.push();
        matrixStack.peek().getModel().multiply(QuaternionHelper.quat4fToQuaternion(droneEntity.getOrientation()));

        this.model.setSize(droneEntity.getConfigValues(Config.SIZE).intValue());
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(model.getLayer(this.getTexture(droneEntity)));
        model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.pop();

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
