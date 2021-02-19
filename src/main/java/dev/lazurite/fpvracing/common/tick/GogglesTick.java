package dev.lazurite.fpvracing.common.tick;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;
import dev.lazurite.fpvracing.common.item.GogglesItem;
import dev.lazurite.fpvracing.common.item.TransmitterItem;
import dev.lazurite.fpvracing.common.item.container.GogglesContainer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

@Environment(EnvType.CLIENT)
public class GogglesTick {
    public static void tick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ItemStack stack = player.inventory.armor.get(3);

            if (stack.getItem() instanceof GogglesItem) {
                GogglesContainer goggles = FPVRacing.GOGGLES_CONTAINER.get(stack);

                if (goggles.isEnabled()) {
                    if (!(player.getCameraEntity() instanceof QuadcopterEntity)) {
                        for (Entity entity : player.world.getEntitiesByClass(QuadcopterEntity.class, new Box(player.getBlockPos()).expand(goggles.getRange()), EntityPredicates.VALID_ENTITY)) {
                            if (entity instanceof QuadcopterEntity) {
                                QuadcopterEntity quad = (QuadcopterEntity) entity;
                                ItemStack hand = player.getMainHandStack();

                                player.setCameraEntity(quad);

                                if (hand.getItem() instanceof TransmitterItem) {
                                    if (quad.getBindId() == FPVRacing.TRANSMITTER_CONTAINER.get(hand).getBindId()) {
                                        quad.getRigidBody().prioritize(player);
                                    }
                                }

                                break;
                            }
                        }
                    }
                } else {
                    if (player.getCameraEntity() instanceof QuadcopterEntity) {
                        QuadcopterEntity quad = (QuadcopterEntity) player.getCameraEntity();

                        if (player.equals(quad.getRigidBody().getPriorityPlayer())) {
                            quad.getRigidBody().prioritize(null);
                        }

                        player.setCameraEntity(player);
                    }
                }
            } else {
                if (!(player.getCameraEntity() instanceof PlayerEntity)) {
                    player.setCameraEntity(player);
                    QuadcopterEntity quad = (QuadcopterEntity) player.getCameraEntity();

                    if (player.equals(quad.getRigidBody().getPriorityPlayer())) {
                        quad.getRigidBody().prioritize(null);
                    }
                }
            }
        }
    }
}
