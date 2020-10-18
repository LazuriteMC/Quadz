package bluevista.fpvracing.client.renderers;

import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.entities.FixedWingEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
