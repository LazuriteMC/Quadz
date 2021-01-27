package dev.lazurite.fpvracing.common.entity.quads;

import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class Voyager extends QuadcopterEntity {
    private static final float thrustForce = 75.0f;
    private static final float thrustCurve = 1.0f;

    public Voyager(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public float getThrustForce() {
        return thrustForce;
    }

    @Override
    public float getThrustCurve() {
        return thrustCurve;
    }
}
