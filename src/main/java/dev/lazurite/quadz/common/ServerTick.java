package dev.lazurite.quadz.common;

import com.google.common.collect.Lists;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.state.Bindable;
import dev.lazurite.quadz.common.state.QuadcopterState;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.util.Frequency;
import dev.lazurite.rayon.core.impl.physics.PhysicsThread;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public class ServerTick {
    public static void tick(MinecraftServer server) {
        int range = server.getPlayerManager().getViewDistance() * 16;
        List<QuadcopterEntity> toActivate = Lists.newArrayList();

        for (ServerPlayerEntity player : PlayerLookup.all(server)) {
            List<QuadcopterEntity> quads = QuadcopterState.getQuadcoptersInRange(player.getEntityWorld(), player.getPos(), range);

            /* Don't forget the camera! */
            if (player.getCameraEntity() instanceof QuadcopterEntity) {
                quads.add((QuadcopterEntity) player.getCameraEntity());
            }

            quads.forEach(quadcopter -> quadcopter.setActive(false));

            Bindable.get(player.getMainHandStack()).ifPresent(transmitter -> {
                for (QuadcopterEntity quadcopter : quads) {
                    if (transmitter.isBoundTo(quadcopter)) {
                        toActivate.add(quadcopter);
                    }
                }
            });

            /* Goggles enabled, enter a quadcopter view if one is nearby */
            ItemStack goggles = player.inventory.armor.get(3).getItem().equals(Quadz.GOGGLES_ITEM) ? player.inventory.armor.get(3) : null;

            if (goggles != null && goggles.getOrCreateTag().getBoolean("enabled")) {
                if (!(player.getCameraEntity() instanceof QuadcopterEntity)) {
                    QuadcopterEntity quadcopter = QuadcopterState.getNearestQuadcopter(player.getEntityWorld(), player.getPos(), range,
                            quad -> ((QuadcopterEntity) quad).getFrequency().equals(Frequency.from(player)));

                    if (quadcopter != null) {
                        player.setCameraEntity(quadcopter);

                        /* Check hotbar for transmitter */
                        for (int i = 0; i < player.inventory.main.size(); i++) {
                            if (player.inventory.main.get(i).getItem().equals(Quadz.TRANSMITTER_ITEM)) {
                                int j = i;

                                /* If there is a transmitter, check if it's bound to a quadcopter */
                                Bindable.get(player.getMainHandStack()).ifPresent(transmitter -> {
                                    if (transmitter.isBoundTo(quadcopter)) {
                                        PhysicsThread.get(server).execute(() -> quadcopter.getRigidBody().prioritize(player));

                                        /* Tell the client to switch slots */
                                        PacketByteBuf buf = PacketByteBufs.create();
                                        buf.writeInt(j);
                                        ServerPlayNetworking.send(player, Quadz.SELECTED_SLOT_S2C, buf);
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
        }

        toActivate.forEach(quadcopter -> quadcopter.setActive(true));
    }
}