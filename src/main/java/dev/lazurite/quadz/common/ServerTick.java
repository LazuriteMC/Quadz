package dev.lazurite.quadz.common;

import com.google.common.collect.Lists;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.state.Bindable;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.rayon.core.impl.physics.PhysicsThread;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

import java.util.List;

public class ServerTick {
    public static void tick(MinecraftServer server) {
        int range = server.getPlayerManager().getViewDistance() * 16;
        List<QuadcopterEntity> toActivate = Lists.newArrayList();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            List<QuadcopterEntity> quads = player.getEntityWorld().getEntitiesByClass(QuadcopterEntity.class, new Box(player.getBlockPos()).expand(range), null);

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
                if (!(player.getCameraEntity() instanceof QuadcopterEntity) && !quads.isEmpty()) {
                    QuadcopterEntity quadcopter = quads.get(0);

                    if (quadcopter != null) {
                        player.setCameraEntity(quadcopter);

                        for (int i = 0; i < player.inventory.main.size(); i++) {
                            if (player.inventory.main.get(i).getItem().equals(Quadz.TRANSMITTER_ITEM)) {
                                int j = i;

                                Bindable.get(player.getMainHandStack()).ifPresent(transmitter -> {
                                    if (transmitter.isBoundTo(quadcopter)) {
                                        PhysicsThread.get(server).execute(() -> quadcopter.getRigidBody().prioritize(player));

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