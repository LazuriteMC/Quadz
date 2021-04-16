package dev.lazurite.quadz.common.state;

import dev.lazurite.quadz.common.item.QuadcopterItem;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.state.item.StackQuadcopterState;
import dev.lazurite.quadz.common.util.Frequency;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * This interface represents the state of any given quadcopter
 * and it can be transformed between item and entity form.
 * @see StackQuadcopterState
 * @see QuadcopterEntity
 */
public interface QuadcopterState extends Bindable {
    /**
     * Finds a specific {@link QuadcopterEntity} which is bound to the given bind ID.
     * @param world the world to search in
     * @param origin the point to search from
     * @param bindId the bind id of the transmitter
     * @param range the maximum range
     * @return the bound {@link QuadcopterEntity} (null if not found)
     */
    @Nullable static QuadcopterEntity getQuadcopterByBindId(World world, Vec3d origin, int bindId, int range) {
        return world.getClosestEntity(
                QuadcopterEntity.class,
                TargetPredicate.DEFAULT.setPredicate(quad -> ((QuadcopterEntity) quad).isBoundTo(bindId)),
                null, origin.x, origin.y, origin.z,
                new Box(new BlockPos(origin)).expand(range));
    }

    /**
     * Finds the closest {@link QuadcopterEntity} to the given origin.
     * @param world the world to search in
     * @param origin the point to search from
     * @param range the maximum range
     * @param predicate a predicate to narrow the search
     * @return the nearest {@link QuadcopterEntity} (null if not found)
     */
    @Nullable static QuadcopterEntity getNearestQuadcopter(World world, Vec3d origin, int range, @Nullable Predicate<LivingEntity> predicate) {
        return world.getClosestEntity(
                QuadcopterEntity.class,
                TargetPredicate.DEFAULT.setPredicate(predicate),
                null, origin.x, origin.y, origin.z,
                new Box(new BlockPos(origin)).expand(range));
    }

    /**
     * Finds all {@link QuadcopterEntity}s inside of a specific range.
     * @param world the world to search in
     * @param origin the point to search from
     * @param range the maximum range
     * @return a {@link List} of {@link QuadcopterEntity}s
     */
    static List<QuadcopterEntity> getQuadcoptersInRange(World world, Vec3d origin, int range) {
        return world.getEntitiesByClass(QuadcopterEntity.class, new Box(new BlockPos(origin)).expand(range), null);
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
