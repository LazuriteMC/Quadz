package dev.lazurite.quadz.common.util;

import com.google.common.collect.Lists;
import dev.lazurite.quadz.common.item.GogglesItem;
import dev.lazurite.quadz.common.state.Bindable;
import dev.lazurite.quadz.common.state.QuadcopterState;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class ServerTick {
    public static void tick(MinecraftServer server) {
        int range = server.getPlayerList().getViewDistance() * 16;
        List<QuadcopterEntity> toActivate = Lists.newArrayList();

        for (ServerPlayer player : PlayerLookup.all(server)) {
            Vec3 pos = player.getCamera().position();
            Level level = player.getLevel();
            List<QuadcopterEntity> quads = QuadcopterState.getQuadcoptersInRange(level, pos, range);

            /* Don't forget the camera! */
            if (player.getCamera() instanceof QuadcopterEntity) {
                quads.add((QuadcopterEntity) player.getCamera());
            }

            quads.forEach(quadcopter -> quadcopter.setActive(false));

            Bindable.get(player.getMainHandItem()).ifPresentOrElse(transmitter -> {
                QuadcopterState.getQuadcopterByBindId(level, pos, transmitter.getBindId(), range).ifPresent(quad -> {
                    toActivate.add(quad);

                    Optional.of(player.getInventory().armor.get(3))
                        .filter(stack -> stack.getItem() instanceof GogglesItem).ifPresentOrElse(goggles -> {
                            if (!(player.getCamera() instanceof QuadcopterEntity)) {
                                player.setCamera(quad);
                                quad.getRigidBody().prioritize(player);
                            }
                        }, () -> {
                            if (player.getCamera() instanceof QuadcopterEntity entity) {
                                if (player.equals(entity.getRigidBody().getPriorityPlayer())) {
                                    quad.getRigidBody().prioritize(null);
                                }

                                player.setCamera(player);
                            }
                        });
                });
            }, () -> {
                if (player.getCamera() instanceof QuadcopterEntity entity) {
                    if (player.equals(entity.getRigidBody().getPriorityPlayer())) {
                        entity.getRigidBody().prioritize(null);
                    }

                    player.setCamera(player);
                }
            });
        }

        toActivate.forEach(quadcopter -> quadcopter.setActive(true));
    }
}