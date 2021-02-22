package dev.lazurite.fpvracing.client.render.entity;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Quaternion;
import dev.lazurite.fpvracing.client.render.model.VoxelRacerOneModel;
import dev.lazurite.fpvracing.common.entity.quads.VoxelRacerOneEntity;
import dev.lazurite.rayon.impl.util.math.QuaternionHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib3.renderer.geo.GeoEntityRenderer;

/**
 * The renderer for voxel racer 1.
 * @see VoxelRacerOneEntity
 */
@Environment(EnvType.CLIENT)
public class VoxelRacerOneEntityRenderer extends GeoEntityRenderer<VoxelRacerOneEntity> {
    public VoxelRacerOneEntityRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher, new VoxelRacerOneModel<>());
        this.shadowRadius = 0.2F;
    }

    @Override
    public void render(VoxelRacerOneEntity voxelRacer, float entityYaw, float tickDelta, MatrixStack stack, VertexConsumerProvider bufferIn, int packedLightIn) {
        stack.push();
        stack.translate(0, -voxelRacer.getRigidBody().boundingBox(new BoundingBox()).getYExtent(), 0);
        stack.multiply(QuaternionHelper.bulletToMinecraft(voxelRacer.getPhysicsRotation(new Quaternion(), tickDelta)));

        float temp = voxelRacer.bodyYaw;
        voxelRacer.bodyYaw = 0;
        voxelRacer.prevBodyYaw = 0;

        super.render(voxelRacer, 0, tickDelta, stack, bufferIn, packedLightIn);
        voxelRacer.bodyYaw = temp;
        stack.pop();
    }
}