package dev.lazurite.fpvracing.client.renderer;

import dev.lazurite.fpvracing.client.model.QuadcopterModel;
import dev.lazurite.fpvracing.server.entity.PhysicsEntity;
import dev.lazurite.fpvracing.client.ClientInitializer;
import dev.lazurite.fpvracing.physics.entity.ClientPhysicsHandler;
import dev.lazurite.fpvracing.server.entity.flyable.QuadcopterEntity;
import dev.lazurite.fpvracing.util.math.QuaternionHelper;
import dev.lazurite.fpvracing.server.ServerInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
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
    private final QuadcopterModel model;

    public QuadcopterRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
        this.shadowRadius = 0.2F;

        int size = PhysicsEntity.SIZE.getKey().getType().fromConfig(ClientInitializer.getConfig(), PhysicsEntity.SIZE.getKey().getName());
        this.model = new QuadcopterModel(size);
    }

    public static void register() {
        EntityRendererRegistry.INSTANCE.register(ServerInitializer.QUADCOPTER_ENTITY,(entityRenderDispatcher,context)->new QuadcopterRenderer(entityRenderDispatcher));
    }

    public void render(QuadcopterEntity quadcopterEntity, float yaw, float delta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();

        if (((ClientPhysicsHandler) quadcopterEntity.getPhysics()).isActive()) {
            matrixStack.peek().getModel().multiply(QuaternionHelper.quat4fToQuaternion(quadcopterEntity.getPhysics().getOrientation()));
        } else {
            matrixStack.peek().getModel().multiply(QuaternionHelper.quat4fToQuaternion(
                    QuaternionHelper.slerp(((ClientPhysicsHandler) quadcopterEntity.getPhysics()).getPrevOrientation(), quadcopterEntity.getPhysics().getOrientation(), delta)
            ));
        }

        if (quadcopterEntity.getValue(PhysicsEntity.SIZE) != null) {
            this.model.setSize(quadcopterEntity.getValue(PhysicsEntity.SIZE));
        } else {
            this.model.setSize(PhysicsEntity.SIZE.getKey().getType().fromConfig(ClientInitializer.getConfig(), PhysicsEntity.SIZE.getKey().getName()));
        }
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
