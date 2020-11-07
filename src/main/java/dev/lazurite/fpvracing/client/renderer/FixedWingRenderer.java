package dev.lazurite.fpvracing.client.renderer;

import dev.lazurite.fpvracing.server.ServerInitializer;
import dev.lazurite.fpvracing.server.entity.flyable.FixedWingEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class FixedWingRenderer extends EntityRenderer<FixedWingEntity> {
    public static final Identifier wingTexture = new Identifier(ServerInitializer.MODID, "textures/entity/drone.png");

    public FixedWingRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
        this.shadowRadius = 0.2F;
    }

    public static void register() {
        EntityRendererRegistry.INSTANCE.register(ServerInitializer.FIXED_WING_ENTITY, (entityRenderDispatcher, context) -> new FixedWingRenderer(entityRenderDispatcher));
    }

    public void render(FixedWingEntity fixedWingEntity, float yaw, float delta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        super.render(fixedWingEntity, yaw, delta, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public Identifier getTexture(FixedWingEntity entity) {
        return wingTexture;
    }

    @Override
    protected int getBlockLight(FixedWingEntity entity, BlockPos blockPos) {
        return 15;
    }

    @Override
    protected int method_27950(FixedWingEntity entity, BlockPos blockPos) {
        return 15;
    }
}
