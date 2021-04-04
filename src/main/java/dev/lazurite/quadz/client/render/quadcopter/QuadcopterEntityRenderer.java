package dev.lazurite.quadz.client.render.quadcopter;

import com.jme3.math.Quaternion;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.rayon.core.impl.util.math.QuaternionHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib3.renderer.geo.GeoEntityRenderer;

@Environment(EnvType.CLIENT)
public class QuadcopterEntityRenderer extends GeoEntityRenderer<QuadcopterEntity> {

    public QuadcopterEntityRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher, new QuadcopterModel());
        this.shadowRadius = 0.1f;
    }

    @Override
    public void render(QuadcopterEntity quadcopter, float entityYaw, float tickDelta, MatrixStack stack, VertexConsumerProvider bufferIn, int packedLightIn) {
        float temp = quadcopter.bodyYaw;
        quadcopter.bodyYaw = 0;
        quadcopter.prevBodyYaw = 0;

        stack.push();
        stack.multiply(QuaternionHelper.bulletToMinecraft(quadcopter.getPhysicsRotation(new Quaternion(), tickDelta)));
        stack.translate(0, -quadcopter.getBoundingBox().getYLength() / 2, 0);
        super.render(quadcopter, 0, tickDelta, stack, bufferIn, packedLightIn);
        stack.pop();

        quadcopter.bodyYaw = temp;
    }
}