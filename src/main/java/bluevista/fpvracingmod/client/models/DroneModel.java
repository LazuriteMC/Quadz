package bluevista.fpvracingmod.client.models;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

public class DroneModel extends EntityModel<DroneEntity> {

    private ModelPart base;

    public DroneModel() {
        this.textureHeight = 16;
        this.textureWidth = 16;
    }

    @Override
    public void setAngles(DroneEntity entity, float limbAngle, float limbDistance, float customAngle, float headYaw, float headPitch) {

    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        base = new ModelPart(this, 0, 0);
        base.addCuboid(-4, -1, -4, 8, 2, 8);

        //matrices.translate(0, 1.35, 0);
        matrices.translate(0, 1.4375, 0);

        base.render(matrices, vertexConsumer, light, overlay);
    }
}
