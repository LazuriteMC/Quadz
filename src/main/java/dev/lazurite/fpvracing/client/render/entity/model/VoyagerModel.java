package dev.lazurite.fpvracing.client.render.entity.model;

import dev.lazurite.fpvracing.common.entity.quads.Voyager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class VoyagerModel extends EntityModel<Voyager> {
    private ModelPart base;

    public VoyagerModel() {
        this.textureHeight = 16;
        this.textureWidth = 16;
    }

    @Override
    public void setAngles(Voyager entity, float limbAngle, float limbDistance, float customAngle, float headYaw, float headPitch) {

    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        base = new ModelPart(this, 0, 0);
        base.addCuboid(8 / -2.0f, 8 / -8.0f, 8 / -2.0f, 8,  8 / 4.0f, 8);
        base.render(matrices, vertexConsumer, light, overlay);
    }
}
