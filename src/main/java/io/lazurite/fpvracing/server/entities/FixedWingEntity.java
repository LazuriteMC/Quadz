package io.lazurite.fpvracing.server.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class FixedWingEntity extends FlyableEntity {
    public FixedWingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected void initDataTracker() {

    }
}
