package bluevista.fpvracing.client.models;

import bluevista.fpvracing.server.entities.DroneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class DroneModel extends EntityModel<DroneEntity> {
    private ModelPart base;
    private int size;

    public DroneModel(int size) {
        this.size = size;
        this.textureHeight = 16;
        this.textureWidth = 16;
    }

    @Override
    public void setAngles(DroneEntity entity, float limbAngle, float limbDistance, float customAngle, float headYaw, float headPitch) {

    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        base = new ModelPart(this, 0, 0);
        base.addCuboid(size / -2.0f, size / -8.0f, size / -2.0f, size, size / 4.0f, size);
        base.render(matrices, vertexConsumer, light, overlay);
    }

    public void setSize(int size) {
        this.size = size;
    }
}
