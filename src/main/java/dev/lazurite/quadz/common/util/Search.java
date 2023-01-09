package dev.lazurite.quadz.common.util;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.entity.Quadcopter;
import dev.lazurite.toolbox.api.util.PlayerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A set of tools for finding {@link Quadcopter} objects.
 * @since 2.0.0
 */
public interface Search {

    /**
     * Returns a list of every {@link Quadcopter} that is being viewed by a player in the entire server.
     * @param server the {@link MinecraftServer} to use
     * @return {@link List} of {@link Quadcopter}s
     */
    static Set<Quadcopter> forAllViewed(MinecraftServer server) {
        return server.getPlayerList().getPlayers().stream().filter(player -> player.getCamera() instanceof Quadcopter)
                .map(player -> (Quadcopter) player.getCamera()).collect(Collectors.toSet());
    }

    /**
     * Returns a list of every {@link Quadcopter} that is being viewed by a player in the given world.
     * @param level the {@link ServerLevel} to use
     * @return {@link List} of {@link Quadcopter}s
     */
    static Set<Quadcopter> forAllViewed(ServerLevel level) {
        return level.players().stream().filter(player -> player.getCamera() instanceof Quadcopter)
                .map(player -> (Quadcopter) player.getCamera()).collect(Collectors.toSet());
    }

    /**
     * Finds all {@link Quadcopter} objects inside of a specific range.
     * @param level the level to search in
     * @param origin the point to search from
     * @param range the maximum range
     * @return a {@link List} of {@link Quadcopter} objects
     */
    static List<Quadcopter> forAllWithinRange(Level level, Vec3 origin, int range) {
        return level.getEntitiesOfClass(Quadcopter.class, new AABB(new BlockPos(origin)).inflate(range), entity -> true);
    }

    /**
     * Finds a specific {@link Quadcopter} which is bound to the given bind ID.
     * @param level the level to search in
     * @param origin the point to search from
     * @param bindId the bind id of the transmitter
     * @param range the maximum range
     * @return the bound {@link Quadcopter}
     */
    static Optional<Quadcopter> forQuadWithBindId(Level level, Vec3 origin, int bindId, int range) {
        var entities = level.getEntities((Entity) null,
                new AABB(new BlockPos(origin)).inflate(range),
                entity -> entity instanceof Quadcopter quadcopter && quadcopter.isBoundTo(bindId));
        return entities.size() > 0 ? Optional.of((Quadcopter) entities.get(0)) : Optional.empty();
    }

    /**
     * Finds the closest {@link Quadcopter} to the given origin.
     * @param level the level to search in
     * @param origin the point to search from
     * @param range the maximum range
     * @param predicate a predicate to narrow the search
     * @return the nearest {@link Quadcopter}
     */
    static Optional<Quadcopter> forNearestQuad(Level level, Vec3 origin, int range, @Nullable Predicate<LivingEntity> predicate) {
        return Optional.ofNullable(level.getNearestEntity(
                Quadcopter.class,
                TargetingConditions.DEFAULT.selector(predicate),
                null, origin.x, origin.y, origin.z,
                new AABB(new BlockPos(origin)).inflate(range)));
    }

    /**
     * Finds a {@link Player} based on the given {@link Quadcopter} and its bind ID.
     * @param quadcopter the {@link Quadcopter} to use in the search
     * @return the matching {@link Player}
     */
    static Optional<? extends Player> forPlayer(Quadcopter quadcopter) {
        if (quadcopter.getLevel().isClientSide()) {
            return quadcopter.getLevel().players().stream()
                    .filter(player -> Bindable.get(player.getMainHandItem(), quadcopter).isPresent() && player.getMainHandItem().getItem() == Quadz.REMOTE_ITEM)
                    .findFirst();
        }

        return PlayerUtil.tracking(quadcopter)
                .stream().filter(player -> Bindable.get(player.getMainHandItem(), quadcopter).isPresent())
                .findFirst();
    }

}
