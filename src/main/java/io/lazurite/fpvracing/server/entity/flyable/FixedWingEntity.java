package io.lazurite.fpvracing.server.entity.flyable;

import io.lazurite.fpvracing.server.entity.FlyableEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class FixedWingEntity extends FlyableEntity {
    public FixedWingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void stepInput(float delta) {

    }
}
