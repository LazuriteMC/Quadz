package dev.lazurite.fpvracing.client.render.entity;

import dev.lazurite.fpvracing.client.render.entity.model.VoxelRacerOneModel;
import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.entity.VoxelRacerOne;
import dev.lazurite.rayon.physics.body.EntityRigidBody;
import dev.lazurite.rayon.physics.helper.math.QuaternionHelper;
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
import net.minecraft.util.math.Box;
import physics.javax.vecmath.Quat4f;

@Environment(EnvType.CLIENT)
public class VoxelRacerOneRenderer extends EntityRenderer<VoxelRacerOne> {
    public static final Identifier texture = new Identifier(FPVRacing.MODID, "textures/entity/drone.png");
    private final VoxelRacerOneModel model = new VoxelRacerOneModel();

    public VoxelRacerOneRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
        this.shadowRadius = 0.2F;
    }

    public static void register() {
        EntityRendererRegistry.INSTANCE.register(FPVRacing.VOXEL_RACER_ONE, (entityRenderDispatcher, context) -> new VoxelRacerOneRenderer(entityRenderDispatcher));
    }

    public void render(VoxelRacerOne voxelRacer, float yaw, float delta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();

        EntityRigidBody body = EntityRigidBody.get(voxelRacer);
        Box box = body.getBox();

        matrixStack.translate(0, box.getYLength() / 2.0, 0);
        matrixStack.multiply(QuaternionHelper.quat4fToQuaternion(QuaternionHelper.slerp(
                body.getPrevOrientation(new Quat4f()),
                body.getTickOrientation(new Quat4f()),
                delta)));
        matrixStack.translate(-box.getXLength() / 2.0, -box.getYLength() / 2.0, -box.getZLength() / 2.0);

        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(model.getLayer(this.getTexture(voxelRacer)));
        model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.pop();
        super.render(voxelRacer, yaw, delta, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public boolean shouldRender(VoxelRacerOne voxelRacer, Frustum frustum, double x, double y, double z) {
        return voxelRacer.shouldRender(x, y, z);
    }

    @Override
    public Identifier getTexture(VoxelRacerOne voxelRacer) {
        return texture;
    }
}
