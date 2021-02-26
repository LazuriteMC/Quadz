package dev.lazurite.fpvracing.client.render.entity;

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
        float temp = voyager.bodyYaw;
        voyager.bodyYaw = 0;
        voyager.prevBodyYaw = 0;

        stack.push();
        stack.multiply(QuaternionHelper.bulletToMinecraft(voyager.getPhysicsRotation(new Quaternion(), tickDelta)));
        stack.translate(0, -voyager.getBoundingBox().getYLength() / 2, 0);
        super.render(voyager, 0, tickDelta, stack, bufferIn, packedLightIn);
        stack.pop();

        voyager.bodyYaw = temp;
    }
}
