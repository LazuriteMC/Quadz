package dev.lazurite.fpvracing.client.tick;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.item.GogglesItem;
import dev.lazurite.fpvracing.common.item.container.GogglesContainer;
import dev.lazurite.fpvracing.common.util.type.VideoCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

@Environment(EnvType.CLIENT)
public class GogglesTick {
    public static void tick(MinecraftClient client) {
        if (client.world != null && client.player != null && !client.isPaused()) {
            if (client.player.inventory.armor.get(3).getItem() instanceof GogglesItem) {
                GogglesContainer goggles = FPVRacing.GOGGLES_CONTAINER.get(client.player.inventory.armor.get(3));
                System.out.println(goggles.isEnabled());

                if (goggles.isEnabled()) {
                    if (!(client.getCameraEntity() instanceof VideoCapable)) {
                        for (Entity entity : client.world.getEntities()) {
                            if (entity instanceof VideoCapable) {
                                client.setCameraEntity(entity);
                                break;
                            }
                        }
                    }
                } else {
                    if (client.getCameraEntity() instanceof VideoCapable) {
                        client.setCameraEntity(client.player);
                    }
                }
            } else {
                if (!(client.getCameraEntity() instanceof PlayerEntity)) {
                    client.setCameraEntity(client.player);
                }
            }
        }
    }
}
