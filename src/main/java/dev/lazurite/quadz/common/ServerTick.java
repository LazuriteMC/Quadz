package dev.lazurite.quadz.common;

import com.google.common.collect.Lists;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.item.TransmitterItem;
import dev.lazurite.quadz.common.item.container.GogglesContainer;
import dev.lazurite.quadz.common.item.container.TransmitterContainer;
import dev.lazurite.quadz.common.util.net.SelectedSlotS2C;
import dev.lazurite.rayon.core.impl.physics.PhysicsThread;
import dev.lazurite.rayon.core.impl.physics.space.MinecraftSpace;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.Optional;

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

            if (player.getMainHandStack().getItem() instanceof TransmitterItem) {
                TransmitterContainer transmitter = Quadz.TRANSMITTER_CONTAINER.get(player.getMainHandStack());

                for (QuadcopterEntity quadcopter : quads) {
                    if (quadcopter.getBindId() == transmitter.getBindId()) {
                        toActivate.add(quadcopter);
                    }
                }
            }

            Optional<GogglesContainer> goggles = Quadz.GOGGLES_CONTAINER.maybeGet(player.inventory.armor.get(3));

            /* Goggles enabled, enter a quadcopter view if one is nearby */
            if (goggles.isPresent()) {
                if (goggles.get().isEnabled()) {
                    if (!(player.getCameraEntity() instanceof QuadcopterEntity) && !quads.isEmpty()) {
                        QuadcopterEntity entity = quads.get(0);

                        if (entity != null) {
                            player.setCameraEntity(entity);

                            for (int i = 0; i < player.inventory.main.size(); i++) {
                                if (player.inventory.main.get(i).getItem().equals(Quadz.TRANSMITTER_ITEM)) {
                                    Optional<TransmitterContainer> transmitter = Quadz.TRANSMITTER_CONTAINER.maybeGet(player.getMainHandStack());

                                    if (transmitter.isPresent()) {
                                        if (entity.getBindId() == transmitter.get().getBindId()) {
                                            PhysicsThread.get(server).execute(() -> entity.getRigidBody().prioritize(player));
                                            SelectedSlotS2C.send(player, i);
                                        }
                                    }

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
            }
        }

        toActivate.forEach(quadcopter -> quadcopter.setActive(true));
    }
}