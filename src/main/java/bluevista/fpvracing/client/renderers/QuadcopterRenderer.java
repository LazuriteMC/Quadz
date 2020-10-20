package bluevista.fpvracing.client.renderers;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.client.models.QuadcopterModel;
import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.physics.entity.ClientEntityPhysics;
import bluevista.fpvracing.server.entities.QuadcopterEntity;
import bluevista.fpvracing.util.math.QuaternionHelper;
import bluevista.fpvracing.server.ServerInitializer;
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
public class QuadcopterRenderer extends EntityRenderer<QuadcopterEntity> {
    public static final Identifier quadTexture = new Identifier(ServerInitializer.MODID, "textures/entity/drone.png");
    private QuadcopterModel model;

    public QuadcopterRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
        this.shadowRadius = 0.2F;
        this.model = new QuadcopterModel(ClientInitializer.getConfig().getIntOption(Config.SIZE));
    }

    public void render(QuadcopterEntity quadcopterEntity, float yaw, float delta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();

        if (((ClientEntityPhysics) quadcopterEntity.getPhysics()).isActive()) {
            matrixStack.peek().getModel().multiply(QuaternionHelper.quat4fToQuaternion(quadcopterEntity.getPhysics().getOrientation()));
        } else {
            matrixStack.peek().getModel().multiply(QuaternionHelper.quat4fToQuaternion(
                    QuaternionHelper.slerp(((ClientEntityPhysics) quadcopterEntity.getPhysics()).getPrevOrientation(), quadcopterEntity.getPhysics().getOrientation(), delta)
            ));
        }

        this.model.setSize(quadcopterEntity.getSize());
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(model.getLayer(this.getTexture(quadcopterEntity)));
        model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.pop();

        super.render(quadcopterEntity, yaw, delta, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public boolean shouldRender(QuadcopterEntity drone, Frustum frustum, double x, double y, double z) {
        return drone.shouldRender(x, y, z);
    }

    @Override
    public Identifier getTexture(QuadcopterEntity entity) {
        return quadTexture;
    }

    @Override
    protected int getBlockLight(QuadcopterEntity entity, BlockPos blockPos) {
        return 15;
    }

    @Override
    protected int method_27950(QuadcopterEntity entity, BlockPos blockPos) {
        return 15;
    }
}
