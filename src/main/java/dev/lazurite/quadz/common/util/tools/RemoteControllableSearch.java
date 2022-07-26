package dev.lazurite.quadz.common.util.tools;

import dev.lazurite.quadz.common.data.model.Bindable;
import dev.lazurite.quadz.common.entity.RemoteControllableEntity;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
 * A set of tools for finding {@link RemoteControllableEntity} objects.
 */
public interface RemoteControllableSearch {
    /**
     * Returns a list of every {@link RemoteControllableEntity} that is being viewed by a player in the entire server.
     * @param server the {@link MinecraftServer} to use
     * @return {@link List} of {@link RemoteControllableEntity}s
     */
    static Set<RemoteControllableEntity> allBeingViewed(MinecraftServer server) {
        return server.getPlayerList().getPlayers().stream().filter(player -> player.getCamera() instanceof RemoteControllableEntity)
                .map(player -> (RemoteControllableEntity) player.getCamera()).collect(Collectors.toSet());
    }

    /**
     * Returns a list of every {@link RemoteControllableEntity} that is being viewed by a player in the given world.
     * @param level the {@link ServerLevel} to use
     * @return {@link List} of {@link RemoteControllableEntity}s
     */
    static Set<RemoteControllableEntity> allBeingViewed(ServerLevel level) {
        return level.players().stream().filter(player -> player.getCamera() instanceof RemoteControllableEntity)
                .map(player -> (RemoteControllableEntity) player.getCamera()).collect(Collectors.toSet());
    }

    /**
     * Finds all {@link RemoteControllableEntity} objects inside of a specific range.
     * @param level the level to search in
     * @param origin the point to search from
     * @param range the maximum range
     * @return a {@link List} of {@link RemoteControllableEntity} objects
     */
    static List<RemoteControllableEntity> allInArea(Level level, Vec3 origin, int range) {
        return level.getEntitiesOfClass(RemoteControllableEntity.class, new AABB(new BlockPos(origin)).inflate(range), entity -> true);
    }

    /**
     * Finds a specific {@link RemoteControllableEntity} which is bound to the given bind ID.
     * @param level the level to search in
     * @param origin the point to search from
     * @param bindId the bind id of the transmitter
     * @param range the maximum range
     * @return the bound {@link RemoteControllableEntity}
     */
    static Optional<RemoteControllableEntity> byBindId(Level level, Vec3 origin, int bindId, int range) {
        final var entities = level.getEntities((Entity) null,
                new AABB(new BlockPos(origin)).inflate(range),
                entity -> entity instanceof RemoteControllableEntity remoteControllable && remoteControllable.isBoundTo(bindId));

        return entities.size() > 0 ? Optional.of((RemoteControllableEntity) entities.get(0)) : Optional.empty();
    }

    /**
     * Finds the closest {@link RemoteControllableEntity} to the given origin.
     * @param level the level to search in
     * @param origin the point to search from
     * @param range the maximum range
     * @param predicate a predicate to narrow the search
     * @return the nearest {@link RemoteControllableEntity}
     */
    static Optional<RemoteControllableEntity> nearest(Level level, Vec3 origin, int range, @Nullable Predicate<LivingEntity> predicate) {
        return Optional.ofNullable(level.getNearestEntity(
                RemoteControllableEntity.class,
                TargetingConditions.DEFAULT.selector(predicate),
                null, origin.x, origin.y, origin.z,
                new AABB(new BlockPos(origin)).inflate(range)));
    }

    /**
     * Finds a {@link Player} based on the given {@link RemoteControllableEntity} and its bind ID.
     * @param remoteControllableEntity the {@link RemoteControllableEntity} to use in the search
     * @return the matching {@link Player}
     */
    static Optional<ServerPlayer> findPlayer(RemoteControllableEntity remoteControllableEntity) {
        return PlayerLookup.tracking(remoteControllableEntity).stream()
                .filter(player -> Bindable.get(player.getMainHandItem())
                .filter(transmitter -> transmitter.getBindId() == remoteControllableEntity.getBindId())
                .isPresent()).findFirst();
    }
}
