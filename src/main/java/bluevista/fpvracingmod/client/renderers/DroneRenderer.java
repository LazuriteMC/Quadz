package bluevista.fpvracingmod.client.renderers;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.util.Identifier;

public class DroneRenderer extends EntityRenderer<DroneEntity> {

    private static final Identifier droneTexture = new Identifier("textures/entity/cow/cow.png");

    public DroneRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public Identifier getTexture(DroneEntity entity) {
        return droneTexture;
    }
}
