package dev.lazurite.fpvracing.common.entity.quads;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class VoxelRacerOneEntity extends QuadcopterEntity {
    private static final float thrustForce = 150.0f;
    private static final float thrustCurve = 1.0f;

    public VoxelRacerOneEntity(EntityType<? extends LivingEntity> type, World world) {
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

    @Override
    public void kill() {
        if (world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
            ItemStack stack = new ItemStack(FPVRacing.VOXEL_RACER_ONE_ITEM);
            CompoundTag tag = new CompoundTag();
            writeCustomDataToTag(tag);
            FPVRacing.QUADCOPTER_CONTAINER.get(stack).readFromNbt(tag);
            dropStack(stack);
        }

        super.kill();
    }
}
