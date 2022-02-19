package dev.lazurite.quadz.common.util;

import dev.lazurite.quadz.common.item.GogglesItem;
import dev.lazurite.quadz.common.bindable.Bindable;
import dev.lazurite.quadz.common.quadcopter.Quadcopter;
import dev.lazurite.quadz.common.quadcopter.entity.QuadcopterEntity;
import dev.lazurite.toolbox.api.util.PlayerUtil;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Optional;

public class ServerTick {
    public static void tick(MinecraftServer server) {
        final var range = server.getPlayerList().getViewDistance() * 16;
        final var toActivate = new ArrayList<QuadcopterEntity>();

        for (var player : PlayerUtil.all(server)) {
            final var pos = player.getCamera().position();
            final var level = player.getLevel();
            final var quads = Quadcopter.getQuadcoptersInRange(level, pos, range);

            /* Don't forget the camera! */
            if (player.getCamera() instanceof QuadcopterEntity quadcopter) {
                quads.add(quadcopter);
            }

            quads.forEach(quadcopter -> quadcopter.setActive(false));
            Bindable.get(player.getMainHandItem())
                    .flatMap(transmitter -> Quadcopter.getQuadcopterByBindId(level, pos, transmitter.getBindId(), range))
                    .ifPresent(toActivate::add);

            if (Optional.of(player.getInventory().armor.get(3)).filter(stack -> stack.getItem() instanceof GogglesItem).isEmpty()) {
                if (player.getCamera() instanceof QuadcopterEntity quadcopter) {
                    if (player.equals(quadcopter.getRigidBody().getPriorityPlayer())) {
                        quadcopter.getRigidBody().prioritize(null);
                    }

                    player.setCamera(player);
                }
            }
        }

        toActivate.forEach(quadcopter -> quadcopter.setActive(true));
    }
}