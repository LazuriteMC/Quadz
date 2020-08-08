package bluevista.fpvracingmod.client.math.inject;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

public interface ServerPlayerEntityInject {
    void setDroneView(Entity entity);

    @SuppressWarnings("ConstantConditions")
    static ServerPlayerEntityInject from(ServerPlayerEntity self) {
        return (ServerPlayerEntityInject) (Object) self;
    }
}
