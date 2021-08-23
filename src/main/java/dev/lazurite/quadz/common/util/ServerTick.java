package dev.lazurite.quadz.common.util;

import com.google.common.collect.Lists;
import dev.lazurite.quadz.common.item.GogglesItem;
import dev.lazurite.quadz.common.state.Bindable;
import dev.lazurite.quadz.common.state.QuadcopterState;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.rayon.core.impl.physics.PhysicsThread;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public class ServerTick {
    public static void tick(MinecraftServer server) {
        int range = server.getPlayerManager().getViewDistance() * 16;
        List<QuadcopterEntity> toActivate = Lists.newArrayList();

        for (ServerPlayerEntity player : PlayerLookup.all(server)) {
            Vec3d pos = player.getCameraEntity().getPos();
            World world = player.getEntityWorld();
            List<QuadcopterEntity> quads = QuadcopterState.getQuadcoptersInRange(world, pos, range);

            /* Don't forget the camera! */
            if (player.getCameraEntity() instanceof QuadcopterEntity) {
                quads.add((QuadcopterEntity) player.getCameraEntity());
            }

            quads.forEach(quadcopter -> quadcopter.setActive(false));

            // TODO change this in 1.17 with Java 16!!!
            Optional<Bindable> transmitter = Bindable.get(player.getMainHandStack());

            if (transmitter.isPresent()) {
                QuadcopterState.getQuadcopterByBindId(world, pos, transmitter.get().getBindId(), range).ifPresent(quad -> {
                    toActivate.add(quad);

                    Optional.of(player.inventory.armor.get(3))
                            .filter(stack -> stack.getItem() instanceof GogglesItem).ifPresent(goggles -> {
                        if (!(player.getCameraEntity() instanceof QuadcopterEntity)) {
                            player.setCameraEntity(quad);
                            PhysicsThread.get(server).execute(() -> quad.getRigidBody().prioritize(player));
                        }
                    });
                });

            } else if (player.getCameraEntity() instanceof QuadcopterEntity) {
                QuadcopterEntity entity = (QuadcopterEntity) player.getCameraEntity();

                if (player.equals(entity.getRigidBody().getPriorityPlayer())) {
                    PhysicsThread.get(server).execute(() -> entity.getRigidBody().prioritize(null));
                }

                player.setCameraEntity(player);
            }
        }

        toActivate.forEach(quadcopter -> quadcopter.setActive(true));
    }
}