package dev.lazurite.fpvracing.common.entity.quads;

import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class VoxelRacerOne extends QuadcopterEntity {
    private static final float thrustForce = 50.0f;
    private static final float thrustCurve = 1.0f;

    public VoxelRacerOne(EntityType<?> type, World world) {
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
