package dev.lazurite.quadz.common.state;

import dev.lazurite.quadz.common.item.QuadcopterItem;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.state.item.StackQuadcopterState;
import dev.lazurite.transporter.impl.pattern.model.Quad;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * This interface represents the state of any given quadcopter,
 * and it can be transformed between item and entity form.
 * @see StackQuadcopterState
 * @see QuadcopterEntity
 */
public interface Quadcopter extends Bindable {
    static List<QuadcopterEntity> getAllViewing(MinecraftServer server) {
        return server.getPlayerList().getPlayers().stream().filter(player -> player.getCamera() instanceof QuadcopterEntity)
                .map(player -> (QuadcopterEntity) player.getCamera()).toList();
    }

    /**
     * Finds a specific {@link QuadcopterEntity} which is bound to the given bind ID.
     * @param level the level to search in
     * @param origin the point to search from
     * @param bindId the bind id of the transmitter
     * @param range the maximum range
     * @return the bound {@link QuadcopterEntity} (null if not found)
     */
    static Optional<QuadcopterEntity> getQuadcopterByBindId(Level level, Vec3 origin, int bindId, int range) {
        var entities = level.getEntities((Entity) null,
                new AABB(new BlockPos(origin)).inflate(range),
                entity -> entity instanceof QuadcopterEntity quadcopter && quadcopter.isBoundTo(bindId));

        if (entities.size() > 0) {
            return Optional.of((QuadcopterEntity) entities.get(0));
        }

        return Optional.empty();
    }

    /**
     * Finds the closest {@link Quadcopter} to the given origin.
     * @param level the level to search in
     * @param origin the point to search from
     * @param range the maximum range
     * @param predicate a predicate to narrow the search
     * @return the nearest {@link QuadcopterEntity} (null if not found)
     */
    static Optional<QuadcopterEntity> getNearestQuadcopter(Level level, Vec3 origin, int range, @Nullable Predicate<LivingEntity> predicate) {
        return Optional.ofNullable(level.getNearestEntity(
                QuadcopterEntity.class,
                TargetingConditions.DEFAULT.selector(predicate),
                null, origin.x, origin.y, origin.z,
                new AABB(new BlockPos(origin)).inflate(range)));
    }

    /**
     * Finds all {@link QuadcopterEntity}s inside of a specific range.
     * @param level the level to search in
     * @param origin the point to search from
     * @param range the maximum range
     * @return a {@link List} of {@link QuadcopterEntity}s
     */
    static List<QuadcopterEntity> getQuadcoptersInRange(Level level, Vec3 origin, int range) {
        return level.getEntitiesOfClass(QuadcopterEntity.class, new AABB(new BlockPos(origin)).inflate(range), entity -> true);
    }

    /**
     * Finds a {@link Player} based on the given {@link QuadcopterEntity} and its bind ID.
     * @param quadcopter the {@link QuadcopterEntity} to find a matching player for
     * @return the matching {@link Player}
     */
    static Optional<ServerPlayer> reverseLookup(QuadcopterEntity quadcopter) {
        return PlayerLookup.tracking(quadcopter).stream()
                .filter(player -> Bindable.get(player.getMainHandItem())
                .filter(transmitter -> transmitter.getBindId() == quadcopter.getBindId())
                .isPresent()).findFirst();
    }

    static Optional<Quadcopter> fromStack(ItemStack stack) {
        Quadcopter state = null;

        if (stack.getItem() instanceof QuadcopterItem) {
            state = new StackQuadcopterState(stack);
        }

        return Optional.ofNullable(state);
    }

    default void copyFrom(Quadcopter quadcopter) {
        this.setBindId(quadcopter.getBindId());
        this.setCameraAngle(quadcopter.getCameraAngle());
        this.setTemplate(quadcopter.getTemplate());
    }

    void setCameraAngle(int cameraAngle);
    void setTemplate(String template);
    void setActive(boolean active);

    int getCameraAngle();
    String getTemplate();
    boolean isActive();
}
