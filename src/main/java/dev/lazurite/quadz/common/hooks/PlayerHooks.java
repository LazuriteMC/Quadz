package dev.lazurite.quadz.common.hooks;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.entity.Quadcopter;
import dev.lazurite.toolbox.api.network.ClientNetworking;
import dev.lazurite.toolbox.api.network.ServerNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerHooks {

    private static final Map<ResourceLocation, Float> joystickValues = new ConcurrentHashMap<>();

    public static float onGetJoystickValues(ResourceLocation axis) {
        return joystickValues.get(axis) == null ? 0.0f : joystickValues.get(axis);
    }

    public static Map<ResourceLocation, Float> onGetAllAxes() {
        return new HashMap<>(joystickValues);
    }

    public static void onSetJoystickValue(ResourceLocation axis, float value) {
        joystickValues.put(axis, value);
    }

    public static void onSync(Player player) {
        var level = player.level;
        var joysticks = new HashMap<>(joystickValues);

        if (level.isClientSide()) {
            ClientNetworking.send(Quadz.Networking.JOYSTICK_INPUT, buf -> {
                buf.writeInt(joysticks.size());

                joysticks.forEach((axis, value) -> {
                    buf.writeResourceLocation(axis);
                    buf.writeFloat(value);
                });
            });
        } else {
            level.players().forEach(p -> {
                if (!p.equals(player) && p instanceof ServerPlayer serverPlayer) {
                    ServerNetworking.send(serverPlayer, Quadz.Networking.JOYSTICK_INPUT, buf -> {
                        buf.writeUUID(serverPlayer.getUUID());
                        buf.writeInt(joysticks.size());

                        joysticks.forEach((axis, value) -> {
                            buf.writeResourceLocation(axis);
                            buf.writeFloat(value);
                        });
                    });
                }
            });
        }
    }

    /**
     * Prevents the camera from going back to the {@link ServerPlayer}
     * when they crouch at this particular instance of the game loop.
     */
    public static boolean onWantsToStopRiding(ServerPlayer player) {
        return !(player.getCamera() instanceof Quadcopter) && player.isCrouching();
    }

}
