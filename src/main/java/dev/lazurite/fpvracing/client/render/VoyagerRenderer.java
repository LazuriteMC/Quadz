package dev.lazurite.fpvracing.client.render;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.client.render.model.VoxelRacerOneModel;
import dev.lazurite.fpvracing.common.entity.Voyager;
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

@Environment(EnvType.CLIENT)
public class VoyagerRenderer extends EntityRenderer<Voyager> {
    public static final Identifier texture = new Identifier(FPVRacing.MODID, "textures/entity/drone.png");
    private final VoxelRacerOneModel model = new VoxelRacerOneModel();

    public VoyagerRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
        this.shadowRadius = 0.2F;
    }

    public static void register() {
        EntityRendererRegistry.INSTANCE.register(FPVRacing.VOXEL_RACER_ONE, (entityRenderDispatcher, context) -> new VoyagerRenderer(entityRenderDispatcher));
    }

    public void render(Voyager voyager, float yaw, float delta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(model.getLayer(this.getTexture(voyager)));
        model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.pop();
        super.render(voyager, yaw, delta, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public boolean shouldRender(Voyager voyager, Frustum frustum, double x, double y, double z) {
        return voyager.shouldRender(x, y, z);
    }

    @Override
    public Identifier getTexture(Voyager voyager) {
        return texture;
    }
}
