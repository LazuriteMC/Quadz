package dev.lazurite.quadz.common.util.type;

import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface QuadcopterState extends Bindable, VideoDevice {
    static QuadcopterEntity findQuadcopter(World world, Vec3d origin, int bindId, int range) {
        return world.getClosestEntity(
                QuadcopterEntity.class,
                TargetPredicate.DEFAULT.setPredicate(quad -> ((QuadcopterEntity) quad).isBoundTo(bindId)),
                null, origin.x, origin.y, origin.z,
                new Box(new BlockPos(origin)).expand(range));
    }

    default void copyFrom(QuadcopterState quadcopter) {
        this.setGodMode(quadcopter.isInGodMode());
        this.setBindId(quadcopter.getBindId());
        this.setFrequency(quadcopter.getFrequency());
        this.setCameraAngle(quadcopter.getCameraAngle());
    }

    void setGodMode(boolean godMode);
    void setCameraAngle(int cameraAngle);

    boolean isInGodMode();
    int getCameraAngle();
}
