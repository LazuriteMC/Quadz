package dev.lazurite.quadz.common.state;

import dev.lazurite.quadz.common.item.QuadcopterItem;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.state.item.StackQuadcopterState;
import dev.lazurite.quadz.common.util.Frequency;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;

public interface QuadcopterState extends Bindable {
    static QuadcopterEntity findQuadcopter(World world, Vec3d origin, int bindId, int range) {
        return world.getClosestEntity(
                QuadcopterEntity.class,
                TargetPredicate.DEFAULT.setPredicate(quad -> ((QuadcopterEntity) quad).isBoundTo(bindId)),
                null, origin.x, origin.y, origin.z,
                new Box(new BlockPos(origin)).expand(range));
    }

    static Optional<QuadcopterState> get(ItemStack stack) {
        QuadcopterState state = null;

        if (stack.getItem() instanceof QuadcopterItem) {
            state = new StackQuadcopterState(stack);
        }

        return Optional.ofNullable(state);
    }

    default void copyFrom(QuadcopterState quadcopter) {
        this.setGodMode(quadcopter.isInGodMode());
        this.setBindId(quadcopter.getBindId());
        this.setFrequency(quadcopter.getFrequency());
        this.setCameraAngle(quadcopter.getCameraAngle());
        this.setTemplate(quadcopter.getTemplate());
    }

    void setGodMode(boolean godMode);
    void setCameraAngle(int cameraAngle);
    void setFrequency(Frequency frequency);
    void setTemplate(String template);

    boolean isInGodMode();
    int getCameraAngle();
    Frequency getFrequency();
    String getTemplate();
}
