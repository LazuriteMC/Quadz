package dev.lazurite.fpvracing.client.render.entity;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Quaternion;
import dev.lazurite.fpvracing.client.render.model.VoyagerModel;
import dev.lazurite.fpvracing.common.entity.quads.VoyagerEntity;
import dev.lazurite.rayon.impl.util.math.QuaternionHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib3.renderer.geo.GeoEntityRenderer;

/**
 * The renderer for voyager.
 * @see VoyagerEntity
 */
@Environment(EnvType.CLIENT)
public class VoyagerEntityRenderer extends GeoEntityRenderer<VoyagerEntity> {
    public VoyagerEntityRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher, new VoyagerModel<>());
        this.shadowRadius = 0.2F;
    }

    @Override
    public void render(VoyagerEntity voyager, float entityYaw, float tickDelta, MatrixStack stack, VertexConsumerProvider bufferIn, int packedLightIn) {
        stack.push();
        stack.translate(0, -voyager.getRigidBody().boundingBox(new BoundingBox()).getYExtent(), 0);
        stack.multiply(QuaternionHelper.bulletToMinecraft(voyager.getPhysicsRotation(new Quaternion(), tickDelta)));

        float temp = voyager.bodyYaw;
        voyager.bodyYaw = 0;
        voyager.prevBodyYaw = 0;

        super.render(voyager, entityYaw, tickDelta, stack, bufferIn, packedLightIn);
        voyager.bodyYaw = temp;
        stack.pop();
    }
}
