package dev.lazurite.quadz.common;

import com.google.common.collect.Lists;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.util.net.SelectedSlotS2C;
import dev.lazurite.rayon.core.impl.physics.PhysicsThread;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

import java.util.List;

public class ServerTick {
    public static int RANGE = 300;

    public static void tick(MinecraftServer server) {
        List<QuadcopterEntity> toActivate = Lists.newArrayList();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            List<QuadcopterEntity> quads = player.getEntityWorld().getEntitiesByClass(QuadcopterEntity.class, new Box(player.getBlockPos()).expand(RANGE), null);

            if (player.getCameraEntity() instanceof QuadcopterEntity) {
                quads.add((QuadcopterEntity) player.getCameraEntity());
            }

            quads.forEach(quadcopter -> quadcopter.setActive(false));

            Quadz.TRANSMITTER_CONTAINER.maybeGet(player.getMainHandStack()).ifPresent(transmitter -> {
                for (QuadcopterEntity quadcopter : quads) {
                    if (transmitter.isBoundTo(quadcopter)) {
                        toActivate.add(quadcopter);
                    }
                }
            });

            /* Goggles enabled, enter a quadcopter view if one is nearby */
            Quadz.GOGGLES_CONTAINER.maybeGet(player.inventory.armor.get(3)).ifPresent(goggles -> {
                if (goggles.isEnabled()) {
                    if (!(player.getCameraEntity() instanceof QuadcopterEntity) && !quads.isEmpty()) {
                        QuadcopterEntity quadcopter = quads.get(0);

                        if (quadcopter != null) {
                            player.setCameraEntity(quadcopter);

                            for (int i = 0; i < player.inventory.main.size(); i++) {
                                if (player.inventory.main.get(i).getItem().equals(Quadz.TRANSMITTER_ITEM)) {
                                    int j = i;

                                    Quadz.TRANSMITTER_CONTAINER.maybeGet(player.getMainHandStack()).ifPresent(transmitter -> {
                                        if (transmitter.isBoundTo(quadcopter)) {
                                            PhysicsThread.get(server).execute(() -> quadcopter.getRigidBody().prioritize(player));
                                            SelectedSlotS2C.send(player, j);
                                        }
                                    });

                                    break;
                                }
                            }
                        }
                    }

                /* Goggles disabled and player is in quadcopter view, reset view */
                } else if (player.getCameraEntity() instanceof QuadcopterEntity) {
                    QuadcopterEntity entity = (QuadcopterEntity) player.getCameraEntity();

                    if (player.equals(entity.getRigidBody().getPriorityPlayer())) {
                        PhysicsThread.get(server).execute(() -> entity.getRigidBody().prioritize(null));
                    }

                    player.setCameraEntity(player);
                }
            });
        }

        toActivate.forEach(quadcopter -> quadcopter.setActive(true));
    }
}